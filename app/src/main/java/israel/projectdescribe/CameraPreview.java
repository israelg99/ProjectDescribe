package israel.projectdescribe;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by Israel on 12/15/2015.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    // For Android logs.
    private static final String TAG = CameraPreview.class.getName();

    private static final int ORIENTATION_PORTRAIT = 90;
    private static final int ORIENTATION_LANDSCAPE = 180;

    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private DescribeMedia describeMedia;

    private static String getTAG() {
        return TAG;
    }

    private static int getOrientationPortrait() {
        return ORIENTATION_PORTRAIT;
    }
    private static int getOrientationLandscape() {
        return ORIENTATION_LANDSCAPE;
    }

    public CameraPreview(Context context) {
        super(context);

        // Create an instance of Camera and applying to it the global Camera instance.
        setCameraInstance();

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        setSurfaceHolder(getHolder());
        getSurfaceHolder().addCallback(this);

        // A deprecated setting, but required on Android versions prior to 3.0.
        getSurfaceHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private SurfaceHolder getSurfaceHolder() {
        return surfaceHolder;
    }
    private void setSurfaceHolder(SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
    }

    private Camera getCamera() {
        return camera;
    }
    private void setCamera(Camera camera) {
        this.camera = camera;
    }
    private void setCameraInstance() {
        setCamera(getCameraInstance());
    }

    private DescribeMedia getDescribeMedia() {
        return describeMedia;
    }
    private void setDescribeMedia(DescribeMedia describeMedia) {
        this.describeMedia = describeMedia;
    }
    private void resetDescribeMedia() {
        this.describeMedia = new DescribeMedia();
    }

    /** A safe way to get an instance of the Camera object. */
    private static Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open(); // Attempt to get a Camera instance.
        } catch (Exception e) {
            // Camera is not available (in use or does not exist).
            Log.e(getTAG(), e.getMessage());
        }
        return camera; // Returns null if camera is unavailable.
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            getCamera().setPreviewDisplay(holder);
            getCamera().setDisplayOrientation(getOrientationPortrait());
            getCamera().startPreview();
        } catch (IOException e) {
            Log.d(getTAG(), "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        destroy();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        /* At the moment, we do not care about the surface changing therefore I will comment
            this method out as we don't need the overhead of stopping and starting camera previews.
         */

        /*

        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (getSurfaceHolder().getSurface() == null) {
            // Preview surface does not exist.
            return;
        }

        // Stop preview before making changes.
        try {
            getCamera().stopPreview();
        } catch (Exception e) {
            // Ignore: Tried to stop a non-existent preview.
        }

        // Set preview size and make any resize, rotate or
        // reformatting changes here.

        // Start preview with new settings.
        try {
            getCamera().setPreviewDisplay(getSurfaceHolder());
            getCamera().startPreview();

        } catch (Exception e) {
            Log.d(getTAG(), "Error starting camera preview: " + e.getMessage());
        }

        */
    }

    public void takePreviewPicture() {
        resetDescribeMedia();
        getCamera().takePicture(null, null, getDescribeMedia());
        getCamera().startPreview();
    }

    /** Release the camera for other applications */
    private void releaseCamera() {
        Log.w(getTAG(), "Camera is being released!");
        if (getCamera() != null) {
            getCamera().stopPreview();
            getCamera().release();
            setCamera(null);
        }
    }

    public void destroy() {
        releaseCamera();
    }
}