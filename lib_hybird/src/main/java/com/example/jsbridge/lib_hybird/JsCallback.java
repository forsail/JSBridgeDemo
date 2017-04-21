package com.example.jsbridge.lib_hybird;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2016/12/28.
 */

public class JsCallback {

    private static final String TAG = "JsCallback";
    private WeakReference<WebView> mWebViewRef;
    private String mCallbackId;
    private boolean mCouldGoOn = true;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public JsCallback(WebView webView, String callbackId) {
        mWebViewRef = new WeakReference<>(webView);
        mCallbackId = callbackId;
    }

    private static final String CALLBACK_JS_FORMAT = "javascript:JsBridge.onComplete('%s', %s);";

    public void apply(boolean isSuccess, String message, JSONObject object) throws JsCallbackException {
        if (mWebViewRef.get() == null) {
            throw new JsCallbackException("the WebView related to the JsCallback has been recycled");
        }
        if (!mCouldGoOn) {
            throw new JsCallbackException("the JsCallback isn't permanent,cannot be called more than once");
        }
        JSONObject result = new JSONObject();

        try {
            JSONObject code = new JSONObject();
            code.put("code", isSuccess ? 0 : 1);
            if (!isSuccess && !TextUtils.isEmpty(message)) {
                code.putOpt("msg", message);
            }
            if (isSuccess) {
                code.putOpt("msg", TextUtils.isEmpty(message) ? "SUCCESS" : message);
            }
            result.putOpt("status", code);
            if (null != object) {
                result.putOpt("data", object);
            } else {
                result.put("data", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //把执行的结果传给js,实际调用的是js暴露给Native的onComplete方法
        final String jsFunc = String.format(CALLBACK_JS_FORMAT, mCallbackId, String.valueOf(result));
        Log.d(TAG, "apply: " + jsFunc);

        if (mWebViewRef != null && mWebViewRef.get() != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mWebViewRef.get().loadUrl(jsFunc);
                }
            });

        }
    }


    public class JsCallbackException extends Exception {

        public JsCallbackException(String msg) {
            super(msg);
        }
    }
}
