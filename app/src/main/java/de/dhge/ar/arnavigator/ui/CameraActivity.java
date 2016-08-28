package de.dhge.ar.arnavigator.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import de.dhge.ar.arnavigator.R;
import de.dhge.ar.arnavigator.util.ContentParser;
import de.dhge.ar.arnavigator.util.ContentType;
import de.dhge.ar.arnavigator.util.HTMLFormatter;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class CameraActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler {
    // Permission constants
    private final int PERMISSION_CAMERA = 1;

    // Views
    private ZBarScannerView mScannerView;
    private LinearLayout arPopupMenu;
    private WebView webView;
    private FloatingActionButton routeButton;
    private ImageView flashButton;
    private TextView identifierLabel;
    private ImageView arTypeIcon;
    private ProgressBar arLoadUrlProgressBar;
    private String result;
    private boolean arShow = false;

    // Context
    private AppCompatActivity context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        getPermissions();

        // Assign View IDs and general setup
        initializeViews();

        // Hide ar views
        toggleARContent(false);

        setupWebView();
        setListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Prevent webView video playback
        resetWebView();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_history) {
            Intent historyIntent = new Intent(this, HistoryActivity.class);
            startActivity(historyIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (arShow) {
            toggleARContent(false);
            restartCamera();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CAMERA: {
                // Show an error dialog if permission is denied
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            this);
                    // set title
                    alertDialogBuilder.setTitle(getString(R.string.camera_permission));

                    // set dialog message
                    alertDialogBuilder
                            .setMessage(getString(R.string.camera_permission_denied))
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // Close the camera app
                                    CameraActivity.this.finish();
                                }
                            });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();
                }
            }

        }
    }

    @Override
    public void handleResult(Result rawResult) {
        // Only allow QR Codes
        if (rawResult.getBarcodeFormat().getName().equals("QRCODE")) {
            result = rawResult.getContents();
            viewARContent();
        } else {
            restartCamera();
        }
    }

    // Restarts the camera from suspened mode
    private void restartCamera() {
        // Note:
        // * Wait few seconds to resume the preview.
        // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
        // * I don't know why this is the case but I don't have the time to figure out.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScannerView.resumeCameraPreview(CameraActivity.this);
            }
        }, 500);
    }

    private void viewARContent() {
        ContentParser cp = null;
        try {
            cp = new ContentParser(result);

            // Set header
            switch (cp.getType()) {
                case ContentType.ROOM:
                    arTypeIcon.setImageResource(R.drawable.ic_room);
                    break;
                case ContentType.MEDIA:
                    arTypeIcon.setImageResource(R.drawable.ic_media);
                    break;
                case ContentType.MAP:
                    arTypeIcon.setImageResource(R.drawable.ic_map);
                    break;
                case ContentType.ONLINE_TARGET:
                    arTypeIcon.setImageResource(R.drawable.ic_online_target);
                    break;
            }
            identifierLabel.setText(cp.getName());

            // Handle types
            switch (cp.getType()) {
                case ContentType.ROOM:
                    // Set webView
                    String roomContent = cp.getContent();

                    if (cp.isRawContent()) {
                        roomContent = new HTMLFormatter(roomContent).prettyPrint("white", "none", "20pt", "");
                    }
                    webView.loadData(roomContent, "text/html", null);
                    break;
                case ContentType.MEDIA:
                    String mediaContent = cp.getContent();

                    if (cp.isRawContent()) {
                        mediaContent = new HTMLFormatter(mediaContent).getWebSite();
                    }
                    // Set webView
                    showARWebViewProgressbar(true);
                    webView.loadData(mediaContent, "text/html", null);
                    break;
                case ContentType.MAP:
                    arTypeIcon.setImageResource(R.drawable.ic_map);
                    break;
                case ContentType.ONLINE_TARGET:
                    showARWebViewProgressbar(true);
                    webView.loadUrl(cp.getContent());
                    break;
                case ContentType.EXIT:
                    arTypeIcon.setImageResource(R.drawable.ic_exit);
                    break;
                case ContentType.STAIRS_UP:
                    // Currently not used
                    break;
                case ContentType.STAIRS_DOWN:
                    // Currently not used
                    break;
                case ContentType.ADJUSTMENT_POINT:
                    // Currently not used
                    break;
            }


        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        toggleARContent(true);

        // Make user aware of result
        // vibration for 200 milliseconds
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(200);
    }

    private void toggleARContent(boolean viewState) {
        arShow = viewState;

        if (viewState) {
            arPopupMenu.setVisibility(View.VISIBLE);
            routeButton.setVisibility(View.VISIBLE);
        } else {
            arPopupMenu.setVisibility(View.GONE);
            routeButton.setVisibility(View.GONE);
            showARWebViewProgressbar(viewState);
            resetWebView();
        }
    }

    private void showARWebViewProgressbar(boolean viewState) {
        if (viewState) {
            arLoadUrlProgressBar.setVisibility(View.VISIBLE);
        } else {
            arLoadUrlProgressBar.setVisibility(View.GONE);
        }
    }

    // Permissions
    private void getPermissions() {
        // Only request permissions for Marshmallow and higher
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
            }
        }
    }

    // Setup
    private void initializeViews() {
        // setup Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Add ZBar scanner view
        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.content_frame);
        mScannerView = new ZBarScannerView(this);
        contentFrame.addView(mScannerView);

        // Initialize views
        arPopupMenu = (LinearLayout) findViewById(R.id.ar_popup_menu);
        webView = (WebView) findViewById(R.id.webview_content);
        routeButton = (FloatingActionButton) findViewById(R.id.btn_route);
        flashButton = (ImageView) findViewById(R.id.iv_flash);
        identifierLabel = (TextView) findViewById(R.id.lbl_identifier);
        arTypeIcon = (ImageView) findViewById(R.id.img_ar_type);
        arLoadUrlProgressBar = (ProgressBar) findViewById(R.id.pb_ar_load_url);
    }

    private void setListeners() {
        // User touched not at popup menu
        mScannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleARContent(false);
                restartCamera();
            }
        });

        routeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent navIntent = new Intent(context, NavigationActivity.class);
                // Transmit flash state
                navIntent.putExtra("flash_enabled", mScannerView.getFlash());
                startActivity(navIntent);
            }
        });

        // Toggle flashlight
        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mScannerView.toggleFlash();
            }
        });
    }

    private void resetWebView() {
        webView.loadUrl("about:blank");
    }

    private void setupWebView() {
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (webView.getProgress() == 100) {
                    showARWebViewProgressbar(false);
                }
            }
        });
        // webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
    }
}