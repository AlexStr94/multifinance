package com.multifinance.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.multifinance.R;
import com.multifinance.data.model.Account;

import java.util.List;

public class AccountsAdapter extends RecyclerView.Adapter<AccountsAdapter.ViewHolder> {

    public interface OnAccountClickListener {
        void onClick(Account account);
    }

    private List<Account> items;
    private final OnAccountClickListener listener;

    public AccountsAdapter(List<Account> items, OnAccountClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setItems(List<Account> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AccountsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountsAdapter.ViewHolder holder, int position) {
        Account acc = items.get(position);
        holder.tvName.setText(acc.getDisplayName());
        holder.tvBalance.setText(String.format("Баланс: %.2f ₽", acc.getBalance()));
        holder.itemView.setOnClickListener(v -> listener.onClick(acc));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvBalance;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_account_name);
            tvBalance = itemView.findViewById(R.id.tv_account_balance);
        }
    }
}
