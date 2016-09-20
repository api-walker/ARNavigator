package de.dhge.ar.arnavigator.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.dhge.ar.arnavigator.R;
import de.dhge.ar.arnavigator.navigation.Node;
import de.dhge.ar.arnavigator.navigation.NodeGraph;
import de.dhge.ar.arnavigator.util.MapUtils;

public class NavigationActivity extends AppCompatActivity {

    // Views
    private NavigationView mNavigationView;
    private ImageView flashButton;

    // Identifier
    final String DATA_DESTINATION = "destination";

    // AR Object
    private String objectID;
    private String objectName;
    private NodeGraph nodeGraph;
    private Node currentNode;
    private String destination;
    private List<String> objectNames;

    private static NavigationActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        initializeMapEnvironment();
        initializeViews();
        setListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        mNavigationView.startCamera();          // Start camera on resume
        manageDestination();
    }

    @Override
    public void onPause() {
        super.onPause();
        mNavigationView.stopCamera();           // Stop camera on pause
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(DATA_DESTINATION, destination);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        destination = savedInstanceState.getString(DATA_DESTINATION);
    }

    public static Object getSystemServiceHelper(String service) {
        if (instance == null)
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

    private void initializeMapEnvironment() {
        String definition = MapUtils.readMapFile(getResources().openRawResource(R.raw.dhge_map));
        nodeGraph = new NodeGraph(definition);
        objectNames = nodeGraph.getNames();
    }

    private void receiveIntentData() {
        Intent scannerIntent = getIntent();

        // Set flash if was enabled
        mNavigationView.setFlash(scannerIntent.getBooleanExtra(CameraActivity.FLASH_ENABLED, false));

        objectID = scannerIntent.getStringExtra(CameraActivity.OBJECT_ID);
        objectName = scannerIntent.getStringExtra(CameraActivity.OBJECT_NAME);

        // remove start object from destination list
        objectNames.remove(objectName);
    }

    private void setListeners() {
        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNavigationView.toggleFlash();
            }
        });
    }

    // If new activity show picker, else set arrow
    public void manageDestination() {
        if (destination == null) {
            // let the user pick a destination
            showDestinationDialog();
        } else {
            setCurrentNode();
        }
    }

    public void showDestinationDialog() {
        AlertDialog.Builder destinationDialog = new AlertDialog.Builder(this);

        destinationDialog.setCancelable(false);
        destinationDialog.setTitle(R.string.title_select_destination)
                .setItems(objectNames.toArray(new String[objectNames.size()]), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Set the selected destination and start navigation
                        destination = objectNames.get(which);
                        setCurrentNode();
                    }
                });

        destinationDialog.create();
        destinationDialog.show();
    }

    private void setCurrentNode() {
        // calculates the path using the map from start to finish (as string or id)
        ArrayList<Node> path = nodeGraph.getPath(objectName, destination);
        currentNode = path.get(0);
    }

    public static Node getCurrentNode() {
        if (instance == null)
            return null;

        return instance.currentNode;
    }
}
