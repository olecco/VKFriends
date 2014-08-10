package com.olecco.android.VKFriends.vk;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.olecco.android.VKFriends.ui.AuthActivity;
import com.olecco.android.VKFriends.vk.responses.VKResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by olecco on 09.08.2014.
 */
public class VKClient {

    public static interface VKApiListener {
        void onMethodExecuted(VKResponse response);
    }

    private static final String VK_AUTH_URL = "https://oauth.vk.com/authorize";
    private static final String VK_AUTH_REDIRECT_URL = "http://null.info";

    private static final String VK_PARAM_CLIENT_ID = "client_id";
    private static final String VK_PARAM_SCOPE = "scope";
    private static final String VK_PARAM_REDIRECT_URI = "redirect_uri";
    private static final String VK_PARAM_DISPLAY = "display";
    private static final String VK_PARAM_RESPONSE_TYPE = "response_type";

    private static final String VK_APP_ID_META = "vk_app_id";
    private static final int REQUEST_CODE_AUTH = 20;

    private static final int API_RESULT_OK = 0;

    private static class SingletonHolder {
        public static final VKClient HOLDER_INSTANCE = new VKClient();
    }

    public static VKClient getInstance() {
        return SingletonHolder.HOLDER_INSTANCE;
    }

    private String mAccessToken;
    private String mUserId;

    private class ApiHandler extends Handler {

        private WeakReference<VKApiListener> mApiListenerRef;

        public ApiHandler(VKApiListener listener) {
            mApiListenerRef = new WeakReference<VKApiListener>(listener);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg != null && API_RESULT_OK == msg.what) {
                VKResponse response = (VKResponse) msg.obj;
                VKApiListener listener = mApiListenerRef.get();
                if (response != null && listener != null) {
                    listener.onMethodExecuted(response);
                }
            }
        }
    };

    private class APIThread extends Thread {

        private VKRequest mRequest;
        private ApiHandler mApiHandler;

        public APIThread(VKRequest request, VKApiListener apiListener) {
            mRequest = request;
            mApiHandler = new ApiHandler(apiListener);
        }

        @Override
        public void run() {
            String res = sendRequest(mRequest);
            VKResponse response = VKResponse.buildResponse(mRequest, res);
            Message message = mApiHandler.obtainMessage();
            message.what = API_RESULT_OK;
            message.obj = response;
            message.sendToTarget();
        }

        private String sendRequest(VKRequest request) {
            try {
                URL url = new URL(request.getRequestUrl());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                return streamToString(connection.getInputStream());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

        private String streamToString(InputStream stream) throws IOException {
            StringBuilder sb = new StringBuilder();
            BufferedReader rd = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }

    }

    public void authorize(Activity activity) {
        String appId = getAppIdFromMetaData(activity);
        if (appId != null) {
            String authUrl = getAuthUrl(appId);
            if (!authUrl.isEmpty() && activity != null ) {
                Intent intent = new Intent(activity, AuthActivity.class);
                intent.putExtra(AuthActivity.AUTH_URL_EXTRA, authUrl);
                intent.putExtra(AuthActivity.REDIRECT_URL_EXTRA, VK_AUTH_REDIRECT_URL);
                activity.startActivityForResult(intent, REQUEST_CODE_AUTH);
            }
        }
    }

    public void authorize(Fragment fragment) {
        Context context = fragment.getActivity();
        if (context != null) {
            String appId = getAppIdFromMetaData(context);
            if (appId != null) {
                String authUrl = getAuthUrl(appId);
                if (!authUrl.isEmpty()) {
                    Intent intent = new Intent(context, AuthActivity.class);
                    intent.putExtra(AuthActivity.AUTH_URL_EXTRA, authUrl);
                    intent.putExtra(AuthActivity.REDIRECT_URL_EXTRA, VK_AUTH_REDIRECT_URL);
                    fragment.startActivityForResult(intent, REQUEST_CODE_AUTH);
                }
            }
        }
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_CODE_AUTH == requestCode && Activity.RESULT_OK == resultCode) {
            if (data != null) {
                String tokenUrl = data.getStringExtra(AuthActivity.AUTH_RESULT_EXTRA);
                return parseTokenUrl(tokenUrl);
            }
        }
        return false;
    }

    private String getAuthUrl(String appId) {
        StringBuilder builder = new StringBuilder();
        builder.append(VK_AUTH_URL + "?")
                .append(VK_PARAM_CLIENT_ID + "=" + appId + "&")
                .append(VK_PARAM_SCOPE + "=friends,photos" + "&")
                .append(VK_PARAM_REDIRECT_URI + "=" + VK_AUTH_REDIRECT_URL + "&")
                .append(VK_PARAM_DISPLAY + "=touch" + "&")
                .append(VK_PARAM_RESPONSE_TYPE + "=token");
        return builder.toString();
    }

    private String getAppIdFromMetaData(Context context) {
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            if (applicationInfo != null && applicationInfo.metaData != null) {
                int vk_app_id = applicationInfo.metaData.getInt(VK_APP_ID_META);
                return String.valueOf(vk_app_id);
            }
        } catch (PackageManager.NameNotFoundException e) { }
        return null;
    }

    private boolean parseTokenUrl(String tokenUrl) {
        if (tokenUrl != null && !tokenUrl.isEmpty()) {
            mAccessToken = extractPattern(tokenUrl, "access_token=(.*?)&");
            mUserId = extractPattern(tokenUrl, "user_id=(\\d*)");
            return isAuthorized();
        }
        return false;
    }

    public boolean isAuthorized() {
        return mAccessToken != null && !mAccessToken.isEmpty();
    }

    public String getUserId() {
        return mUserId;
    }

    public void getFriends(String userId, int count, int offset, VKApiListener apiListener) {
        VKRequest request = new VKRequest(VKMethod.FRIENDS_GET, mAccessToken);
        request.addParameter("user_id", userId);
        request.addParameter("fields", "photo_50,photo_100");
        if (count != 0) {
            request.addParameter("count", String.valueOf(count));
        }
        if (offset != 0) {
            request.addParameter("offset", String.valueOf(offset));
        }
        executeRequest(request, apiListener);
    }

    private void executeRequest(VKRequest request, VKApiListener apiListener) {
        new APIThread(request, apiListener).start();
    }

    private String extractPattern(String string, String pattern){
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(string);
        if (!m.find())
            return null;
        return m.toMatchResult().group(1);
    }

}
