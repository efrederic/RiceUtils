package com.taken.riceutils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 *
 */
public class WebViews extends Activity{

    public WebViews() {
        // Required empty public constructor
    }

    WebView web;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);

        web = (WebView) findViewById(R.id.webview);
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
        web.getSettings().setJavaScriptEnabled(true);
        web.loadUrl(getString(R.string.dining_link));
        web.loadUrl(getIntent().getStringExtra("url"));
    }

//    public class myWebClient extends WebViewClient {
//        @Override
//        public void onPageStarted(WebView view, String url, Bitmap favicon) {
//            // TODO Auto-generated method stub
//            super.onPageStarted(view, url, favicon);
//        }
//
//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            // TODO Auto-generated method stub
//
//            view.loadUrl(url);
//            return true;
//        }
//    }

    // To handle "Back" key press event for WebView to go back to previous screen.
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && web.canGoBack()) {
            if (web.canGoBack()) {
                web.goBack();
            }
            else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}



