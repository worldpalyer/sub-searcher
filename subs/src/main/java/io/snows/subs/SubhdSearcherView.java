package io.snows.subs;

import android.content.Context;
import android.util.AttributeSet;
import java.net.URLEncoder;

public class SubhdSearcherView extends SubSearcherView {

    public SubhdSearcherView(Context context) {
        super(context, null);
    }

    public SubhdSearcherView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SubhdSearcherView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected String downSubsScript() {
        return "document.getElementById(\"down\").click();";
    }

    @Override
    protected String findSubsScript() {
        String script = "function findSubs() {\n" +
                "    var subs = [];\n" +
                "    $(\".d_title a\").each(function (e) {\n" +
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
    protected String createSearch(String key) throws Exception {
        return "http://subhd.com/search0/" + URLEncoder.encode(key, "utf-8");
    }
}
