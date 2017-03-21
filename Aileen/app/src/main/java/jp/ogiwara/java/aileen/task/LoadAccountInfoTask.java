package jp.ogiwara.java.aileen.task;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ArrayMap;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelContentDetails;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistListResponse;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import jp.ogiwara.java.aileen.MainActivity;
import jp.ogiwara.java.aileen.R;
import jp.ogiwara.java.aileen.fragment.LabelListFragment;
import jp.ogiwara.java.aileen.fragment.PlayListFragment;
import jp.ogiwara.java.aileen.utils.Constants;

/**
 * Created by ogiwara on 2017/03/19.
 */

public class LoadAccountInfoTask extends AsyncTask<Void,Void,Void>{

    final String TAG = LoadPlayListTask.class.getName();

    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = new JacksonFactory();

    MainActivity activity;
    GoogleAccountCredential credential;


    public LoadAccountInfoTask(MainActivity a,GoogleAccountCredential c){
        activity = a;
        credential = c;
    }

    @Override
    protected Void doInBackground(Void... voids){
        YouTube youTube;
        try{
            youTube = new YouTube.Builder(transport,jsonFactory,credential).setApplicationName(
                    activity.getResources().getResourceName(R.string.app_name)
            ).build();

            getChannelRatedList(youTube);
            getOtherList(youTube);
        }catch (UserRecoverableAuthIOException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    private void getOtherList(YouTube y) throws IOException{
        YouTube.Playlists.List lists = y.playlists().list("id,snippet").setPart("snippet");
        lists.setFields("items");
        lists.setMine(true);
        lists.setMaxResults(Constants.ITEM_READ_COUNT);
        PlaylistListResponse response =lists.execute();
        List<Playlist> playlists = response.getItems();
        if(playlists != null){
            Iterator<Playlist> iterator = playlists.iterator();
            while(iterator.hasNext()){
                Playlist playlist = iterator.next();
                activity.playLists.add(playlist.getSnippet().getTitle(),playlist.getId());
            }
        }
    }

    private void getChannelRatedList(YouTube y) throws IOException{
        YouTube.Channels.List channelRequest = y.channels().list("id,contentDetails");
        channelRequest.setMine(true);
        channelRequest.setFields("items");
        channelRequest.setMaxResults(Constants.ITEM_READ_COUNT);
        ChannelListResponse channelListResponse = channelRequest.execute();
        final ChannelContentDetails.RelatedPlaylists relatedPlaylists = channelListResponse.getItems().get(0).getContentDetails().getRelatedPlaylists();
        activity.playLists.add("Likes",relatedPlaylists.getLikes());
        System.out.println("Watch-Histroy: " + relatedPlaylists.getWatchHistory());
    }

    @Override
    protected void onPostExecute(Void v){

        activity.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getTitle().toString() == activity.getResources().getString(R.string.label_list)){
                    FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.BaseLayout, LabelListFragment.create(activity,credential)).commit();
                }else if(item.getTitle().toString() == activity.getResources().getString(R.string.favorite)){
                    FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.BaseLayout, PlayListFragment.create(activity,credential,activity.playLists.get("Favorites"))).commit();
                }else{
                    FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.BaseLayout,PlayListFragment.create(activity,credential,activity.playLists.get(item.getTitle().toString()))).commit();
                }
                activity.drawerLayout.closeDrawer(Gravity.START);
                return true;
            }
        });

        Menu menu = activity.navigationView.getMenu().addSubMenu(Menu.NONE,1,Menu.NONE,R.string.playlist);
        for(String name : activity.playLists.keySet()){
            menu.add(name);
        }

        System.out.println(activity.playLists);
        activity.loadFirstFragment();
    }
}
