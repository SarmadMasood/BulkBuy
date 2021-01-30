package com.pk.bulkbuy.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pk.bulkbuy.R;
import com.pk.bulkbuy.adapters.MyOrdersAdapter;
import com.pk.bulkbuy.database.DB_Handler;
import com.pk.bulkbuy.database.SessionManager;
import com.pk.bulkbuy.pojo.Cart;
import com.pk.bulkbuy.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Preeth on 1/7/2018
 */

public class MyOrders extends AppCompatActivity {

    MyOrdersAdapter adapter;
    List<Cart> orderHistory;
    FirebaseUser user;
    FirebaseAuth auth;
    FirebaseDatabase database;
    ListView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myorders);

        database = FirebaseDatabase.getInstance();

        // Set Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set Title
        TextView titleToolbar = findViewById(R.id.titleToolbar);
        titleToolbar.setText(R.string.my_orders);

        // Back Button
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // Hide Cart Icon
        ImageView cart = findViewById(R.id.cart);
        cart.setVisibility(View.GONE);

        // Get Orders From DB
        DB_Handler db_handler = new DB_Handler(this);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        orderHistory = db_handler.getOrders(user.getUid());

        adapter = new MyOrdersAdapter(this,orderHistory);
        fetchOrders();

        // Fill ListView
        listView = findViewById(R.id.listview);
        listView.setAdapter(adapter);
    }

    protected void fetchOrders() {
        final String uid = user.getUid();
        final DB_Handler db_handler = new DB_Handler(this);
        DatabaseReference ordersRef = database.getReference().child("Orders").child(uid);
        ordersRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                List<Cart> orders = new ArrayList<>();
                Cart order = snapshot.getValue(Cart.class);
                orders.add(order);
                db_handler.insertOrderHistory(orders, uid);
                orderHistory = db_handler.getOrders(user.getUid());
                adapter = new MyOrdersAdapter(getApplicationContext(),orderHistory);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                List<Cart> orders = new ArrayList<>();
                Cart order = snapshot.getValue(Cart.class);
                if (order.getStatus().compareToIgnoreCase("verified") == 0) {
                    orders.add(order);
                }
                db_handler.insertOrderHistory(orders, uid);
                orderHistory = db_handler.getOrders(user.getUid());
                adapter = new MyOrdersAdapter(getApplicationContext(),orderHistory);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                db_handler.deleteOrder(snapshot.getKey());
                    orderHistory = db_handler.getOrders(user.getUid());
                    adapter = new MyOrdersAdapter(getApplicationContext(),orderHistory);
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }
}
