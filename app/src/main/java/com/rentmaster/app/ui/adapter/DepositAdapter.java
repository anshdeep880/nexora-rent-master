package com.rentmaster.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.rentmaster.app.R;
import com.rentmaster.app.data.entity.Deposit;
import com.rentmaster.app.util.FormatUtils;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DepositAdapter extends RecyclerView.Adapter<DepositAdapter.DepositViewHolder> {
    private List<Deposit> deposits = new ArrayList<>();

    @NonNull
    @Override
    public DepositViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new DepositViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DepositViewHolder holder, int position) {
        Deposit deposit = deposits.get(position);
        holder.text1.setText("$" + deposit.amount + (deposit.refunded ? " (Refunded)" : ""));
        holder.text2.setText(FormatUtils.formatDateWithMonth(deposit.date) + " - " + deposit.notes);
    }

    @Override
    public int getItemCount() {
        return deposits.size();
    }

    public void setDeposits(List<Deposit> deposits) {
        this.deposits = deposits;
        notifyDataSetChanged();
    }

    static class DepositViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;
        DepositViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
