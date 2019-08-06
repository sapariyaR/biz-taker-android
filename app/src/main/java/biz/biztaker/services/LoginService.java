package biz.biztaker.services;

import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import biz.biztaker.R;
import biz.biztaker.commonClasses.BizTackerWebService;
import biz.biztaker.commonClasses.BizTakerApp;
import biz.biztaker.commonClasses.BizTakerWebServiceCallBack;
import biz.biztaker.commonClasses.URLHelper;
import biz.biztaker.entity.Person;

/**
 * Created by Anand Jakhaniya on 18-02-2018.
 * @author Anand Jakhaniya
 */

public class LoginService {

    private final String TAG = this.getClass().getName();
    private BizTackerWebService bizTackerWebService  = BizTackerWebService.getInstance();
    private BizTakerApp bizTakerApp = BizTakerApp.getInstance();

    public void getPersonFromServer(final String mEmail, final String mPassword, final BizTakerWebServiceCallBack bizTakerWebServiceCallBack) throws Exception {
        String url = URLHelper.getCustomLoginUrl();
        JSONObject parameters = new JSONObject();
        parameters.put("email", mEmail);
        parameters.put("password", mPassword);
        JsonObjectRequest jsonObjReq = bizTackerWebService.getJsonObjectRequest(Request.Method.POST, url, parameters,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        afterLoginResponseProccess(bizTakerWebServiceCallBack,response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                        bizTakerWebServiceCallBack.onErrorResponse(error);
                    }
                });
        bizTackerWebService.addToRequestQueue(jsonObjReq);
    }

    private void afterLoginResponseProccess(BizTakerWebServiceCallBack bizTakerWebServiceCallBack, JSONObject response) {
        Gson gson = new GsonBuilder().create();
        Person person = gson.fromJson(response.toString(), Person.class);
        SharedPreferences sharedPreferences = BizTakerApp.sharedPreferences;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("loginStatus",true);
        Boolean status = editor.commit();
        Log.d(TAG, bizTakerApp.getApplicationContext().getResources().getString(R.string.action_sign_in) + status);
        BizTakerApp.setPersonInSharedPreferences(person);
        bizTakerApp.initializeRequiredData();
        bizTakerWebServiceCallBack.onResponse(person);
    }
}
