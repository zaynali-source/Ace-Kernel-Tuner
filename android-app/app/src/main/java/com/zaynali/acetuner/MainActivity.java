package com.zaynali.acetuner;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private LinearLayout root, nav, content;
    private WebView webView;
    private TextView status;
    private final int bg = Color.rgb(16,20,26), panel = Color.rgb(26,33,43), accent = Color.rgb(255,180,84), text = Color.rgb(232,237,243);

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bg);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(bg);
        root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));

        // Persistent bottom navigation: it remains visible in both app sections.
        nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(8, 6, 8, 6);
        nav.setBackgroundColor(panel);
        nav.setElevation(10f);
        addTab("Tuner WebUI", v -> showTuner());
        addTab("Game Bar", v -> showGameBar());
        root.addView(nav, new LinearLayout.LayoutParams(-1, 60));

        setContentView(root);
        showTuner();
    }

    private void addTab(String label, View.OnClickListener click) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(text);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(panel));
        b.setOnClickListener(click);
        nav.addView(b, new LinearLayout.LayoutParams(0, -1, 1));
    }

    private void clearContent() {
        content.removeAllViews();
        webView = null;
        status = null;
    }

    private void showTuner() {
        clearContent();
        WebView wv = new WebView(this);
        webView = wv;
        wv.setBackgroundColor(bg);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setDomStorageEnabled(true);
        wv.setWebChromeClient(new WebChromeClient());
        wv.setWebViewClient(new WebViewClient());
        wv.addJavascriptInterface(new RootBridge(), "ksu");
        content.addView(wv, new LinearLayout.LayoutParams(-1, -1));
        wv.loadUrl("file:///android_asset/index.html");
    }

    private void showGameBar() {
        clearContent();
        ScrollView sc = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setPadding(20, 20, 20, 24);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(bg);

        TextView title = new TextView(this);
        title.setText("ACE GAME BAR");
        title.setTextSize(22);
        title.setTextColor(accent);
        title.setPadding(0, 0, 0, 10);
        box.addView(title);

        TextView desc = new TextView(this);
        desc.setText("Floating monitor for POCO F6 / Snapdragon 8s Gen 3. This Game Bar is part of the APK, not the KernelSU module.");
        desc.setTextColor(text);
        desc.setTextSize(14);
        box.addView(desc);

        status = new TextView(this);
        status.setText("Checking root access and device metrics…");
        status.setTextColor(text);
        status.setTextSize(15);
        status.setPadding(0, 22, 0, 22);
        box.addView(status);

        Button refresh = new Button(this);
        refresh.setText("Refresh status");
        refresh.setAllCaps(false);
        refresh.setOnClickListener(v -> readStatus());
        box.addView(refresh);

        Button start = new Button(this);
        start.setText("Start floating Game Bar");
        start.setAllCaps(false);
        start.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivity(i);
                Toast.makeText(this, "Allow display over other apps, then return and tap Start again", Toast.LENGTH_LONG).show();
                return;
            }
            startForegroundService(new Intent(this, GameBarService.class));
            Toast.makeText(this, "Game Bar start requested", Toast.LENGTH_SHORT).show();
        });
        box.addView(start);

        Button stop = new Button(this);
        stop.setText("Stop floating Game Bar");
        stop.setAllCaps(false);
        stop.setOnClickListener(v -> {
            stopService(new Intent(this, GameBarService.class));
            Toast.makeText(this, "Game Bar stopped", Toast.LENGTH_SHORT).show();
        });
        box.addView(stop);

        TextView note = new TextView(this);
        note.setText("The companion app uses the installed module for tuning. FPS/GPU readings are device-interface dependent and are not guaranteed in this build.");
        note.setTextColor(Color.LTGRAY);
        note.setPadding(0, 20, 0, 0);
        box.addView(note);

        sc.addView(box);
        content.addView(sc, new LinearLayout.LayoutParams(-1, -1));
        readStatus();
    }

    private void readStatus() {
        if (status == null) return;
        status.setText("Reading device metrics…");
        executor.execute(() -> {
            String cmd = "echo 'MODULE='$(test -x /data/adb/modules/cpufreq_max_cap/cfg.sh && echo present || echo missing); " +
                "for p in /sys/devices/system/cpu/cpufreq/policy*; do [ -r $p/scaling_cur_freq ] && echo ${p##*/}:$(cat $p/scaling_cur_freq):$(cat $p/scaling_max_freq):$(cat $p/scaling_governor); done; " +
                "echo BATT_TEMP=$(cat /sys/class/power_supply/battery/temp 2>/dev/null); echo RAM=$(grep MemAvailable /proc/meminfo | awk '{print $2}')";
            String result = runRoot(cmd);
            runOnUiThread(() -> {
                if (status != null) status.setText(result.isEmpty()
                    ? "No output. Grant root in KernelSU and check device interfaces."
                    : result.replace("MODULE=", "Module: ").replace("BATT_TEMP=", "Battery temperature (raw): ").replace("RAM=", "Available RAM (kB): "));
            });
        });
    }

    private String runRoot(String command) {
        StringBuilder out = new StringBuilder();
        try {
            Process p = new ProcessBuilder("/system/bin/su", "-c", command).redirectErrorStream(true).start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = r.readLine()) != null) out.append(line).append('\n');
            p.waitFor();
        } catch (Exception e) {
            return "Root command failed: " + e.getClass().getSimpleName() + ". Check KernelSU root permission.";
        }
        return out.toString().trim();
    }

    private class RootBridge {
        @JavascriptInterface
        public void exec(String command, String options, String callbackName) {
            executor.execute(() -> {
                String result = runRoot(command);
                String js = "window[" + JSONObjectQuote.quote(callbackName) + "] && window[" + JSONObjectQuote.quote(callbackName) + "](0," + JSONObjectQuote.quote(result) + ");";
                runOnUiThread(() -> {
                    WebView current = webView;
                    if (current != null) current.evaluateJavascript(js, null);
                });
            });
        }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}