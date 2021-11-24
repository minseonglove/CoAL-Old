package com.mins.bitalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("debug","AlarmReceiver실행");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){ // 오레오 이후버전
            Intent in = new Intent(context, RestartService.class); // 바로실행
            context.startForegroundService(in); // 죽지않는 서비스인 포그라운드 서비스로 실행
        }
        else{ // 아니면
            Intent in = new Intent(context, WatchService.class); // 바로실행
            context.startService(in);
        }
    }
}