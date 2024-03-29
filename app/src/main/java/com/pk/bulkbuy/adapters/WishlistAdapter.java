package com.pk.bulkbuy.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pk.bulkbuy.R;
import com.pk.bulkbuy.activities.ProductDetails;
import com.pk.bulkbuy.database.DB_Handler;
import com.pk.bulkbuy.database.SessionManager;
import com.pk.bulkbuy.pojo.Product;
import com.pk.bulkbuy.utils.Constants;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Preeth on 1/7/2018
 */

public class WishlistAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<Product> productList;

    public WishlistAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return productList.size();
    }

    @Override
    public Object getItem(int i) {
        return productList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        // TODO Auto-generated method stub
        Holder holder = new Holder();
        View rowView;

        rowView = inflater.inflate(R.layout.wishlist_item, null);
        holder.title = rowView.findViewById(R.id.title);
        holder.price = rowView.findViewById(R.id.price);
        holder.remove = rowView.findViewById(R.id.remove);
        holder.imageView = rowView.findViewById(R.id.wish_img);

        holder.title.setText(productList.get(position).getName());
        holder.price.setText(productList.get(position).getPrice_range());

        String imageURL = productList.get(position).getImageURL();

        Picasso.get().load(imageURL).fit().error(R.drawable.ic_image_grey600_36dp).into(holder.imageView);

        // Product Item Click
        holder.itemLay = rowView.findViewById(R.id.itemLay);
        holder.itemLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProductDetails.class);
                intent.putExtra("ProductId", productList.get(position).getId());
                context.startActivity(intent);
            }
        });

        // Wish List Item Click
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Remove Item From Wish List
                DB_Handler db_handler = new DB_Handler(context);
                SessionManager sessionManager = new SessionManager(context);
                if (db_handler.removeShortlistedItem(productList.get(position).getId(), sessionManager.getSessionData(Constants.SESSION_EMAIL))) {
                    productList.remove(position);
                    notifyDataSetChanged();
                }
            }
        });

        return rowView;
    }

    public class Holder {
        RelativeLayout itemLay;
        TextView title, price;
        ImageView remove;
        ImageView imageView;
    }
}
