package io.snows.subs;

import android.webkit.ValueCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface SubSearcher {
    void search(Map<String, String> having, String ws, String key, ValueCallback<List<String>> callback);

    void more(ValueCallback<List<String>> callback);

    boolean isRunning();

}
