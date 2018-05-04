package io.snows.subs;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.extract.ExtractArchive;
import com.github.junrar.rarfile.FileHeader;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public abstract class SubSearcherView extends WebView implements DownloadListener, SubSearcher {
    private String key;
    protected String running;
    protected String regRunning;
    private String searching;
    private String downing;
    private ValueCallback<List<String>> callback;
    private List<Map<String, Object>> slinks;
    private int slinkIdx = 0;
    private String ws;
    private Exception err;
    private Map<String, String> having = new HashMap<>();
    private Handler h = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 10:
                    onSubsDone();
                    break;
                case 20:
                    evaluateJavascript(downSubsScript(), null);
                    break;
                case 21:
                    break;
                case 30:
                    onMatchRegRunning(SubSearcherView.this, msg.obj.toString());
                    break;
                case 100:
                    loadUrl("about:blank");
                    break;
            }
        }
    };

    public SubSearcherView(Context context) {
        super(context, null);
        this.init();
    }

    public SubSearcherView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public SubSearcherView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    private void init() {
        this.setWebViewClient(this.wvc);
        this.setWebChromeClient(this.wcc);
        this.addJavascriptInterface(this, "subs");
        this.setDownloadListener(this);
        WebSettings settings = this.getSettings();
        settings.setJavaScriptEnabled(true);
    }

    public static Map<String, String> loadHaving(String ws) {
        Map<String, String> having = new HashMap<>();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(ws + "/.subs.json");
            byte[] data = IOUtils.toByteArray(fis);
            fis.close();
            fis = null;
            JSONObject json = new JSONObject(new String(data, "utf-8"));
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                having.put(key, json.getString(key));
            }
        } catch (IOException e) {

        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {

            }
        }
        return having;
    }

    public static void storeHaving(String ws, Map<String, String> having) throws IOException {
        String data = new JSONObject(having).toString();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(ws + "/.subs.json");
            fos.write(data.getBytes());
            fos.flush();
            fos.close();
            fos = null;
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {

            }
        }
    }

    public Exception getErr() {
        return err;
    }

    private WebViewClient wvc = new WebViewClient() {
        @Override
        public void onPageFinished(WebView view, String url) {
            if (callback == null) {
                return;
            }
            if (url.equals(running)) {
                if (url.equals(searching)) {
                    callFindSubs(view);
                } else {
                    running = null;
                    h.sendEmptyMessageDelayed(20, 1500);
                }
            } else if (regRunning != null && url.matches(regRunning)) {
                Message msg = new Message();
                msg.obj = url;
                msg.what = 30;
                h.sendMessageDelayed(msg, 1500);
            }
        }
    };

    protected abstract String downSubsScript();

    protected abstract String findSubsScript();

    protected abstract String createSearch(String key) throws Exception;

    protected void onMatchRegRunning(WebView view, String url) {

    }

    private void callFindSubs(WebView view) {
        view.evaluateJavascript(findSubsScript(), null);
//        script = "subs.onSubsData(findSubs());";
//        view.evaluateJavascript(script, null);
        //web.loadUrl("http://www.baidu.com");
    }

    @JavascriptInterface
    public void onSubsData(String value) {
        if (callback == null) {
            return;
        }
        try {
            JSONArray ls = new JSONArray(value);
            NormalizedLevenshtein l = new NormalizedLevenshtein();
            for (int i = 0; i < ls.length(); i++) {
                JSONObject obj = ls.getJSONObject(i);
                Map<String, Object> sub = new HashMap<>();
                String text = obj.getString("text");
                sub.put("text", text);
                sub.put("href", obj.getString("href"));
                sub.put("distance", l.distance(key, text));
                slinks.add(sub);
            }
            Collections.sort(slinks, new DistanceCompartor());
            err = null;
        } catch (Exception e) {
            err = e;
        }
        h.sendEmptyMessageDelayed(10, 1500);
    }

