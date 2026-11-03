package com.example.utilityhub.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.utilityhub.R;
import com.example.utilityhub.models.TileItem;
import java.util.List;

public class TileAdapter extends RecyclerView.Adapter<TileAdapter.TileViewHolder> {
    private final Context context;
    private final List<TileItem> tileList;

    public TileAdapter(Context context, List<TileItem> tileList) {
        this.context = context;
        this.tileList = tileList;
    }

    @NonNull
    @Override
    public TileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tile, parent, false);
        return new TileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TileViewHolder holder, int position) {
        TileItem item = tileList.get(position);
        holder.textTitle.setText(item.getTitle());
        holder.imageIcon.setImageResource(item.getIconResId());

        holder.itemView.setOnClickListener(v -> {
            if (item.getTargetActivity() != null) {
                Intent intent = new Intent(context, item.getTargetActivity());
                context.startActivity(intent);
            } else {
                // Плитка-заглушка: повідомлення за формулюванням із завдання ЛР 1
                Toast.makeText(context, item.getTitle() + " перебуває в розробці",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return tileList.size();
    }

    static class TileViewHolder extends RecyclerView.ViewHolder {
        ImageView imageIcon;
        TextView textTitle;

        public TileViewHolder(@NonNull View itemView) {
            super(itemView);
            imageIcon = itemView.findViewById(R.id.imageTileIcon);
            textTitle = itemView.findViewById(R.id.textTileTitle);
        }
    }
}
