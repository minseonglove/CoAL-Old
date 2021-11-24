package com.mins.bitalert;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.util.Locale;

public class CoinSelectFragment extends Fragment {
    ListView listView;
    EditText searchBar;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.coin_search, container, false);
        ImageButton go_setting_btn = rootView.findViewById(R.id.go_setting_btn);
        listView = rootView.findViewById(R.id.listView1);
        searchBar = rootView.findViewById(R.id.searchBar);

        ListViewAdapter adapter = new ListViewAdapter(getContext(), MainActivity.nameList);
        listView.setAdapter(adapter);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = searchBar.getText().toString().toUpperCase(Locale.getDefault());
                adapter.filter(text);
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity.coin_name = MainActivity.nameList.get(position);
                String[] split = MainActivity.coin_name.split("\\(");
                split[1] = split[1].replace(")","");
                MainActivity.result_str[0] = split[1];
                MainActivity activity = (MainActivity) getActivity();
                activity.onFragmentChanged(0);
            }
        });

        go_setting_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                activity.onFragmentChanged(0);
            }
        });

        return rootView;
    }
}