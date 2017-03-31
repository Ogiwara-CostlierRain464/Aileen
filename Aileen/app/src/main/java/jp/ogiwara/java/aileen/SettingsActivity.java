package jp.ogiwara.java.aileen;


import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * 必要なのは?
 *
 * モバイルでのデータセーブ、オープンソースライセンス、色の変更？ */
public class SettingsActivity extends AppCompatPreferenceActivity {

    final String TAG = SettingsActivity.class.getName();

    @Override
    public void onCreate(Bundle s){
        super.onCreate(s);
        Intent intent = getIntent();

        Toolbar toolbar = new Toolbar(getApplicationContext());
        toolbar.setTitle(getString(R.string.settings));
        toolbar.setElevation(6.0f);
        toolbar.setTitleMargin(50,50,50,50);
        toolbar.setMinimumHeight(200);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setBackgroundColor(getResources().getColor(R.color.pink));

        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        root.addView(toolbar,0);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        // 使用できる Fragment か確認する
        if (DataFragment.class.getName().equals(fragmentName) ||
                HistoryFragment.class.getName().equals(fragmentName)) {
            return true;
        }
        return false;
    }

    public static class DataFragment extends PreferenceFragment{
        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data);
        }
    }

    public static class HistoryFragment extends PreferenceFragment{
        @Override
        public void onCreate(Bundle savedInstance){
            super.onCreate(savedInstance);
            addPreferencesFromResource(R.xml.pref_history);

            Preference p = (Preference) findPreference("clean_history");
            p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    MainActivity.getInstance().historyVideos.clear();
                    Toast.makeText(getActivity().getApplicationContext(),R.string.pref_history_clean_history_dialog, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            Preference p2 = (Preference) findPreference("clean_labeled");
            p2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    MainActivity.getInstance().labeledVideos.clear();
                    Toast.makeText(getActivity().getApplicationContext(),R.string.pref_history_clean_labeled_dialog, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
    }
}
