package com.example.utilityhub.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.utilityhub.R;
import com.example.utilityhub.models.PasswordItem;
import com.example.utilityhub.security.CryptoManager;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PasswordAdapter
        extends RecyclerView.Adapter<PasswordAdapter.PasswordViewHolder> {
    public interface OnPasswordActionListener {
        void onEdit(PasswordItem item);
        void onDelete(PasswordItem item);
    }

    private final Context context;
    private final List<PasswordItem> items;
    private final OnPasswordActionListener listener;

    private final CryptoManager cryptoManager = new CryptoManager();

    private final Set<String> visibleIds = new HashSet<>();

    public PasswordAdapter(Context context,
                           List<PasswordItem> items,
                           OnPasswordActionListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PasswordViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_password, parent, false);
        return new PasswordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PasswordViewHolder holder, int position) {
        PasswordItem item = items.get(position);
        holder.textService.setText(item.getService());
        holder.textLogin.setText(
                item.getLogin().isEmpty()
                        ? "Логін: —"
                        : "Логін: " + item.getLogin());
        boolean visible = visibleIds.contains(item.getId());
        if (visible) {
            try {
                holder.textPassword.setText(
                        cryptoManager.decrypt(item.getEncryptedPassword()));
                holder.btnShow.setText("Сховати");
            } catch (Exception e) {
                holder.textPassword.setText("Помилка розшифрування");
                holder.btnShow.setText("Показати");
                visibleIds.remove(item.getId());
            }
        } else {
            holder.textPassword.setText("••••••••");
            holder.btnShow.setText("Показати");
        }
        holder.btnShow.setOnClickListener(v -> {
            if (visibleIds.contains(item.getId())) {
                visibleIds.remove(item.getId());
            } else {
                visibleIds.add(item.getId());
            }
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(adapterPosition);
            }
        });
        holder.btnCopy.setOnClickListener(v -> copyPassword(item));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    private void copyPassword(PasswordItem item) {
        try {
            String plainPassword =
                    cryptoManager.decrypt(item.getEncryptedPassword());
            ClipboardManager clipboard = (ClipboardManager)
                    context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(
                    "password", plainPassword);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context,
                    "Пароль скопійовано",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context,
                    "Не вдалося розшифрувати пароль",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PasswordViewHolder extends RecyclerView.ViewHolder {
        TextView textService;
        TextView textLogin;
        TextView textPassword;
        Button btnShow;
        Button btnCopy;
        Button btnEdit;
        Button btnDelete;
        PasswordViewHolder(@NonNull View itemView) {
            super(itemView);
            textService = itemView.findViewById(R.id.textService);
            textLogin = itemView.findViewById(R.id.textLogin);
            textPassword = itemView.findViewById(R.id.textPassword);
            btnShow = itemView.findViewById(R.id.btnShow);
            btnCopy = itemView.findViewById(R.id.btnCopy);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
