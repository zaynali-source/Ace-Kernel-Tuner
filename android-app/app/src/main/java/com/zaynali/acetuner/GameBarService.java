package com.zaynali.acetuner;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class GameBarService extends Service {
    private static final String CHANNEL="ace_gamebar";
    private WindowManager wm; private TextView view; private Handler handler; private boolean attached=false;
    private final Runnable poll = new Runnable() { @Override public void run() { updateMetrics(); handler.postDelayed(this, 2500); } };
    @Override public void onCreate() {
        super.onCreate(); handler=new Handler(Looper.getMainLooper()); createNotification(); wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        view=new TextView(this); view.setTextColor(Color.WHITE); view.setTextSize(12); view.setPadding(14,10,14,10); view.setBackgroundColor(0xDD10141A); view.setText("ACE GAME BAR\nStarting monitor…");
        int type=Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;
        WindowManager.LayoutParams lp=new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
        lp.gravity=Gravity.TOP|Gravity.END; lp.x=12; lp.y=130; try { wm.addView(view,lp); attached=true; } catch(Exception ignored) {}
        handler.post(poll);
    }
    private void createNotification() {
        if(Build.VERSION.SDK_INT>=26){NotificationManager nm=getSystemService(NotificationManager.class);nm.createNotificationChannel(new NotificationChannel(CHANNEL,"Ace Game Bar",NotificationManager.IMPORTANCE_LOW));}
        Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,CHANNEL):new Notification.Builder(this);
        startForeground(41,b.setContentTitle("Ace Game Bar").setContentText("Live performance monitor running").setSmallIcon(android.R.drawable.ic_menu_info_details).build());
    }
    private void updateMetrics() { new Thread(() -> {
        String s="ACE GAME BAR\nFPS: device interface dependent\n"+read("for p in /sys/devices/system/cpu/cpufreq/policy*; do [ -r $p/scaling_cur_freq ] && echo ${p##*/} $(($(cat $p/scaling_cur_freq)/1000))/$(($(cat $p/scaling_max_freq)/1000)) MHz; done")+"\nBattery temp: "+read("cat /sys/class/power_supply/battery/temp 2>/dev/null")+"\nRAM free: "+read("grep MemAvailable /proc/meminfo | awk '{print $2 \" kB\"}'");
        handler.post(() -> {if(view!=null)view.setText(s);});
    }).start(); }
    private String read(String cmd) { try { Process p=new ProcessBuilder("/system/bin/su","-c",cmd).redirectErrorStream(true).start(); BufferedReader r=new BufferedReader(new InputStreamReader(p.getInputStream()));StringBuilder b=new StringBuilder();String l;while((l=r.readLine())!=null)b.append(l).append('\n');p.waitFor();return b.toString().trim(); } catch(Exception e){return "N/A";} }
    @Override public int onStartCommand(Intent i,int f,int id){return START_STICKY;}
    @Override public void onDestroy(){if(handler!=null)handler.removeCallbacks(poll);if(attached&&wm!=null&&view!=null)try{wm.removeView(view);}catch(Exception ignored){};view=null;super.onDestroy();}
    @Override public IBinder onBind(Intent intent){return null;}
}
