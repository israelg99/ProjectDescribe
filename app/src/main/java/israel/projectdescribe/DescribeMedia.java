package israel.projectdescribe;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Israel on 12/15/2015.
 */

public class DescribeMedia implements Camera.PictureCallback {

    // For Android logs.
    private static final String TAG = DescribeMedia.class.getName();

    private byte[] mediaBytes;
    private File mediaFile;
    private Bitmap mediaBitmap;

    private static String getTAG() {
        return TAG;
    }

    public byte[] getMediaBytes() {
        return mediaBytes;
    }
    private void setMediaBytes(byte[] mediaBytes) {
        this.mediaBytes = mediaBytes;
    }

    public File getMediaFile() {
        return mediaFile;
    }
    private void setMediaFile(File mediaFile) {
        this.mediaFile = mediaFile;
    }

    private Bitmap getMediaBitmap() {
        return mediaBitmap;
    }
    private void setMediaBitmap(Bitmap mediaBitmap) {
        this.mediaBitmap = mediaBitmap;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.w(getTAG(), "onPictureTaken() is called!");

        // No need to pre-load bitmaps or files anymore, and that's good, because they are very expensive.
        if(DescribeClient.gotResponse()) {
            applyPictureBytes(data);
            new DescribeClient().execute(getMediaBytes());
        }
    }

    /** Create a File for saving an image or video.
              NOT SUPPORTED AT THE MOMENT.           */
    private void applyMediaFile(MediaType type){

        // Create a media file name.
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        if (type == MediaType.IMAGE){
            setMediaFile(new File("IMG_" + timeStamp + ".jpg"));
        } else if(type == MediaType.VIDEO) {
            setMediaFile(new File("VID_" + timeStamp + ".mp4"));
        }
    }

    private void applyPictureBytes(byte[] data) {
        setMediaBytes(data);
        if (getMediaBytes() == null) {
            Log.e(getTAG(), "Error setting media picture bytes!");
            return;
        }

        Log.w(getTAG(), "The picture bytes have been successfully stored!");
    }

    private void applyPictureBitmap(byte[] imageBytes) {
        setMediaBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
    }

    // Using it's own bytes.
    private void applyPictureBitmap() {
        applyPictureBitmap(getMediaBytes());
    }
}
