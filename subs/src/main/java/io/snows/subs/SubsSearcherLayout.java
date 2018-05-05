package io.snows.subs;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.ValueCallback;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SubsSearcherLayout extends FrameLayout implements SubSearcher {

    private KeyBuilderView mKeyBuilder;
    private List<SubSearcher> mSubs = new ArrayList<>();

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

    private synchronized void init() {
        if (mKeyBuilder != null && !mSubs.isEmpty()) {
            return;
        }
        int count = this.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = this.getChildAt(i);
            if (v instanceof SubSearcher) {
                mSubs.add((SubSearcher) v);
            } else if (v instanceof KeyBuilderView) {
                mKeyBuilder = (KeyBuilderView) v;
            }
        }
    }

    public void search(final Map<String, String> having, final String ws, final String key, final ValueCallback<List<String>> callback) {
        init();
        this.mKeyBuilder.build(key, new ValueCallback<List<String>>() {
            @Override
            public void onReceiveValue(List<String> value) {
                String locKey = key;
                if (value != null && !value.isEmpty()) {
                    locKey = value.get(0);
                }
                Map<String, String> locHaving = having;
                if (locHaving == null) {
                    locHaving = SubSearcherView.loadHaving(ws);
                }
                for (SubSearcher searcher : mSubs) {
                    searcher.search(locHaving, ws, locKey, callback);
                }
            }
        });
    }

    public void more(ValueCallback<List<String>> callback) {
        init();
        for (SubSearcher searcher : mSubs) {
            searcher.more(callback);
        }
    }

    public boolean isRunning() {
        init();
        if (this.mKeyBuilder.isRunning()) {
            return true;
        }
        for (SubSearcher searcher : mSubs) {
            if (searcher.isRunning()) {
                return true;
            }
        }
        return false;
    }
}
