package de.dhge.ar.arnavigator.ui;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import de.dhge.ar.arnavigator.R;
import de.dhge.ar.arnavigator.util.ContentType;

/**
 * A fragment representing a single ScanResult detail screen.
 * This fragment is either contained in a {@link ScanResultListActivity}
 * in two-pane mode (on tablets) or a {@link ScanResultDetailActivity}
 * on handsets.
 */
public class ScanResultDetailFragment extends Fragment {
    // Information IDs
    public static final String SCAN_CONTENT = "scannedContent";
    public static final String SCAN_NAME = "scannedName";
    public static final String SCAN_TYPE = "scannedType";

    // Information about Item
    private String itemType;
    private String itemName;
    private String itemContent;

    // Views
    private WebView webView;
    private Activity activity;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ScanResultDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this.getActivity();

        if (getArguments().containsKey(SCAN_CONTENT)) {
            itemType = getArguments().getString(SCAN_TYPE);
            itemName = getArguments().getString(SCAN_NAME);
            itemContent = getArguments().getString(SCAN_CONTENT);

            setTitle(activity, String.format("%s %s...", itemName, getString(R.string.loading)));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.scanresult_detail, container, false);

        webView = (WebView) rootView.findViewById(R.id.scanresult_content);
        setupWebView();

        switch (itemType) {
            case ContentType.ROOM:
                webView.loadData(itemContent, "text/html", null);
                break;
            case ContentType.MEDIA:
                webView.loadData(itemContent, "text/html", null);
                break;
            case ContentType.ONLINE_TARGET:
                webView.loadUrl(itemContent);
                break;
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Prevent webView video playback
        resetWebView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Prevent webView video playback
        resetWebView();
    }

    // Sets title of toolbar
    private void setTitle(Activity activity, String text) {
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(text);
        }
    }

    // Initialize WebView
    private void setupWebView() {
        webView.setBackgroundColor(Color.GREEN);
        webView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (webView.getProgress() == 100) {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            setTitle(activity, itemName);
                        }
                    });

                }
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
    }

    private void resetWebView() {
        webView.loadUrl("about:blank");
    }
}

