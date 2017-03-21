package jp.ogiwara.java.aileen.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.youtube.model.PlaylistItem;

import java.util.ArrayList;

import jp.ogiwara.java.aileen.MainActivity;
import jp.ogiwara.java.aileen.R;
import jp.ogiwara.java.aileen.model.YouTubeVideo;
import jp.ogiwara.java.aileen.task.LoadPlayListTask;

/**
 * 1: View読み込み
 * 2: List準備
 */
public class PlayListFragment extends Fragment {

    SwipeRefreshLayout swipeRefreshLayout;

    public MainActivity mainActivity;
    public GoogleAccountCredential credential;
    public String playListId;
    public ArrayList<YouTubeVideo> videos = new ArrayList<>();

    public PlayListFragment(){

    }

    @CheckResult
    public static PlayListFragment create(MainActivity a,GoogleAccountCredential c,String id){
        PlayListFragment p = new PlayListFragment();
        p.mainActivity = a;
        p.credential = c;
        p.playListId = id;
        return p;
    }

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
    }

    @Override
    public View onCreateView(LayoutInflater i,ViewGroup container,Bundle s){
        super.onCreateView(i,container,s);
        View row =  i.inflate(R.layout.fragment_play_list,container,false);
        swipeRefreshLayout = (SwipeRefreshLayout) row.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //TODO check this later
            }
        });
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.black));
        loadVideos();
        return row;
    }

    private void loadVideos(){
        new LoadPlayListTask(mainActivity.checkedLists,this,playListId,credential).execute();
    }
}
