package com.mins.bitalert;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class WatchThread extends Thread {
    public static boolean[] run_flag = new boolean[50];
    private final int num; //알람넘버
    private final String name; // 코인이름
    private final int candle; // 캔들
    private final int indicator; // 지표
    private final Double price; // 가격
    private final int price_updown; // 값 업다운
    private final int ma_n; // 이평선 n
    private final int ma_updown; // 이평선 업다운
    private final int rsi_n; // rsi n
    private final Double rsi_value; // rsi value
    private final int rsi_updown; // rsi 업다운
    private final int rsi_signal; // rsi signal
    private final int rsi_signal_updown; // rsi signal 업다운
    private final int stoch_n; // stoch n
    private final int stoch_k; // stoch %k
    private final int stoch_d; // stoch %d
    private final Double stoch_value; // stoch value
    private final int stoch_updown; // stoch n
    private final int stoch_signal_updown; // stoch signal 업다운
    private final int macd_n; // macd n
    private final int macd_m; // macd m
    private final Double macd_value; // macd value
    private final int macd_updown; // macd updown
    private final int macd_signal; // macd signal
    private final int macd_signal_updown; // macd signal 업다운
    private String msg="";

    private boolean first_watch;
    private boolean next_candle; // 다음 캔들로 넘어감
    private boolean first_rsi = true;
    private boolean first_macd = true; // macd
    private String first_time; // 처음감시시간

    private Double[] rsi_signal_arr = new Double[101]; // 시그널 계산
    private Double[] macd_signal_arr = new Double[101]; // 시그널 계산

    NotificationManager manager;
    NotificationCompat.Builder builder;
    private static final String CHANNEL_ID = "channel1";
    private static final String CHANEL_NAME = "Channel1";

    public Double rsi_current; // 현재값 받아보는용도

    private final String path;

    public static MainActivity mainActivity = null;

    public WatchThread(Context context, String path, int num, String[] result_str) throws ParseException {
        this.path = path;
        this.num = num;
        name = result_str[0];
        candle = Integer.parseInt(result_str[1]);
        indicator = Integer.parseInt(result_str[2]);
        price = Double.parseDouble(result_str[3]);
        price_updown = Integer.parseInt(result_str[4]);
        ma_n = Integer.parseInt(result_str[5]);
        ma_updown = Integer.parseInt(result_str[6]);
        rsi_n = Integer.parseInt(result_str[7]);
        rsi_value = Double.parseDouble(result_str[8]);
        rsi_updown = Integer.parseInt(result_str[9]);
        rsi_signal = Integer.parseInt(result_str[10]);
        rsi_signal_updown = Integer.parseInt(result_str[11]);
        stoch_n = Integer.parseInt(result_str[12]);
        stoch_k = Integer.parseInt(result_str[13]);
        stoch_d = Integer.parseInt(result_str[14]);
        stoch_value = Double.parseDouble(result_str[15]);
        stoch_updown = Integer.parseInt(result_str[16]);
        stoch_signal_updown = Integer.parseInt(result_str[17]);
        macd_n = Integer.parseInt(result_str[18]);
        macd_m = Integer.parseInt(result_str[19]);
        macd_value = Double.parseDouble(result_str[20]);
        macd_updown = Integer.parseInt(result_str[21]);
        macd_signal = Integer.parseInt(result_str[22]);
        macd_signal_updown = Integer.parseInt(result_str[23]);
        first_watch = false;
        next_candle = false;
        run_flag[num] = result_str[24].equals("on"); // 준비

        //푸시알람 설정
        builder = null;
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE); //버전 오레오 이상일 경우
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            manager.createNotificationChannel( new NotificationChannel(CHANNEL_ID, CHANEL_NAME, NotificationManager.IMPORTANCE_DEFAULT) );
        }else{
            builder = new NotificationCompat.Builder(context);
        }
        builder = new NotificationCompat.Builder(context,CHANNEL_ID); //하위 버전일 경우 }else{ builder = new NotificationCompat.Builder(this); }

        PendingIntent mPendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentTitle(null);
        builder.setContentText(null);
        builder.setSmallIcon(R.mipmap.ic_logo);
        builder.setAutoCancel(true);
        builder.setContentIntent(mPendingIntent);
        builder.setDefaults(Notification.DEFAULT_VIBRATE);
    }

    public void run(){
        try {
            while (!WatchService.mumcha_flag) {
                Thread.sleep(1000); // 1초 쉼
                if (!run_flag[num])
                    continue;
                switch (indicator) {
                    case 0:
                        watch_price();
                        break;
                    case 1:
                        watch_ma();
                        break;
                    case 2:
                        watch_rsi();
                        break;
                    case 3:
                        watch_stoch();
                        break;
                    case 4:
                        watch_macd();
                }
            }
        }catch (InterruptedException | IOException e) {
            Log.d("debug", "인터럽트 or 통신에러");
        }
    }
    //가격감시
    private void watch_price() throws IOException { // 가격감시
        Double current_price = 0.0;
        // 가격 감시는 무조건 1분봉
        Document doc = org.jsoup.Jsoup.connect("https://api.upbit.com/v1/candles/minutes/1?market=" + name + "&count=1").timeout(5000).ignoreContentType(true).get();
        String str = doc.text().replace("\":", ",");
        String[] str_split = str.split(",");

        String[] time_split = str_split[3].split(":");
        if (!next_candle)
            is_next(time_split[1]);// 다음 캔들인지 검사

        if (next_candle) {
            if (price_updown == 0) { // 상승돌파
                current_price = Double.parseDouble(str_split[9]); // 고가
                if (price < current_price) { // 돌파
                    run_flag[num] = false; // 감시끝내기
                    msg = name + " " + candle + "분\n" + "가격 " + price + " 상승";
                }
            } else if (price_updown == 1) { // 하락돌파
                current_price = Double.parseDouble(str_split[11]); // 저가
                if (price > current_price) { // 돌파
                    run_flag[num] = false; // 감시끝내기
                    msg = name + " " + candle + "분\n" + "가격 " + price + " 하락";
                }
            }
        }
        else{
            current_price = Double.parseDouble(str_split[13]); // 현재가로 판단
            if (price_updown == 0 && (price < current_price)){ // 상승돌파
                run_flag[num] = false;
                msg = name + " " + candle + "분\n" + "가격 " + price + " 상승";
            }
            else if (price_updown == 1 && (price > current_price)){ // 하락돌파
                run_flag[num] = false;
                msg = name + " " + candle + "분\n" + "가격 " + price + " 하락";
            }
        }
        if(!run_flag[num]){
            edit_alert(); // 수정으로 고쳐야함 이게 몇번째라는걸 어떻게 알지
            builder.setContentText(msg);
            Notification notification = builder.build();
            manager.notify(1,notification);
        }
    }

    //이평선감시
    private void watch_ma(){ // 이평선
        Double[] ma_and_price = {0.0, 0.0}; // 0 : 이평선, 1 : 현재가격
        cal_ma(ma_n,ma_and_price);

        if (ma_updown == 0 && (ma_and_price[1] >  ma_and_price[0])){ // 상승돌파
            run_flag[num] = false;
            msg = name + " " + candle + "분\n" + ma_n + "일 이동평균선" + " 상승돌파..!";
        }
        else if (ma_updown == 1 && (ma_and_price[1] < ma_and_price[0])){ // 하락돌파
            run_flag[num] = false;
            msg = name + " " + candle + "분\n" + ma_n + "일 이동평균선" + " 깨져버렸어..!";
        }
        if(!run_flag[num]){
            edit_alert();
            builder.setContentText(msg);
            Notification notification = builder.build();
            manager.notify(1,notification);
        }
    }

    //이평선계산
    private void cal_ma(int n,Double[] ma_and_price){ // n일 이평선 계산
        Double current_ma = 0.0; // 이평선 값 초기화
        Double current_price = 0.0; // 현재가
        Document doc = null;
        try {
            doc = org.jsoup.Jsoup.connect("https://api.upbit.com/v1/candles/minutes/"+ candle +"?market=" + name + "&count=" + n).timeout(5000).ignoreContentType(true).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String str = doc.text().replace("\":",",");
        String[] str_split = str.split("\\}\\,\\{"); // 각 캔들로 나누기

        String[] time_split = str_split[0].split(",");
        String[] time_split2 = time_split[3].split(":");
        if(!next_candle)
            is_next(time_split2[1]);// 다음 캔들인지 검사

        for(int i=0;i<(int)n;i++){ // 캔들들의 종가 총합을 구한다
            String[] str_split2 = str_split[i].split(",");
            if (i == 0) { // 이평선 값과 비교할 최근가격 구하기
                if(next_candle) {
                    if (ma_updown == 0) {
                        current_price = Double.parseDouble(str_split2[9]); // 고가
                    } else if (ma_updown == 1) {
                        current_price = Double.parseDouble(str_split2[11]); // 저가
                    }
                }
                else {
                    current_price = Double.parseDouble(str_split2[13]); // 현재가
                }
            }
            current_ma += Double.parseDouble(str_split2[13]);
        }
        current_ma /= n;
        ma_and_price[0] = current_ma;
        ma_and_price[1] = current_price;
    }

    //RSI 감시
    private void watch_rsi(){
        Double rsi = 0.0;
        int count = 100;
        Document doc = null;
        if(first_rsi) { // 첫번째 계산이거나 다음 캔들로 넘어갈때만
            try {
                doc = org.jsoup.Jsoup.connect("https://api.upbit.com/v1/candles/minutes/" + candle + "?market=" + name + "&count=" + (count*2)).timeout(5000).ignoreContentType(true).get();
            } catch (IOException e) {
                Log.d("debug","WatchThread rsi에러1");
                e.printStackTrace();
            }
        }
        else{ // 이후엔 100개 때온다
            try {
                doc = org.jsoup.Jsoup.connect("https://api.upbit.com/v1/candles/minutes/" + candle + "?market=" + name + "&count=" + count).timeout(5000).ignoreContentType(true).get();
            } catch (IOException e) {
                Log.d("debug","WatchThread rsi에러2");
                e.printStackTrace();
            }
        }
        String str = doc.text().replace("\":",",");
        String[] str_split = str.split("\\}\\,\\{"); // 각 캔들로 나누기
        String[] time_split = str_split[0].split(",");

        if(first_rsi) {
            first_rsi = false;
            for (int j = 100; j >= 0; j--) {
                rsi = cal_rsi(j, count, str_split);
                rsi_signal_arr[j] = rsi;
            }
        }
        else {
            rsi = cal_rsi(0, count, str_split);
            rsi_signal_arr[0] = rsi;
        }
        Log.d("debug",name + "WatchThread rsi : "+rsi);
        rsi_current = rsi;
        Double c_rsi_signal = rsi_signal_arr[100];
        for (int i=99;i>=0;i--){
            c_rsi_signal = (2.0 / (rsi_signal + 1)) * rsi_signal_arr[i] + (1 - (2.0) / (rsi_signal + 1)) * c_rsi_signal;
        }
        if(rsi_updown == 0 && rsi > rsi_value){ // 상승돌파
            msg = name + " " + candle + "분\n" + "RSI(" + rsi_n + ") " + rsi_value + "% 상승";
            if(rsi_signal_updown == 1 && c_rsi_signal < rsi){ // 골든크로스
                run_flag[num] = false;
                msg = msg + "\n시그널(" + rsi_signal + ") 골든크로스";
            }
            else if(rsi_signal_updown == 2 && c_rsi_signal > rsi) { // 데드크로스
                run_flag[num] = false;
                msg = msg + "\n시그널(" + rsi_signal + ") 데드크로스";
            }
            else if(rsi_signal_updown == 0){ // 사용안함
                run_flag[num] = false;
            }
        }
        else if(rsi_updown == 1 && rsi < rsi_value){ // 하락돌파
            msg = name + " " + candle + "분\n" + "RSI(" + rsi_n + ") " + rsi_value + "% 하락";
            if(rsi_signal_updown == 1 && c_rsi_signal < rsi){ // 골든크로스
                run_flag[num] = false;
                msg = msg + "\n시그널(" + rsi_signal + ") 골든크로스";
            }
            else if(rsi_signal_updown == 2 && c_rsi_signal > rsi) { // 데드크로스
                run_flag[num] = false;
                msg = msg + "\n시그널(" + rsi_signal + ") 데드크로스";
            }
            else if(rsi_signal_updown == 0){ // 사용안함
                run_flag[num] = false;
            }
        }

        String[] time_split2 = time_split[3].split(":");
        if(!next_candle) {
            is_next(time_split2[1]);// 다음 캔들인지 검사
            if(next_candle){ // 다음캔들로 넘어감
                first_rsi = true;
                next_candle = false;
                first_watch = false;
            }
        }

        if(!run_flag[num]){
            edit_alert();
            builder.setContentText(msg);
            Notification notification = builder.build();
            manager.notify(1,notification);
        }
    }
    //RSI 계산
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


    private void is_next(String time){ // 다음 캔들인지 검사
        if(!first_watch){ // 이게 첫 감시면
            first_watch = true;
            first_time = time; // 시간 저장
        }
        if (!first_time.equals(time)){ // 봉이 넘어갔으면
            next_candle = true;
        }
    }
    //stoch 감시
    private void watch_stoch() throws IOException{
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

        if(stoch_updown == 0 && current_stoch > stoch_value){ // 상승돌파
            msg = name + " " + candle + "분\n" + "Stochastic Slow(" + stoch_n + ", " + stoch_k + ", " + stoch_d + ") " + stoch_value + " 상승돌파..!";
            if(stoch_signal_updown == 1 && current_stoch > stoch_ma_ma){ // 골든크로스
                run_flag[num] = false;
                msg = msg + "\n시그널 골든크로스";
            }
            else if(stoch_signal_updown == 2 && current_stoch < stoch_ma_ma){ // 데드크로스
                run_flag[num] = false;
                msg = msg + "\n시그널 데드크로스";
            }
            else if(stoch_signal_updown == 0){
                run_flag[num] = false;
            }
        }
        else if(stoch_updown == 1 && current_stoch < stoch_value) { // 하락돌파
            msg = name + " " + candle + "분\n" + "Stochastic Slow(" + stoch_n + ", " + stoch_k + ", " + stoch_d + ") " + stoch_value + " 하락돌파..!";
            if(stoch_signal_updown == 1 && current_stoch > stoch_ma_ma){ // 골든크로스
                run_flag[num] = false;
                msg = msg + "\n시그널 골든크로스";
            }
            else if(stoch_signal_updown == 2 && current_stoch < stoch_ma_ma){ // 데드크로스
                run_flag[num] = false;
                msg = msg + "\n시그널 데드크로스";
            }
            else if(stoch_signal_updown == 0){
                run_flag[num] = false;
            }
        }
        if(!run_flag[num]){
            edit_alert();
            builder.setContentText(msg);
            Notification notification = builder.build();
            manager.notify(1,notification);
        }
    }
    //MACD 감시
    private void watch_macd(){
        Double[] macd_s_p = {0.0,0.0,0.0}; // 0 : macd 1 : signal 2 : 현재가
        cal_macd(macd_n,macd_m,macd_signal,macd_s_p);
        if(macd_updown == 0 && macd_s_p[0] > macd_value){
            msg = name + " " + candle + "분\n" + "MACD(" + macd_n + ", " + macd_m + ") " + macd_value + " 상승돌파..!";
            if(macd_signal_updown == 1 && macd_s_p[0] > macd_s_p[1]){
                run_flag[num] = false;
                msg = msg + "\n시그널(" + macd_signal + ") 골든크로스";
            }
            else if(macd_signal_updown == 2 && macd_s_p[0] < macd_s_p[1]){
                run_flag[num] = false;
                msg = msg + "\n시그널("+ macd_signal +") 데드크로스";
            }
            else if(macd_signal_updown == 0){
                run_flag[num] = false;
            }
        }
        else if(macd_updown == 1 && macd_s_p[0] < macd_value){
            msg = name + " " + candle + "분\n" + "MACD(" + macd_n + ", " + macd_m + ") " + macd_value + " 상승돌파..!";
            if(macd_signal_updown == 1 && macd_s_p[0] > macd_s_p[1]){
                run_flag[num] = false;
                msg = msg + "\n시그널(" + macd_signal + ") 골든크로스";
            }
            else if(macd_signal_updown == 2 && macd_s_p[0] < macd_s_p[1]){
                run_flag[num] = false;
                msg = msg + "\n시그널("+ macd_signal +") 데드크로스";
            }
            else if(macd_signal_updown == 0){
                run_flag[num] = false;
            }
        }
        if(!run_flag[num]){
            edit_alert();
            builder.setContentText(msg);
            Notification notification = builder.build();
            manager.notify(1,notification);
        }
    }
    //MACD 계산
    private void cal_macd(int n,int m, int s, Double[] macd_s_p){
        //지수이동평균으로 할 것 rsi때 한 것 처럼
        Double long_ma = 0.0;
        Double short_ma = 0.0;
        Double macd = 0.0;
        Double c_macd_signal = 0.0;
        Document doc = null;

        int count = 100;
        Double val = 0.0; // 장기이평선 마지막값
        String[] time_split;
        // count일만큼 때온다
        if(first_macd) { // 첫번째 계산이거나 다음 캔들로 넘어갈때만
            try {
                doc = org.jsoup.Jsoup.connect("https://api.upbit.com/v1/candles/minutes/" + candle + "?market=" + name + "&count=" + (count*2)).timeout(5000).ignoreContentType(true).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{ // 이후엔 100개 때온다
            try {
                doc = org.jsoup.Jsoup.connect("https://api.upbit.com/v1/candles/minutes/" + candle + "?market=" + name + "&count=" + count).timeout(5000).ignoreContentType(true).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String str = doc.text().replace("\":",",");
        String[] str_split = str.split("\\}\\,\\{"); // 각 캔들로 나누기
        time_split = str_split[0].split(",");

        //지수 이동 평균 계산
        if(first_macd) { // 첫 계산일때만 모든 캔들을 받아서 지수 이동 평균 계산
            first_macd = false;
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
        }
        else{
            for (int i = (count - 1); i >= 0; i--) {
                String[] str_split2 = str_split[i].split(",");
                val = Double.parseDouble(str_split2[13]);
                if(i == (count-1)) {
                    short_ma = val;
                    long_ma = val;
                }
                else {
                    short_ma = (2.0 / (n + 1)) * val + (1 - (2.0) / (n + 1)) * short_ma;
                    long_ma = (2.0 / (m + 1)) * val + (1 - (2.0) / (m + 1)) * long_ma;
                }
            }
            macd_signal_arr[0] = short_ma - long_ma;
        }
        macd = short_ma - long_ma; // macd구함
        c_macd_signal = macd_signal_arr[100];
        for(int i = 99; i >= 0; i--){
            c_macd_signal = (2.0 / (s + 1)) * macd_signal_arr[i] + (1 - (2.0) / (s + 1)) * c_macd_signal;
        }

        macd_s_p[0] = macd;
        macd_s_p[1] = c_macd_signal;
        macd_s_p[2] = macd - c_macd_signal;

        String[] time_split2 = time_split[3].split(":");
        if(!next_candle) {
            is_next(time_split2[1]);// 다음 캔들인지 검사
            if(next_candle){ // 다음캔들로 넘어감
                first_macd = true;
                next_candle = false;
                first_watch = false;
            }
        }
    }
    //알람 수정 run플래그 내용 수정
    private void edit_alert(){
        int count = 0;
        String str = "";
        String text = "";
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            br = new BufferedReader(new FileReader(path+"current_alert.txt"));
        } catch (FileNotFoundException e) {
            Log.d("debug","WatchThread 파일읽기 오류");
        }
        try {
            while (((str = br.readLine()) != null)) {
                count++;
                if(count == num) {
                    str = str.replace("on", "off");
                    run_flag[num] = false;
                }
                text = text + str + "\n";
            }
            br.close();
        }
        catch (IOException e){ }
        try {
            bw = new BufferedWriter(new FileWriter(path+"current_alert.txt", false));
        } catch (IOException e) { }
        try {
            bw.write(text);
            bw.close();
        } catch (IOException e){ }
        mainActivity.onFragmentRefresh();
    }
}
