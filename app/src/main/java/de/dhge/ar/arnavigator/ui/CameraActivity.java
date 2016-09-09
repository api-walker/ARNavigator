package de.dhge.ar.arnavigator.ui;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import de.dhge.ar.arnavigator.R;
import de.dhge.ar.arnavigator.util.ContentParser;
import de.dhge.ar.arnavigator.util.ContentType;
import de.dhge.ar.arnavigator.util.HTMLFormatter;
import de.dhge.ar.arnavigator.util.ScanResult;
import de.dhge.ar.arnavigator.util.TinyDB;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class CameraActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler {
    // Permission constants
    private final int PERMISSION_CAMERA = 1;

    // Identifier
    static final String FLASH_ENABLED = "flash_enabled";
    static final String OBJECT_ID = "object_id";
    static final String OBJECT_NAME = "object_name";
    private final int PROGRESSBAR = 100;

    // Views
    private ZBarScannerView mScannerView;
    private LinearLayout arPopupMenu;
    private WebView webView;
    private FloatingActionButton routeButton;
    private ImageView flashButton;
    private TextView identifierLabel;
    private ImageView arTypeIcon;
    private ProgressBar arLoadUrlProgressBar;
    private ProgressDialog webcontentDownloadDialog;

    // Context
    private AppCompatActivity context = this;

    // DB
    private TinyDB db;

    // Flags
    private boolean flashEnabled = false;
    private boolean arShow = false;
    private boolean webContentFound = false;
    private boolean showURLIntent = false;

    // AR Object
    private String objectID;
    private String objectName;
    private String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        getPermissions();
        initializePolicies();

        initializeDB();

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
        outState.putBoolean(FLASH_ENABLED, flashEnabled);

        if (!showURLIntent) {
            // Prevent webView video playback
            toggleARContent(false);
        } else {
            showURLIntent = false;
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
        flashEnabled = savedInstanceState.getBoolean(FLASH_ENABLED);
        mScannerView.setFlash(flashEnabled);
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
            toggleARContent(false);
            Intent historyIntent = new Intent(this, ScanResultListActivity.class);
            startActivity(historyIntent);
        } else if (id == R.id.action_exit) {
            // Close the camera app
            CameraActivity.this.finish();
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
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESSBAR:
                if (webContentFound) {
                    webcontentDownloadDialog = new ProgressDialog(this);
                    webcontentDownloadDialog.setMessage(getString(R.string.downloading_webcontent));
                    webcontentDownloadDialog.setIndeterminate(false);
                    webcontentDownloadDialog.setMax(100);
                    webcontentDownloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    webcontentDownloadDialog.setCancelable(true);
                    webcontentDownloadDialog.show();
                    return webcontentDownloadDialog;
                }
            default:
                return null;
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
        String content = "";

        try {
            cp = new ContentParser(result);

            if (cp.isValidContent()) {
                content = cp.getContent();
                // Set intent information
                objectID = cp.getID();
                objectName = cp.getName();

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
                    case ContentType.EXIT:
                        arTypeIcon.setImageResource(R.drawable.ic_exit);
                        break;
                }
                identifierLabel.setText(cp.getName());

                // Handle types
                switch (cp.getType()) {
                    case ContentType.ROOM:
                        // is webcontent ?
                        if(cp.isWebContent()) {
                            setWebContent(content);
                        }
                        else {
                            // Set webView
                            if (cp.isRawContent()) {
                                content = new HTMLFormatter(content).prettyPrint("white", "none", "20pt", "");
                            }
                            webView.loadData(content, "text/html", null);
                        }
                        break;
                    case ContentType.MEDIA:
                        // is webcontent ?
                        if(cp.isWebContent()) {
                            setWebContent(content);
                        }
                        else {
                            if (cp.isRawContent()) {
                                content = new HTMLFormatter(content).getWebSite();
                            }
                            // Set webView
                            showARWebViewProgressbar(true);
                            webView.loadData(content, "text/html", null);
                        }
                        break;
                    case ContentType.MAP:
                        break;
                    case ContentType.ONLINE_TARGET:
                        showARWebViewProgressbar(true);
                        webView.loadUrl(content);
                        break;
                    case ContentType.EXIT:
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

                if (!webContentFound) {
                    // Save scanned object
                    List<ScanResult> savedEntries = ScanResult.getAll(db);
                    savedEntries.add(new ScanResult(cp.getType(), cp.getName(), content));
                    ScanResult.saveScanResults(db, savedEntries);

                    toggleARContent(true);

                    // Make user aware of result
                    // vibration for 200 milliseconds
                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(200);
                }
            } else {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        this);
                // set title
                alertDialogBuilder.setTitle(getString(R.string.invalid_qr_code_title));

                // set dialog message
                alertDialogBuilder
                        .setMessage(getString(R.string.invalid_qr_code_text))
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                restartCamera();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        } catch (ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }

    private void toggleARContent(boolean viewState) {
        arShow = viewState;

        if (viewState) {
            arPopupMenu.setVisibility(View.VISIBLE);
            routeButton.setVisibility(View.VISIBLE);
            // hide flash button on popup
            flashButton.setVisibility(View.GONE);
        } else {
            arPopupMenu.setVisibility(View.GONE);
            routeButton.setVisibility(View.GONE);
            flashButton.setVisibility(View.VISIBLE);
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

    private void setWebContent(String url) {
        webContentFound = true;
        // Download online xml file
        new WebContentDownloader().execute(url);
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
    private void initializeDB() {
        // setup Toolbar
        db = new TinyDB(context);
    }

    private void initializePolicies() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

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
                toggleARContent(false);

                Intent navIntent = new Intent(context, NavigationActivity.class);
                // Transmit flash state
                navIntent.putExtra(FLASH_ENABLED, mScannerView.getFlash());
                // Object details
                navIntent.putExtra(OBJECT_ID, objectID);
                navIntent.putExtra(OBJECT_NAME, objectName);

                startActivity(navIntent);
            }
        });

        // Toggle flashlight
        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mScannerView.toggleFlash();
                flashEnabled = !flashEnabled;
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

            @Override
            public boolean shouldOverrideUrlLoading(WebView wv, String url) {
                if (url.startsWith("tel:") || url.startsWith("sms:") || url.startsWith("smsto:") || url.startsWith("mms:") || url.startsWith("mmsto:")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    showURLIntent = true;
                    return true;
                } else if (url.startsWith("mailto:")) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                    startActivity(intent);
                    showURLIntent = true;
                    return true;
                }
                return false;
            }
        });
        // webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
    }

    // Appendix
    // Download xml from web
    class WebContentDownloader extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(PROGRESSBAR);
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... webcontent_url) {
            int count;
            try {
                URL url = new URL(webcontent_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = conection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                // Output stream
                OutputStream output = new ByteArrayOutputStream();

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();
                result = output.toString();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * Updating progress bar
         */

        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            webcontentDownloadDialog.setProgress(Integer.parseInt(progress[0]));
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            dismissDialog(PROGRESSBAR);
            webContentFound = false;
            viewARContent();
        }
    }
}