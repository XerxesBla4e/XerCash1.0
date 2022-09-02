package com.example.xercash10.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xercash10.Models.Investment;
import com.example.xercash10.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class InvestmentAdapter extends RecyclerView.Adapter<InvestmentAdapter.ViewHolder> {

    private ArrayList<Investment> investments = new ArrayList<>();
    private int num = -1;
    private Context context;

    public InvestmentAdapter(Context context) {
        this.context = context;
    }

    public InvestmentAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_investment, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(investments.get(position).getName());
        holder.initDate.setText(investments.get(position).getInit_date());
        holder.finishDate.setText(investments.get(position).getFinish_date());
        holder.amount.setText(String.valueOf(investments.get(position).getAmount()));
        holder.roi.setText(String.valueOf(investments.get(position).getMonthly_roi()));
        holder.profit.setText(String.valueOf(getTotalProfit(investments.get(position))));

        if (num == -1) {
            holder.parent.setCardBackgroundColor(context.getResources().getColor(R.color.lightblue));
            num = 1;
        } else {
            holder.parent.setCardBackgroundColor(context.getResources().getColor(R.color.lightGreen));
            num = -1;
        }
    }

    private double getTotalProfit(Investment investment) {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        double profit = 0.0;
        try {
            calendar.setTime(sdf.parse(investment.getInit_date()));
            int initMonths = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);
            calendar.setTime(sdf.parse(investment.getFinish_date()));
            int finishMonths = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);
            int months = finishMonths - initMonths;

            for (int i = 0; i < months; i++) {
                profit += investment.getAmount() * investment.getMonthly_roi() / 100;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return profit;
    }

    public void setInvestments(ArrayList<Investment> investments) {
        this.investments = investments;
        notifyDataSetChanged();
    }

    @Override

    public int getItemCount() {
        return investments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name, initDate, finishDate, roi, profit, amount;
        private CardView parent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.txtInvestmentName);
            initDate = itemView.findViewById(R.id.txtInitDate);
            finishDate = itemView.findViewById(R.id.txtFinDate);
            roi = itemView.findViewById(R.id.txtROI);
            profit = itemView.findViewById(R.id.txtProfit);
            amount = itemView.findViewById(R.id.txtAmount);
            parent = itemView.findViewById(R.id.parent);
        }
    }
}
