package com.sample.preemptiveloginandroid;

/**
 * Created by nathanh on 18/02/16.
 */
public class Constants {
    static final String ACTION_LOGIN = "com.sample.userauthandroid.broadcast.login";
    static final String ACTION_LOGIN_SUCCESS = "com.sample.userauthandroid.broadcast.login.success";
    static final String ACTION_LOGIN_FAILURE = "com.sample.userauthandroid.broadcast.login.failure";
    static final String ACTION_LOGIN_REQUIRED = "com.sample.userauthandroid.broadcast.login.required";

    static final String ACTION_LOGOUT = "com.sample.userauthandroid.broadcast.logout";
    static final String ACTION_LOGOUT_SUCCESS = "com.sample.userauthandroid.broadcast.logout.success";
    static final String ACTION_LOGOUT_FAILURE = "com.sample.userauthandroid.broadcast.logout.failure";

    static final String PREFERENCES_FILE = "com.sample.userauthandroid.preferences";
    static final String PREFERENCES_KEY_USER = "com.sample.userauthandroid.preferences.user";
}
