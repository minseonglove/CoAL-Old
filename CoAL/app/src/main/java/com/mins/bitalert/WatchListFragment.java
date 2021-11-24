package com.mins.bitalert;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class WatchListFragment extends Fragment {
    private ListView listView;
    private WatchListAdapter adapter;
    Intent serviceIntent;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.watch_list, container, false);

        ImageButton btn_go_search = rootView.findViewById(R.id.go_search_btn);
        Button btn_go_setting = rootView.findViewById(R.id.go_setting_btn);
        adapter = new WatchListAdapter();
        adapter.addActivity((MainActivity)getActivity());
        listView = (ListView) rootView.findViewById(R.id.listView);
        if(WatchService.serviceIntent==null){ // 감시 실행
            serviceIntent = new Intent(getContext(), WatchService.class);
            getContext().startService(serviceIntent);
        }
        else{ // 이미 실행 중 일때
            serviceIntent = WatchService.serviceIntent;
        }
        try {
            listView.setAdapter(adapter);
            makeList();
        }
        catch(Exception e) {
            Toast.makeText(getContext(),"인텐트없음",Toast.LENGTH_SHORT).show();
        }

        btn_go_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                activity.onFragmentChanged(1);
            }
        });

        btn_go_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                activity.onFragmentChanged(0);
            }
        });
        return rootView;
    }

    private void makeList(){
        BufferedReader br = null;
        String name = "";
        String indicator = "";
        boolean onoff;
        String str = null;
        int type = -1;
        try {
            br = new BufferedReader(new FileReader(getContext().getFilesDir()+"current_alert.txt"));
            while(((str = br.readLine()) != null)){
                String[] split1 = str.split("\\+"); // 앞의 숫자를 땐다
                String[] result_str = split1[1].split(","); // 설정했던 결과를 가져온다
                String opt1 = ""; // 상승하락
                String opt2 = ""; // 골든데드
                name = result_str[0] + " " + result_str[1] + "분"; // 이름, 분캔들
                type = Integer.parseInt(result_str[2]);

                if(type == 0){
                    if(result_str[4].equals("0")) // pon
                        opt1 = "상승돌파";
                    else
                        opt1 = "하락돌파";
                }
                else if(type == 1){
                    if(result_str[6].equals("0")) // 이평선
                        opt1 = "상승돌파";
                    else
                        opt1 = "하락돌파";
                }
                else if(type == 2){
                    if(result_str[9].equals("0")) // rsi
                        opt1 = "상승돌파";
                    else
                        opt1 = "하락돌파";
                    if(result_str[11].equals("1"))
                        opt2 = "골든크로스";
                    else if(result_str[11].equals("2"))
                        opt2 = "데드크로스";
                }
                else if(type == 3){
                    if(result_str[16].equals("0")) // stoch
                        opt1 = "상승돌파";
                    else
                        opt1 = "하락돌파";
                    if(result_str[17].equals("1"))
                        opt2 = "골든크로스";
                    else if(result_str[17].equals("2"))
                        opt2 = "데드크로스";
                }
                else if(type == 4){
                    if(result_str[21].equals("0")) // macd
                        opt1 = "상승돌파";
                    else
                        opt1 = "하락돌파";
                    if(result_str[21].equals("1"))
                        opt2 = "골든크로스";
                    else if(result_str[21].equals("2"))
                        opt2 = "데드크로스";
                }

                switch (type){
                    case 0: //가격
                        indicator = "가격 " + result_str[3] + " " + opt1;
                        break;
                    case 1: // 이평선
                        indicator = "이동평균선 " + result_str[5] + "일선 " + opt1;
                        break;
                    case 2: // RSI
                        indicator = "RSI (" + result_str[7] + ") " + result_str[8] + "% " + opt1;
                        if(!result_str[11].equals("0"))
                            indicator = indicator + " 시그널 " + result_str[10] + "일선 " + opt2;
                        break;
                    case 3: // stoch
                        indicator = "Stochastic Slow (" + result_str[12] + "," + result_str[13] + "," + result_str[14] + ") "
                                + result_str[15] + " " + opt1;
                        if(!result_str[17].equals("0"))
                            indicator = indicator + " %K,%D " + opt2;
                        break;
                    case 4: // macd 반드시 추가해야됨
                        indicator = "MACD (" + result_str[18] + "," + result_str[19] + ") " + result_str[20] + " " + opt1;
                        if(!result_str[23].equals("0"))
                            indicator = indicator + " 시그널 " + result_str[22] + "일선 " + opt2;
                }
                onoff = result_str[24].equals("on");
                adapter.addItem(name,indicator, onoff);
                adapter.notifyDataSetChanged(); // 변경 저장
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(getContext(),"저장된 파일 없음",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IOException e) {
            try {
                Toast.makeText(getContext(), "파일 읽기 실패", Toast.LENGTH_SHORT).show();
                BufferedWriter bw = new BufferedWriter(new FileWriter(SplashActivity.myPath + "current_alert.txt", true));
                bw.close();
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.onFragmentRefresh();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            e.printStackTrace();
        }
    }
}
