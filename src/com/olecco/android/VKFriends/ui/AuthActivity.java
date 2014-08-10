package com.olecco.android.VKFriends.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.olecco.android.VKFriends.R;

import java.net.CookieManager;

public class AuthActivity extends Activity {

    public final static String AUTH_URL_EXTRA = "AUTH_URL_EXTRA";
    public final static String REDIRECT_URL_EXTRA = "REDIRECT_URL_EXTRA";
    public final static String AUTH_RESULT_EXTRA = "AUTH_RESULT_EXTRA";

    private WebView mWebView;
    private String mAuthUrl;
    private String mRedirectUrl;

    private class VKWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (url.startsWith(mRedirectUrl)) {
                Intent intent = new Intent();
                intent.putExtra(AUTH_RESULT_EXTRA, url);
                setResult(RESULT_OK, intent);
                finish();
            }
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth);

        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.clearCache(true);
        mWebView.setWebViewClient(new VKWebViewClient());
        android.webkit.CookieManager.getInstance().removeAllCookie();

        readIntentData();
        mWebView.loadUrl(mAuthUrl);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void readIntentData() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mAuthUrl = extras.getString(AUTH_URL_EXTRA);
            mRedirectUrl = extras.getString(REDIRECT_URL_EXTRA);
        }
        if (mAuthUrl == null || mRedirectUrl == null) {
            setResult(RESULT_CANCELED);
            finish();
        }
    }
}
