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
import jp.ogiwara.java.aileen.task.LoadVideosTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class LabelListFragment extends Fragment {

    public MainActivity mainActivity;

    public LabelListFragment() {
        // Required empty public constructor
    }

    @CheckResult
    public static LabelListFragment create(MainActivity activity){
        LabelListFragment l = new LabelListFragment();
        l.mainActivity = activity;
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
        return inflater.inflate(R.layout.fragment_label_list, container, false);
    }

    private void start(){
        loadLabelVideos();
    }

    private void loadLabelVideos(){
        new LoadVideosTask(mainActivity).execute(mainActivity.checkedLists);
    }
}
