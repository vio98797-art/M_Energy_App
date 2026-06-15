package com.menergy.creditledger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class LedgerAdapter extends ArrayAdapter<LedgerModel> {

    public LedgerAdapter(Context context, ArrayList<LedgerModel> list) {
        super(context, 0, list);
    }

    private static class ViewHolder {
        TextView tvName;
        TextView tvDate;
        TextView tvAmount;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_ledger, parent, false);
            holder = new ViewHolder();
            holder.tvName = convertView.findViewById(R.id.tvItemName);
            holder.tvDate = convertView.findViewById(R.id.tvItemDate);
            holder.tvAmount = convertView.findViewById(R.id.tvItemAmount);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LedgerModel model = getItem(position);
        if (model != null) {
            holder.tvName.setText(model.getName());
            holder.tvDate.setText("ရက်စွဲ: " + model.getDate());

            String amountText = String.format(Locale.getDefault(), "%,d MMK", model.getAmount());

            if ("Pay".equalsIgnoreCase(model.getType())) {
                holder.tvAmount.setText("- " + amountText);
                holder.tvAmount.setTextColor(0xFFFF5252);
            } else {
                holder.tvAmount.setText("+ " + amountText);
                holder.tvAmount.setTextColor(0xFF4CAF50);
            }
        }

        return convertView;
    }
}
