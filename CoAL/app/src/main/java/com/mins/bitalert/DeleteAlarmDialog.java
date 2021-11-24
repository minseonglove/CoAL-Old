package com.mins.bitalert;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DeleteAlarmDialog{
    private final Context context;
    private MainActivity mainActivity;

    public DeleteAlarmDialog(Context context, MainActivity mainActivity){
        this.context = context;
        this.mainActivity = mainActivity;
    }

    public void CallFunction(int num){
        final Dialog dig = new Dialog(context);
        dig.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dig.setContentView(R.layout.delete_alarm_dialog);
        if(dig.getWindow() != null){
            dig.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dig.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dig.setCancelable(false);
        }
        dig.show();

        ImageButton btn_yes = (ImageButton) dig.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete_alert(num);
                mainActivity.onFragmentRefresh();
                Log.d("debug", "알람 삭제");
                dig.dismiss();
            }
        });
        ImageButton btn_no = (ImageButton) dig.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dig.dismiss();
            }
        });
    }

    //알람 삭제
    private void delete_alert(int num){
        int count = 0;
        String str = "";
        String text = "";
        BufferedReader br = null;
        BufferedWriter bw = null;
        WatchThread.run_flag[num] = false;
        try {
            br = new BufferedReader(new FileReader(SplashActivity.myPath+"current_alert.txt"));
        } catch (FileNotFoundException e) {
            Log.d("debug","WatchThread 파일읽기 오류");
        }
        try {
            while (((str = br.readLine()) != null)) {
                String[] split = str.split("\\+");
                Log.d("debug",  split[0] + " " + num);
                if(Integer.parseInt(split[0]) == num)
                    continue;
                count++;
                str = count + "+" + split[1];
                text = text + str + "\n";
            }
            br.close();
        }
        catch (IOException e){ }
        try {
            bw = new BufferedWriter(new FileWriter(SplashActivity.myPath+"current_alert.txt", false));
        } catch (IOException e) { }
        try {
            bw.write(text);
            bw.close();
        }
        catch (IOException e){ }
    }

}
