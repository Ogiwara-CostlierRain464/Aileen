package jp.ogiwara.java.aileen.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by ogiwara on 2017/03/22.
 */

public class DownloadVideoService extends Service {

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }
}
