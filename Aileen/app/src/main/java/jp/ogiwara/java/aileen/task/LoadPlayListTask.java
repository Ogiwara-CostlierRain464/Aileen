package jp.ogiwara.java.aileen.task;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jp.ogiwara.java.aileen.R;
import jp.ogiwara.java.aileen.fragment.PlayListFragment;
import jp.ogiwara.java.aileen.model.YouTubeVideo;
import jp.ogiwara.java.aileen.utils.Constants;
import jp.ogiwara.java.aileen.utils.ISO8601DurationConverter;
import jp.ogiwara.java.aileen.utils.NetworkSingleton;

/**
 * Created by ogiwara on 2017/03/20.
 *
 * 特定のプレイリストの読み込み
 *
 * //TODO 通信の最適化
 */
public class LoadPlayListTask extends AsyncTask<Void,Void,Void> {

    final String TAG = LoadPlayListTask.class.getName();

    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = new JacksonFactory();
    GoogleAccountCredential credential;

    ArrayList<String> labeledVideos;
    PlayListFragment playListFragment;

    String playlistId;

    public LoadPlayListTask(ArrayList<String> l,PlayListFragment a,String id,GoogleAccountCredential c){
        playListFragment = a;
        labeledVideos = l;
        credential = c;
        playlistId = id;
    }

    @Override
    protected Void doInBackground(Void... voids){
        Log.d(TAG,"Loading playlist for: "+playlistId);
        YouTube youTube;
        YouTube.Videos.List videoList;
        try{
            youTube = new YouTube.Builder(transport,jsonFactory,credential)
                    .setApplicationName(playListFragment.getResources().getResourceName(R.string.app_name))
                    .build();

            YouTube.PlaylistItems.List list = youTube.playlistItems().list("id,snippet,contentDetails")/*.setPart("contentDetails,snippet")*/;
            list.setFields("items(contentDetails/videoId,snippet/title," +
                    "snippet/thumbnails/default/url),nextPageToken");
            list.setPlaylistId(playlistId);
            list.setMaxResults(Constants.ITEM_READ_COUNT);
            PlaylistItemListResponse response = list.execute();
            List<PlaylistItem> items = response.getItems();

            System.out.println(items);

            videoList = youTube.videos().list("id,contentDetails,statistics").setPart("contentDetails,statistics");
            videoList.setKey(Constants.API_KEY);
            videoList.setFields("items(contentDetails/duration,statistics/viewCount),nextPageToken");
            videoList.setMaxResults(Constants.ITEM_READ_COUNT);

            StringBuilder contentDetails = new StringBuilder();
            int ii = 0;
            for (PlaylistItem item : items) {
                if(item.getContentDetails() != null) {
                    contentDetails.append(item.getContentDetails().getVideoId());
                    contentDetails.append(",");
                }
            }
            Log.d(TAG,contentDetails.toString());
            videoList.setId(contentDetails.toString());
            VideoListResponse resp = videoList.execute();
            List<Video> videos = resp.getItems();
            System.out.println(videos);
            Iterator<PlaylistItem> pit = items.iterator();
            Iterator<Video> vit = videos.iterator();
            while(pit.hasNext()){
                PlaylistItem playlistItem = pit.next();

                if(playlistItem.getSnippet().getThumbnails() == null)
                    continue;

                YouTubeVideo youTubeVideo = new YouTubeVideo();
                youTubeVideo.id = playlistItem.getContentDetails().getVideoId();
                youTubeVideo.title = playlistItem.getSnippet().getTitle();
                youTubeVideo.thumbnailURL = playlistItem.getSnippet().getThumbnails().getDefault().getUrl();

                try{
                    Video videoItem = vit.next();
                    String isoTime = videoItem.getContentDetails().getDuration();
                    String time = ISO8601DurationConverter.convert(isoTime);
                    youTubeVideo.duration = time;
                    youTubeVideo.viewCount = videoItem.getStatistics().getViewCount().toString();
                }catch (Exception e){
                    e.printStackTrace();
                    youTubeVideo.duration ="0";
                    youTubeVideo.viewCount = "0";
                }

                playListFragment.videos.add(youTubeVideo);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v){
        System.out.println(playListFragment.videos);

        LinearLayout cardLiner = (LinearLayout) playListFragment.getActivity().findViewById(R.id.cardLinear);
        cardLiner.removeAllViews();

        for(final YouTubeVideo youTubeVideo : playListFragment.videos){

            LayoutInflater inflater = (LayoutInflater) playListFragment.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.video_item,null);
            CardView cardView = (CardView) linearLayout.findViewById(R.id.video_item);

            ImageLoader i = NetworkSingleton.getInstance(playListFragment.getActivity().getApplicationContext()).getImageLoader();

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
                label.setBackground(playListFragment.getResources().getDrawable(R.drawable.label));
            }

            label.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(labeledVideos.contains(youTubeVideo.id)){
                        v.setBackground(playListFragment.getResources().getDrawable(R.mipmap.label_outline));
                        labeledVideos.remove(youTubeVideo.id);
                    }else{
                        v.setBackground(playListFragment.getResources().getDrawable(R.mipmap.label));
                        labeledVideos.add(youTubeVideo.id);
                    }
                }
            });

            final ImageView imageView = (ImageView) linearLayout.findViewById(R.id.moreButton);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(playListFragment.getContext(),imageView);
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
