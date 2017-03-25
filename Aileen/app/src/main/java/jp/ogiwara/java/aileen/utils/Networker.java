package jp.ogiwara.java.aileen.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.SparseArray;

import at.huber.youtubeExtractor.YtFile;

/**
 * Created by ogiwara on 2017/03/24.
 */

public class Networker {
    public static YtFile getAsHigherQuality(SparseArray<YtFile> ytFiles){
        if(ytFiles.get(141) != null){
            return ytFiles.get(141);
        }else if(ytFiles.get(251) != null){
            return  ytFiles.get(251);
        }else if(ytFiles.get(140) != null){
            return ytFiles.get(140);
        }else{
            return ytFiles.get(17);
        }
    }

    public static boolean isMobileInternet(Context context){
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if(networkInfo == null){
            return true;
        }
        return (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE || networkInfo.getType() == ConnectivityManager.TYPE_MOBILE_DUN) ? true : false;
    }
}
