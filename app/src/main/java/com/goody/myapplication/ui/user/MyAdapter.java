package com.goody.myapplication.ui.user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.goody.myapplication.R;

import java.util.ArrayList;

public class MyAdapter extends BaseAdapter {

    Context mContext = null;
    LayoutInflater mLayoutInflater = null;
    ArrayList<UserItemData> sample;

    public MyAdapter(Context context, ArrayList<UserItemData> data) {
        mContext = context;
        sample = data;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return sample.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public UserItemData getItem(int position) {
        return sample.get(position);
    }

    @Override
    public View getView(int position, View converView, ViewGroup parent) {
        View view = mLayoutInflater.inflate(R.layout.user_listview, null);

        ImageView imageView = (ImageView)view.findViewById(R.id.custom_img);
        TextView text = (TextView)view.findViewById(R.id.custom_text);

        imageView.setImageResource(sample.get(position).img);
        text.setText(sample.get(position).text);

        return view;
    }
}
