package io.snows.sub_searcher;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.ValueCallback;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import io.snows.subs.SubSearcher;
import io.snows.subs.SubSearcherView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onTest(View view) {
        try {
//            List<String> xsubs= SubSearcher.listSubs(new File("/data/data/io.snows.sub_searcher/cache/subs/tmp_149750909615638.zip_"));
//            if(true){
//                return;
//            }
            SubSearcher subs = (SubSearcher) this.findViewById(R.id.subs);
            //web.loadUrl("http://wwww.baidu.com");
            String ws = this.getCacheDir().getAbsolutePath() + "/subs";
//            SubSearcherView.autoDecompress(new HashMap<String, String>(), new File(ws + "/tmp/[zmk.tw]The.Crossing.S01E01.720p.WEB.H264-DEFLATE.chs.eng.rar"), new File(ws));
//            if (true) {
//                return;
//            }
//            SubSearcher subs = new SubhdSearcher(web, ws);
//            SubSearcher subs = new AssrtSearcher(web, ws);
//            web.loadUrl("http://wwww.baidu.com");
//            //ViewGroup vv=(ViewGroup)getWindow().getDecorView().getRootView();
//            //vv.addView(subs.getWeb());
            subs.search(null, ws, "the.crossing.s01e02", new ValueCallback<List<String>>() {
                @Override
                public void onReceiveValue(List<String> value) {
                    System.out.println(value);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
