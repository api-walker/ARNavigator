package de.dhge.ar.arnavigator;

import android.content.res.Configuration;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import me.dm7.barcodescanner.core.CameraPreview;
import me.dm7.barcodescanner.core.CameraWrapper;

public class NavigationActivity extends AppCompatActivity{

    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        // Add NavigationView
        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.fl_navigation);
        mNavigationView = new NavigationView(this);
        contentFrame.addView(mNavigationView);
    }

    @Override
    public void onResume() {
        super.onResume();
        mNavigationView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mNavigationView.stopCamera();           // Stop camera on pause
    }
}
