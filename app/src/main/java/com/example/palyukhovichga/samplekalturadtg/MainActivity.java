package com.example.palyukhovichga.samplekalturadtg;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kaltura.dtg.ContentManager;
import com.kaltura.dtg.DownloadItem;
import com.kaltura.dtg.DownloadState;
import com.kaltura.dtg.DownloadStateListener;
import com.kaltura.playersdk.KPPlayerConfig;
import com.kaltura.playersdk.PlayerViewController;
import com.kaltura.playersdk.events.KPErrorEventListener;
import com.kaltura.playersdk.events.KPStateChangedEventListener;
import com.kaltura.playersdk.events.KPlayerState;
import com.kaltura.playersdk.types.KPError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements KPErrorEventListener, KPStateChangedEventListener {

    private static final String TAG = "MainActivity";

    private ContentManager mContentManager;

    private PlayerViewController mPlayer;
    private ViewGroup mPlayerContainer;

    private List<Item> items;
    private ItemsAdapter adapter;

    private DownloadStateListener downloadStateListener;

    private PlayerViewController.SourceURLProvider mSourceURLProvider = new PlayerViewController.SourceURLProvider() {
        @Override
        public String getURL(String entryId, String currentURL) {

            String playbackURL = mContentManager.getPlaybackURL(entryId);

            return playbackURL;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContentManager = ContentManager.getInstance(this);

        mPlayerContainer = (ViewGroup) findViewById(R.id.layout_player_container);

        downloadStateListener = new DownloadStateListener() {
            @Override
            public void onDownloadComplete(final DownloadItem downloadItem) {
                Item item = findItem(downloadItem.getItemId());
                item.setState(DownloadState.COMPLETED);
                notifyAdapter();
            }

            @Override
            public void onProgressChange(final DownloadItem downloadItem, final long downloadedBytes) {
                Item item = findItem(downloadItem.getItemId());
                item.setProgress((int) (100 * (double) downloadedBytes / (double) downloadItem.getEstimatedSizeBytes()));
                notifyAdapter();
            }

            @Override
            public void onDownloadStart(final DownloadItem downloadItem) {
            }

            @Override
            public void onDownloadPause(final DownloadItem item) {
                Log.d(TAG, "Download paused");
            }

            @Override
            public void onDownloadFailure(DownloadItem item, Exception error) {
                Log.d(TAG, error.toString());
            }

            @Override
            public void onDownloadMetadata(DownloadItem item, Exception error) {
                DownloadItem.TrackSelector trackSelector = item.getTrackSelector();

                if (trackSelector != null) {
                    List<DownloadItem.Track> downloadedVideoTracks = trackSelector.getDownloadedTracks(DownloadItem.TrackType.VIDEO);

                    List<DownloadItem.Track> availableTracks = trackSelector.getAvailableTracks(DownloadItem.TrackType.AUDIO);
                    if (availableTracks.size() > 0) {
                        trackSelector.setSelectedTracks(DownloadItem.TrackType.AUDIO, availableTracks);
                    }
                    try {
                        trackSelector.apply();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                item.startDownload();
            }

            @Override
            public void onTracksAvailable(DownloadItem item, DownloadItem.TrackSelector trackSelector) {

            }
        };

        mContentManager.addDownloadStateListener(downloadStateListener);
        mContentManager.start(new ContentManager.OnStartedListener() {
            @Override
            public void onStarted() {
                createItems();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContentManager.removeDownloadStateListener(downloadStateListener);
        mContentManager.stop();
    }

    void createItems() {
        items = new ArrayList<>();

        String key = "webvtt";
        KPPlayerConfig config = createConfig("1_kbt0jshy", key);
        String remoteUrl = "http://www.kaltura.com/p/1851571/playManifest/entryId/1_kbt0jshy/format/mpegdash/manifest.mpd";
        items.add(new Item(config, key, remoteUrl));

        key = "sintel-dash-drm";
        config = createConfig("0_pl5lbfo0", key);
        remoteUrl = "http://cfvod.kaltura.com/edash/p/1851571/sp/185157100/serveFlavor/entryId/0_pl5lbfo0/v/2/flavorId/0_ywkmqnkg/forceproxy/true/name/a.mp4/manifest.mpd";
        items.add(new Item(config, key, remoteUrl));

        key = "count-dash-drm";
        config = createConfig("0_uafvpmv8", key);
        remoteUrl = "http://cfvod.kaltura.com/edash/p/1851571/sp/185157100/serveFlavor/entryId/0_uafvpmv8/v/2/flavorId/0_6ackygkg/forceproxy/true/name/a.mp4/manifest.mpd";
        items.add(new Item(config, key, remoteUrl));

        key = "count-dash-clear";
        config = createConfig("0_7xgt6xfm", key);
        remoteUrl = "http://cfvod.kaltura.com/dasha/p/1851571/sp/185157100/serveFlavor/entryId/0_7xgt6xfm/v/2/flavorId/0_uzum8idx/forceproxy/true/name/a.mp4/manifest.mpd";
        items.add(new Item(config, key, remoteUrl));

        key = "cat-dash-clear";
        config = createConfig("1_aegxx56o", key);
        remoteUrl = "http://cfvod.kaltura.com/dasha/p/1851571/sp/185157100/serveFlavor/entryId/1_aegxx56o/v/1/flavorId/1_,aope8b8c,ygii4ciz,/forceproxy/true/name/a.mp4.urlset/manifest.mpd";
        items.add(new Item(config, key, remoteUrl));

        key = "kaltura";
        config = createConfig("1_cvd1usav", key);
        remoteUrl = "http://cfvod.kaltura.com/pd/p/1851571/sp/185157100/serveFlavor/entryId/1_cvd1usav/v/11/flavorId/1_wabcvjtu/name/a.mp4";
        items.add(new Item(config, key, remoteUrl));

        adapter = new ItemsAdapter(this, items);
        ListView listView = (ListView) findViewById(R.id.items_list);
        listView.setAdapter(adapter);
    }

    KPPlayerConfig createConfig(String entryId, String key) {
        KPPlayerConfig config = new KPPlayerConfig("http://kgit.html5video.org/tags/v2.48.1/mwEmbedFrame.php",
                "31956421", "1851571");
        config.setEntryId(entryId);
        config.setLocalContentId(key);
        config.addConfig("autoPlay", "true");
        return config;
    }


    boolean isDownloaded(Item item) {
        DownloadItem downloadItem = findDownloadItem(item);

        return downloadItem != null && downloadItem.getState() == DownloadState.COMPLETED;
    }

    DownloadItem findDownloadItem(Item item) {
        return mContentManager.findItem(item.getDownloadItemId());
    }

    String getLocalPath(Item item) {
        return mContentManager.getLocalFile(item.getDownloadItemId()).getAbsolutePath();
    }

    private Item findItem(String itemId) {
        for (Item item : items) {
            if (itemId.equals(item.getDownloadItemId())) {
                return item;
            }
        }
        throw new RuntimeException("Unknown item id: " + itemId);
    }

    private void notifyAdapter() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private PlayerViewController getPlayer(KPPlayerConfig config) {
        if (mPlayer == null) {
            mPlayer = new PlayerViewController(this);
            mPlayerContainer.addView(mPlayer, new ViewGroup.LayoutParams(mPlayerContainer.getLayoutParams()));

            mPlayer.loadPlayerIntoActivity(this);

            mPlayer.initWithConfiguration(config);

            mPlayer.setCustomSourceURLProvider(mSourceURLProvider);

            mPlayer.setOnKPErrorEventListener(this);
            mPlayer.setOnKPStateChangedEventListener(this);

        } else {
            if (mPlayer.getParent() == null) {
                mPlayerContainer.addView(mPlayer, new ViewGroup.LayoutParams(mPlayerContainer.getLayoutParams()));
            }

            mPlayer.changeConfiguration(config);
        }

        return mPlayer;
    }

    @Override
    public void onKPlayerStateChanged(PlayerViewController playerViewController, KPlayerState state) {
        Log.d(TAG, "onKPlayerStateChanged: " + state);
    }

    @Override
    public void onKPlayerError(PlayerViewController playerViewController, KPError error) {
        Log.d(TAG, "onKPlayerError " + error.getException());
    }

    private static class ViewHolder {
        TextView name;
        Button startDownloadButton;
        Button pauseDownloadButton;
        Button stopDownloadButton;
        Button playButton;
        ProgressBar progressBar;
    }

    private class ItemsAdapter extends ArrayAdapter<Item> {
        public ItemsAdapter(Context context, List<Item> items) {
            super(context, R.layout.view_download_item, items);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            Item item = getItem(position);
            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.view_download_item_content, parent, false);

                holder = new ViewHolder();
                holder.name = convertView.findViewById(R.id.item_name);
                holder.startDownloadButton = convertView.findViewById(R.id.item_button_start);
                holder.pauseDownloadButton = convertView.findViewById(R.id.item_button_pause);
                holder.stopDownloadButton = convertView.findViewById(R.id.item_button_stop);
                holder.playButton = convertView.findViewById(R.id.item_button_play);
                holder.progressBar = convertView.findViewById(R.id.item_progress);

                holder.startDownloadButton.setOnClickListener(new OnStartClickListener(position));
                holder.pauseDownloadButton.setOnClickListener(new OnPauseClickListener(position));
                holder.stopDownloadButton.setOnClickListener(new OnStopClickListener(position));
                holder.playButton.setOnClickListener(new OnPlayClickListener(position));

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.name.setText(item.name);

            if (isDownloaded(item)) {
                item.setState(DownloadState.COMPLETED);
            }

            switch (item.getState()) {
                case NEW: {
                    holder.startDownloadButton.setEnabled(true);
                    holder.pauseDownloadButton.setEnabled(false);
                    holder.stopDownloadButton.setEnabled(false);
                    holder.playButton.setEnabled(false);
                    holder.progressBar.setProgress(0);
                    break;
                }
                case IN_PROGRESS: {
                    holder.startDownloadButton.setEnabled(false);
                    holder.pauseDownloadButton.setEnabled(true);
                    holder.stopDownloadButton.setEnabled(true);
                    holder.playButton.setEnabled(false);
                    holder.progressBar.setProgress(item.getProgress());
                    break;
                }
                case PAUSED: {
                    holder.startDownloadButton.setEnabled(true);
                    holder.pauseDownloadButton.setEnabled(false);
                    break;
                }
                case COMPLETED: {
                    holder.startDownloadButton.setEnabled(false);
                    holder.pauseDownloadButton.setEnabled(false);
                    holder.stopDownloadButton.setEnabled(true);
                    holder.playButton.setEnabled(true);
                    holder.progressBar.setProgress(100);
                    break;
                }
                default:
                    break;
            }

            return convertView;
        }
    }

    private class OnPlayClickListener implements View.OnClickListener {
        private int itemPosition;

        public OnPlayClickListener(int itemPosition) {
            this.itemPosition = itemPosition;
        }
        @Override
        public void onClick(View view) {
            Item item = items.get(itemPosition);
            getPlayer(item.config);
        }
    }

    private class OnStartClickListener implements View.OnClickListener {
        private int itemPosition;

        public OnStartClickListener(int itemPosition) {
            this.itemPosition = itemPosition;
        }

        @Override
        public void onClick(View view) {
            Item item = items.get(itemPosition);
            item.setState(DownloadState.IN_PROGRESS);
            adapter.notifyDataSetChanged();

            DownloadItem downloadItem = mContentManager.createItem(item.getDownloadItemId(), item.contentUrl);
            if (downloadItem == null) {
                downloadItem = mContentManager.findItem(item.getDownloadItemId());
            }
            if (downloadItem != null) {
                downloadItem.loadMetadata();
            }
        }
    }

    private class OnPauseClickListener implements View.OnClickListener {
        private int itemPosition;

        public OnPauseClickListener(int itemPosition) {
            this.itemPosition = itemPosition;
        }
        @Override
        public void onClick(View view) {
            Log.d(TAG, "pause");

            Item item = items.get(itemPosition);
            item.setState(DownloadState.PAUSED);
            adapter.notifyDataSetChanged();

            DownloadItem downloadItem = mContentManager.createItem(item.getDownloadItemId(), item.contentUrl);
            if (downloadItem == null) {
                downloadItem = mContentManager.findItem(item.getDownloadItemId());
            }
            if (downloadItem != null) {
                downloadItem.pauseDownload();
            }
        }
    }

    private class OnStopClickListener implements View.OnClickListener {
        private int itemPosition;

        public OnStopClickListener(int itemPosition) {
            this.itemPosition = itemPosition;
        }

        @Override
        public void onClick(View view) {
            Log.d(TAG, "stop");

            Item item = items.get(itemPosition);
            item.setState(DownloadState.NEW);
            item.setProgress(0);
            adapter.notifyDataSetChanged();

            DownloadItem downloadItem = mContentManager.createItem(item.getDownloadItemId(), item.contentUrl);
            if (downloadItem == null) {
                downloadItem = mContentManager.findItem(item.getDownloadItemId());
            }
            if (downloadItem != null) {
                mContentManager.removeItem(item.getDownloadItemId());
            }
        }
    }
}
