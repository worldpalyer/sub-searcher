package io.snows.subs;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

import java.net.URLEncoder;

public class ZimukuSearcherView extends SubSearcherView {

    public ZimukuSearcherView(Context context) {
        super(context);
    }

    public ZimukuSearcherView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ZimukuSearcherView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected String findSubsScript() {
        String script = "function findSubs() {\n" +
                "    var subs = [];\n" +
                "    $(\".first a\").each(function (e) {\n" +
                "        var href = $(this).attr(\"href\");\n" +
                "        if (/^\\/.*$/.test(href)) {\n" +
                "            subs.push({ href: window.location.origin + href, text: $(this).text() });\n" +
                "        } else if (/^http.*$/.test(href)) {\n" +
                "            subs.push({ href: href, text: $(this).text() });\n" +
                "        } else {\n" +
                "            var idx = window.location.pathname.lastIndexOf(\"/\");\n" +
                "            if (idx < 0) {\n" +
                "                subs.push({ href: window.location.origin + href, text: $(this).text() });\n" +
                "            } else {\n" +
                "                subs.push({ href: window.location.origin + window.location.pathname.substr(0, idx) + href, text: $(this).text() });\n" +
                "            }\n" +
                "        }\n" +
                "    });\n" +
                "    return JSON.stringify(subs);\n" +
                "}\n" +
                "subs.onSubsData(findSubs());";
        return script;
    }

    @Override
    protected String downSubsScript() {
        this.regRunning = ".*/dld/.*";
        return "window.location.href=document.getElementById('down1').href;";
    }

    @Override
    protected void onMatchRegRunning(WebView view, String url) {
        this.regRunning = null;
        view.evaluateJavascript("document.getElementsByClassName(\"btn-danger\")[0].click();", null);
    }

    @Override
    protected String createSearch(String key) throws Exception {
        return "https://www.zimuku.cn/search?q=" + URLEncoder.encode(key, "utf-8");
    }
}
