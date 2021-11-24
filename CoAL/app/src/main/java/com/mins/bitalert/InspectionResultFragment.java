package com.mins.bitalert;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InspectionResultFragment extends Fragment {
    ListView listView;

    TextView search_detail;
    TextView search_number;

    private int candle; // 캔들
    private int indicator; // 지표
    private Double price; // 가격
    private int price_updown; // 값 업다운
    private int ma_n; // 이평선 n
    private int ma_updown; // 이평선 업다운
    private int rsi_n; // rsi n
    private Double c_rsi_value; // rsi value
    private int rsi_updown; // rsi 업다운
    private int rsi_signal; // rsi signal
    private int rsi_signal_updown; // rsi signal 업다운
    private int stoch_n; // stoch n
    private int stoch_k; // stoch %k
    private int stoch_d; // stoch %d
    private Double c_stoch_value; // stoch value
    private int stoch_updown; // stoch n
    private int stoch_signal_updown; // stoch signal 업다운
    private int macd_n; // macd n
    private int macd_m; // macd m
    private Double c_macd_value; // macd value
    private int macd_updown; // macd updown
    private int macd_signal; // macd signal
    private int macd_signal_updown; // macd signal 업다운
    private Double[] rsi_signal_arr = new Double[101]; // 시그널 계산
    private Double[] macd_signal_arr = new Double[101]; // 시그널 계산

    private boolean passflag = false; // 조건에 맞으면 on
    InspectionResultAdapter adapter;
    String tmp;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.inspection_result, container, false);
        ImageButton go_back_btn = rootView.findViewById(R.id.go_back_btn);
        listView = rootView.findViewById(R.id.listView2);
        search_detail = rootView.findViewById(R.id.search_detail);
        search_number = rootView.findViewById(R.id.search_number);
        List<String> list = new ArrayList<>();
        int len = MainActivity.coin_code.size();
        adapter = new InspectionResultAdapter(getContext(), list);
        listView.setAdapter(adapter);

        Thread mainThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mapping_value(CoinInspectionFragment.result_str);
                for(int i = 0; i < len; i++){
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        Log.d("debug","검사 강제종료");
                        break;
                    }

                    setSearch_number( (int)((i+1)/(double)len*100)+"% ( "+ len + " / " + (i+1) + " )");
                    try{
                        switch (indicator) {
                            case 0: //가격
                                watch_price(MainActivity.coin_code.get(i));
                                break;
                            case 1: //이평선
                                watch_ma(MainActivity.coin_code.get(i));
                                break;
                            case 2: //rsi
                                watch_rsi(MainActivity.coin_code.get(i));
                                break;
                            case 3: //스토치
                                watch_stoch(MainActivity.coin_code.get(i));
                                break;
                            case 4: //macd
                                watch_macd(MainActivity.coin_code.get(i));
                        }
                    } catch (IOException e){
                        Toast.makeText(getContext(), "인터넷 오류", Toast.LENGTH_SHORT).show();
                        Log.d("debug","인터넷 오류");
                        break;
                    }
                    //결과 텍스트에 추가
                    if(passflag){
                        tmp = MainActivity.nameList.get(i);
                        getActivity().runOnUiThread(add_list);
                        passflag = false;
                    }
                }
                Log.d("debug","Done");
            }
        });
        mainThread.start();

        go_back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mainThread.isAlive())
                    mainThread.interrupt();
                MainActivity activity = (MainActivity) getActivity();
                activity.onFragmentChanged(1);
            }
        });
        return rootView;
    }
    final Runnable add_list = new Runnable() { //가격
        @Override
        public void run() {
            adapter.addItem(tmp);
        }
    };

    public void setSearch_number(final String message){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                search_number.setText(message);
            }
        });
    }

    public void setSearch_detail(final String message){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                search_detail.setText(message);
            }
        });
    }

    private void mapping_value(String[] result_str){
        candle = Integer.parseInt(result_str[1]);
        indicator = Integer.parseInt(result_str[2]);
        price = Double.parseDouble(result_str[3]);
        price_updown = Integer.parseInt(result_str[4]);
        ma_n = Integer.parseInt(result_str[5]);
        ma_updown = Integer.parseInt(result_str[6]);
        rsi_n = Integer.parseInt(result_str[7]);
        c_rsi_value = Double.parseDouble(result_str[8]);
        rsi_updown = Integer.parseInt(result_str[9]);
        rsi_signal = Integer.parseInt(result_str[10]);
        rsi_signal_updown = Integer.parseInt(result_str[11]);
        stoch_n = Integer.parseInt(result_str[12]);
        stoch_k = Integer.parseInt(result_str[13]);
        stoch_d = Integer.parseInt(result_str[14]);
        c_stoch_value = Double.parseDouble(result_str[15]);
        stoch_updown = Integer.parseInt(result_str[16]);
        stoch_signal_updown = Integer.parseInt(result_str[17]);
        macd_n = Integer.parseInt(result_str[18]);
        macd_m = Integer.parseInt(result_str[19]);
        c_macd_value = Double.parseDouble(result_str[20]);
        macd_updown = Integer.parseInt(result_str[21]);
        macd_signal = Integer.parseInt(result_str[22]);
        macd_signal_updown = Integer.parseInt(result_str[23]);

        String name = result_str[1] + "분";
        String opt1 = ""; // 상승하락
        String opt2 = ""; // 골든데드

        switch (indicator){
            case 0:
                if(result_str[4].equals("0")) // pon
                    opt1 = "상승돌파";
                else
                    opt1 = "하락돌파";
                break;
            case 1:
                if(result_str[6].equals("0")) // 이평선
                    opt1 = "상승돌파";
                else
                    opt1 = "하락돌파";
                break;
            case 2:
                if(result_str[9].equals("0")) // rsi
                    opt1 = "상승돌파";
                else
                    opt1 = "하락돌파";
                if(result_str[11].equals("1"))
                    opt2 = "골든크로스";
                else if(result_str[11].equals("2"))
                    opt2 = "데드크로스";
                break;
            case 3:
                if(result_str[16].equals("0")) // stoch
                    opt1 = "상승돌파";
                else
                    opt1 = "하락돌파";
                if(result_str[17].equals("1"))
                    opt2 = "골든크로스";
                else if(result_str[17].equals("2"))
                    opt2 = "데드크로스";
                break;
            case 4:
                if(result_str[21].equals("0")) // macd
                    opt1 = "상승돌파";
                else
                    opt1 = "하락돌파";
                if(result_str[21].equals("1"))
                    opt2 = "골든크로스";
                else if(result_str[21].equals("2"))
                    opt2 = "데드크로스";
        }
        switch (indicator){
            case 0: //가격
                name = name + " 가격 " + result_str[3] + " " + opt1;
                break;
            case 1: // 이평선
                name = name + " 이동평균선 " + result_str[5] + "일선 " + opt1;
                break;
            case 2: // RSI
                name = name + " RSI (" + result_str[7] + ") " + result_str[8] + "% " + opt1;
                if(!result_str[11].equals("0"))
                    name = name + " 시그널 " + result_str[10] + "일선 " + opt2;
                break;
            case 3: // stoch
                name = name + " Stochastic Slow (" + result_str[12] + "," + result_str[13] + "," + result_str[14] + ") "
                        + result_str[15] + " " + opt1;
                if(!result_str[17].equals("0"))
                    name = name + " %K,%D " + opt2;
                break;
            case 4: // macd 반드시 추가해야됨
                name = name + " MACD (" + result_str[18] + "," + result_str[19] + ") " + result_str[20] + " " + opt1;
                if(!result_str[23].equals("0"))
                    name = name + " 시그널 " + result_str[22] + "일선 " + opt2;
        }
        setSearch_detail(name);
    }

    //----------여기서 부터 지표계산함수----------//
    private void watch_price(String name) throws IOException { // 가격감시
        Double current_price = 0.0;
        // 가격 감시는 무조건 1분봉
        Document doc = org.jsoup.Jsoup.connect("https://api.upbit.com/v1/candles/minutes/1?market=" + name + "&count=1").timeout(5000).ignoreContentType(true).get();
        String str = doc.text().replace("\":", ",");
        String[] str_split = str.split(",");
        current_price = Double.parseDouble(str_split[13]); // 현재가로 판단
        if (price_updown == 0 && (price < current_price)){ // 상승돌파
            passflag = true;
        }
        else if (price_updown == 1 && (price > current_price)){ // 하락돌파
            passflag = true;
        }
    }
    //이평선
    private void watch_ma(String name) throws IOException{
        Double[] ma_and_price = {0.0, 0.0}; // 0 : 이평선, 1 : 현재가격
        cal_ma(name, ma_n, ma_and_price);
        if (ma_updown == 0 && (ma_and_price[1] >  ma_and_price[0])){ // 상승돌파
            passflag = true;
        }
        else if (ma_updown == 1 && (ma_and_price[1] < ma_and_price[0])){ // 하락돌파
            passflag = true;
        }
    }
    //이평선계산
    private void cal_ma(String name,int n,Double[] ma_and_price) throws IOException{ // n일 이평선 계산
        double current_ma = 0.0; // 이평선 값 초기화
        double current_price = 0.0; // 현재가

        Document doc = org.jsoup.Jsoup.connect("https://api.upbit.com/v1/candles/minutes/"+ candle +"?market=" + name + "&count=" + n).timeout(5000).ignoreContentType(true).get();

        String str = doc.text().replace("\":",",");
        String[] str_split = str.split("\\}\\,\\{"); // 각 캔들로 나누기
        for(int i=0;i<(int)n;i++){ // 캔들들의 종가 총합을 구한다
            String[] str_split2 = str_split[i].split(",");
            if (i == 0) { // 이평선 값과 비교할 최근가격 구하기
                current_price = Double.parseDouble(str_split2[13]); // 현재가
            }
            current_ma += Double.parseDouble(str_split2[13]);
        }
        current_ma /= n;
        ma_and_price[0] = current_ma;
        ma_and_price[1] = current_price;
    }
    //RSI 감시
    public void watch_rsi(String name) throws IOException{
        Double rsi = 0.0;
        int count = 100;

        Document doc = org.jsoup.Jsoup.connect("https://api.upbit.com/v1/candles/minutes/" + candle + "?market=" + name + "&count=" + (count*2)).timeout(5000).ignoreContentType(true).get();

        String str = doc.text().replace("\":",",");
        String[] str_split = str.split("\\}\\,\\{"); // 각 캔들로 나누기

        for (int j = 100; j >= 0; j--) {
            rsi = cal_rsi(j, count, str_split);
            rsi_signal_arr[j] = rsi;
        }
        Double c_rsi_signal = rsi_signal_arr[100];
        for (int i=99;i>=0;i--){
            c_rsi_signal = (2.0 / (rsi_signal + 1)) * rsi_signal_arr[i] + (1 - (2.0) / (rsi_signal + 1)) * c_rsi_signal;
        }
        if(rsi_updown == 0 && rsi > c_rsi_value){ // 상승돌파
            if(rsi_signal_updown == 1 && c_rsi_signal < rsi){ // 골든크로스
                passflag = true;
            }
            else if(rsi_signal_updown == 2 && c_rsi_signal > rsi) { // 데드크로스
                passflag = true;
            }
            else if(rsi_signal_updown == 0){ // 사용안함
                passflag = true;
            }
        }
        else if(rsi_updown == 1 && rsi < c_rsi_value){ // 하락돌파
            if(rsi_signal_updown == 1 && c_rsi_signal < rsi){ // 골든크로스
                passflag = true;
            }
            else if(rsi_signal_updown == 2 && c_rsi_signal > rsi) { // 데드크로스
                passflag = true;
            }
            else if(rsi_signal_updown == 0){ // 사용안함
                passflag = true;
            }
        }
    }
    // RSI 계산
    private double cal_rsi(int j, int count, String[] str_split) {// rsi 계산
        Double val = 0.0;
        Double ad = 0.0;
        Double au = 0.0;
        for (int i = count + j - 1; i > count + j - rsi_n - 1; i--) { // 맨 뒤에서 5개 캔들부터 계산
            String[] str_split2 = str_split[i].split(","); // 이전캔들
            String[] str_split3 = str_split[i - 1].split(","); // 현캔들
            val = Double.parseDouble(str_split2[13]) - Double.parseDouble(str_split3[13]); // 종가차이
            if (val > 0) { // 이전 캔들보다 주가가 하락
                ad += val;
            } else if (val < 0) { // 이전 캔들보다 주가가 상승
                au += Math.abs(val);
            }
        }
        ad /= rsi_n;
        au /= rsi_n;
        for (int i = count + j - rsi_n - 1; i > 0; i--) { // 나머지 캔들 계산
            String[] str_split2 = str_split[i].split(","); // 이전캔들
            String[] str_split3 = str_split[i - 1].split(","); // 현캔들
            val = Double.parseDouble(str_split2[13]) - Double.parseDouble(str_split3[13]); // 종가차이
            // 다운로드 받은 파일을 다시 확인해볼것 한개 더 이전 캔들의 값부터 구해야 할거 같아
            if (val > 0) {
                ad = (ad * (rsi_n - 1) + val) / rsi_n;
                au = (au * (rsi_n - 1)) / rsi_n;
            } else if (val < 0) {
                au = (au * (rsi_n - 1) + Math.abs(val)) / rsi_n;
                ad = (ad * (rsi_n - 1)) / rsi_n;
            } else {
                ad = (ad * (rsi_n - 1)) / rsi_n;
                au = (au * (rsi_n - 1)) / rsi_n;
            }
        }
        return (au / (au + ad) * 100); // rsi 계산
    }
    //stoch 감시
    private void watch_stoch(String name) throws IOException{
        int n, m, t; // n,m은 %K t는 %D
        n=stoch_n; m=stoch_k; t=stoch_d;
        Double stoch = 0.0;
        Double stoch_ma = 0.0;
        Double stoch_ma_ma = 0.0; // 스토캐스틱 슬로우 이평선
        Double min = 0.0;
        Double max = 0.0;
        Double current_price = 0.0;
        Double current_stoch = 0.0; // 스토캐스틱 슬로우 값
        // 캔들 수는 n+(m-1)+(t-1)
        Document doc = org.jsoup.Jsoup.connect("https://api.upbit.com/v1/candles/minutes/"+ candle +"?market=" + name + "&count=" + (n+m+t-2)).timeout(5000).ignoreContentType(true).get();
        String str = doc.text().replace("\":",",");
        String[] str_split = str.split("\\}\\,\\{"); // 각 캔들로 나누기

        for(int k=0; k<t; k++) { // stoch slow %D (스토캐스틱 슬로우의 이평선)
            for (int i=k; i < m+k; i++) { // stoch slow (스토캐스틱의 이평선)
                for (int j=i; j < n+i; j++) { // stoch
                    String[] str_split3 = str_split[j].split(","); // 캔들을 자르고
                    if (j == i) { // 최근 캔들 값 저장
                        current_price = Double.parseDouble(str_split3[13]);
                        min = Double.parseDouble(str_split3[11]);
                        max = Double.parseDouble(str_split3[9]);
                    } else { // 최고가 최저가 저장
                        if (min > Double.parseDouble(str_split3[11]))
                            min = Double.parseDouble(str_split3[11]);
                        else if (max < Double.parseDouble(str_split3[9]))
                            max = Double.parseDouble(str_split3[9]);
                    }
                }
                //이동평균을 내기 위한 stoch값계산
                // (당일종가 - 최근 n일동안 최저가) / 최근 n일동안 최고가 - 최근 n일동안 최저가 * 100
                stoch = (current_price - min) / (max - min) * 100;
                stoch_ma += stoch;
            }
            stoch_ma /= m; // stoch slow값 계산
            if(k == 0) // 최근 slow값 저장 값만 비교할때는 이걸 쓴다
                current_stoch = stoch_ma;
            stoch_ma_ma += stoch_ma;
            stoch_ma = 0.0; // 초기화
        }
        stoch_ma_ma /= t; // stoch slow %D 계산

        if(stoch_updown == 0 && current_stoch > c_stoch_value){ // 상승돌파
            if(stoch_signal_updown == 1 && current_stoch > stoch_ma_ma){ // 골든크로스
                passflag = true;
            }
            else if(stoch_signal_updown == 2 && current_stoch < stoch_ma_ma){ // 데드크로스
                passflag = true;
            }
            else if(stoch_signal_updown == 0){
                passflag = true;
            }
        }
        else if(stoch_updown == 1 && current_stoch < c_stoch_value) { // 하락돌파
            if(stoch_signal_updown == 1 && current_stoch > stoch_ma_ma){ // 골든크로스
                passflag = true;
            }
            else if(stoch_signal_updown == 2 && current_stoch < stoch_ma_ma){ // 데드크로스
                passflag = true;
            }
            else if(stoch_signal_updown == 0){
                passflag = true;
            }
        }
    }
    //MACD 감시
    private void watch_macd(String name) throws IOException {
        Double[] macd_s_p = {0.0,0.0,0.0}; // 0 : macd 1 : signal 2 : 현재가
        cal_macd(name, macd_n,macd_m,macd_signal,macd_s_p);
        if(macd_updown == 0 && macd_s_p[0] > c_macd_value){
            if(macd_signal_updown == 1 && macd_s_p[0] > macd_s_p[1]){
                passflag = true;
            }
            else if(macd_signal_updown == 2 && macd_s_p[0] < macd_s_p[1]){
                passflag = true;
            }
            else if(macd_signal_updown == 0){
                passflag = true;
            }
        }
        else if(macd_updown == 1 && macd_s_p[0] < c_macd_value){
            if(macd_signal_updown == 1 && macd_s_p[0] > macd_s_p[1]){
                passflag = true;
            }
            else if(macd_signal_updown == 2 && macd_s_p[0] < macd_s_p[1]){
                passflag = true;
            }
            else if(macd_signal_updown == 0){
                passflag = true;
            }
        }
    }
    //MACD 계산
    private void cal_macd(String name, int n,int m, int s, Double[] macd_s_p) throws IOException{
        //지수이동평균으로 할 것 rsi때 한 것 처럼
        Double long_ma = 0.0;
        Double short_ma = 0.0;
        Double macd = 0.0;
        Double c_macd_signal = 0.0;
        Document doc = null;

        int count = 100;
        Double val = 0.0; // 장기이평선 마지막값

        doc = org.jsoup.Jsoup.connect("https://api.upbit.com/v1/candles/minutes/" + candle + "?market=" + name + "&count=" + (count*2)).timeout(5000).ignoreContentType(true).get();

        String str = doc.text().replace("\":",",");
        String[] str_split = str.split("\\}\\,\\{"); // 각 캔들로 나누기

        //지수 이동 평균 계산
        for(int j=100;j>=0;j--) {
            for (int i = (count + j - 1); i >= j; i--) {
                String[] str_split2 = str_split[i].split(",");
                val = Double.parseDouble(str_split2[13]);
                if (i == (count + j)) {
                    short_ma = val;
                    long_ma = val;
                } else {
                    short_ma = (2.0 / (n + 1)) * val + (1 - (2.0) / (n + 1)) * short_ma;
                    long_ma = (2.0 / (m + 1)) * val + (1 - (2.0) / (m + 1)) * long_ma;
                }
            }
            macd_signal_arr[j] = short_ma - long_ma; // 시그널 계산을 위해 macd들을 저장
        }
        macd = short_ma - long_ma; // macd구함
        c_macd_signal = macd_signal_arr[100];
        for(int i = 99; i >= 0; i--){
            c_macd_signal = (2.0 / (s + 1)) * macd_signal_arr[i] + (1 - (2.0) / (s + 1)) * c_macd_signal;
        }
        macd_s_p[0] = macd;
        macd_s_p[1] = c_macd_signal;
        macd_s_p[2] = macd - c_macd_signal;
    }
}
