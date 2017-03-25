package jp.ogiwara.java.aileen.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import java.io.IOException;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import jp.ogiwara.java.aileen.MainActivity;
import jp.ogiwara.java.aileen.R;
import jp.ogiwara.java.aileen.model.YouTubeVideo;
import jp.ogiwara.java.aileen.utils.Constants;
import jp.ogiwara.java.aileen.utils.Networker;


public class BackgroundAudioService extends Service {

    final String TAG = BackgroundAudioService.class.getName();

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_STOP = "action_stop";

    YouTubeVideo videoItem;

    private MediaPlayer mediaPlayer;
    private MediaControllerCompat controller;
    private MediaSessionCompat session;

    private NotificationCompat.Builder builder = null;

    private boolean isStarting = false;

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        initMediaPlayer();
        initMediaSessions();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(1);
    }

    private void initMediaPlayer(){
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                restartVideo();
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isStarting = false;
            }
        });
    }

    private void initMediaSessions(){
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        PendingIntent buttonReceiveIntent = PendingIntent.getBroadcast(
                getApplicationContext(),0,
                new Intent(Intent.ACTION_MEDIA_BUTTON),
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        session = new MediaSessionCompat(getApplicationContext(),"simple player session",
                null,buttonReceiveIntent);

        try {
            controller = new MediaControllerCompat(getApplicationContext(),session.getSessionToken());

            session.setCallback(new MediaSessionCompat.Callback() {
                @Override
                public void onPlay() {
                    super.onPlay();
                    buildNotification(generateAction(R.mipmap.pause,"Pause",ACTION_PAUSE));
                }

                @Override
                public void onPause() {
                    super.onPause();
                    pauseVideo();
                    buildNotification(generateAction(R.mipmap.play,"Play",ACTION_PLAY));
                }

                @Override
                public void onStop() {
                    super.onStop();
                    stopPlayer();
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(1);
                    Intent intent = new Intent(getApplicationContext(),BackgroundAudioService.class);
                    stopService(intent);
                }
            });
        }catch (RemoteException re){
            re.printStackTrace();
        }
    }

    private void buildNotification(NotificationCompat.Action action){
        NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle();

        Intent intent = new Intent(getApplicationContext(),BackgroundAudioService.class);
        intent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(getApplicationContext(),1,intent,0);

        Intent clickIntent = new Intent(this,MainActivity.class);
        clickIntent.setAction(Intent.ACTION_MAIN);
        clickIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent clickPendingIntent = PendingIntent.getActivity(getApplicationContext(),0,clickIntent,0);

        style.setShowActionsInCompactView(0);
        builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setSmallIcon(R.mipmap.iconmini);
        builder.setContentTitle(videoItem.title)
                .setShowWhen(false)
                .setContentIntent(clickPendingIntent)
                .setDeleteIntent(stopPendingIntent)
                .setOngoing(false)
                .setStyle(style)
                .addAction(action);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1,builder.build());
    }

    private NotificationCompat.Action generateAction(int icon,String title,String intentAction){
        Intent intent = new Intent(getApplicationContext(),BackgroundAudioService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(),1,intent,0);
        return new NotificationCompat.Action.Builder(icon,title,pendingIntent).build();
    }

    private void handleIntent(Intent intent){
        if(intent == null || intent.getAction() == null)
           return;

        if(intent.getSerializableExtra(Constants.YOUTUBE_TYPE_VIDEO ) != null)
        videoItem = (YouTubeVideo) intent.getSerializableExtra(Constants.YOUTUBE_TYPE_VIDEO);

        String action = intent.getAction();
        if (action.equalsIgnoreCase(ACTION_PLAY)) {
            Log.i(TAG,"handleIntent#play");
            playVideo();
            controller.getTransportControls().play();
        } else if (action.equalsIgnoreCase(ACTION_PAUSE)) {
            Log.i(TAG,"handleIntent#pause");
            controller.getTransportControls().pause();
        } else if (action.equalsIgnoreCase(ACTION_STOP)) {
            Log.i(TAG,"handleIntent#stop");
            controller.getTransportControls().stop();
        }
    }

    private void playVideo(){
        isStarting = true;
        extractUrlAndPlay();
    }

    private void pauseVideo(){
        if(mediaPlayer.isPlaying())
            mediaPlayer.pause();
    }

    private void resumeVideo(){
        if(mediaPlayer != null)
            mediaPlayer.start();
    }

    private void restartVideo(){
        mediaPlayer.start();
    }

    private void stopPlayer(){
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    private void extractUrlAndPlay(){
        String link = Constants.YOUTUBE_BASE_URL + videoItem.id;
        Log.d(TAG,"Start playing for:" + link);
        new YouTubeExtractor(this){
            @Override
            protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta videoMeta){
                if(ytFiles == null){
                    Toast.makeText(getApplicationContext(), "ERROR Play",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                MainActivity.getInstance().historyVideos.add(videoItem.id);
                YtFile ytFile = getQuality(ytFiles);
                try{
                   if(mediaPlayer != null){
                       mediaPlayer.reset();
                       mediaPlayer.setDataSource(ytFile.getUrl());
                       mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                       mediaPlayer.prepare();
                       mediaPlayer.start();

                       Toast.makeText(getBaseContext(),videoMeta.getTitle(),Toast.LENGTH_SHORT).show();
                   }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }.execute(link);
    }

    private YtFile getQuality(SparseArray<YtFile> ytFiles){
        if(Networker.isMobileInternet(getApplicationContext())){
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            int val = Integer.parseInt(pref.getString("lte_quality_list","1"));

            switch (val){
                case 2:
                    return Networker.getAsHigherQuality(ytFiles);
                case 1:
                    return ytFiles.get(251) != null ? ytFiles.get(251) : (ytFiles.get(141) != null ? ytFiles.get(141) : ytFiles.get(17));
                case 0:
                    return ytFiles.get(17);
                default:
                    Log.w(TAG,"InVail value!!");
                    return ytFiles.get(17);
            }
        }else{
            return Networker.getAsHigherQuality(ytFiles);
        }
        /*ytFiles.get(141); //mp4a - stereo, 44.1 KHz 256 Kbps
        ytFiles.get(251); //webm - stereo, 48 KHz 160 Kbps
        ytFiles.get(140);  //mp4a - stereo, 44.1 KHz 128 Kbps
        ytFiles.get(17); //mp4 - stereo, 44.1 KHz 96-100 Kbps*/
    }
}