//    @JavascriptInterface
//    public void onDownUrl(String url) {
//        if (this.downing != null) {
//            return;
//        }
//        this.downing = url;
//        OkHttpClient client = new OkHttpClient();
//        Request request = new Request.Builder().url(url).build();
//        client.newCall(request).enqueue(down);
//    }

    public void onSubsDone() {
        if (this.callback == null) {
            return;
        }
        if (err != null) {
            this.callback.onReceiveValue(null);
            return;
        }
        if (this.slinks.isEmpty()) {
            this.callback.onReceiveValue(null);
            return;
        }
        this.slinkIdx = 0;
        String url = this.slinks.get(this.slinkIdx).get("href").toString();
        this.running = url;
        this.evaluateJavascript("window.location.href='" + url + "'", null);
    }

    private WebChromeClient wcc = new WebChromeClient() {

    };

    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        try {
            h.sendEmptyMessage(100);
            if (this.downing != null) {
                return;
            }
            this.downing = url;
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            client.newCall(request).enqueue(down);
        } catch (Exception e) {
            this.err = e;
        }
    }

    private static String getHeaderFileName(Response response) {
        String dispositionHeader = response.header("Content-Disposition");
        if (!TextUtils.isEmpty(dispositionHeader)) {
            dispositionHeader.replace("attachment;filename=", "");
            dispositionHeader.replace("filename*=utf-8", "");
            String[] strings = dispositionHeader.split("; ");
            if (strings.length > 1) {
                dispositionHeader = strings[1].replace("filename=", "");
                dispositionHeader = dispositionHeader.replace("\"", "");
                return dispositionHeader;
            }
            return "";
        }
        return "";
    }

    private Callback down = new Callback() {
        public void onFailure(Call call, IOException e) {
            err = e;
            callback.onReceiveValue(null);
        }

        public void onResponse(Call call, Response response) throws IOException {
            File temp = new File(ws, "tmp");
            File file = null;
            FileOutputStream fos = null;
            List<String> subs = null;
            try {
                if (!temp.exists()) {
                    temp.mkdirs();
                }
                String filename = getHeaderFileName(response);
                if (filename == null || filename.isEmpty()) {
                    String path = response.request().url().url().getPath();
                    String[] ps = path.split("/");
                    filename = "tmp_" + ps[ps.length - 1];
                }
                file = new File(temp, filename);
                int len;
                byte[] buf = new byte[2048];
                InputStream inputStream = response.body().byteStream();
                fos = new FileOutputStream(file);
                while ((len = inputStream.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                }
                fos.flush();
                fos.close();
                fos = null;
                inputStream.close();
                //
                File exout = new File(ws);
                err = autoDecompress(having, file, exout);
                subs = listSubs(exout);
                storeHaving(ws, having);
            } catch (Exception e) {
                if (fos != null) {
                    fos.close();
                }
                err = e;
            }
            if (file != null) {
                file.delete();
            }
            callback.onReceiveValue(subs);
        }
    };

    public static Set<String> SUB_EXTS = new HashSet<String>() {
        {
            add(".ssa");
            add(".ass");
            add(".smi");
            add(".srt");
            add(".sub");
            add(".lrc");
            add(".sst");
            add(".xss");
            add(".psb");
            add(".ssb");
        }
    };

    public static Exception decompressACC(Map<String, String> set, File src, File out) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(src);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ArchiveInputStream input = new ArchiveStreamFactory().createArchiveInputStream(bis);
            ArchiveEntry entry;
            while ((entry = input.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String name = new File(entry.getName()).getName();
                int idx = name.lastIndexOf(".");
                if (idx < 0) {
                    continue;
                }
                String ext = name.substring(idx);
                if (!SUB_EXTS.contains(ext.toLowerCase())) {
                    continue;
                }
                String oname = name.substring(0, idx);
                File outfile = new File(out, name);
                int i = 0;
                while (outfile.exists()) {
                    i++;
                    name = oname + "_" + i + ext;
                    outfile = new File(out, name);
                }

                fos = new FileOutputStream(outfile);
                Sha1OutputStream hos = new Sha1OutputStream(fos);
                byte[] content = new byte[(int) entry.getSize()];
                input.read(content, 0, content.length);
                hos.write(content);
                fos.close();
                fos = null;
                String sha1 = hos.sha1();
                if (set.containsKey(sha1) && new File(out, set.get(sha1)).exists()) {
                    outfile.delete();
                } else {
                    set.put(sha1, outfile.getName());
                }
            }
            input.close();
            return null;
        } catch (Exception e) {
            return e;
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {

            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {

            }
        }
    }

    public static Exception decompressRAR(Map<String, String> set, File src, File out) {
        Archive arch = null;
        FileOutputStream fos = null;
        try {
            arch = new Archive(src);
            FileHeader fh = null;
            while (true) {
                fh = arch.nextFileHeader();
                if (fh == null) {
                    break;
                }
                if (fh.isDirectory()) {
                    continue;
                }
                String name = new File(fh.getFileNameString()).getName();
                int idx = name.lastIndexOf(".");
                if (idx < 0) {
                    continue;
                }
                String ext = name.substring(idx);
                if (!SUB_EXTS.contains(ext.toLowerCase())) {
                    continue;
                }
                String oname = name.substring(0, idx);
                File outfile = new File(out, name);
                int i = 0;
                while (outfile.exists()) {
                    i++;
                    name = oname + "_" + i + ext;
                    outfile = new File(out, name);
                }
                fos = new FileOutputStream(outfile);
                Sha1OutputStream hos = new Sha1OutputStream(fos);
                arch.extractFile(fh, hos);
                fos.close();
                fos = null;
                String sha1 = hos.sha1();
                if (set.containsKey(sha1) && new File(out, set.get(sha1)).exists()) {
                    outfile.delete();
                } else {
                    set.put(sha1, outfile.getName());
                }
            }
            return null;
        } catch (Exception e) {
            return e;
        } finally {
            try {
                if (arch != null) {
                    arch.close();
                }
            } catch (Exception e) {

            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {

            }
        }
    }


    public static Exception autoDecompress(Map<String, String> set, File src, File out) {
        String[] parts = src.getName().split("\\.");
        String ext = parts.length < 2 ? "" : parts[parts.length - 1];
        Exception err;
        if ("rar".equals(ext.toLowerCase())) {
            err = decompressRAR(set, src, out);
            if (err != null) {
                err = decompressACC(set, src, out);
            }
        } else {
            err = decompressACC(set, src, out);
            if (err != null) {
                err = decompressRAR(set, src, out);
            }
        }
        return err;
    }


    public void search(Map<String, String> having, String ws, String key, ValueCallback<List<String>> callback) {
        try {
            this.err = null;
            this.ws = ws;
            this.key = key;
            this.callback = callback;
            this.searching = this.createSearch(this.key);
            this.running = searching;
            this.slinks = new ArrayList<>();
            this.searching = this.searching.trim();
            this.having = having;
            this.downing = null;
            this.loadUrl(this.searching);
        } catch (Exception e) {
            this.err = e;
            callback.onReceiveValue(null);
        }
    }

    public void more(ValueCallback<List<String>> callback) {
        if (this.slinks.size() <= this.slinkIdx + 1) {
            callback.onReceiveValue(null);
            return;
        }
        this.slinkIdx++;
        String url = this.slinks.get(this.slinkIdx).get("href").toString();
        this.running = url;
        this.evaluateJavascript("window.location.href='" + url + "'", null);
    }

    public static List<String> listSubs(File out) {
        List<String> subs = new ArrayList<>();
        Queue<File> folders = new LinkedBlockingQueue<>();
        folders.add(out);
        while (folders.size() > 0) {
            File base = folders.remove();
            File[] childs = base.listFiles();
            for (int i = 0; i < childs.length; i++) {
                File f = childs[i];
                if (f.isDirectory()) {
                    folders.add(f);
                    continue;
                }
                if (f.getName().matches("(?i)^.*\\.(srt|ssa|ass|sup)$")) {
                    subs.add(f.getAbsolutePath());
                }
            }
        }
        return subs;
    }

    static class DistanceCompartor implements Comparator<Map<String, Object>> {

        @Override
        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
            Double d1 = (Double) o1.get("distance");
            Double d2 = (Double) o2.get("distance");
            return d1.compareTo(d2);
        }

        @Override
        public boolean equals(Object obj) {
            return false;
        }
    }
}
