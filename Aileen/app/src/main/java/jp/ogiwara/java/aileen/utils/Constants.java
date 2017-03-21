package jp.ogiwara.java.aileen.utils;

import android.util.SparseArray;

import com.google.android.gms.common.Scopes;
import com.google.api.services.youtube.YouTubeScopes;

import java.util.ArrayList;

/**
 * Created by ogiwara on 2017/03/19.
 */

public class Constants {

    public static final String API_KEY = "AIzaSyBLW3Flnssn9fJEKTuyb2V1AlyqSMF4W-s";

    public static final String[] ACCESS_SCOPES=
            {Scopes.PROFILE, YouTubeScopes.YOUTUBE};

    public static final long ITEM_READ_COUNT = 50;

    //設定
    public static final String PREFERENCE_FILE_NAME = "user_data";
    public static final String JSON_STRING_KEY = "json";

    public static class Setting{

        public Setting(String a,ArrayList<String> c){
            accountName = a;
            checkedVideos = c;
        }

        public String accountName;
        public ArrayList<String> checkedVideos;
    }
}
