package jp.ogiwara.java.aileen.utils;

import android.util.SparseArray;

import com.google.android.gms.common.Scopes;
import com.google.api.services.youtube.YouTubeScopes;

import java.util.ArrayList;

/**
 * Created by ogiwara on 2017/03/19.
 */

public final class Constants {

    public static final String API_KEY = "AIzaSyBLW3Flnssn9fJEKTuyb2V1AlyqSMF4W-s";

    public static final String[] ACCESS_SCOPES=
            {Scopes.PROFILE, YouTubeScopes.YOUTUBE};

    public static final String YOUTUBE_BASE_URL = "http://youtube.com/watch?v=";

    public static final String YOUTUBE_TYPE_VIDEO = "YT_VIDEO";

    public static final long ITEM_READ_COUNT = 50;

    public static final int MAX_LIST_COUNT = 30;
    //設定
    public static final String PREFERENCE_FILE_NAME = "user_data";
    public static final String JSON_STRING_KEY = "json";

    public static class Setting{

        public Setting(String a,ArrayList<String> c,ArrayList<String> h){
            accountName = a;
            labeled = c;
            history = h;
        }

        public String accountName;
        public ArrayList<String> labeled;
        public ArrayList<String> history;
    }
}
