package com.pk.bulkbuy.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pk.bulkbuy.database.DB_Handler;
import com.pk.bulkbuy.database.SessionManager;
import com.pk.bulkbuy.pojo.Cart;
import com.pk.bulkbuy.pojo.Category;
import com.pk.bulkbuy.pojo.Product;
import com.pk.bulkbuy.pojo.Tax;
import com.pk.bulkbuy.pojo.Variant;
import com.pk.bulkbuy.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Preeth on 1/6/2018
 */

public class SyncDBService extends IntentService {

    DB_Handler db_handler;
    Intent intent;
    FirebaseDatabase database;
    DatabaseReference catRef, subcatRef, sub2catRef;

    public SyncDBService(String name) {
        super(name);
    }

    public SyncDBService() {
        super("SyncDBService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        db_handler = new DB_Handler(this);
        database = FirebaseDatabase.getInstance();
        catRef = database.getReference().child("Categories");
        subcatRef = database.getReference().child("Categories").child("2");
        sub2catRef = database.getReference().child("Categories").child("3");

         //putData();
         fetchAndProcessData();
       // fetchData();
        this.intent = intent;
    }

    public static String charRemoveAt(String str, int p) {
        if (p>=0){
            return str.substring(0, p) + str.substring(p + 1);
        }
        return str;
    }

    private void putData(){
        String id = "9999";

        Product product = new Product();
        product.setId(id);
        product.setName("Apple Macbook Pro");
        Tax tax = new Tax();
        tax.setName("tax");
        tax.setValue(786.0);
        product.setTax(tax);
        product.setShortlisted(false);
        product.setPrice_range("1299$-2199$");
        product.setDateAdded("01-24-2021");
        product.setImageURL("https://megastore.pk/wp-content/uploads/2020/10/macbook-pro-13-og-202005-2.jpg");

        List<Variant> variants = new ArrayList<>();
        Variant variant1 = new Variant();
        variant1.setId(UUID.randomUUID().toString());
        variant1.setSize("13 inch");
        variant1.setColor("Rose Gold");
        variant1.setPrice("1299");

        Variant variant2 = new Variant();
        variant2.setId(UUID.randomUUID().toString());
        variant2.setSize("15 inch");
        variant2.setPrice("2199");
        variant2.setColor("Space Grey");
        variants.add(variant1);
        variants.add(variant2);
        product.setVariants(variants);
        List<Product> products = new ArrayList<>();
        products.add(product);

        Category category = new Category();
        category.setId(UUID.randomUUID().toString());
        category.setName("Laptops");

        Category subcategory = new Category();
        subcategory.setId(UUID.randomUUID().toString());
        subcategory.setName("Apple Laptops");

        Category sub2 = new Category();
        sub2.setId(UUID.randomUUID().toString());
        sub2.setName("Macbooks");
        sub2.setProducts(products);

        List<String> subcategories2 = new ArrayList<>();
        subcategories2.add(sub2.getId());
        subcategory.setChildCategories(subcategories2);


        List<String> subcategories = new ArrayList<>();
        subcategories.add(subcategory.getId());
        category.setChildCategories(subcategories);
        catRef.child("1").setValue(category);
        subcatRef.setValue(subcategory);
        sub2catRef.setValue(sub2);
    }

    private void fetchAndProcessData(){
        final List<Category> categories = new ArrayList<>();
        catRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int size = (int) snapshot.getChildrenCount();
                for (int i=0;i<size;i++){
                    categories.add(snapshot.child(String.valueOf(i+1)).getValue(Category.class));
                }

                for (int k=0;k<categories.size();k++) {
                    Category category = categories.get(k);
                    final String CategoryID = category.getId();
                    String CategoryName = category.getName();

                    // insert category into local DB
                    db_handler.insertCategories(CategoryID, CategoryName);

                    List<Product> productList = category.getProducts();
                    if (productList!=null){
                        for (int j = 0; j < productList.size(); j++) {
                            String ProductID = productList.get(j).getId();
                            String ProductName = productList.get(j).getName();
                            String imageURL = productList.get(j).getImageURL();
                            String Date = productList.get(j).getDateAdded();
                            String TaxName = productList.get(j).getTax().getName();
                            Double TaxValue = productList.get(j).getTax().getValue();

                            // insert products into local DB
                            db_handler.insertProducts(ProductID, CategoryID, ProductName, Date, TaxName, TaxValue, imageURL);

                            // Get Variants
                            List<Variant> variantList = productList.get(j).getVariants();
                            for (int p = 0; p < variantList.size(); p++) {
                                String VariantID = variantList.get(p).getId();
                                String Size = null;
                                String Color = variantList.get(p).getColor();
                                String Price = String.valueOf(variantList.get(p).getPrice());

                                try {
                                    // Size May Produce NullPointerException
                                    Size = variantList.get(p).getSize().toString();
                                } catch (NullPointerException ignore) {
                                }

                                // insert variants into local DB
                                db_handler.insertVariants(VariantID, Size, Color, Price, ProductID);

                            }
                        }
                    }
                    List<String> childCategories = category.getChildCategories();
                    if (childCategories!=null) {
                        for (int l = 0; l < childCategories.size(); l++) {

                            String SubcategoryID = childCategories.get(l);

                            // insert childs into subcategory mapping
                            db_handler.insertChildCategoryMapping(CategoryID, SubcategoryID);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user!=null) {
            final String uid = user.getUid();
            DatabaseReference ordersRef = database.getReference().child("Orders").child(uid);
            if (uid!="" && uid!=null){
                ordersRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        List<Cart> orders = new ArrayList<>();
                        Cart order = snapshot.getValue(Cart.class);
                        orders.add(order);
                        db_handler.insertOrderHistory(orders,uid);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        List<Cart> orders = new ArrayList<>();
                        Cart order = snapshot.getValue(Cart.class);
                        if (order.getStatus().compareToIgnoreCase("verified")==0){
                            orders.add(order);
                        }
                        db_handler.insertOrderHistory(orders,uid);
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                            db_handler.deleteOrder(snapshot.getKey());
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
//                ordersRef.addValueEventListener(new ValueEventListener() {
//                    DB_Handler db_handler = new DB_Handler(getApplicationContext());
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        List<Cart> storedOrders = db_handler.getOrders(uid);
//
//                        for (int i=0;i<storedOrders.size();i++){
//                            System.out.println(storedOrders.get(i).getId());
//                            DatabaseReference subOrderRef = ordersRef.child(storedOrders.get(i).getId());
//                            subOrderRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError error) {
//
//                                }
//                            });
//
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
            }
        }
    }

    // Fetch Data From URL
//    private void fetchData() {
//        // Initialize Retrofit
//        RetrofitBuilder retrofitBuilder = new RetrofitBuilder(this);
//        OkHttpClient httpClient = retrofitBuilder.setClient();
//        Retrofit retrofit = retrofitBuilder.retrofitBuilder(httpClient);
//        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);
//
//        // Call Web Service
//        Call<ResponseJSON> call = retrofitInterface.fetchData();
//        call.enqueue(new Callback<ResponseJSON>() {
//            @Override
//            public void onResponse(Call<ResponseJSON> call, Response<ResponseJSON> response) {
//                try {
//                    if (response.body() != null) {
//                        processData(response.body());
//                    }
//                } catch (Exception ignore) {
//                    reply(getResources().getString(R.string.Error500));
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseJSON> call, Throwable t) {
//                reply(Util.getErrorMessage(t,SyncDBService.this));
//            }
//        });
//    }
//
//    // Process JSON and Save In Local DB
//    private void processData(ResponseJSON responseJSON) {
//        try {
//
//            // Get Categories
//            List<Category> categoryList = responseJSON.getCategories();
//            for (int i = 0; i < categoryList.size(); i++) {
//
//                int CategoryID = responseJSON.getCategories().get(i).getId();
//                String CategoryName = responseJSON.getCategories().get(i).getName();
//
//                // insert category into local DB
//                db_handler.insertCategories(CategoryID, CategoryName);
//
//                // Get Products
//                List<Product> productList = responseJSON.getCategories().get(i).getProducts();
//                for (int j = 0; j < productList.size(); j++) {
//                    Integer ProductID = productList.get(j).getId();
//                    String ProductName = productList.get(j).getName();
//                    String Date = productList.get(j).getDateAdded();
//                    String TaxName = productList.get(j).getTax().getName();
//                    Double TaxValue = productList.get(j).getTax().getValue();
//
//                    // insert products into local DB
//                    db_handler.insertProducts(ProductID, CategoryID, ProductName, Date, TaxName, TaxValue, ima);
//
//                    // Get Variants
//                    List<Variant> variantList = productList.get(j).getVariants();
//                    for (int p = 0; p < variantList.size(); p++) {
//                        int VariantID = variantList.get(p).getId();
//                        String Size = null;
//                        String Color = variantList.get(p).getColor();
//                        String Price = String.valueOf(variantList.get(p).getPrice());
//
//                        try {
//                            // Size May Produce NullPointerException
//                            Size = variantList.get(p).getSize().toString();
//                        } catch (NullPointerException ignore) {
//                        }
//                        // insert variants into local DB
//                        db_handler.insertVariants(VariantID, Size, Color, Price, ProductID);
//                    }
//                }
//
//                // Get Child Categories
//                List<Integer> childCategories = categoryList.get(i).getChildCategories();
//                for (int k = 0; k < childCategories.size(); k++) {
//                    int SubcategoryID = childCategories.get(k);
//
//                    // insert childs into subcategory mapping
//                    db_handler.insertChildCategoryMapping(CategoryID, SubcategoryID);
//                }
//            }
//
//            // Get Rankings
//            List<Ranking> rankingList = responseJSON.getRankings();
//            for (int i = 0; i < rankingList.size(); i++) {
//                // Get Products Rank List
//                List<ProductRank> productRankList = rankingList.get(i).getProducts();
//                for (int j = 0; j < productRankList.size(); j++) {
//                    try {
//                        int id = productRankList.get(j).getId();
//                        switch (i) {
//                            case 0: // Most Viewed Products
//                                int viewCount = productRankList.get(j).getViewCount();
//
//                                // update product table
//                                db_handler.updateCounts(DB_Handler.VIEW_COUNT, viewCount, id);
//                                break;
//
//                            case 1: // Most Ordered Products
//                                int orderCount = productRankList.get(j).getOrderCount();
//
//                                // update product table
//                                db_handler.updateCounts(DB_Handler.ORDER_COUNT, orderCount, id);
//                                break;
//
//                            case 2: // Most Shared Products
//                                int shareCount = productRankList.get(j).getShares();
//
//                                // update product table
//                                db_handler.updateCounts(DB_Handler.SHARE_COUNT, shareCount, id);
//                                break;
//                        }
//                    } catch (Exception ignore) {
//                    }
//                }
//            }
//
//            reply("success");
//            Log.i("DB Sync","success");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    // Reply Message
    private void reply(String value)
    {
        Bundle bundle = intent.getExtras();
        bundle.putString("message",value);
        if (bundle != null) {
            Messenger messenger = (Messenger) bundle.get("messenger");
            Message msg = Message.obtain();
            msg.setData(bundle); //put the data here
            try {
                messenger.send(msg);
            } catch (RemoteException e) {
                Log.i("error", "error");
            }
        }
    }
}
