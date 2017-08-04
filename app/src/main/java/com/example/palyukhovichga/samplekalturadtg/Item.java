package com.example.palyukhovichga.samplekalturadtg;

import com.kaltura.dtg.DownloadState;
import com.kaltura.playersdk.KPPlayerConfig;

/**
 * Created by palyukhovichga on 25.07.2017.
 */

public class Item {
    KPPlayerConfig config;
    String name;
    String contentUrl;
    DownloadState state;
    int progress;

    public Item(KPPlayerConfig config, String name, String contentUrl) {
        this.config = config;
        this.name = name;
        this.contentUrl = contentUrl;
        this.state = DownloadState.NEW;
        progress = 0;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getDownloadItemId() {
        return config.getEntryId();
    }

    public synchronized DownloadState getState() {
        return state;
    }

    public synchronized void setState(DownloadState state) {
        this.state = state;
    }

    public synchronized int getProgress() {
        return progress;
    }

    public synchronized void setProgress(int progress) {
        this.progress = progress;
    }
}
