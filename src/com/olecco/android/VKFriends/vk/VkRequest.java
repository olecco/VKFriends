package com.olecco.android.VKFriends.vk;

import com.olecco.android.VKFriends.vk.responses.VKResponse;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by olecco on 10.08.2014.
 */
public class VKRequest {

    private static final String VK_API_URL = "https://api.vk.com/method/";

    private VKMethod mMethod;
    private String mAccessToken;
    private Map<String, String> mParameters = new HashMap<String, String>();

    public VKRequest(VKMethod method, String accessToken) {
        mMethod = method;
        mAccessToken = accessToken;
    }

    public VKRequest addParameter(String paramName, String paramValue) {
        mParameters.put(paramName, paramValue);
        return this;
    }

    public String getRequestUrl() {
        StringBuilder builder = new StringBuilder();
        builder.append(VK_API_URL)
                .append(mMethod.getMethodName() + "?");

        Set<String> paramNames = mParameters.keySet();
        boolean isFirst = true;
        for (String paramName: paramNames) {
            if (!isFirst) {
                builder.append("&");
            }
            isFirst = false;
            builder.append(paramName + "=" + mParameters.get(paramName));
        }
        if (mAccessToken != null) {
            builder.append("&access_token=" + mAccessToken);
        }
        return builder.toString();
    }

    public VKMethod getMethod() {
        return mMethod;
    }

}
