package com.mins.bitalert;

import static com.mins.bitalert.MainActivity.result_str;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class CoinInspectionFragment extends Fragment {
    Spinner min_spinner;
    Spinner indicator_spinner;
    Spinner updown_spinner;
    EditText pon_edit;

    //ma
    EditText ma_edit;
    Spinner ma_updown_spinner;

    //rsi
    EditText rsi_edit;
    EditText rsi_value;
    Spinner rsi_spinner;
    EditText rsi_value2;
    Spinner rsi_spinner2;

    //stoch
    EditText stoch_edit;
    EditText stoch_edit2;
    EditText stoch_edit3;
    EditText stoch_value;
    Spinner stoch_spinner;
    Spinner stoch_spinner2;

    //macd
    EditText macd_edit;
    EditText macd_edit2;
    EditText macd_value;
    Spinner macd_spinner;
    EditText macd_value2;
    Spinner macd_spinner2;

    LinearLayout pon_layout;
    LinearLayout ma_layout;
    LinearLayout rsi_layout;
    LinearLayout rsi_layout2;
    LinearLayout rsi_layout3;
    LinearLayout stoch_layout;
    LinearLayout stoch_layout2;
    LinearLayout stoch_layout3;
    LinearLayout macd_layout;
    LinearLayout macd_layout2;
    LinearLayout macd_layout3;

    int indicate;
    public static String[] result_str = {"0","x","x","x","0","0","0","0",
            "0","0","0","0","0","0","0","0",
            "0","0","0","0","0","0","0","0",
            "0","0","0"};
    ArrayAdapter<CharSequence> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.coin_inspection, container, false);

        ImageButton go_list_btn = rootView.findViewById(R.id.go_list_btn);
        Button btn_search_start = rootView.findViewById(R.id.btn_search_start);

        min_spinner = rootView.findViewById(R.id.min_spinner);
        indicator_spinner = rootView.findViewById(R.id.indicator_spinner);
        updown_spinner = rootView.findViewById(R.id.updown_spinner);
        pon_edit = rootView.findViewById(R.id.pon_edit);

        //layout
        pon_layout = rootView.findViewById(R.id.pon_layout);
        ma_layout = rootView.findViewById(R.id.ma_layout);
        rsi_layout = rootView.findViewById(R.id.rsi_layout);
        rsi_layout2 = rootView.findViewById(R.id.rsi_layout2);
        rsi_layout3 = rootView.findViewById(R.id.rsi_layout3);
        stoch_layout = rootView.findViewById(R.id.stoch_layout);
        stoch_layout2 = rootView.findViewById(R.id.stoch_layout2);
        stoch_layout3 = rootView.findViewById(R.id.stoch_layout3);
        macd_layout = rootView.findViewById(R.id.macd_layout);
        macd_layout2 = rootView.findViewById(R.id.macd_layout2);
        macd_layout3 = rootView.findViewById(R.id.macd_layout3);

        //ma
        ma_edit = rootView.findViewById(R.id.ma_edit);
        ma_updown_spinner = rootView.findViewById(R.id.ma_updown_spinner);

        //rsi
        rsi_edit = rootView.findViewById(R.id.rsi_edit);
        rsi_value = rootView.findViewById(R.id.rsi_value);
        rsi_spinner = rootView.findViewById(R.id.rsi_spinner);
        rsi_value2 = rootView.findViewById(R.id.rsi_value2);
        rsi_spinner2 = rootView.findViewById(R.id.rsi_spinner2);

        //stoch
        stoch_edit = rootView.findViewById(R.id.stoch_edit);
        stoch_edit2 = rootView.findViewById(R.id.stoch_edit2);
        stoch_edit3 = rootView.findViewById(R.id.stoch_edit3);
        stoch_value = rootView.findViewById(R.id.stoch_value);
        stoch_spinner = rootView.findViewById(R.id.stoch_spinner);
        stoch_spinner2 = rootView.findViewById(R.id.stoch_spinner2);

        //macd
        macd_edit = rootView.findViewById(R.id.macd_edit);
        macd_edit2 = rootView.findViewById(R.id.macd_edit2);
        macd_value = rootView.findViewById(R.id.macd_value);
        macd_spinner = rootView.findViewById(R.id.macd_spinner);
        macd_value2 = rootView.findViewById(R.id.macd_value2);
        macd_spinner2 = rootView.findViewById(R.id.macd_spinner2);

        /*차트 시간 설정*/
        String[] min_items = {"1","3","5","10","15","30","60","240"}; // 분캔들
        result_str[1] = min_items[0]; // 기본
        adapter = ArrayAdapter.createFromResource(getActivity(), R.array.min_items, R.layout.spinner_item);
        min_spinner.setAdapter(adapter);
        min_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                result_str[1] = min_items[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        /*감시 지표 설정*/
        indicate = 0;//지표저장
        String[] indicator_items = {"가격","이동평균선","RSI","stochastic","MACD"}; //지표
        result_str[2] = indicator_items[0]; // 기본
        adapter = ArrayAdapter.createFromResource(getActivity(), R.array.indicator_items, R.layout.spinner_item);
        indicator_spinner.setAdapter(adapter);
        indicator_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                clearSetting(); // 상태 초기화
                if(position == 0){ // 가격선택
                    pon_layout.setVisibility(View.VISIBLE);
                    result_str[3] = "x"; // 가격 필수 선택
                }
                else if(position == 1){ // 이평선
                    ma_layout.setVisibility(View.VISIBLE);
                    result_str[5] = "x"; // N 필수 선택
                }
                else if(position == 2){ // RSI
                    rsi_layout.setVisibility(View.VISIBLE);
                    rsi_layout2.setVisibility(View.VISIBLE);
                    rsi_layout3.setVisibility(View.VISIBLE);
                    result_str[7] = "x"; // N 필수 선택
                    result_str[8] = "x"; // 값 필수 선택
                }
                else if(position == 3){ // 스토치
                    stoch_layout.setVisibility(View.VISIBLE);
                    stoch_layout2.setVisibility(View.VISIBLE);
                    stoch_layout3.setVisibility(View.VISIBLE);
                    result_str[12] = "x";
                    result_str[13] = "x";
                    result_str[14] = "x"; // 스토치 값 필수 선택
                    result_str[15] = "x"; // 값 필수 선택
                }
                else if(position == 4){ // MACD
                    macd_layout.setVisibility(View.VISIBLE);
                    macd_layout2.setVisibility(View.VISIBLE);
                    macd_layout3.setVisibility(View.VISIBLE);
                    result_str[18] = "x"; // N 필수 선택
                    result_str[19] = "x"; // N2 필수 선택
                    result_str[20] = "x"; // 값 필수 선택
                }
                indicate = position;
                result_str[2] = Integer.toString(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        /*pon*/
        pon_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                result_str[3] = pon_edit.getText().toString();
                if(result_str[3].equals("")) {
                    if(indicate != 0) // 가격이 아니면 안넣어도됨
                        result_str[3] = "0";
                    else
                        result_str[3] = "x";
                }
            }
        });

        /*이평선 n*/
        ma_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                result_str[5] = ma_edit.getText().toString();
                if(result_str[5].equals("")) {
                    if(indicate != 1) // 이평선이 아니면 안넣어도됨
                        result_str[15] = "0";
                    else
                        result_str[15] = "x";
                }
            }
        });

        /*rsi n*/
        rsi_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                result_str[7] = rsi_edit.getText().toString();
                if(result_str[7].equals("")) {
                    if(indicate != 2) // rsi가 아니라면 안넣어도됨
                        result_str[7] = "0";
                    else
                        result_str[7] = "x";
                }
            }
        });

        /*rsi value*/
        rsi_value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                result_str[8] = rsi_value.getText().toString();
                if(result_str[8].equals("")) {
                    if(indicate != 2) // rsi가 아니라면 안넣어도됨
                        result_str[8] = "0";
                    else
                        result_str[8] = "x";
                }
            }
        });

        /*rsi signal*/
        rsi_value2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                result_str[10] = rsi_value2.getText().toString();
                if(result_str[10].equals("")) {
                    if(indicate == 2 && !result_str[11].equals("0")) // rsi시그널을 사용중이라면
                        result_str[10] = "x";
                    else
                        result_str[10] = "0";
                }
            }
        });

        /*stoch n*/
        stoch_edit.addTextChangedListener(new TextWatcher() { // stoch n
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                result_str[12] = stoch_edit.getText().toString();
                if(result_str[12].equals("")) {
                    if(indicate != 3)// 스토치가 아니라면 안넣어도됨
                        result_str[12] = "0";
                    else
                        result_str[12] = "x";
                }
            }
        });

        /*stoch %K*/
        stoch_edit2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                result_str[13] = stoch_edit2.getText().toString();
                if(result_str[13].equals("")) {
                    if(indicate != 3)// 스토치가 아니라면 안넣어도됨
                        result_str[13] = "0";
                    else
                        result_str[13] = "x";
                }
            }
        });

        /*stoch %D*/
        stoch_edit3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                result_str[14] = stoch_edit3.getText().toString();
                if(result_str[14].equals("")) {
                    if(indicate != 3)// 스토치가 아니라면 안넣어도됨
                        result_str[14] = "0";
                    else
                        result_str[14] = "x";
                }
            }
        });

        /*stoch value*/
        stoch_value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                result_str[15] = stoch_value.getText().toString();
                if(result_str[15].equals("")) {
                    if(indicate == 3 && !result_str[16].equals("0"))
                        result_str[15] = "x";
                    else
                        result_str[15] = "0";
                }
            }
        });

        /*macd n*/
        macd_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                result_str[18] = macd_edit.getText().toString();
                if(result_str[18].equals("")) {
                    if(indicate != 4)// macd가 아니라면 안넣어도됨
                        result_str[18] = "0";
                    else
                        result_str[18] = "x";
                }
            }
        });

        /*macd m*/
        macd_edit2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                result_str[19] = macd_edit2.getText().toString();
                if(result_str[19].equals("")) {
                    if(indicate != 4)// macd가 아니라면 안넣어도됨
                        result_str[19] = "0";
                    else
                        result_str[19] = "x";
                }
            }
        });

        /*macd value*/
        macd_value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                result_str[20] = macd_value.getText().toString();
                if(result_str[20].equals("")) {
                    if(indicate != 4)// macd가 아니라면 안넣어도됨
                        result_str[20] = "0";
                    else
                        result_str[20] = "x";
                }
            }
        });

        /*macd value2*/
        macd_value2.addTextChangedListener(new TextWatcher() { // macd value
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                result_str[22] = macd_value2.getText().toString();
                if(result_str[22].equals("")) {
                    if(indicate == 4 && !result_str[23].equals("0"))
                        result_str[22] = "x";
                    else
                        result_str[22] = "0";
                }
            }
        });

        /*가격 돌파 여부*/
        adapter = ArrayAdapter.createFromResource(getActivity(), R.array.updown_items, R.layout.spinner_item);
        updown_spinner.setAdapter(adapter);
        updown_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                result_str[4] = Integer.toString(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        /*이평선 돌파 여부*/
        ma_updown_spinner.setAdapter(adapter);
        ma_updown_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                result_str[6] = Integer.toString(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        /*rsi 돌파 여부*/
        rsi_spinner.setAdapter(adapter);
        rsi_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                result_str[9] = Integer.toString(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        /*스토치 돌파 여부*/
        stoch_spinner.setAdapter(adapter);
        stoch_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                result_str[16] = Integer.toString(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        /*macd 돌파 여부*/
        macd_spinner.setAdapter(adapter);
        macd_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                result_str[21] = Integer.toString(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        /*이평선 교차여부*/
        String[] cross_items = {"사용안함","골든 크로스","데드 크로스"};
        adapter = ArrayAdapter.createFromResource(getActivity(), R.array.cross_items, R.layout.spinner_item);
        rsi_spinner2.setAdapter(adapter);
        rsi_spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){ // 사용안함
                    rsi_value2.setText(""); // 텍스트 비우기
                    result_str[10] = "0"; // 디폴트값
                    rsi_value2.setVisibility(View.INVISIBLE);
                }
                else { // 다른거선택
                    result_str[10] = "x"; // 반드시채워야함
                    rsi_value2.setVisibility(View.VISIBLE);
                }
                result_str[11] = Integer.toString(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        stoch_spinner2.setAdapter(adapter);
        stoch_spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                result_str[17] = Integer.toString(position); // 수정필요
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        macd_spinner2.setAdapter(adapter);
        macd_spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){ // 사용안함
                    macd_value2.setText(""); // 텍스트 비우기
                    result_str[22] = "0"; // 디폴트값
                    macd_value2.setVisibility(View.INVISIBLE);
                }
                else { // 다른거선택
                    result_str[22] = "x"; // 반드시채워야함
                    macd_value2.setVisibility(View.VISIBLE);
                }
                result_str[11] = Integer.toString(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        btn_search_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean ok_flag = true;
                for(String str : result_str) {
                    if (str.equals("x")) {
                        Toast.makeText(getContext(), "필요한 내용을 모두 채워 주세요", Toast.LENGTH_SHORT).show();
                        ok_flag = false;
                        break;
                    }
                }
                if(ok_flag) {
                    switch (result_str[2]) {
                        case "0":
                            ok_flag = valueCheck(result_str[3], "가격을 입력해주세요", false);
                            break;
                        case "1":
                            ok_flag = valueCheck(result_str[5], "N은 1~99사이의 값을 입력해주세요", true);
                            break;
                        case "2":  // RSI
                            ok_flag = valueCheck(result_str[7], "N은 1~99사이의 값을 입력해주세요", true); // rsi n검사

                            if (ok_flag)
                                ok_flag = valueCheck(result_str[8], "RSI값은 1~99사이의 값을 입력해주세요", true); // rsi value검사
                            if (!result_str[11].equals("0") && ok_flag) { // 시그널 체크를 넣고 시그널 값을 안넣었다
                                ok_flag = valueCheck(result_str[10], "시그널은 1~99사이의 값을 입력해주세요", true);
                            }
                            break;
                        case "3":  // stoch
                            ok_flag = valueCheck(result_str[12], "N은 1~99사이의 값을 입력해주세요", true); // stoch n

                            if (ok_flag)
                                ok_flag = valueCheck(result_str[13], "%K은 1~99사이의 값을 입력해주세요", true);
                            if (ok_flag)
                                ok_flag = valueCheck(result_str[14], "%D은 1~99 사이의 값을 입력해주세요", true);
                            if (ok_flag)
                                ok_flag = valueCheck(result_str[15], "stochastic slow 값은 1~99사이의 값을 입력해주세요", true);
                            break;
                        case "4":  // MACD
                            int n = 0, m = 0;
                            try {
                                n = Integer.parseInt(result_str[18]); // n
                                m = Integer.parseInt(result_str[19]); // m
                            } catch (NumberFormatException e) {
                                Toast.makeText(getContext(), "N,M은 정수만 입력해주세요", Toast.LENGTH_SHORT).show();
                                ok_flag = false;
                            }
                            if (n == 0 || m == 0 || n >= 100 || m >= 100) {
                                Toast.makeText(getContext(), "N,M은 1~99사이의 값을 입력해주세요", Toast.LENGTH_SHORT).show();
                                ok_flag = false;
                            } else if (n >= m) {
                                Toast.makeText(getContext(), "N은 M보다 작아야 합니다", Toast.LENGTH_SHORT).show();
                                ok_flag = false;
                            }
                            break;
                    }
                }
                if(ok_flag) {
                    MainActivity activity = (MainActivity) getActivity();
                    activity.onFragmentChanged(4);
                }
            }
        });

        go_list_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                activity.onFragmentChanged(3);
            }
        });

        return rootView;
    }

    private void clearSetting(){
        pon_layout.setVisibility(View.GONE);
        ma_layout.setVisibility(View.GONE);
        rsi_layout.setVisibility(View.GONE);
        rsi_layout2.setVisibility(View.GONE);
        rsi_layout3.setVisibility(View.GONE);
        stoch_layout.setVisibility(View.GONE);
        stoch_layout2.setVisibility(View.GONE);
        stoch_layout3.setVisibility(View.GONE);
        macd_layout.setVisibility(View.GONE);
        macd_layout2.setVisibility(View.GONE);
        macd_layout3.setVisibility(View.GONE);

        pon_edit.setText("");
        ma_edit.setText("");
        rsi_edit.setText("");
        rsi_value.setText("");
        rsi_value2.setText("");
        stoch_edit.setText("");
        stoch_edit2.setText("");
        stoch_edit3.setText("");
        stoch_value.setText("");
        macd_edit.setText("");
        macd_edit2.setText("");
        macd_value.setText("");
        macd_value2.setText("");
        for(int i=3;i<24;i++){ // 초기화
            result_str[i] = "0";
        }
    }

    private boolean valueCheck(String value, String msg, boolean flag){
        try{
            Double d = Double.parseDouble(value);
            if(d <= 0){ // 0이하
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                return false;
            }
            if(flag && d >= 100){ // 100이상
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        } catch (NumberFormatException e){
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
