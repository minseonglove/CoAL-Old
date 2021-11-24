package com.mins.bitalert;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.widget.Toast;

import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public static Intent serviceIntent = null;

    public AlertSettingFragment alertSettingFragment;
    public CoinInspectionFragment coinInspectionFragment;
    public CoinSelectFragment coinSelectFragment;
    public WatchListFragment watchListFragment;
    public InspectionResultFragment inspectionResultFragment;

    public static Fragment fragment = null;

    public static boolean is_editing = false;

    /*
    종목이름, 분봉, 지표, 가격, 가격 상승하락, ma_n, ma 상승하락, rsi n,
    rsi value, rsi value 상승하락, rsi시그널, rsi시그널 상승하락, stoch N, %K, %D, value
    value돌파, %K%D교차, macd n, macd m, , macd value, value돌파, macd signal, macd signal 돌파, 온오프상태
     */
    public static String[] result_str = {"x","x","x","x","0","0","0","0",
                                    "0","0","0","0","0","0","0","0","0",
                                    "0","0","0","0","0","0","0","0"};
    public static String coin_name = "코인 이름";
    private static String restr = "";
    private static boolean endflag = false;
    public static List<String> nameList = new ArrayList<>();
    public static List<String> coin_code = new ArrayList<>();
    public static boolean is_name_parse = false;

    private long backKeyPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //절전모드
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
        boolean isWhiteListing = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            isWhiteListing = pm.isIgnoringBatteryOptimizations(getApplicationContext().getPackageName());
        }
        if (!isWhiteListing) {
            Intent intent = new Intent();
            intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
            startActivity(intent);
        }

        if(WatchService.serviceIntent==null){ // 감시 실행
            serviceIntent = new Intent(this, WatchService.class);
            startService(serviceIntent);
        }
        else{ // 이미 실행 중 일때
            serviceIntent = WatchService.serviceIntent;
        }
        watchListFragment = new WatchListFragment();
        coinInspectionFragment = new CoinInspectionFragment();
        coinSelectFragment = new CoinSelectFragment();
        alertSettingFragment = new AlertSettingFragment();
        inspectionResultFragment = new InspectionResultFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.container, watchListFragment).commit();
        fragment = watchListFragment;
        getData(); // 종목이름추출

        WatchThread.mainActivity = this;
    }

    @Override
    public void onBackPressed(){
        if(System.currentTimeMillis() > backKeyPressedTime + 2000){
            backKeyPressedTime = System.currentTimeMillis();
            Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
        else{
            finish();
        }
    }

    public void onFragmentChanged(int idx){
        if(idx == 0)
            fragment = alertSettingFragment;
        else if(idx == 1)
            fragment = coinInspectionFragment;
        else if(idx == 2)
            fragment = coinSelectFragment;
        else if(idx == 3)
            fragment = watchListFragment;
        else if(idx == 4)
            fragment = inspectionResultFragment;
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.frag_fadein, R.anim.frag_fadeout).replace(R.id.container, fragment).commit();
    }

    public void onFragmentRefresh(){
        if(fragment == watchListFragment)
            getSupportFragmentManager().beginTransaction().detach(fragment).attach(fragment).commit();
    }

    private class AsyncJsoup1 extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params){
            try{
                Document doc = org.jsoup.Jsoup.connect("https://api.upbit.com/v1/market/all?isDetails=false").timeout(10000).ignoreContentType(true).get();
                restr = doc.text();
                endflag = true;
            } catch (IOException e){
                // 에러 메세지를 집어넣고 다시 함수 가동
                e.printStackTrace();
            }
            return null;
        }
    }

    private void getData(){
        AsyncJsoup1 jsoupAsyncTask = new AsyncJsoup1();
        jsoupAsyncTask.execute();
        getMarket();
    }

    private void getMarket(){
        while(true){
            if(endflag)
                break;
        }
        restr = restr.replaceAll("\"", "");
        String[] splistr = restr.split("\\}\\,\\{");
        int len = splistr.length;
        for(int i=0; i < len; i++){
            String[] marketStr = splistr[i].split("[:\\,]");
            nameList.add(marketStr[3]+"("+marketStr[1]+")");
            coin_code.add(marketStr[1]);
        }
        is_name_parse = true;
    }
}