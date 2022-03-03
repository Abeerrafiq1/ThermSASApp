package com.example.thermsasapp;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

/**
 @author: Abeer Rafiq

 Purpose of Class: To properly display notifications in recycler view.
 It will be used by the notificationActivity class.
 */
public class notifyAdapter extends RecyclerView.Adapter<notifyAdapter.ViewHolder> {

    // Class variables
    private ArrayList<String> notifications;
    public notifyAdapter(ArrayList<String> notif){
        notifications = notif;
    }

    // Used to initialize viewHolder
    @Override
    public notifyAdapter.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType){
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_row, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    // Used to bind view holder to the adapter
    @Override
    public void onBindViewHolder(notifyAdapter.ViewHolder holder, int position){
        holder.notifyType.setText(notifications.get(position));
    }

    // To get size of arrayList
    @Override
    public int getItemCount() {
        return notifications.size();
    }

    // Holds item's views and is used to display the notifications
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView notifyType;
        public ViewHolder(View itemView){
            super(itemView);
            notifyType = itemView.findViewById(R.id.title2);
        }
    }
}