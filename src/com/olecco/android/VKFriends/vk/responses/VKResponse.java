package com.olecco.android.VKFriends.vk.responses;

import com.olecco.android.VKFriends.vk.VKRequest;

/**
 * Created by olecco on 10.08.2014.
 */
public class VKResponse {

    private VKRequest mRequest;

    public VKResponse(VKRequest request, String responseStr) {
        mRequest = request;
        parse(responseStr);
    }

    protected void parse(String responseStr) { }

    public static VKResponse buildResponse(VKRequest request, String responseStr) {
        switch (request.getMethod()) {
            case FRIENDS_GET: return new VKUserListResponse(request, responseStr);
            default: return null;
        }
    }

}
