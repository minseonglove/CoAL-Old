package com.mins.bitalert;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class InspectionResultAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    private List<String> list;
    private ArrayList<String> arrayList;

    public InspectionResultAdapter(Context context, List<String> list){
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
        final InspectionResultAdapter.ViewHolder holder;
        final String str = list.get(position);
        if(view == null){
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.item_recycler, null);
            holder.tv_name = (TextView) view.findViewById(R.id.text);
            view.setTag(holder);
        }
        else{
            holder = (InspectionResultAdapter.ViewHolder) view.getTag();
        }
        holder.tv_name.setText(str);
        return view;
    }
    public void addItem(String coin){
        list.add(coin);
        notifyDataSetChanged();
    }
}
