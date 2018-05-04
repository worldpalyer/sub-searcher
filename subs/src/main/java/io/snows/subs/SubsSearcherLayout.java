package io.snows.subs;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.webkit.ValueCallback;
import android.widget.FrameLayout;

import java.util.List;
import java.util.Map;

public class SubsSearcherLayout extends FrameLayout implements SubSearcher {

    public SubsSearcherLayout(@NonNull Context context) {
        super(context);
    }

    public SubsSearcherLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SubsSearcherLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                              @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void search(Map<String, String> having, String ws, String key, ValueCallback<List<String>> callback) {
        if (having == null) {
            having = SubSearcherView.loadHaving(ws);
        }
        int count = this.getChildCount();
        for (int i = 0; i < count; i++) {
            SubSearcher searcher = (SubSearcher) this.getChildAt(i);
            searcher.search(having, ws, key, callback);
        }
    }

    public void more(ValueCallback<List<String>> callback) {
        int count = this.getChildCount();
        for (int i = 0; i < count; i++) {
            SubSearcher searcher = (SubSearcher) this.getChildAt(i);
            searcher.more(callback);
        }
    }

}
