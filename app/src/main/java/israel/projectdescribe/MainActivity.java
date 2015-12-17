package israel.projectdescribe;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Israel on 12/15/2015.
 */

public class MainActivity extends AppCompatActivity {

    // For Android logs.
    private static final String TAG = MainActivity.class.getName();

    // For scheduling the camera shots.
    private static final int DEFAULT_INITIAL = 5;
    private static final int DEFAULT_DELAY = 3;

    private static TextView description;

    private CameraPreview cameraPreview;

    private ScheduledExecutorService executor;

    private static String getTAG() {
        return TAG;
    }

    private static int getDefaultInitial() {
        return DEFAULT_INITIAL;
    }
    private static int getDefaultDelay() {
        return DEFAULT_DELAY;
    }

    private static TextView getDescription() {
        return description;
    }
    private static void setDescription(TextView description) {
        MainActivity.description = description;
    }
    private void refreshDescription() {
        setDescription((TextView) findViewById(R.id.description));
    }

    private CameraPreview getCameraPreview() {
        return cameraPreview;
    }
    private void setCameraPreview(CameraPreview cameraPreview) {
        this.cameraPreview = cameraPreview;
    }

    private ScheduledExecutorService getExecutor() {
        return executor;
    }
    private void setExecutor(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Basic assignments, should not be modified.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Assigning the description ID to the TextView variable description.
        refreshDescription();

        // TODO: Do something interactive if the camera cannot be found which means null.
        // Create our Preview view and set it as the content of our activity.
        setCameraPreview(new CameraPreview(this));
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(getCameraPreview());

        // Scheduling the camera shots with the default initial and delay seconds.
        scheduleCameraShots(getDefaultInitial(), getDefaultDelay());
    }

    private void scheduleCameraShots(int initial, int delay) {
        setExecutor(Executors.newSingleThreadScheduledExecutor());
        final Runnable task = new Runnable() {
            @Override
            public void run() {
                Log.w(getTAG(), "Running the task!");
                getCameraPreview().takePreviewPicture();
            }
        };
        getExecutor().scheduleWithFixedDelay(task, initial, delay, TimeUnit.SECONDS);
    }

    // Must not be JSON or some other format, just the description.
    public static void updateDescription(String desc) {
        if(getDescription() == null) {
            return;
        }
        getDescription().setText(desc);
    }

    @Override
    protected void onDestroy() {
        getCameraPreview().destroy(); // Mainly to release the camera.
        super.onDestroy();
    }
}
