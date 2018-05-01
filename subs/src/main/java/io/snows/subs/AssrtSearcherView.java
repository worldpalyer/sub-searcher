package io.snows.subs;

import android.content.Context;
import android.util.AttributeSet;
import java.net.URLEncoder;

public class AssrtSearcherView extends SubSearcherView {

    public AssrtSearcherView(Context context) {
        super(context, null);
    }

    public AssrtSearcherView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AssrtSearcherView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected String downSubsScript() {
        return "document.getElementById(\"btn_download\").click();";
    }

    @Override
    protected String findSubsScript() {
        String script="function findSubs() {\n" +
                "    var subs = [];\n" +
                "    var titles = document.getElementsByClassName(\"introtitle\");\n" +
                "    for (var i = 0; i < titles.length; i++) {\n" +
                "        var title = titles[i];\n" +
                "        var href = title.href;\n" +
                "        var text = title.title;\n" +
                "        if (/^\\/.*$/.test(href)) {\n" +
                "            subs.push({ href: window.location.origin + href, text: text });\n" +
                "        } else if (/^http.*$/.test(href)) {\n" +
                "            subs.push({ href: href, text: text });\n" +
                "        } else {\n" +
                "            var idx = window.location.pathname.lastIndexOf(\"/\");\n" +
                "            if (idx < 0) {\n" +
                "                subs.push({ href: window.location.origin + href, text: text });\n" +
                "            } else {\n" +
                "                subs.push({ href: window.location.origin + window.location.pathname.substr(0, idx) + href, text: text });\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "    return JSON.stringify(subs);\n" +
                "}\n"+
                "subs.onSubsData(findSubs());";
        return script;
    }

    @Override
    protected String createSearch(String key) throws Exception {
        return "http://assrt.net/sub/?searchword=" + URLEncoder.encode(key, "utf-8");
    }
}
