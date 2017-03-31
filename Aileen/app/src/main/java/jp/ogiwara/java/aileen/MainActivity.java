package jp.ogiwara.java.aileen;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.PermissionChecker;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ArrayMap;
import com.google.api.client.util.ExponentialBackOff;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;

import jp.ogiwara.java.aileen.fragment.OfflineListFragment;
import jp.ogiwara.java.aileen.fragment.PlayListFragment;
import jp.ogiwara.java.aileen.task.LoadAccountInfoTask;
import jp.ogiwara.java.aileen.utils.Constants;
import jp.ogiwara.java.aileen.utils.NetworkSingleton;


/** 各関数内で次の処理を呼ぶ
 *
 *
 * 1: View読み込み
 * 2: 権限チェック{@link MainActivity#loadPermission()}
 * 3: 設定確認 {@link MainActivity#loadSettings()}
 * 4: アカウント情報 {@link MainActivity#loadAccount()}
 * 5: アカウントアイコンの読み込み {@link MainActivity#loadAccountInfo()}
 * 6: Fragment準備
 */
public class MainActivity extends AppCompatActivity {

    public  static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
    private static final int REQUEST_CODE_CONTACT_PERMISSION = 1;
    private static final int REQUEST_ACCOUNT_PICKER = 2;
    public static final int REQUEST_AUTHORIZATION = 3;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = MainActivity.class.getName();

    private static MainActivity instance;

    public Toolbar toolbar;
    public DrawerLayout drawerLayout;
    public NavigationView navigationView;
    NetworkImageView accountImageView;
    ImageView accountImageCover;
    TextView accountNameTextView;

    SharedPreferences preferences;

    String accountName;

    public GoogleAccountCredential credential;
    GoogleApiClient googleApiClient;

    public ArrayMap<String,String> playLists = new ArrayMap<>();
    public ArrayList<String> labeledVideos = new ArrayList<>(Constants.MAX_LIST_COUNT);
    public ArrayList<String> historyVideos = new ArrayList<>(Constants.MAX_LIST_COUNT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        start();
    }

    public static MainActivity getInstance(){
        return instance;
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
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle(getString(R.string.app_name) + BuildConfig.VERSION_NAME );
                alertDialog.setIcon(R.mipmap.icon);
                alertDialog.setMessage(getString(R.string.author) + "\n\n" +
                getString(R.string.email) + "\n\n" +
                getString(R.string.github_link) +  "\n\n" +
                getString(R.string.date) + "\n");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();

                return true;
            case R.id.toolbar_settings:
                Intent intent = new Intent(getApplicationContext(),SettingsActivity.class);
                intent.putStringArrayListExtra("labeled",labeledVideos);
                intent.putStringArrayListExtra("history",historyVideos);
                startActivity(intent);
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
                accountImageCover = (ImageView) navigationView.findViewById(R.id.drawer_header_account_cover);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Log.d("TAG","Navigation:" + item.getTitle().toString() + "has selected");

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                if(item.getTitle().toString().equals(getResources().getString(R.string.label_list))){
                    Log.d(TAG,"Labeled list is:" + labeledVideos.toString());
                    transaction.replace(R.id.BaseLayout, OfflineListFragment.create(MainActivity.this,OfflineListFragment.TYPE.label)).commit();
                }else if(item.getTitle().toString().equals(getResources().getString(R.string.favorite))) {
                    transaction.replace(R.id.BaseLayout, PlayListFragment.create(MainActivity.this, playLists.get("Favorites"))).commit();
                }else if(item.getTitle().toString().equals(getResources().getString(R.string.history))) {
                    Log.d(TAG,"Labeled list is:" + historyVideos.toString());
                    transaction.replace(R.id.BaseLayout, OfflineListFragment.create(MainActivity.this,OfflineListFragment.TYPE.history)).commit();
                }else{
                    transaction.replace(R.id.BaseLayout,PlayListFragment.create(MainActivity.this,playLists.get(item.getTitle().toString()))).commit();
                }
                toolbar.setTitle(item.getTitle());
                drawerLayout.closeDrawer(Gravity.START);
                return true;
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
                != PackageManager.PERMISSION_GRANTED

          || PermissionChecker.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED

          || PermissionChecker.checkSelfPermission(getApplicationContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        }else{
            loadSettings();
        }
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.GET_ACCOUNTS,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE_CONTACT_PERMISSION);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == REQUEST_CODE_CONTACT_PERMISSION){//拒否された場合
            if(grantResults.length != 3 ||
                    grantResults[0] != PackageManager.PERMISSION_GRANTED
                    || grantResults[1] != PackageManager.PERMISSION_GRANTED
                    || grantResults[2] != PackageManager.PERMISSION_GRANTED
                    ){
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
            labeledVideos = new ArrayList<>();
            historyVideos = new ArrayList<>();
        }else{
            accountName = setting.accountName;
            labeledVideos = setting.labeled;
            historyVideos = setting.history;
        }
        loadAccount();
    }

    //終了時に保存
    private void saveSettings(){
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String jsonInstanceString = gson.toJson(new Constants.Setting(accountName,labeledVideos,historyVideos));
        editor.putString(Constants.JSON_STRING_KEY,jsonInstanceString);
        editor.apply();
    }
    //endregion

    //region choose account
    private void loadAccount(){
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
        Log.d(TAG,"chooseAccount Activity?");
        startActivityForResult(credential.newChooseAccountIntent(),REQUEST_ACCOUNT_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG,"onActivity");
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
                    loadAccount();
                break;
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if(resultCode == Activity.RESULT_OK){
                    if(credential.getSelectedAccountName() == null)
                        chooseAccount();
                }else{
                    final int connectionStatusCode = GooglePlayServicesUtil
                            .isGooglePlayServicesAvailable(getApplicationContext());
                    if(GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)){
                        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                                connectionStatusCode, MainActivity.this,
                                REQUEST_GOOGLE_PLAY_SERVICES);
                        dialog.show();
                    }else{
                        Log.w(TAG,"UnRecoverable..");
                    }
                }
                break;
            case RC_SIGN_IN:
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                Log.d(TAG,"ActivityResult#RC SIGN IN :" + result.isSuccess());
                if(result.isSuccess()){
                    GoogleSignInAccount account = result.getSignInAccount();
                    accountImageView.setImageUrl(account.getPhotoUrl().toString(),NetworkSingleton.getInstance(getApplicationContext()).getImageLoader());
                    //accountImageCover.setImageDrawable(getDrawable(R.drawable.materialmini));
                    //accountImageCover.setDefaultImageResId(R.drawable.materialmini);
                    accountNameTextView.setText(account.getDisplayName());
                }else{
                    Log.w(TAG,"Google + FAILED! at activity result");
                }
                break;
            default:
                Log.w(TAG,"Un catch activity result!");
                break;
        }
    }
    //endregion

    //region load account info
    private void loadAccountInfo(){
        Log.d(TAG,"loadAccountInfo");
        setProfileInfo();
        new LoadAccountInfoTask(this,credential).execute();
    }

    private void setProfileInfo(){

        if(googleApiClient != null)
            return;

        Log.d(TAG,"setProfileInfo");
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .setAccountName(accountName)
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.e(TAG,"Google+ onConnectionFailed");
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        Log.d(TAG,"Auth.GoogleSignInApi Activity?");
        startActivityForResult(intent,RC_SIGN_IN);
    }
    //endregion

    //Playlistの読み込みが終わってから
    public void loadFirstFragment(){
        toolbar.setTitle(R.string.favorite);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.BaseLayout, PlayListFragment.create(MainActivity.this, playLists.get("Favorites"))).commit();
    }
}
