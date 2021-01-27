package com.pk.bulkbuy.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.arch.core.executor.TaskExecutor;
import com.pk.bulkbuy.R;
import com.pk.bulkbuy.activities.ProductDetails;
import com.pk.bulkbuy.database.DB_Handler;
import com.pk.bulkbuy.database.SessionManager;
import com.pk.bulkbuy.pojo.Product;
import com.pk.bulkbuy.utils.Constants;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Preeth on 1/4/18
 */

public class ProductListAdapter extends BaseAdapter implements Filterable {

    private Context context;
    private LayoutInflater inflater;
    private List<Product> productList;
    private ValueFilter valueFilter;
    private List<Product> mStringFilterList;

    public ProductListAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        mStringFilterList = productList;
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
        final Holder holder = new Holder();
        View rowView;

        rowView = inflater.inflate(R.layout.product_grid_item, null);
        holder.name = rowView.findViewById(R.id.name);
        holder.price = rowView.findViewById(R.id.price);
        holder.heart = rowView.findViewById(R.id.heart);
        holder.imageView = rowView.findViewById(R.id.product_image);

        holder.name.setText(productList.get(position).getName());
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
                Activity activity = (Activity) context;
                activity.overridePendingTransition(0,0);
            }
        });

        if (productList.get(position).getShortlisted()) {
            holder.heart.setImageResource(R.drawable.ic_heart_grey);
        }

        // Wish List Item Click
        holder.heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add / Remove Item To Wish List
                DB_Handler db_handler = new DB_Handler(context);
                SessionManager sessionManager = new SessionManager(context);
                if (!productList.get(position).getShortlisted()) {
                    holder.heart.setImageResource(R.drawable.ic_heart_grey);
                    if (db_handler.shortlistItem(productList.get(position).getId(), sessionManager.getSessionData(Constants.SESSION_EMAIL)) > 0) {
                        productList.get(position).setShortlisted(true);
                        Toast.makeText(context, "Item Added To Wish List", Toast.LENGTH_LONG).show();
                    }
                } else {
                    holder.heart.setImageResource(R.drawable.ic_heart_grey600_24dp);
                    if (db_handler.removeShortlistedItem(productList.get(position).getId(), sessionManager.getSessionData(Constants.SESSION_EMAIL))) {
                        productList.get(position).setShortlisted(false);
                        Toast.makeText(context, "Item Removed From Wish List", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        return rowView;
    }

    @Override
    public Filter getFilter() {
        if(valueFilter==null) {

            valueFilter=new ValueFilter();
        }

        return valueFilter;
    }
    private class ValueFilter extends Filter {

        //Invoked in a worker thread to filter the data according to the constraint.
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results=new FilterResults();
            if(constraint!=null && constraint.length()>0){
                ArrayList<Product> filterList=new ArrayList<Product>();
                for(int i=0;i<mStringFilterList.size();i++){
                    if((mStringFilterList.get(i).getName().toUpperCase())
                            .contains(constraint.toString().toUpperCase())) {
                        Product product = new Product();
                        product.setName(mStringFilterList.get(i).getName());
                        product.setId(mStringFilterList.get(i).getId());
                        product.setDateAdded(mStringFilterList.get(i).getDateAdded());
                        product.setPrice_range(mStringFilterList.get(i).getPrice_range());
                        product.setShortlisted(mStringFilterList.get(i).getShortlisted());
                        product.setTax(mStringFilterList.get(i).getTax());
                        product.setVariants(mStringFilterList.get(i).getVariants());
                        product.setImageURL(mStringFilterList.get(i).getImageURL());
                        filterList.add(product);
                    }
                }
                results.count=filterList.size();
                results.values=filterList;
            }else{
                results.count=mStringFilterList.size();
                results.values=mStringFilterList;
            }
            return results;
        }


        //Invoked in the UI thread to publish the filtering results in the user interface.
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            productList=(ArrayList<Product>) results.values;
            notifyDataSetChanged();
        }
    }


public class Holder {
        RelativeLayout itemLay;
        TextView name, price;
        ImageView heart;
        ImageView imageView;
    }
}
