package com.example.thermsasapp;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

/**
 @author: Abeer Rafiq

 Purpose of Class: To properly display messages in recycler view.
 It will be used by the messageActivity class.
 */
public class messageAdapter extends RecyclerView.Adapter<messageAdapter.ViewHolder> {

    // Class variables
    private ArrayList<String> messages;
    private String typeItemRow;
    public messageAdapter(ArrayList<String> notif, String type){
        this.messages = notif;
        this.typeItemRow = type;
    }

    // Used to initialize viewHolder
    @Override
    public messageAdapter.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType){
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_row, parent, false);
        if (this.typeItemRow.equals("type1")) {
            view = inflater.inflate(R.layout.item_row, parent, false);
        } else if (this.typeItemRow.equals("type2")){
            view = inflater.inflate(R.layout.item_row_big_text_size, parent, false);
        }
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    // Used to bind view holder to the adapter
    @Override
    public void onBindViewHolder(messageAdapter.ViewHolder holder, int position){
        holder.notifyType.setText(messages.get(position));
    }

    // To get size of arrayList
    @Override
    public int getItemCount() {
        return messages.size();
    }

    // Holds item's views and is used to display the messages
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView notifyType;
        public ViewHolder(View itemView){
            super(itemView);
            notifyType = itemView.findViewById(R.id.title2);
        }
    }
}