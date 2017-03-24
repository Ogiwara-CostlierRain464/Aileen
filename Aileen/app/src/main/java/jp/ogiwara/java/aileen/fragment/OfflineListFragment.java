package jp.ogiwara.java.aileen.fragment;


import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.ogiwara.java.aileen.MainActivity;
import jp.ogiwara.java.aileen.R;
import jp.ogiwara.java.aileen.task.LoadVideosTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class OfflineListFragment extends Fragment {

    public enum TYPE{
        label,
        history
    }

    public TYPE type;

    public MainActivity mainActivity;
    public OfflineListFragment() {
        // Required empty public constructor
        setRetainInstance(true);
    }

    @CheckResult
    public static OfflineListFragment create(MainActivity activity,TYPE type){
        OfflineListFragment l = new OfflineListFragment();
        l.mainActivity = activity;
        l.type = type;
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
        loadVideos();
    }

    private void loadVideos(){
        new LoadVideosTask(mainActivity).execute(type == TYPE.label ? mainActivity.labeledVideos : mainActivity.historyVideos);
    }
}
