package com.mins.bitalert;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentTransaction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class WatchListAdapter extends BaseAdapter {
    // Declare Variable
    private ArrayList<ViewHolder> arrayList;
    private MainActivity mainActivity;

    public WatchListAdapter(){
        arrayList = new ArrayList<ViewHolder>();
    }

    public class ViewHolder {
        String tv_name;
        String tv_indicator;
        boolean on_off;
    }

    @Override
    public int getCount() { return arrayList.size(); }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) { // 뷰가져오기
        final Context context = parent.getContext();
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.watch_list_item, null);
        }

        TextView name = (TextView) view.findViewById(R.id.Name);
        TextView indicator = (TextView) view.findViewById(R.id.Indicator);
        Switch switchButton = (Switch) view.findViewById(R.id.switch_button);
        ImageButton deleteButton = (ImageButton) view.findViewById(R.id.delete_button);
        switchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(!MainActivity.is_editing) {
                MainActivity.is_editing = true;
                edit_alert(position + 1, isChecked);
                MainActivity.is_editing = false;
            }
            else{
                Toast.makeText(context, "알람설정을 변경 중 입니다.", Toast.LENGTH_SHORT).show();
                switchButton.setChecked(!isChecked);
            }
        });
        switchButton.setOnLongClickListener(v -> {
            if(switchButton.getVisibility() == View.VISIBLE) {
                // 스위치 사라지고 x 버튼 팝업
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.xscale);
                Animation animation2 = AnimationUtils.loadAnimation(context, R.anim.delete_popup);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) { }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        switchButton.setVisibility(View.GONE);
                        deleteButton.setVisibility(View.VISIBLE);
                        deleteButton.startAnimation(animation2);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) { }
                });
                switchButton.startAnimation(animation);
            }
            return true;
        });
        deleteButton.setOnClickListener(v -> {
            DeleteAlarmDialog dialog = new DeleteAlarmDialog(v.getContext(), mainActivity);
            dialog.CallFunction(position+1);
            //  switchButton.setVisibility(View.VISIBLE);
            //   deleteButton.setVisibility(View.GONE);
        });
        deleteButton.setOnLongClickListener(v -> {
            if(switchButton.getVisibility() == View.GONE){
                // x 버튼 사라지고 스위치 팝업
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.delete_shrink);
                Animation animation2 = AnimationUtils.loadAnimation(context, R.anim.back_scale);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) { }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        switchButton.setVisibility(View.VISIBLE);
                        deleteButton.setVisibility(View.GONE);
                        switchButton.startAnimation(animation2);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) { }
                });
                deleteButton.startAnimation(animation);
            }
            return true;
        });
        LinearLayout item_container = (LinearLayout) view.findViewById(R.id.item_container);
        item_container.setOnLongClickListener(v -> {
            if(switchButton.getVisibility() == View.VISIBLE) {
                // 스위치 사라지고 x 버튼 팝업
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.xscale);
                Animation animation2 = AnimationUtils.loadAnimation(context, R.anim.delete_popup);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) { }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        switchButton.setVisibility(View.GONE);
                        deleteButton.setVisibility(View.VISIBLE);
                        deleteButton.startAnimation(animation2);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) { }
                });
                switchButton.startAnimation(animation);
            }
            if(switchButton.getVisibility() == View.GONE){
                // x 버튼 사라지고 스위치 팝업
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.delete_shrink);
                Animation animation2 = AnimationUtils.loadAnimation(context, R.anim.back_scale);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) { }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        switchButton.setVisibility(View.VISIBLE);
                        deleteButton.setVisibility(View.GONE);
                        switchButton.startAnimation(animation2);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) { }
                });
                deleteButton.startAnimation(animation);
            }
            return true;
        });
        item_container.setOnClickListener(v -> {
            if(switchButton.getVisibility() == View.GONE){
                // x 버튼 사라지고 스위치 팝업
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.delete_shrink);
                Animation animation2 = AnimationUtils.loadAnimation(context, R.anim.back_scale);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) { }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        switchButton.setVisibility(View.VISIBLE);
                        deleteButton.setVisibility(View.GONE);
                        switchButton.startAnimation(animation2);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) { }
                });
                deleteButton.startAnimation(animation);
            }
        });

        ViewHolder holder = arrayList.get(position);

        name.setText(holder.tv_name);
        indicator.setText(holder.tv_indicator);
        switchButton.setChecked(holder.on_off);

        Shader shader = new LinearGradient(0,0,0, name.getLineHeight(),
                Color.parseColor("#80000000"), Color.parseColor("#FF000000"), Shader.TileMode.REPEAT);
        name.getPaint().setShader(shader);
        shader = new LinearGradient(0,0,0, indicator.getLineHeight(),
                Color.parseColor("#80196065"), Color.parseColor("#FF196065"), Shader.TileMode.REPEAT);
        indicator.getPaint().setShader(shader);
        return view;
    }

    public void addItem(String name, String indicator, boolean on_off){
        ViewHolder item = new ViewHolder();
        item.tv_name = name;
        item.tv_indicator = indicator;
        item.on_off = on_off;

        arrayList.add(item);
    }

    public void addActivity(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    private void edit_alert(int num, boolean flag){
        int count = 0;
        String str = "";
        String text = "";
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            br = new BufferedReader(new FileReader(SplashActivity.myPath+"current_alert.txt"));
        } catch (FileNotFoundException e) {
            Log.d("debug","WatchThread 파일읽기 오류");
        }
        try {
            while (((str = br.readLine()) != null)) {
                count++;
                if(count == num) {
                    if(flag)
                        str = str.replace("off", "on");
                    else
                        str = str.replace("on", "off");
                    WatchThread.run_flag[num] = flag;
                }
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
        } catch (IOException e){ }
        Log.d("debug", num + "알람 설정 변화 " + flag);
    }
}
