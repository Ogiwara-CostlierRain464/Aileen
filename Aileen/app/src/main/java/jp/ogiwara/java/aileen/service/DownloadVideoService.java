package jp.ogiwara.java.aileen.service;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.SparseArray;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import jp.ogiwara.java.aileen.model.YouTubeVideo;
import jp.ogiwara.java.aileen.utils.Constants;
import jp.ogiwara.java.aileen.utils.Networker;

/**
 * Created by ogiwara on 2017/03/22.
 */

public class DownloadVideoService extends Service {

    YouTubeVideo videoItem;

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        handleIntent(intent);
        return super.onStartCommand(intent,flags,startId);
    }

    private void handleIntent(Intent intent){
        if(intent == null)
            return;

        if(intent.getSerializableExtra(Constants.YOUTUBE_TYPE_VIDEO) != null)
            videoItem = (YouTubeVideo) intent.getSerializableExtra(Constants.YOUTUBE_TYPE_VIDEO);
            download();
    }

    private void download(){
        String link = Constants.YOUTUBE_BASE_URL + videoItem.id;
        new YouTubeExtractor(this){
            @Override
            protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta videoMeta){
                if(ytFiles == null){
                    Toast.makeText(getApplicationContext(), "ERROR Download",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                YtFile ytFile = Networker.getAsHigherQuality(ytFiles);
                downloadFromUrl(ytFile.getUrl(),videoMeta.getTitle(),getCleanedFileName(videoMeta.getTitle()) +"."+ ytFile.getFormat().getExt());
            }
        }.extract(link,true,false);
    }

    private String getCleanedFileName(String row){
        String title;
        if(row.length() > 55){
            title = row.substring(0,55);
        }else{
            title = row;
        }
        title = title.replaceAll("\\\\|>|<|\"|\\||\\*|\\?|%|:|#|/.", "");
        return title;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent){
        //Cancel download
    }

    private long downloadFromUrl(String youtubeDlUrl,String downloadTitle,String fileName){
        Uri uri = Uri.parse(youtubeDlUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(downloadTitle);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS + File.separator + "Aileen",fileName);
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        return manager.enqueue(request);
    }
}
