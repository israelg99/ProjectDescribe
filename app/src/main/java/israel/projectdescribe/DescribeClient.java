package israel.projectdescribe;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by Israel on 12/16/2015.
 */

public class DescribeClient extends AsyncTask<byte[], Integer, String> {

    private static final String TAG = DescribeClient.class.getName();

    private static final String DEFAULT_URL = "http://psorcerer.mooo.com:8888/caption";

    private static boolean gotResponse = true;

    private HttpURLConnection connection;
    private URL url;

    private static String getTAG() {
        return TAG;
    }

    public static String getDefaultURL() {
        return DEFAULT_URL;
    }

    public static boolean gotResponse() {
        return gotResponse;
    }
    private static void setGotResponse(boolean gotResponse) {
        DescribeClient.gotResponse = gotResponse;
    }
    private static void justGotResponse() {
        setGotResponse(true);
    }
    private static void resetResponse() {
        setGotResponse(false);
    }

    private HttpURLConnection getConnection() {
        return connection;
    }
    private void setConnection(HttpURLConnection connection) {
        this.connection = connection;
    }
    private void openConnection() {
        try {
            setConnection((HttpURLConnection) getURL().openConnection());
        } catch (IOException e) {
            Log.e(getTAG(), "Unable to open URL connection: " + e.getMessage());
        }
    }
    private void openConnection(String url) {
        setURL(url);
        openConnection();
    }
    private void openCheckConnection(String url) {
        try {
            if(getConnection() == null || getConnection().getResponseCode() != 200) {
                openConnection(url);
            }
        } catch (IOException e) {
            Log.e(getTAG(), "Error getting response code and opening a connection: " + e.getMessage());
        }
    }
    private void closeConnection() {
        getConnection().disconnect();
        setConnection(null);
    }

    private URL getURL() {
        return url;
    }
    private void setURL(URL url) {
        this.url = url;
    }
    private void setURL(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            Log.e(getTAG(), "Unable to change URL: " + e.getMessage());
        }
    }

    private String describeImage(String url, Bitmap bitmap) {

        // Reset response, should be here.
        resetResponse();

        // Setting the HTTP connection up.
        //openConnection(url);    openCheckConnection(url) is more efficient.

        openCheckConnection(url);

        getConnection().setDoOutput(true);
        getConnection().setUseCaches(false);
        //().setChunkedStreamingMode(0); Makes broken pipes.. issues!
        getConnection().setRequestProperty("Connection", "keep-alive");
        getConnection().setRequestProperty("Content-Type", "image/jpeg");
        try {
            getConnection().setRequestMethod("POST");
        } catch (ProtocolException e) {
            Log.e(getTAG(), "Error setting request method to POST: " + e.getMessage());
        }

        // Connecting to the server.
        try {
            getConnection().connect();
        } catch (IOException e) {
            Log.e(getTAG(), "Error connecting to the server: " + e.getMessage());
        }

        /* Sending HTTP message to the server. */

        Log.w(getTAG(), "Sending the HTTP message!");

        // Setting up the output stream.
        BufferedOutputStream outputStream = null;
        try {
            outputStream = new BufferedOutputStream(getConnection().getOutputStream());
        } catch (IOException e) {
            Log.e(getTAG(), "Error setting and getting connection's output stream: " + e.getMessage());
        }
        if(outputStream == null) {
            return "Failed setting and getting connection's output stream.";
        }

        // Compressing the bitmap into the output stream.
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream);

        // Closing the sending components.
        try {
            outputStream.flush();

            outputStream.close();
        } catch (IOException e) {
            Log.e(getTAG(), "Unable to close response components: " + e.getMessage());
        }

        /* Receiving response from the server */

        Log.w(getTAG(), "Getting the HTTP response from the server!");

        // Getting the actual response from the server, adn the response code.
        BufferedInputStream responseStream = null;
        try {
            Log.w(getTAG(), "Response code: " + getConnection().getResponseCode());
            responseStream = new BufferedInputStream(getConnection().getInputStream());
        } catch (IOException e) {
            Log.e(getTAG(), "Error getting response from the connection's input stream: " + e.getMessage());
            e.printStackTrace();
        }
        if(responseStream == null) {
            return "Failed getting response from the connection's input stream.";
        }

        // Deciphering a string response from the server response.
        BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));

        String line;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            while ((line = responseStreamReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            responseStreamReader.close();
        } catch (IOException e) {
            Log.e(getTAG(), "Error building string response: " + e.getMessage());
        }

        justGotResponse();
        return stringBuilder.toString();
    }

    private String describeImage(String url, byte[] imageBytes) {
        // Decoding the image bytes to a bitmap.
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        return describeImage(url, bitmap);
    }

    /**

       We should only use AsyncTask with newly created and disposable objects.
       Consequently, we close our connection after the task is finished.
       Specifically in {@link #onPostExecute(String)}, that's where we close the connection.

       Otherwise, you can use the DescribeClient class freely.
       Therefore, the connection shalt not close, for AsyncTask methods aren't called.
       AsyncTask methods are {@link #doInBackground(byte[]...)} and {@link #onPostExecute(String)}

     */

    @SuppressWarnings("JavaDoc")
    @Override
    protected String doInBackground(byte[]... params) {
        return describeImage(getDefaultURL(), params[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        Log.w(getTAG(), "Our response!! - " + result);
        JSON_updateDescription(result);
        closeConnection();
    }

    protected String JSON_getDescription(String JSON) {
        JsonReader jsonReader = new JsonReader(new StringReader(JSON));
        String desc = "Nothing..";

        try {
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                if (name.equals("caption")) {
                    desc = jsonReader.nextString();
                } else {
                    jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
        } catch (IOException e) {
            Log.e(getTAG(), "Error while parsing the JSON file received from the server: " + e.getMessage());
        }

        return desc;
    }

    protected void JSON_updateDescription(String JSON) {
        updateDescription(JSON_getDescription(JSON));
    }
    protected void updateDescription(String desc) {
        MainActivity.updateDescription(desc);
    }
}
