package de.dhge.ar.arnavigator.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

import de.dhge.ar.arnavigator.R;
import de.dhge.ar.arnavigator.navigation.Node;
import de.dhge.ar.arnavigator.navigation.NodeGraph;

public class NavigationActivity extends AppCompatActivity {

    private NavigationView mNavigationView;
    private ImageView flashButton;

    // AR Object
    private String objectID;
    private String objectName;
    private NodeGraph nodeGraph;

    private static NavigationActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        // current hardcoded map
        // each line has one statement either N or Node; C or Connection
        // N ID X Y Name  -- creates a new Node with ID at (X,Y) and the specified name
        // C ID1 ID2 Dist -- creates a new Connection from Node ID1 to Node ID2 (and vice versa) with the speficied distance
        String definition = "Node 1 0 0 Start\n" +
                "Node 2 2 0 S1\n" +
                "Node 3 4 0 S2\n" +
                "Node 4 7 0 Kreuzung\n" +
                "Node 5 7 -1 Treppe\n" +
                "Node 6 7 -3 Weg\n" +
                "Node 7 7 3 AndererWeg\n" +
                "Node 8 6 4 Unten\n" +
                "Node 9 4 6 Labor\n" +
                "C 1 2 2\n" +
                "C 2 3 2\n" +
                "C 3 4 3\n" +
                "C 4 5 1\n" +
                "C 4 6 3\n" +
                "C 4 7 3\n" +
                "C 5 8 6\n" +
                "C 8 9 4";

        nodeGraph = new NodeGraph(definition);

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

    public static Object getSystemServiceHelper(String service)
    {
        if(instance == null)
            return null;

        return instance.getSystemService(service);
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

        // calculates the path using the map from start to finish (as string or id)
        ArrayList<Node> path = nodeGraph.getPath(objectName, "Labor");
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
