package com.example.thermsasapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class notifyAdapter extends RecyclerView.Adapter<notifyAdapter.ViewHolder> {

    private ArrayList<String> notifications;
    public notifyAdapter(ArrayList<String> notif){
        notifications = notif;
    }

    @Override
    public notifyAdapter.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType){
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_row, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(notifyAdapter.ViewHolder holder, int position){
        holder.notifyType.setText(notifications.get(position));
    }
    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView notifyType;
        public ViewHolder(View itemView){
            super(itemView);
            notifyType = itemView.findViewById(R.id.title2);
        }

    }

}