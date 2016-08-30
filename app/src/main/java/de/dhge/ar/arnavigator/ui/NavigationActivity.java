package de.dhge.ar.arnavigator.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import de.dhge.ar.arnavigator.R;

public class NavigationActivity extends AppCompatActivity {

    private NavigationView mNavigationView;
    private ImageView flashButton;

    // AR Object
    private String objectID;
    private String objectName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        initializeViews();
        setListeners();
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

    // Setup
    private void initializeViews() {
        // Add NavigationView
        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.fl_navigation);
        flashButton = (ImageView) findViewById(R.id.iv_nav_flash);
        mNavigationView = new NavigationView(this);
        contentFrame.addView(mNavigationView);

        receiveIntentData();
    }

    private void receiveIntentData() {
        Intent scannerIntent = getIntent();

        // Set flash if was enabled
        mNavigationView.setFlash(scannerIntent.getBooleanExtra(CameraActivity.FLASH_ENABLED, false));

        objectID = scannerIntent.getStringExtra(CameraActivity.OBJECT_ID);
        objectName = scannerIntent.getStringExtra(CameraActivity.OBJECT_NAME);
    }

    private void setListeners() {
        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNavigationView.toggleFlash();
            }
        });
    }
}
