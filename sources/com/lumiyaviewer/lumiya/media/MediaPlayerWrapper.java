package com.lumiyaviewer.lumiya.media;

import android.media.MediaPlayer;
import com.lumiyaviewer.lumiya.Debug;

/* JADX INFO: loaded from: classes.dex */
public class MediaPlayerWrapper implements Runnable, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnPreparedListener {
    private MediaPlayer mediaPlayer = null;
    private Thread workingThread = null;
    private volatile boolean mustPlay = false;
    private volatile boolean mustExit = false;
    private volatile String mediaURL = "";

    @Override // android.media.MediaPlayer.OnErrorListener
    public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
        Debug.Log("MediaPlayerWrapper: onError: what = " + i + ", extra = " + i2);
        return false;
    }

    @Override // android.media.MediaPlayer.OnInfoListener
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i2) {
        Debug.Log("MediaPlayerWrapper: onInfo: what = " + i + ", extra = " + i2);
        return false;
    }

    @Override // android.media.MediaPlayer.OnPreparedListener
    public void onPrepared(MediaPlayer mediaPlayer) {
        Debug.Log("MediaPlayerWrapper: prepared, starting playback");
        mediaPlayer.start();
    }

    public void play(String str) {
        synchronized (this) {
            if (this.mustExit) {
                return;
            }
            this.mustPlay = true;
            this.mustExit = false;
            String strTrim = str.trim();
            if (strTrim.toLowerCase().startsWith("http://")) {
                strTrim = "http://" + strTrim.substring(7);
            } else if (strTrim.toLowerCase().startsWith("https://")) {
                strTrim = "https://" + strTrim.substring(8);
            }
            this.mediaURL = strTrim;
            if (this.workingThread == null) {
                this.workingThread = new Thread(this);
                this.workingThread.start();
            }
            notify();
        }
    }

    public void release() {
        synchronized (this) {
            this.mustPlay = false;
            this.mustExit = true;
            this.mediaURL = "";
            this.workingThread = null;
            notify();
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:30:0x00cf  */
    /* JADX WARN: Removed duplicated region for block: B:43:? A[RETURN, SYNTHETIC] */
    @Override // java.lang.Runnable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void run() {
        /*
            Method dump skipped, instruction units count: 218
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lumiyaviewer.lumiya.media.MediaPlayerWrapper.run():void");
    }

    public void stop() {
        synchronized (this) {
            if (this.mustExit) {
                return;
            }
            this.mustPlay = false;
            this.mediaURL = "";
            notify();
        }
    }
}
