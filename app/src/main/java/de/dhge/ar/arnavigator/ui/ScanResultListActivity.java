package de.dhge.ar.arnavigator.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.dhge.ar.arnavigator.R;
import de.dhge.ar.arnavigator.util.ContentType;
import de.dhge.ar.arnavigator.util.ScanResult;
import de.dhge.ar.arnavigator.util.TinyDB;

/**
 * An activity representing a list of ScanResults. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ScanResultDetailActivity} representing
 * item name. On tablets, the activity presents the list of items and
 * item name side-by-side using two vertical panes.
 */
public class ScanResultListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanresult_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        View recyclerView = findViewById(R.id.scanresult_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.scanresult_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        List<ScanResult> scans = ScanResult.getAll(new TinyDB(this));
        ArrayList<ScanResult> mostRecentScans = new ArrayList<>(scans);
        // Order to most recent
        Collections.reverse(mostRecentScans);
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(mostRecentScans));
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<ScanResult> mValues;

        public SimpleItemRecyclerViewAdapter(List<ScanResult> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.scanresult_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            // Set header
            switch (holder.mItem.getType()) {
                case ContentType.ROOM:
                    holder.scanTypeImageView.setImageResource(R.drawable.ic_room);
                    break;
                case ContentType.MEDIA:
                    holder.scanTypeImageView.setImageResource(R.drawable.ic_media);
                    break;
                case ContentType.MAP:
                    holder.scanTypeImageView.setImageResource(R.drawable.ic_map);
                    break;
                case ContentType.ONLINE_TARGET:
                    holder.scanTypeImageView.setImageResource(R.drawable.ic_online_target);
                    break;
            }
            holder.scanNameTextView.setText(holder.mItem.getName());

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(ScanResultDetailFragment.SCAN_TYPE, holder.mItem.getType());
                        arguments.putString(ScanResultDetailFragment.SCAN_NAME, holder.mItem.getName());
                        arguments.putString(ScanResultDetailFragment.SCAN_CONTENT, holder.mItem.getContent());
                        ScanResultDetailFragment fragment = new ScanResultDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.scanresult_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, ScanResultDetailActivity.class);
                        intent.putExtra(ScanResultDetailFragment.SCAN_TYPE, holder.mItem.getType());
                        intent.putExtra(ScanResultDetailFragment.SCAN_NAME, holder.mItem.getName());
                        intent.putExtra(ScanResultDetailFragment.SCAN_CONTENT, holder.mItem.getContent());

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final ImageView scanTypeImageView;
            public final TextView scanNameTextView;
            public ScanResult mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                scanTypeImageView = (ImageView) view.findViewById(R.id.scanresult_type);
                scanNameTextView = (TextView) view.findViewById(R.id.scanresult_name);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + scanNameTextView.getText() + "'";
            }
        }
    }
}