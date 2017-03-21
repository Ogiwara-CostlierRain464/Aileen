package jp.ogiwara.java.aileen.task;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
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
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.ogiwara.java.aileen.R;
import jp.ogiwara.java.aileen.fragment.LabelListFragment;
import jp.ogiwara.java.aileen.fragment.PlayListFragment;
import jp.ogiwara.java.aileen.model.YouTubeVideo;
import jp.ogiwara.java.aileen.utils.Constants;
import jp.ogiwara.java.aileen.utils.ISO8601DurationConverter;
import jp.ogiwara.java.aileen.utils.NetworkSingleton;

/**
 * Created by ogiwara on 2017/03/21.
 *
 * //TODO 通信の最適化
 */

public class LoadLabelListTask extends AsyncTask<Void,Void,Void> {

    final String TAG = LoadLabelListTask.class.getName();

    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = new JacksonFactory();
    GoogleAccountCredential credential;

    ArrayList<String> labeledVideos;
    LabelListFragment labelListFragment;

    public LoadLabelListTask(ArrayList<String> l, LabelListFragment f, GoogleAccountCredential c){
        labeledVideos = l;
        credential = c;
        labelListFragment = f;
    }

    @Override
    protected Void doInBackground(Void... voids){

        YouTube youTube;
        try{
            youTube = new YouTube.Builder(transport,jsonFactory,credential)
                    .setApplicationName(labelListFragment.getResources().getResourceName(R.string.app_name))
                    .build();

            YouTube.Videos.List videoList = youTube.videos().list("id,snippet,statistics,contentDetails");
            videoList.setKey(Constants.API_KEY);
            videoList.setFields("items");
            videoList.setMaxResults(Constants.ITEM_READ_COUNT);

            StringBuilder builder = new StringBuilder();
            for(String id : labeledVideos){
                builder.append(id);
                builder.append(",");
            }
            videoList.setId(builder.toString());
            VideoListResponse resp = videoList.execute();
            List<Video> videos = resp.getItems();
            for(Video v : videos){
                YouTubeVideo youTubeVideo = new YouTubeVideo();
                youTubeVideo.id = v.getId();
                youTubeVideo.title = v.getSnippet().getTitle();
                youTubeVideo.thumbnailURL = v.getSnippet().getThumbnails().getDefault().getUrl();
                String isoTime = v.getContentDetails().getDuration();
                String time = ISO8601DurationConverter.convert(isoTime);
                youTubeVideo.duration = time;
                youTubeVideo.viewCount = v.getStatistics().getViewCount().toString();

                labelListFragment.videos.add(youTubeVideo);
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void v){
        LinearLayout cardLiner = (LinearLayout) labelListFragment.getActivity().findViewById(R.id.cardLinear);
        cardLiner.removeAllViews();

        for(final YouTubeVideo youTubeVideo : labelListFragment.videos){
            LayoutInflater inflater = (LayoutInflater) labelListFragment.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.video_item,null);
            CardView cardView = (CardView) linearLayout.findViewById(R.id.video_item);

            ImageLoader i = NetworkSingleton.getInstance(labelListFragment.getActivity().getApplicationContext()).getImageLoader();

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

            CheckBox label = (CheckBox) linearLayout.findViewById(R.id.labelButton);

            if(labeledVideos.contains(youTubeVideo.id)){
                label.setBackground(labelListFragment.getResources().getDrawable(R.drawable.label));
            }

            label.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(labeledVideos.contains(youTubeVideo.id)){
                        v.setBackground(labelListFragment.getResources().getDrawable(R.mipmap.label_outline));
                        labeledVideos.remove(youTubeVideo.id);
                    }else{
                        v.setBackground(labelListFragment.getResources().getDrawable(R.mipmap.label));
                        labeledVideos.add(youTubeVideo.id);
                    }
                }
            });

            final ImageView imageView = (ImageView) linearLayout.findViewById(R.id.moreButton);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(labelListFragment.getContext(),imageView);
                    popupMenu.getMenuInflater().inflate(R.menu.more_menu,popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });

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
