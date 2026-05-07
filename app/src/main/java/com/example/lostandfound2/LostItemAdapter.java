package com.example.lostandfound2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class LostItemAdapter extends BaseAdapter {
    private final Context context;
    private List<LostItem> itemList;

    public LostItemAdapter(Context context, List<LostItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    public void setItemList(List<LostItem> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return itemList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_lost_found, parent, false);
        }

        TextView tvTitle = convertView.findViewById(R.id.tvItemTitle);
        TextView tvSub = convertView.findViewById(R.id.tvItemSubtitle);

        LostItem item = itemList.get(position);
        tvTitle.setText(item.getPostType() + " " + item.getName() + " ...");
        tvSub.setText(item.getCategory() + " | " + item.getLocation());
        return convertView;
    }
}

