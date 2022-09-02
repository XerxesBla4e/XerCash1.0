package com.example.xercash10.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.PointerIcon;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xercash10.Models.Loan;
import com.example.xercash10.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class LoanAdapter extends RecyclerView.Adapter<LoanAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Loan> loans = new ArrayList<>();
    private int num = -1;

    public LoanAdapter(Context context) {
        this.context = context;
    }

    public LoanAdapter() {

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_loan, parent, false);
        return new ViewHolder(view);
    }

    public void setLoans(ArrayList<Loan> loans) {
        this.loans = loans;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(loans.get(position).getName());
        holder.initDate.setText(loans.get(position).getInit_date());
        holder.finishDate.setText(loans.get(position).getFinish_date());
        holder.amount.setText(String.valueOf(loans.get(position).getRemained_amount()));
        holder.roi.setText(String.valueOf(loans.get(position).getMonthly_roi()));
        holder.remained_amount.setText(String.valueOf(loans.get(position).getRemained_amount()));

        holder.loss.setText(String.valueOf(getTotalLoss(loans.get(position))));
        holder.monthly_payment.setText(String.valueOf(loans.get(position).getMonthly_payment()));

        if (num == -1) {
            holder.parent.setCardBackgroundColor(context.getResources().getColor(R.color.lightblue));
            num = 1;
        } else {
            holder.parent.setCardBackgroundColor(context.getResources().getColor(R.color.lightGreen));
            num = -1;
        }
    }

    private double getTotalLoss(Loan loan) {
        double loss = 0.0;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date initDate = null;
        try {
            initDate = sdf.parse(loan.getInit_date());
            calendar.setTime(initDate);
            int initMonth = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);

            Date finishDate = sdf.parse(loan.getFinish_date());
            calendar.setTime(finishDate);
            int finishMonth = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);

            int months = finishMonth - initMonth;
            for (int i = 0; i < months; i++) {
                loss += loan.getInit_amount() * loan.getMonthly_roi() / 100;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return loss;
    }

    @Override
    public int getItemCount() {
        return loans.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name, initDate, finishDate, roi, loss, amount, remained_amount, monthly_payment;
        private CardView parent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.txtInvestmentName);
            initDate = itemView.findViewById(R.id.txtInitDate);
            finishDate = itemView.findViewById(R.id.txtFinDate);
            roi = itemView.findViewById(R.id.txtROI);
            loss = itemView.findViewById(R.id.txtLoss);
            amount = itemView.findViewById(R.id.txtAmount);
            parent = itemView.findViewById(R.id.parent);
            monthly_payment = itemView.findViewById(R.id.txtMonthlyPayment);
            remained_amount = itemView.findViewById(R.id.txtRemainedAmount);
        }
    }
}
