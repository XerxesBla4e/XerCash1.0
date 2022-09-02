package com.example.xercash10.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xercash10.Models.Item;
import com.example.xercash10.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {

    public interface GetItem {
        void OnGettingItemResult(Item item);
    }

    private GetItem getItem;
    private ArrayList<Item> items = new ArrayList<>();
    private Context context;
    private DialogFragment dialogFragment;

    public ItemsAdapter(Context context, DialogFragment dialogFragment) {
        this.context = context;
        this.dialogFragment = dialogFragment;
    }

    public ItemsAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_items, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(items.get(position).getName());
        Picasso.get().load(items.get(position).getImage_url())
                .into(holder.image);
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    getItem = (GetItem) dialogFragment;
                    getItem.OnGettingItemResult(items.get(position));
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        private TextView name;
        private CardView parent;

        public ViewHolder(View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.itemImage);
            name = itemView.findViewById(R.id.itemName);
            parent = itemView.findViewById(R.id.parent);
        }
    }
}
