package jp.ogiwara.java.aileen.task;

import android.os.AsyncTask;
import android.speech.tts.Voice;

import java.util.ArrayList;

/**
 * Created by ogiwara on 2017/03/21.
 *
 * 指定したPlaylistId内のVideoIdを取得、その後LoadVideosTask
 */
public class LoadPlayListVideosTask extends AsyncTask<Void,Void,ArrayList<String>> {

    public LoadPlayListVideosTask(){

    }

    public LoadPlayListVideosTask setParams(){
        return this;
    }

    @Override
    protected ArrayList<String> doInBackground(Void... voids) {

    }


    @Override
    protected void onPostExecute(Void v){

    }
}


