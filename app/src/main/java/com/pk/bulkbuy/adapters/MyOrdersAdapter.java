package com.pk.bulkbuy.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pk.bulkbuy.R;
import com.pk.bulkbuy.activities.ProductDetails;
import com.pk.bulkbuy.database.DB_Handler;
import com.pk.bulkbuy.database.SessionManager;
import com.pk.bulkbuy.pojo.Cart;
import com.pk.bulkbuy.utils.Constants;
import com.pk.bulkbuy.utils.Util;
import com.squareup.picasso.Picasso;

import java.io.StringReader;
import java.util.List;

import static android.content.Context.SYSTEM_HEALTH_SERVICE;
import static com.pk.bulkbuy.service.SyncDBService.charRemoveAt;

/**
 * Created by Preeth on 1/7/2018
 */

public class MyOrdersAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<Cart> shoppingCart;
    private FirebaseDatabase database;

    public MyOrdersAdapter(Context context, List<Cart> shoppingCart) {
        this.context = context;
        this.shoppingCart = shoppingCart;
        database = FirebaseDatabase.getInstance();
        inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return shoppingCart.size();
    }

    @Override
    public Object getItem(int i) {
        return shoppingCart.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint({"ViewHolder", "SetTextI18n", "InflateParams"})
    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        // TODO Auto-generated method stub
        final Holder holder = new Holder();
        View rowView;

        rowView = inflater.inflate(R.layout.shoppingcart_item, null);
        holder.title = rowView.findViewById(R.id.title);
        holder.size = rowView.findViewById(R.id.size);
        holder.color = rowView.findViewById(R.id.color);
        holder.price = rowView.findViewById(R.id.price);
        holder.tax = rowView.findViewById(R.id.tax);
        holder.qty = rowView.findViewById(R.id.quantity);
        holder.remove = rowView.findViewById(R.id.remove);
        holder.qtyLay = rowView.findViewById(R.id.qtyLay);
        holder.status = rowView.findViewById(R.id.orderStatus);
        holder.imageView = rowView.findViewById(R.id.cart_img);
        holder.status.setVisibility(View.VISIBLE);
        if (shoppingCart.get(position).getStatus().compareToIgnoreCase("verified")==0){
            holder.status.setImageResource(R.drawable.verified);
        }

        String imageURL = shoppingCart.get(position).getProduct().getImageURL();
        Picasso.get().load(imageURL).fit().error(R.drawable.ic_image_grey600_36dp).into(holder.imageView);

        holder.title.setText(shoppingCart.get(position).getProduct().getName());
        holder.color.setText("Color: " + shoppingCart.get(position).getVariant().getColor());

        String size = String.valueOf(shoppingCart.get(position).getVariant().getSize());
        try {
            if (size != null && !size.equalsIgnoreCase("null") && !size.equalsIgnoreCase("0.0")) {
                holder.size.setText("Size: " + size);
            } else {
                holder.size.setVisibility(View.GONE);
            }
        } catch (NullPointerException e) {
            holder.size.setVisibility(View.GONE);
        }

        // Calculate Price Value
        final int[] quantity = {shoppingCart.get(position).getItemQuantity()};
        String taxName = shoppingCart.get(position).getProduct().getTax().getName();
        final Double taxValue = shoppingCart.get(position).getProduct().getTax().getValue();
        final Double priceValue = Double.valueOf(shoppingCart.get(position).getVariant().getPrice());

        holder.qty.setVisibility(View.VISIBLE);
        holder.qty.setText(String.valueOf("Quantity: "+quantity[0]));
        holder.price.setText("Rs." + Util.formatDouble(calculatePrice(taxValue, priceValue, quantity[0])));
        holder.tax.setText("("+taxName + ": Rs." + taxValue+")");


        // Product Item Click
        holder.itemLay = rowView.findViewById(R.id.itemLay);
        holder.itemLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProductDetails.class);
                intent.putExtra("ProductId", shoppingCart.get(position).getProduct().getId());
                context.startActivity(intent);
            }
        });

        // Hide Remove Button
        //holder.remove.setVisibility(View.GONE);
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Delete Item From DB
                DB_Handler db_handler = new DB_Handler(context);
                final String id = shoppingCart.get(position).getId();
                if (db_handler.deleteOrder(id)) {
                    shoppingCart.remove(position);
                    notifyDataSetChanged();

                    SessionManager sessionManager = new SessionManager(context);
                    String sessionEmail = sessionManager.getSessionData(Constants.SESSION_EMAIL);
                    sessionEmail = charRemoveAt(sessionEmail,sessionEmail.indexOf('.'));
                    final DatabaseReference ordersRef = database.getReference().child("Orders").child(sessionEmail).child(id);
                    ordersRef.removeValue();
                } else {
                    Toast.makeText(context, "error deleting item", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Hide Quantity Update Buttons
        holder.qtyLay.setVisibility(View.GONE);

        return rowView;
    }

    private Double calculatePrice(Double taxValue, Double priceValue, int quantity) {
        return (taxValue + priceValue) * quantity;
    }

    public class Holder {
        RelativeLayout itemLay;
        LinearLayout qtyLay;
        TextView title, price, size, color, tax, qty;
        ImageView remove, status, imageView;
    }
}
