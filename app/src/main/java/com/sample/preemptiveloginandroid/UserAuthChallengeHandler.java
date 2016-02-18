package com.sample.preemptiveloginandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.worklight.wlclient.api.WLAccessTokenListener;
import com.worklight.wlclient.api.WLAuthorizationManager;
import com.worklight.wlclient.api.WLClient;
import com.worklight.wlclient.api.WLFailResponse;
import com.worklight.wlclient.api.WLLoginResponseListener;
import com.worklight.wlclient.api.WLLogoutResponseListener;
import com.worklight.wlclient.api.challengehandler.WLChallengeHandler;
import com.worklight.wlclient.auth.AccessToken;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nathanh on 18/02/16.
 */
public class UserAuthChallengeHandler extends WLChallengeHandler {
    private static String securityCheckName = "UserAuthSecurityCheck";
    private int remainingAttempts = -1;
    private String errorMsg = "";
    private Context context;
    private boolean isChallenged = false;

    private LocalBroadcastManager broadcastManager;

    private UserAuthChallengeHandler() {
        super(securityCheckName);
        context = WLClient.getInstance().getContext();
        broadcastManager = LocalBroadcastManager.getInstance(context);

        //Reset the current user
        SharedPreferences preferences = context.getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(Constants.PREFERENCES_KEY_USER);
        editor.commit();

        //Receive login requests
        broadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    JSONObject credentials = new JSONObject(intent.getStringExtra("credentials"));
                    login(credentials);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },new IntentFilter(Constants.ACTION_LOGIN));

        //Receive logout requests
        broadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                logout();
            }
        }, new IntentFilter(Constants.ACTION_LOGOUT));


    }

    public static UserAuthChallengeHandler createAndRegister(){
        UserAuthChallengeHandler challengeHandler = new UserAuthChallengeHandler();
        WLClient.getInstance().registerChallengeHandler(challengeHandler);
        return challengeHandler;
    }


    @Override
    public void handleChallenge(JSONObject jsonObject) {
        Log.d(securityCheckName, "Challenge Received");
        isChallenged = true;
        try {
            if(jsonObject.isNull("errorMsg")){
                errorMsg = "";
            }
            else{
                errorMsg = jsonObject.getString("errorMsg");
            }

            remainingAttempts = jsonObject.getInt("remainingAttempts");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_LOGIN_REQUIRED);
        intent.putExtra("errorMsg", errorMsg);
        intent.putExtra("remainingAttempts",remainingAttempts);
        broadcastManager.sendBroadcast(intent);

    }

    @Override
    public void handleFailure(JSONObject error) {
        super.handleFailure(error);
        isChallenged = false;
        if(error.isNull("failure")){
            errorMsg = "Failed to login. Please try again later.";
        }
        else {
            try {
                errorMsg = error.getString("failure");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_LOGIN_FAILURE);
        intent.putExtra("errorMsg",errorMsg);
        broadcastManager.sendBroadcast(intent);
        Log.d(securityCheckName, "handleFailure");
    }

    @Override
    public void handleSuccess(JSONObject identity) {
        super.handleSuccess(identity);
        isChallenged = false;
        try {
            //Save the current user
            SharedPreferences preferences = context.getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.PREFERENCES_KEY_USER, identity.getJSONObject("user").toString());
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_LOGIN_SUCCESS);
        broadcastManager.sendBroadcast(intent);
        Log.d(securityCheckName, "handleSuccess");
    }

    public void login(JSONObject credentials){
        if(isChallenged){
            submitChallengeAnswer(credentials);
        }
        else{
            WLAuthorizationManager.getInstance().login(securityCheckName, credentials, new WLLoginResponseListener() {
                @Override
                public void onSuccess() {
                    Log.d(securityCheckName, "Login Preemptive Success");

                }

                @Override
                public void onFailure(WLFailResponse wlFailResponse) {
                    Log.d(securityCheckName, "Login Preemptive Failure");
                }
            });
        }
    }


    public void logout(){
        WLAuthorizationManager.getInstance().logout(securityCheckName, new WLLogoutResponseListener() {
            @Override
            public void onSuccess() {
                Log.d(securityCheckName, "Logout Success");
                Intent intent = new Intent();
                intent.setAction(Constants.ACTION_LOGOUT_SUCCESS);
                broadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onFailure(WLFailResponse wlFailResponse) {
                Log.d(securityCheckName, "Logout Failure");
                Intent intent = new Intent();
                intent.setAction(Constants.ACTION_LOGOUT_FAILURE);
                broadcastManager.sendBroadcast(intent);

            }
        });
    }

}
