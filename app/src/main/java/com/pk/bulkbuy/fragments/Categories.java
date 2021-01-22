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
import com.pk.bulkbuy.adapters.CategoryListAdapter;
import com.pk.bulkbuy.database.DB_Handler;
import com.pk.bulkbuy.pojo.Category;

import java.util.List;

/**
 * Created by Preeth on 1/3/2018
 */

public class Categories extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.listview, container, false);

        // load products
        DB_Handler db_handler = new DB_Handler(getActivity());
        List<Category> categoryList = db_handler.getCategoryList();

        // fill listview with data
        ListView listView= view.findViewById(R.id.listview);
        listView.setAdapter(new CategoryListAdapter(getActivity(), categoryList));

        return view;
    }
}
