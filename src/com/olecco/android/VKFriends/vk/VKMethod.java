package com.olecco.android.VKFriends.vk;

/**
 * Created by olecco on 10.08.2014.
 */
public enum VKMethod {
    FRIENDS_GET("friends.get");

    private String mMethodName;

    VKMethod(String methodName) {
        mMethodName = methodName;
    }

    public String getMethodName() {
        return mMethodName;
    }
}
