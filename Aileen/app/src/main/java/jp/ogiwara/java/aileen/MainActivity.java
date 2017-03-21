package jp.ogiwara.java.aileen;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ArrayMap;
import com.google.api.client.util.ExponentialBackOff;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;

import jp.ogiwara.java.aileen.fragment.LabelListFragment;
import jp.ogiwara.java.aileen.fragment.PlayListFragment;
import jp.ogiwara.java.aileen.task.LoadAccountInfoTask;
import jp.ogiwara.java.aileen.utils.Constants;
import jp.ogiwara.java.aileen.utils.NetworkSingleton;


/**
 * 各関数内で次の処理を呼ぶ
 *
 * 1: View読み込み
 * 2: 権限チェック{@link MainActivity#loadPermission()}
 * 3: 設定確認 {@link MainActivity#loadSettings()}
 * 4: アカウント情報 {@link MainActivity#loadAccount()}
 * 5: アカウントアイコンの読み込み {@link MainActivity#loadAccountInfo()}
 * 6: Fragment準備
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CONTACT_PERMISSION = 1;
    private static final int REQUEST_ACCOUNT_PICKER = 2;
    private static final int REQUEST_AUTHORIZATION = 3;
    private static final String TAG = MainActivity.class.getName();

    Toolbar toolbar;
    public DrawerLayout drawerLayout;
    public NavigationView navigationView;
    NetworkImageView accountImageView;
    NetworkImageView accountImageCover;
    TextView accountNameTextView;

    SharedPreferences preferences;

    String accountName;

    GoogleAccountCredential credential;
    GoogleApiClient googleApiClient;

    public ArrayMap<String,String> playLists = new ArrayMap<>();
    public ArrayList<String> checkedLists = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.tool_bar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.toolbar_about:
                return true;
            case R.id.toolbar_settings:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        saveSettings();
        super.onDestroy();
    }

    private void start(){
        loadView();
    }

    //region View
    private void loadView(){
        Log.d(TAG,"Loading View...");
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.addOnLayoutChangeListener(new View.OnLayoutChangeListener(){
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom){
                accountImageView = (NetworkImageView) navigationView.findViewById(R.id.drawer_header_account_image);
                accountNameTextView = (TextView) navigationView.findViewById(R.id.drawer_header_account_name);
                accountImageCover = (NetworkImageView) navigationView.findViewById(R.id.drawer_header_account_cover);
            }
        });

        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setNavigationIcon(R.mipmap.hamberger);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d(TAG,"Navigation!");
                drawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        loadPermission();
    }
    //endregion

    //region Permission
    private void loadPermission(){
        Log.d(TAG,"Loading Permission...");
        if(PermissionChecker.checkSelfPermission(getApplicationContext(), Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        }else{
            loadSettings();
        }
    }

    private void requestPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.GET_ACCOUNTS)){
            new AlertDialog.Builder(this)
                    .setTitle(R.string.permission_dialog_title)
                    .setMessage(R.string.permission_dialog_message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.GET_ACCOUNTS},REQUEST_CODE_CONTACT_PERMISSION);
                        }
                    }).create().show();
        }else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.GET_ACCOUNTS},REQUEST_CODE_CONTACT_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == REQUEST_CODE_CONTACT_PERMISSION){//拒否された場合
            if(grantResults.length != 1 ||
                    grantResults[0] != PackageManager.PERMISSION_GRANTED){
                new AlertDialog.Builder(this).setTitle(R.string.permission_denied_title).setMessage(R.string.permission_denied_message).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.finish();
                    }
                }).create().show();
            }else{
                loadSettings();
            }
        }else{
            super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }
    //endregion

    //region Settings
    //起動時に読み込み
    private void loadSettings(){
        Log.d(TAG,"Loading Settigs...");
        preferences = getSharedPreferences(Constants.PREFERENCE_FILE_NAME,MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonString = preferences.getString(Constants.JSON_STRING_KEY,"");
        Constants.Setting setting = gson.fromJson(jsonString,Constants.Setting.class);

        if(setting == null){
            accountName = null;
        }else{
            accountName = setting.accountName;
            checkedLists = setting.checkedVideos;
        }
        loadAccount();
    }

    //終了時に保存
    private void saveSettings(){
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String jsonInstanceString = gson.toJson(new Constants.Setting(accountName,checkedLists));
        editor.putString(Constants.JSON_STRING_KEY,jsonInstanceString);
        editor.apply();
    }
    //endregion

    //region choose account
    private void loadAccount(){
        Log.d(TAG,"Loading Account...");
        credential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(Constants.ACCESS_SCOPES));
        credential.setBackOff(new ExponentialBackOff());
        if(accountName == null){
            chooseAccount();
        }else{
            credential.setSelectedAccountName(accountName);
            loadAccountInfo();
        }
    }

    private void chooseAccount(){
        startActivityForResult(credential.newChooseAccountIntent(),REQUEST_ACCOUNT_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        switch (requestCode){
            case REQUEST_ACCOUNT_PICKER:
                if(resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null){
                    String account = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                    if(account != null) {
                        accountName = account;
                        credential.setSelectedAccountName(account);
                        saveSettings();
                        loadAccountInfo();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                //再度loadAccountInfo
                loadAccountInfo();
                break;
            default:
                Log.w(TAG,"Un catch activity result!");
                break;
        }
    }
    //endregion

    //region load account info
    private void loadAccountInfo(){
        Log.d(TAG,"Loading Account Info...");
        setProfileInfo();
        new LoadAccountInfoTask(this,credential).execute();
    }

    private void setProfileInfo(){
        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        if(!googleApiClient.isConnected() || Plus.PeopleApi.getCurrentPerson(googleApiClient) == null){
                            accountImageView.setImageDrawable(null);
                            accountImageCover.setImageDrawable(null);
                            accountNameTextView.setText(accountName);
                        }else{
                            Log.d(TAG,"Profile");
                            Person currentPerson = Plus.PeopleApi.getCurrentPerson(googleApiClient);
                            if(currentPerson.hasImage()){
                                accountImageView.setImageUrl(currentPerson.getImage().getUrl(), NetworkSingleton.getInstance(getApplicationContext()).getImageLoader());
                            }else{
                                accountImageView.setImageDrawable(getDrawable(R.mipmap.icon));
                            }
                            if(currentPerson.hasDisplayName()){
                                accountNameTextView.setText(currentPerson.getDisplayName());
                            }else{
                                accountNameTextView.setText(accountName);
                            }
                            if(currentPerson.hasCover()){
                                accountImageCover.setImageUrl(currentPerson.getCover().getCoverPhoto().getUrl(),NetworkSingleton.getInstance(getApplicationContext()).getImageLoader());
                            }else{
                                accountImageCover.setImageDrawable(getDrawable(R.drawable.material));
                            }
                        }
                        googleApiClient.disconnect();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.w(TAG,"onConnectionSuspended!");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.e(TAG,connectionResult.getErrorMessage());
                    }
                }).addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .setAccountName(accountName).build();
        googleApiClient.connect();
    }
    //endregion

    //Playlistの読み込みが終わってから
    public void loadFirstFragment(){
        //TEST!
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.BaseLayout, LabelListFragment.create(this,credential)).commit();
    }
}
