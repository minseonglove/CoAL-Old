package com.mins.bitalert;

import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.ParseException;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WatchService extends Service {
    private Thread mainThread;
    public static Intent serviceIntent = null;
    public static boolean mumcha_flag = false; // 멈춰!가 true면 앱이 종료됐을때 부활하지 않는다
    public static List<WatchThread> wt = new ArrayList<>();
    public WatchService(){ }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate(){
        super.onCreate();
        startForegroundService("감시 중");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        serviceIntent = intent;
        for(int i = 0; i < wt.size(); i++)
            wt.get(i).interrupt();
        wt.clear();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        BufferedReader br = null;
        String str = null;
        String app_Path = getFilesDir().toString();
        int count = 0; // 알람 갯수를 샌다
        try {
            br = new BufferedReader(new FileReader(app_Path+"current_alert.txt"));
            while (((str = br.readLine()) != null)) {
                count++;
            }
            br.close();
        } catch (FileNotFoundException e) {
            Log.d("debug","WatchService : 1.텍스트 파일 없음");
        }
        catch (IOException e){
            Log.d("debug","WatchService : 1.텍스트 파일 IO");
        }
        // 설정된 알람이 1개 이상이면 감시실행
        if(count != 0) {
            Log.d("debug","현재 count : " + count);
            count = 0; // 초기화
            try {
                br = new BufferedReader(new FileReader(app_Path + "current_alert.txt"));
            } catch (FileNotFoundException e) {
                Log.d("debug","읽기 실패 2");
            }
            try {
                while (((str = br.readLine()) != null)) {
                    Log.d("debug", "알람내용 : " + str);
                    String[] split1 = str.split("\\+"); // 앞의 숫자를 땐다
                    String[] result_str = split1[1].split(","); // 설정했던 결과를 가져온다
                    wt.add(new WatchThread(getApplicationContext(), app_Path, count+1, result_str)); // 객채생성
                    wt.get(count).start(); // 감시시작
                    Log.d("debug",(count+1) + "번 알람 감시시작");
                    count++;
                }
                br.close();
            } catch (IOException e) {
                showToast(getApplication(), "WatchService : 2.파일 IO에러");
            } catch (ParseException e) {
                showToast(getApplication(), "WatchService : 파스 오류");
            }
        }
        else{
            Log.d("debug","현재 count : " + count);
        }

        //앱을 날려버리면 이곳에서 인터럽트가 걸려서 재시작
        mainThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean run = true;
                while (run) {
                    try {
                        Thread.sleep(5000); // 1 minute
                    } catch (InterruptedException e) {
                        run = false;
                        Log.d("debug","WatchService 인터럽트");
                        e.printStackTrace();
                    }
                }
            }
        });
        mainThread.start();

        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d("debug","onDestroy : " + mumcha_flag);
        serviceIntent = null;
        if(!mumcha_flag)
            setAlarmTimer(); // 1초 뒤에 알람리시버 실행
        else
            showToast(getApplication(), "Stop Undead Mode");
        Thread.currentThread().interrupt();
        mainThread.interrupt();
        mainThread =null;
    }

    @Override
    public void onTaskRemoved (Intent rootIntent){
        super.onTaskRemoved(rootIntent);
        serviceIntent = null;
        Log.d("debug","onTaskRemoved : " + mumcha_flag);
        if(!mumcha_flag) {
            Log.d("debug","AlarmTimer호출");
            setAlarmTimer(); // 1초 뒤에 알람리시버 실행
        }
        else
            showToast(getApplication(), "Stop Undead Mode");
        Thread.currentThread().interrupt();
        mainThread.interrupt();
        mainThread =null;
    }

    protected void setAlarmTimer() {
        final Calendar c = Calendar.getInstance();
        //1970년 1월 1일 부터 경과한시간의 밀리세컨을 리턴합니다
        c.setTimeInMillis(System.currentTimeMillis());
        c.add(Calendar.SECOND, 3); // 3초뒤로 설정
        Intent intent = new Intent(this, AlarmReceiver.class);
        //펜딩인텐트는 AlarmReceiver에게 작업을 요청하는 이벤트를 사전에 생성시킨다
        //1초 뒤에 실행하기 위해 이런 작업을 하는거 같다
        PendingIntent sender = PendingIntent.getBroadcast(this, 0,intent,0);
        AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender); // 1초뒤에 알람을 보냅니다
    }

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = "fcm_default_channel";//getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_logo)//drawable.splash)
                        .setContentTitle("Service test")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,"Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
    private void startForegroundService(String messageBody){
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "service_channel";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "smart_channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .createNotificationChannel(channel);

            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(this);
        }
        builder.setSmallIcon(R.mipmap.ic_logo)//drawable.splash)
                .setContentTitle(null)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        startForeground(1, builder.build());
    }
    public void showToast(final Application application, final String msg) {
        Handler h = new Handler(application.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(application, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
