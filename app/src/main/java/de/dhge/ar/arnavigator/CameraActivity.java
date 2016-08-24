package de.dhge.ar.arnavigator;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
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

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class CameraActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler {
    private ZBarScannerView mScannerView;
    private LinearLayout arPopupMenu;
    private WebView webView;
    private FloatingActionButton routeButton;
    private TextView identifierLabel;
    private ImageView arTypeIcon;
    private ProgressBar arLoadUrlProgressBar;

    private String result;
    private boolean arShow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
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
        identifierLabel = (TextView) findViewById(R.id.lbl_identifier);
        arTypeIcon = (ImageView) findViewById(R.id.img_ar_type);
        arLoadUrlProgressBar = (ProgressBar) findViewById(R.id.pb_ar_load_url);

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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(arShow) {
            toggleARContent(false);
            restartCamera();
        }
        else {
            super.onBackPressed();
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
            switch(cp.getType())
            {
                case ContentType.ROOM:
                    arTypeIcon.setImageResource(R.drawable.ic_room);
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
            switch(cp.getType())
            {
                case ContentType.ROOM:
                    // Set webView
                    webView.loadData(cp.getContent(), "text/html", null);
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

    // Setup
    private void setListeners() {
        // User touched not at popup menu
        mScannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleARContent(false);
                restartCamera();
            }
        });
    }

    private void resetWebView() {
        webView.loadUrl("about:blank");
    }

    private void setupWebView() {
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
    }
}