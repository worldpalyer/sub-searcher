package io.snows.subs;

import android.webkit.ValueCallback;

import java.util.List;

public interface SubSearcher {
    void search(String ws, String key, ValueCallback<List<String>> callback);
    void more(ValueCallback<List<String>> callback);
}
