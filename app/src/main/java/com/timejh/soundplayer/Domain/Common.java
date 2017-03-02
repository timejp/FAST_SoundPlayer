package com.timejh.soundplayer.Domain;

import android.net.Uri;

/**
 * Created by tokijh on 2017. 2. 28..
 */

public abstract class Common {
    public abstract String getTitle();
    public abstract String getArtist();
    public abstract String getDuration();
    public abstract Uri getImageUri();
}