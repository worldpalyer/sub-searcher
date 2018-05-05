package io.snows.subs;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class KeyBuilderView extends WebView {

//    public static Set<String> IGNORES = new HashSet<String>() {
//        {
//            add("hevc");
//            add("avc");
//            add("1080");
//            add("720");
//
//            add("1080p");
//            add("720p");
//            add("h.264");
//            add("cam");
//            add("ts");
//            add("tc");
//            add("dvd");
//            add("scr");
//            add("hdrip");
//            add("hdvd");
//            add("dvd5");
//            add("dvd9");
//            add("bd");
//            add("4k");
//            add("aac");
//            add("web-dl");
//            add("DTS-MT");
//            add("BluRay");
//            add("x264");
//        }
//    };

    private Exception err;
    private ValueCallback<List<String>> callback;

    public KeyBuilderView(Context context) {
        super(context);
        this.init();
    }

    public KeyBuilderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public KeyBuilderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    public Exception getErr() {
        return err;
    }

    private Handler h = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    loadUrl("about:blank");
                    break;
            }
        }
    };

    private WebViewClient wvc = new WebViewClient() {
        @Override
        public void onPageFinished(WebView view, String url) {
            view.evaluateJavascript("" +
                    "function findKeys() {\n" +
                    "    var ems = document.getElementsByTagName(\"em\");\n" +
                    "    var emsc = {};\n" +
                    "    for (var i = 0; i < ems.length; i++) {\n" +
                    "        var key = ems[i].innerText.trim().toLowerCase().replace(\".\", \" \");\n" +
                    "        if (emsc[key]) {\n" +
                    "            emsc[key]++;\n" +
                    "        } else {\n" +
                    "            emsc[key] = 1;\n" +
                    "        }\n" +
                    "    }\n" +
                    "    return JSON.stringify(emsc);\n" +
                    "}\n" +
                    "mkeys.onKeysData(findKeys());", null);
        }
    };

    private void init() {
        this.setWebViewClient(this.wvc);
        this.setWebChromeClient(new WebChromeClient());
        this.addJavascriptInterface(this, "mkeys");
        WebSettings settings = this.getSettings();
        settings.setJavaScriptEnabled(true);
    }

    @JavascriptInterface
    public void onKeysData(String value) {
        if (callback == null) {
            return;
        }
        try {
            JSONObject ls = new JSONObject(value);
            Iterator<String> keys = ls.keys();
            Map<String, Integer> kc = new HashMap<>();
            List<String> sortKeys = new ArrayList<>();
            while (keys.hasNext()) {
                String key = keys.next();
                kc.put(key, ls.getInt(key));
                sortKeys.add(key);
            }
            Collections.sort(sortKeys, Collections.reverseOrder(new KeysCompartor(kc)));
            err = null;
            this.callback.onReceiveValue(sortKeys);
            this.callback = null;
        } catch (Exception e) {
            err = e;
            this.callback.onReceiveValue(null);
            this.callback = null;
        }
        h.sendEmptyMessage(100);
    }

    public void build(String key, ValueCallback<List<String>> callback) {
        try {
            key = key.split("&")[0];
            this.callback = callback;
            this.loadUrl("https://www.baidu.com/s?wd=" + URLEncoder.encode(key, "utf-8"));
        } catch (Exception e) {
            this.err = e;
            callback.onReceiveValue(null);
            this.callback = null;
        }
    }

    static class KeysCompartor implements Comparator<String> {
        private Map<String, Integer> kc;

        public KeysCompartor(Map<String, Integer> kc) {
            this.kc = kc;
        }

        @Override
        public int compare(String o1, String o2) {
            return kc.get(o1).compareTo(kc.get(o2));
        }

        @Override
        public boolean equals(Object obj) {
            return false;
        }
    }
//    public extends WebView(String uri) {
//        mUri = uri;
//
//        String[] parts = mUri.split("\\?")[0].split("/");
//        if (parts.length > 0) {
//            mFilename = parts[parts.length - 1];
//        }
//        if (parts.length > 1) {
//            mFolder = parts[parts.length - 2];
//        }
//    }
//
//    public String build() {
//        String[] parts;
//        parts = mFilename.split("\\.");
//
//    }

    public boolean isRunning() {
        return this.callback != null;
    }
}
