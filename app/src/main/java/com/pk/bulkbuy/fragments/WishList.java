package com.pk.bulkbuy.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.pk.bulkbuy.R;
import com.pk.bulkbuy.adapters.WishlistAdapter;
import com.pk.bulkbuy.database.DB_Handler;
import com.pk.bulkbuy.database.SessionManager;
import com.pk.bulkbuy.pojo.Product;
import com.pk.bulkbuy.utils.Constants;

import java.util.List;

/**
 * Created by Preeth on 1/7/2018
 */

public class WishList extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.listview, container, false);

        // get data
        DB_Handler db_handler = new DB_Handler(getActivity());
        SessionManager sessionManager = new SessionManager(getActivity());
        List<Product> productList = db_handler.getShortListedItems(sessionManager.getSessionData(Constants.SESSION_EMAIL));

        // fill listview with data
        ListView listView= view.findViewById(R.id.listview);
        listView.setAdapter(new WishlistAdapter(getActivity(), productList));
        return view;
    }
}
