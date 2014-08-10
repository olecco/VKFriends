package com.olecco.android.VKFriends.vk.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by olecco on 10.08.2014.
 */
public class VKUser {

    private int mUserId;
    private String mFirstName;
    private String mLastName;
    private String mPhoto50Url;
    private String mPhoto100Url;

    public VKUser(JSONObject jsonObject) {
        try {
            mUserId = jsonObject.getInt("uid");
            mFirstName = jsonObject.getString("first_name");
            mLastName = jsonObject.getString("last_name");
            mPhoto50Url = jsonObject.getString("photo_50");
            mPhoto100Url = jsonObject.getString("photo_100");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getUserId() {
        return mUserId;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public String getPhoto50Url() {
        return mPhoto50Url;
    }

    public String getPhoto100Url() {
        return mPhoto100Url;
    }

}
