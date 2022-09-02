package com.example.xercash10.Dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.xercash10.AddInvestment;
import com.example.xercash10.AddLoanActivity;
import com.example.xercash10.ItemActivity;
import com.example.xercash10.R;
import com.example.xercash10.ShoppingActivity;
import com.example.xercash10.TransferActivity;

public class AddTransactionDialog extends DialogFragment {

    private RelativeLayout shopping, investment, loan, transaction, item;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_transaction, null);
        shopping = view.findViewById(R.id.shoppingRelLayout);
        investment = view.findViewById(R.id.investmentRelLayout);
        loan = view.findViewById(R.id.loanRelLayout);
        transaction = view.findViewById(R.id.sendrecvRelLayout);
        item = view.findViewById(R.id.addItemRelLayout);

        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent x6 = new Intent(getActivity(), ItemActivity.class);
                startActivity(x6);
            }
        });
        shopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent x = new Intent(getActivity(), ShoppingActivity.class);
                startActivity(x);
            }
        });
        loan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent x1 = new Intent(getActivity(), AddLoanActivity.class);
                startActivity(x1);
            }
        });
        investment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent x1 = new Intent(getActivity(), AddInvestment.class);
                startActivity(x1);
            }
        });
        transaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent x2 = new Intent(getActivity(), TransferActivity.class);
                startActivity(x2);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Add Transaction")
                .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setView(view);

        return builder.create();
    }
}
