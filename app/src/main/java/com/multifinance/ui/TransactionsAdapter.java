package com.multifinance.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.multifinance.R;
import com.multifinance.data.model.Transaction;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder> {

    private List<Transaction> transactions = new ArrayList<>();

    public TransactionsAdapter(List<Transaction> transactions) {
        if (transactions != null) {
            this.transactions = transactions;
        }
    }

    public void setItems(List<Transaction> items) {
        this.transactions = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction t = transactions.get(position);

        holder.tvDescription.setText(t.getTransactionInformation());
        holder.tvAmount.setText(String.format("%.2f ₽", t.getAmountValue()));

        // Цвет: доход зелёный, расход красный
        if (t.getAmountValue() >= 0) {
            holder.tvAmount.setTextColor(holder.itemView.getContext().getColor(R.color.incomeGreen));
        } else {
            holder.tvAmount.setTextColor(holder.itemView.getContext().getColor(R.color.expenseRed));
        }

        if (t.getValueDateTime() != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            holder.tvDate.setText(t.getValueDateTime());
        } else {
            holder.tvDate.setText("—");
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvAmount, tvDate;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvDate = itemView.findViewById(R.id.tv_date);
        }
    }
}
