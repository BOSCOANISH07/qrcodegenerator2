package com.example.qrcodegenerator.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrcodegenerator.R;
import com.example.qrcodegenerator.models.QRCodeItem;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class QRCodeAdapter extends FirestoreRecyclerAdapter<QRCodeItem, QRCodeAdapter.QRCodeViewHolder> {

    private OnItemClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public QRCodeAdapter(@NonNull FirestoreRecyclerOptions<QRCodeItem> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull QRCodeViewHolder holder, int position, @NonNull QRCodeItem model) {
        holder.tvType.setText(model.getType());
        holder.tvData.setText(model.getData());
        holder.tvDate.setText(dateFormat.format(model.getTimestamp()));
    }

    @NonNull
    @Override
    public QRCodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_qr_code, parent, false);
        return new QRCodeViewHolder(view);
    }

    class QRCodeViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvData, tvDate;

        public QRCodeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvType);
            tvData = itemView.findViewById(R.id.tvData);
            tvDate = itemView.findViewById(R.id.tvDate);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getSnapshots().getSnapshot(position), position);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}