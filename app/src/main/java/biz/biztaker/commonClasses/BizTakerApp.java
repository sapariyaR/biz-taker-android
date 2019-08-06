package biz.biztaker.commonClasses;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;

import biz.biztaker.R;
import biz.biztaker.activity.LoginActivity;
import biz.biztaker.entity.Person;
import biz.biztaker.room.BizTakerDatabase;
import biz.biztaker.room.BizTakerRoomDB;

/**
 * Created by Anand Jakhaniya on 11-02-2018.
 * - BizTakerApp is base class for maintaining global application state,
 *   like onCreate(), onTerminate(), onConfigurationChanged() etc.
 * - Use this class to maintain Application states and code related to that.
 * - Do not use this class as an utility class.
 *
 * @author Anand Jakhaniya
 */

public class BizTakerApp extends Application {

    private final String TAG = this.getClass().getSimpleName();
    private static BizTakerApp ourInstance = null;
    /* Access preference file */
    public static SharedPreferences sharedPreferences;
    public static Person person;
    public AppCompatActivity currentActivity = null;

    /**
     * This method should be synchronized because this method may be access by two threads at a time,
     * so there is a chance to create two instance.(This is an exceptional case of singleton while
     * working in multithreading environment).
     * @return singleton instance of BizTakerApp
     */
    public static synchronized BizTakerApp getInstance() {
        if (ourInstance == null) {
            ourInstance = new BizTakerApp();
        }
        return ourInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ourInstance = this;
        BizTakerDatabase.init(getApplicationContext());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        DatabaseSourceImpl.getInstance();

        initializeRequiredData();
    }

    public void initializeRequiredData() {

        Boolean loginStatus = sharedPreferences.getBoolean("loginStatus",false);
        if (loginStatus) {
            Person person = getPersonInSharedPreferences();
            BizTakerApp.setPerson(person);

            String fcmToken = BizTakerApp.sharedPreferences.getString("fcmToken", null);
            if (fcmToken == null || fcmToken.isEmpty()) {
                new RegisterFcmTokenInServer().execute();
            }
        }
    }

    public Boolean checkPlayServices(){


        return false;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        DatabaseSourceImpl baseDataSourceImpl = DatabaseSourceImpl.getInstance();
        baseDataSourceImpl.close();
    }

    public static void setPerson(Person person) {
        BizTakerApp.person = person;
    }

    public static void setPersonInSharedPreferences(Person person){
        SharedPreferences sharedPreferences = BizTakerApp.sharedPreferences;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new GsonBuilder().create();
        editor.putString("person", gson.toJson(person));
        Boolean commitStatus = editor.commit();
    }

    public static Person getPersonInSharedPreferences(){
        SharedPreferences sharedPreferences = BizTakerApp.sharedPreferences;
        String personString = sharedPreferences.getString("person",null);
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(personString, Person.class);
    }

    public static String getAppStorageAbsolutePath(){
        File rootDirectory = Environment.getExternalStorageDirectory();
        File file = new File(rootDirectory.getAbsolutePath() + "/" +ourInstance.getApplicationContext().getResources().getString(R.string.app_name));
        if (!file.exists()) {
            Boolean createStatus = file.mkdirs();
            Log.d("BizTakerApp"," create Storage Absolute Path status : " + createStatus);
        }
        return file.getAbsolutePath();
    }

    public static synchronized BizTakerRoomDB getDb() {
        return BizTakerDatabase.get().getDb();
    }

    public static synchronized BizTakerRoomDB getUiDb() {
        return BizTakerDatabase.get().getUiDb();
    }

    public void logout() {
        SharedPreferences sharedPreferences = BizTakerApp.sharedPreferences;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("loginStatus",false);
        editor.remove("person");
        editor.remove("fcmToken");
        editor.remove("refresh_token");
        editor.remove("auth_token");
        Boolean status = editor.commit();
        Log.d(TAG, getApplicationContext().getResources().getString(R.string.action_logout) + status);
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        currentActivity.startActivity(intent);
        currentActivity.finish();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the person.
     */
    @SuppressLint("StaticFieldLeak")
    public class RegisterFcmTokenInServer extends AsyncTask<Void, Void, Boolean> {

        private String fcmToken;
        private String errorMessage;

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                fcmToken = FirebaseInstanceId.getInstance().getToken();
                Log.d(TAG, "Refreshed Firebase token: " + fcmToken);
                // TODO: Implement this method to send any registration to your app's servers.
                //sendRegistrationToServer(refreshedToken);

                FirebaseMessaging.getInstance().subscribeToTopic("mytopic");
                return true;
            } catch (Exception e) {
                errorMessage = e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (success) {
                SharedPreferences sharedPreferences = BizTakerApp.sharedPreferences;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("fcmToken",fcmToken);
                Boolean status = editor.commit();
                Log.d(TAG, "FCM Token Save in SharedPreferences : " + status);
            } else {
                Log.d(TAG, "FCM Token fail to generate : " + errorMessage);
            }
        }

        @Override
        protected void onCancelled() {

        }
    }
}
