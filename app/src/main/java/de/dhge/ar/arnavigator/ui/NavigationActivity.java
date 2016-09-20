package de.dhge.ar.arnavigator.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private Node currentNode;

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
        String definition = readMapFile(getResources().openRawResource(R.raw.dhge_map));
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

    public static Node getCurrentNode()
    {
        if(instance == null)
            return null;

        return instance.currentNode;
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
        currentNode = path.get(0);
    }

    private void setListeners() {
        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNavigationView.toggleFlash();
            }
        });
    }

    // Reads map from resources
    private String readMapFile(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {

        }
        return outputStream.toString();
    }
}
