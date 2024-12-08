package com.example.microadventure;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivityAdapter extends RecyclerView.Adapter<HistoryActivityAdapter.ViewHolder> {
    private final List<ActivityHistory> activityHistoryList;

    public HistoryActivityAdapter(List<ActivityHistory> activityHistoryList) {
        this.activityHistoryList = activityHistoryList != null ? activityHistoryList : new ArrayList<>();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewActivity;
        TextView textViewDate;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewActivity = itemView.findViewById(R.id.textViewActivity);
            textViewDate = itemView.findViewById(R.id.textViewDate);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ActivityHistory activity = activityHistoryList.get(position);
        holder.textViewActivity.setText(activity.getActivityName());
        holder.textViewDate.setText(activity.getDate());
    }

    @Override
    public int getItemCount() {
        return activityHistoryList.size();
    }
}
