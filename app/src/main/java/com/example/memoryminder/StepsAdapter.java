package com.example.memoryminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StepsAdapter extends RecyclerView.Adapter<StepsAdapter.ViewHolder> {

    private final Context context;
    private final List<StepItem> stepItems;
    private final OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(String steps, String dataKey);
        void onItemLongClick(String dataKey);
    }

    public StepsAdapter(Context context, List<StepItem> stepItems, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.stepItems = stepItems;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_step, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StepItem item = stepItems.get(position);
        holder.textViewTitle.setText("Steps:");
        holder.textViewDescription.setText(item.getSteps());

        holder.cardView.setOnClickListener(v -> onItemClickListener.onItemClick(item.getSteps(), item.getDataKey()));
        holder.cardView.setOnLongClickListener(v -> {
            onItemClickListener.onItemLongClick(item.getDataKey());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return stepItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textViewTitle;
        final TextView textViewDescription;
        final CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            cardView = (CardView) itemView; // Cast to CardView
        }
    }
}
