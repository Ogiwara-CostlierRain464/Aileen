package jp.ogiwara.java.aileen.fragment;


import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.util.ArrayList;

import jp.ogiwara.java.aileen.MainActivity;
import jp.ogiwara.java.aileen.R;
import jp.ogiwara.java.aileen.model.YouTubeVideo;
import jp.ogiwara.java.aileen.task.LoadLabelListTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class LabelListFragment extends Fragment {

    public MainActivity mainActivity;
    public GoogleAccountCredential credential;
    public ArrayList<YouTubeVideo> videos = new ArrayList<>();

    public LabelListFragment() {
        // Required empty public constructor
    }

    @CheckResult
    public static LabelListFragment create(MainActivity a, GoogleAccountCredential c){
        LabelListFragment l = new LabelListFragment();
        l.mainActivity = a;
        l.credential = c;
        return l;
    }

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_label_list, container, false);
    }

    private void start(){
        loadLabelVideos();
    }

    private void loadLabelVideos(){
        new LoadLabelListTask(mainActivity.checkedLists,this,credential).execute();
    }
}
