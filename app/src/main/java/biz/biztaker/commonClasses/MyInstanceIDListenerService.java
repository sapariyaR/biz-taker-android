package biz.biztaker.commonClasses;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by Anand Jakhaniya on 11-02-2018.
 * @author Anand Jakhaniya
 */

public class MyInstanceIDListenerService extends FirebaseInstanceIdService {

    private final String TAG = this.getClass().getSimpleName();

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed Firebase token: " + refreshedToken);
        // TODO: Implement this method to send any registration to your app's servers.
        //sendRegistrationToServer(refreshedToken);

        FirebaseMessaging.getInstance().subscribeToTopic("mytopic");
        //FirebaseMessaging.getInstance().unsubscribeToTopic("mytopic");
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the person.
     */
    public void sendRegistrationToServer(String fcmToken){
        SharedPreferences sharedPreferences = BizTakerApp.sharedPreferences;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("fcmToken",fcmToken);
        Boolean status = editor.commit();
        Log.d(TAG, "FCM Token Save in SharedPreferences : " + status);
    }
}
