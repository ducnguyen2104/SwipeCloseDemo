package com.ducnguyenvan.swipeclosedemo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<ItemRow> items;
    Context context;

    public MyAdapter(ArrayList<ItemRow> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_row,parent,false);
        return new ItemRowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ItemRow item = items.get(position);
        ((ItemRowViewHolder)holder).bind(item);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    private static class ItemRowViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public ItemRowViewHolder(View itemView) {
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.title);
        }

        public void bind(ItemRow item) {
            title.setText(item.getTitle());
        }
    }
}
