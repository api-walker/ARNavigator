/* This code is adapted from CameraHandlerThread.java(https://github.com/dm77/barcodescanner/blob/b23c3bab18a14e912341ffb7ca28d0894a572d3e/core/src/main/java/me/dm7/barcodescanner/core/CameraHandlerThread.java)
 * It's modified to fit the new Classes
*/
package de.dhge.ar.arnavigator;

import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import me.dm7.barcodescanner.core.CameraUtils;
import me.dm7.barcodescanner.core.CameraWrapper;

// This code is mostly based on the top answer here: http://stackoverflow.com/questions/18149964/best-use-of-handlerthread-over-other-similar-classes
public class NavigationHandlerThread extends HandlerThread {
    private static final String LOG_TAG = "CameraHandlerThread";

    private NavigationView mScannerView;

    public NavigationHandlerThread(NavigationView scannerView) {
        super("NavigationHandlerThread");
        mScannerView = scannerView;
        start();
    }

    public void startCamera(final int cameraId) {
        Handler localHandler = new Handler(getLooper());
        localHandler.post(new Runnable() {
            @Override
            public void run() {
                final Camera camera = CameraUtils.getCameraInstance(cameraId);
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mScannerView.setupCameraPreview(CameraWrapper.getWrapper(camera, cameraId));
                    }
                });
            }
        });
    }
}