package com.olecco.android.VKFriends.vk.responses;

import com.olecco.android.VKFriends.vk.VKRequest;
import com.olecco.android.VKFriends.vk.model.VKUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by olecco on 10.08.2014.
 */
public class VKUserListResponse extends VKResponse {

    private List<VKUser> mUsers;

    public VKUserListResponse(VKRequest request, String responseStr) {
        super(request, responseStr);
    }

    @Override
    protected void parse(String responseStr) {
        mUsers = new ArrayList<VKUser>();
        try {
            JSONObject jsonObject = new JSONObject(responseStr);
            JSONArray usersJson = jsonObject.getJSONArray("response");
            for (int i = 0; i < usersJson.length(); i++) {
                VKUser user = new VKUser(usersJson.getJSONObject(i));
                mUsers.add(user);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<VKUser> getUsers() {
        return mUsers;
    }
}
