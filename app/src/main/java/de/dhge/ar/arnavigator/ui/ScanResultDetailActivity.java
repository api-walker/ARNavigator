package de.dhge.ar.arnavigator.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import de.dhge.ar.arnavigator.R;

/**
 * An activity representing a single ScanResult detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item name are presented side-by-side with a list of items
 * in a {@link ScanResultListActivity}.
 */
public class ScanResultDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanresult_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ScanResultDetailFragment.SCAN_TYPE,
                    getIntent().getStringExtra(ScanResultDetailFragment.SCAN_TYPE));
            arguments.putString(ScanResultDetailFragment.SCAN_NAME,
                    getIntent().getStringExtra(ScanResultDetailFragment.SCAN_NAME));
            arguments.putString(ScanResultDetailFragment.SCAN_CONTENT,
                    getIntent().getStringExtra(ScanResultDetailFragment.SCAN_CONTENT));
            ScanResultDetailFragment fragment = new ScanResultDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.scanresult_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, ScanResultListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
