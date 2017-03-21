package jp.ogiwara.java.aileen.task;

import android.os.AsyncTask;
import android.speech.tts.Voice;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.ogiwara.java.aileen.MainActivity;
import jp.ogiwara.java.aileen.R;
import jp.ogiwara.java.aileen.utils.Constants;

/**
 * Created by ogiwara on 2017/03/21.
 *
 * 指定したPlaylistId内のVideoIdを取得、その後LoadVideosTask
 */
public class LoadPlayListVideosTask extends AsyncTask<String,Void,ArrayList<String>> {

    String TAG = LoadPlayListVideosTask.class.getName();

    MainActivity mainActivity;

    public LoadPlayListVideosTask(MainActivity activity){
        mainActivity = activity;
    }

    @Override
    protected ArrayList<String> doInBackground(String... params) {
        Log.d(TAG,"Loading playlist for: "+params[0]);
        ArrayList<String> videos = new ArrayList<>();
        try {
            YouTube youTube = new YouTube.Builder(AndroidHttp.newCompatibleTransport(),
                    new JacksonFactory(),mainActivity.credential)
                    .setApplicationName(mainActivity.getResources().getResourceName(R.string.app_name))
                    .build();

            YouTube.PlaylistItems.List list = youTube.playlistItems().list("id,contentDetails");
            list.setFields("items(contentDetails/videoId)");
            list.setPlaylistId(params[0]);
            list.setMaxResults(Constants.ITEM_READ_COUNT);
            PlaylistItemListResponse response = list.execute();
            List<PlaylistItem> items = response.getItems();
            for(PlaylistItem item : items){
                videos.add(item.getContentDetails().getVideoId());
            }
            Log.d(TAG,videos.toString());
        } catch (IOException e){
            e.printStackTrace();
        }
        return videos;
    }


    @Override
    protected void onPostExecute(ArrayList<String> v){
        new LoadVideosTask(mainActivity).execute(v);
    }
}


