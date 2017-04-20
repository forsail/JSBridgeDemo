package com.example.jsbridge.jsbridge;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jsbridge.lib_hybird.JsCallJava;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainOneActivity";
    int index;
    private WebView webView;
    private TextView hello;
    private JsCallJava mJsCallJava;

    @SuppressLint("AddJavascriptInterface")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init view
        hello = (TextView) findViewById(R.id.id_hello);
        webView = (WebView) findViewById(R.id.id_webView);
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!TextUtils.isEmpty(url) && url.contains("asher")) {
                    Log.d(TAG, "shouldOverrideUrlLoading: " + Thread.currentThread().getName());
                    Toast.makeText(MainActivity.this, "intercept", Toast.LENGTH_SHORT).show();
                } else {
                    view.loadUrl(url);
                }
                return true;
            }
        });

        //init jsBridge
        mJsCallJava = new JsCallJava();

        //init webView
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                return super.onJsConfirm(view, url, message, result);
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                result.confirm(mJsCallJava.call(view, message));
                return true;
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d(TAG, "onConsoleMessage: " + consoleMessage.message());
                return super.onConsoleMessage(consoleMessage);
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/html/jsCode.html");
        webView.addJavascriptInterface(this, "android");

        hello.setText("Call JS method");
        hello.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 19) {
                    webView.evaluateJavascript("javascript:show_text(" + index + ")", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            Log.d(TAG, "onReceiveValue: " + Thread.currentThread().getName());
                            Log.d(TAG, "onReceiveValue: " + value);
                        }
                    });
                } else {
                    webView.loadUrl("javascript:show_text(" + index + ")");
                }

                index++;
            }
        });


    }

    @JavascriptInterface
    public void jsCallAndroid() {
        Log.d(TAG, "jsCallAndroid: " + Thread.currentThread().getName());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: " + Thread.currentThread().getName());
                Toast.makeText(MainActivity.this, "js call android", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
