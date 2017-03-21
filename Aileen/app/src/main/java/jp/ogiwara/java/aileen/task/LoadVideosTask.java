package jp.ogiwara.java.aileen.task;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jp.ogiwara.java.aileen.MainActivity;
import jp.ogiwara.java.aileen.R;
import jp.ogiwara.java.aileen.model.YouTubeVideo;
import jp.ogiwara.java.aileen.utils.Constants;
import jp.ogiwara.java.aileen.utils.ISO8601DurationConverter;
import jp.ogiwara.java.aileen.utils.NetworkSingleton;

/**
 * Created by ogiwara on 2017/03/21.
 */

public class LoadVideosTask extends AsyncTask<ArrayList<String>,Void,Void> {

    final String TAG = LoadVideosTask.class.getName();

    MainActivity mainActivity;

    ArrayList<YouTubeVideo> videos = new ArrayList<>();

    public LoadVideosTask(MainActivity activity){
        mainActivity = activity;
    }



    @Override
    protected Void doInBackground(ArrayList<String>... params){
        try{
            YouTube youTube = new YouTube.Builder(AndroidHttp.newCompatibleTransport(),
                    new JacksonFactory(),mainActivity.credential)
                    .setApplicationName(mainActivity.getResources().getResourceName(R.string.app_name))
                    .build();

            YouTube.Videos.List videoList = youTube.videos().list("id,snippet,statistics,contentDetails");
            videoList.setKey(Constants.API_KEY);
            //videoList.setFields("items(snippet/title,snippet/thumbnails/high/url,contentDetails/duration,statistics/viewCount)");
            videoList.setFields("items,nextPageToken");
            videoList.setMaxResults(Constants.ITEM_READ_COUNT);
            StringBuilder builder = new StringBuilder();
            for(String id : params[0]){
                builder.append(id);
                builder.append(",");
            }
            videoList.setId(builder.toString());
            VideoListResponse resp = videoList.execute();
            List<Video> videolist = resp.getItems();
            Log.d(TAG,videolist.toString());

            Iterator<Video> vit = videolist.iterator();
            while(vit.hasNext()){
                Video video = vit.next();

                if(video.getSnippet().getThumbnails() == null){
                    continue;
                }

                YouTubeVideo youTubeVideo = new YouTubeVideo();
                youTubeVideo.id = video.getId();
                youTubeVideo.title = video.getSnippet().getTitle();
                youTubeVideo.thumbnailURL = video.getSnippet().getThumbnails().getHigh().getUrl();
                String isoTime = video.getContentDetails().getDuration();
                String time = ISO8601DurationConverter.convert(isoTime);
                youTubeVideo.duration = time;
                youTubeVideo.viewCount = video.getStatistics().getViewCount().toString();

                videos.add(youTubeVideo);
            }

            /*for(Video v : resp.getItems()){
                YouTubeVideo youTubeVideo = new YouTubeVideo();
                youTubeVideo.id = v.getId();
                youTubeVideo.title = v.getSnippet().getTitle();
                youTubeVideo.thumbnailURL = v.getSnippet().getThumbnails().getHigh().getUrl();
                String isoTime = v.getContentDetails().getDuration();
                String time = ISO8601DurationConverter.convert(isoTime);
                youTubeVideo.duration = time;
                youTubeVideo.viewCount = v.getStatistics().getViewCount().toString();

                videos.add(youTubeVideo);
            }*/
            Log.d(TAG,resp.getItems().toString());
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v){
        LinearLayout cardLiner = (LinearLayout) mainActivity.findViewById(R.id.cardLinear);
        cardLiner.removeAllViews();

        for(final YouTubeVideo youTubeVideo : videos){
            LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.video_item,null);
            final CardView cardView = (CardView) linearLayout.findViewById(R.id.video_item);

            ImageLoader i = NetworkSingleton.getInstance(mainActivity.getApplicationContext()).getImageLoader();

            NetworkImageView thumbNail = (NetworkImageView) linearLayout.findViewById(R.id.video_thumbnail);

            thumbNail.setImageUrl(youTubeVideo.thumbnailURL,i);
            TextView duration = (TextView) linearLayout.findViewById(R.id.video_duration);
            duration.setText(youTubeVideo.duration);
            TextView title = (TextView) linearLayout.findViewById(R.id.video_title);
            title.setText(youTubeVideo.title);
            TextView views = (TextView) linearLayout.findViewById(R.id.views_number);
            views.setText(String.format("%,d",Integer.parseInt(youTubeVideo.viewCount)) + " views");
            TextView view = (TextView) linearLayout.findViewById(R.id.video_id);
            view.setText(youTubeVideo.id);//隠しView!

            final CheckBox label = (CheckBox) linearLayout.findViewById(R.id.labelButton);

            if(mainActivity.checkedLists.contains(youTubeVideo.id)){
                label.setBackground(mainActivity.getResources().getDrawable(R.drawable.label));
            }

            label.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mainActivity.checkedLists.contains(youTubeVideo.id)){
                        v.setBackground(mainActivity.getResources().getDrawable(R.mipmap.label_outline));
                        mainActivity.checkedLists.remove(youTubeVideo.id);
                    }else{
                        v.setBackground(mainActivity.getResources().getDrawable(R.mipmap.label));
                        mainActivity.checkedLists.add(youTubeVideo.id);
                    }
                }
            });

            ImageView imageView = (ImageView) linearLayout.findViewById(R.id.moreButton);
            /*imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(mainActivity.getApplicationContext(),label);
                    popupMenu.getMenuInflater().inflate(R.menu.more_menu,popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            //TODO more menu...
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });*///TODO Check THIS!!

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO Start Music
                }
            });
            cardLiner.addView(linearLayout);
        }
    }
}
