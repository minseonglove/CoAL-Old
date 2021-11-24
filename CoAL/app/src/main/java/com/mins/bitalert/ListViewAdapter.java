package com.mins.bitalert;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListViewAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflater;
    private List<String> list;
    private ArrayList<String> arrayList;
    private static boolean flag = false;

    public ListViewAdapter(Context context, List<String> list){
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
        this.arrayList = new ArrayList<>();
        this.arrayList.addAll(list);
    }

    public class ViewHolder {
        TextView tv_name;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        final String str = list.get(position);
        if(view == null){
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.item_recycler, null);
            holder.tv_name = (TextView) view.findViewById(R.id.text);
            view.setTag(holder);
        }
        else{
            holder = (ViewHolder) view.getTag();
        }
        holder.tv_name.setText(str);
        return view;
    }
    public void filter (String charText){
        list.clear();
        if(charText.length() == 0){
            list.addAll(arrayList);
        }
        else{
            for(String str : arrayList){
                if(str.toUpperCase().contains(charText))
                    list.add(str);
            }
        }
        notifyDataSetChanged();
    }
}
