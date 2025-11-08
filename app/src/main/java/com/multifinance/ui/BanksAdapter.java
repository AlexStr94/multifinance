package com.multifinance.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.multifinance.R;
import com.multifinance.data.model.Bank;

import java.util.ArrayList;
import java.util.List;

public class BanksAdapter extends RecyclerView.Adapter<BanksAdapter.BankViewHolder> {

    public interface OnBankClickListener {
        void onBankClick(Bank bank);
    }

    private List<Bank> banks;
    private final OnBankClickListener listener;

    public BanksAdapter(List<Bank> banks, OnBankClickListener listener) {
        this.banks = banks;
        this.listener = listener;
    }

    public void setItems(List<Bank> newBanks) {
        this.banks = newBanks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bank, parent, false);
        return new BankViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BankViewHolder holder, int position) {
        Bank bank = banks.get(position);
        holder.name.setText(bank.getName());

        holder.itemView.setOnClickListener(v -> listener.onBankClick(bank));

        // Поддержка base64 логотипа
        if (bank.getPicture() != null && !bank.getPicture().isEmpty()) {
            try {
                byte[] decoded = Base64.decode(bank.getPicture(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                holder.logo.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.logo.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.secondaryText));
            }
        } else {
            holder.logo.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.secondaryText));
        }
    }

    @Override
    public int getItemCount() {
        return banks != null ? banks.size() : 0;
    }

    static class BankViewHolder extends RecyclerView.ViewHolder {
        ImageView logo;
        TextView name;

        BankViewHolder(View itemView) {
            super(itemView);
            logo = itemView.findViewById(R.id.iv_bank_logo);
            name = itemView.findViewById(R.id.tv_bank_name);
        }
    }
}
