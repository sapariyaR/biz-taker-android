package biz.biztaker.commonClasses;

import android.content.SharedPreferences;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Anand Jakhaniya on 11-02-2018.
 * @author Anand Jakhaniya
 */

public class BizTackerWebService {

    private static BizTackerWebService bizTackerWebService = null;
    private RequestQueue mRequestQueue;

    private BizTackerWebService(){
        mRequestQueue = getRequestQueue();
    }

    public static synchronized BizTackerWebService getInstance() {

        if (bizTackerWebService == null){
            bizTackerWebService = new BizTackerWebService();
        }
        return bizTackerWebService;
    }


    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(BizTakerApp.getInstance().getApplicationContext());
        }
        return mRequestQueue;
    }

    public JsonObjectRequest getJsonObjectRequest(int method, String url, JSONObject parameters,
                                                  Response.Listener<JSONObject> responseJsonListener, Response.ErrorListener errorListener) throws JSONException {

        return new JsonObjectRequest(method,url, parameters,responseJsonListener,errorListener){
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {

                bizTackerWebService.handleAuthTokenOrRefresh(response);
                return super.parseNetworkResponse(response);
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                if(volleyError.networkResponse != null && volleyError.networkResponse.data != null){
                    Gson gson = new GsonBuilder().create();
                    JsonObject response = gson.fromJson(new String(volleyError.networkResponse.data), JsonObject.class);
                    volleyError = new VolleyError(response.get("message").getAsString());
                }

                return volleyError;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = super.getHeaders();
                String auth_token = BizTakerApp.sharedPreferences.getString("auth_token",null);
                if (auth_token != null && !auth_token.isEmpty()){
                    headers.put("Authorization", "Bearer " + auth_token);
                }
                return headers;
            }
        };
    }

    public <T> void addToRequestQueue(Request<T> req) throws AuthFailureError {
        getRequestQueue().add(req);
    }

    private void handleAuthTokenOrRefresh(NetworkResponse response) {
        Map<String, String> paramHeaders = response.headers;

        if (response.statusCode == 401){
            String refresh_token = paramHeaders.get("refresh_token");
            refreshAuthToken(refresh_token);
        } else {
            String refresh_token = paramHeaders.get("refresh_token");
            String auth_token = paramHeaders.get("auth_token");

            SharedPreferences.Editor editor = BizTakerApp.sharedPreferences.edit();
            if (refresh_token != null) {
                editor.putString("refresh_token", refresh_token);
            }
            if (auth_token != null){
                editor.putString("auth_token", auth_token);
            }
            Boolean tokenStoreStatus = editor.commit();
        }
    }

    public void refreshAuthToken(final String refresh_token){
        JsonObjectRequest refreshRequest = new JsonObjectRequest(Request.Method.POST,URLHelper.getRefreshAuthTokenUrl(), null,null,null){
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {

                bizTackerWebService.handleAuthTokenOrRefresh(response);
                return super.parseNetworkResponse(response);
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = super.getHeaders();
                headers.put("Authorization", "Bearer " + refresh_token);
                return headers;
            }
        };
        try {
            addToRequestQueue(refreshRequest);
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
            Crashlytics.logException(authFailureError);
        }
    }
}
