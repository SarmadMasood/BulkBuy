package com.pk.bulkbuy.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pk.bulkbuy.R;
import com.pk.bulkbuy.SplashActivity;
import com.pk.bulkbuy.activities.MyOrders;
import com.pk.bulkbuy.database.DB_Handler;
import com.pk.bulkbuy.database.SessionManager;
import com.pk.bulkbuy.interfaces.FinishActivity;
import com.pk.bulkbuy.pojo.User;
import com.pk.bulkbuy.utils.Constants;

/**
 * Created by Preeth on 1/6/2018
 */

public class Account extends Fragment {

    DB_Handler db_handler;
    TextView name, email, mobile;
    RelativeLayout orders, logoutLay;
    FinishActivity finishActivityCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        finishActivityCallback = (FinishActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.account, container, false);

        // Get User
        db_handler = new DB_Handler(getActivity());
        SessionManager sessionManager = new SessionManager(getActivity());
        User user = db_handler.getUser(sessionManager.getSessionData(Constants.SESSION_EMAIL));

        // Set Values
        setIds(view);
        setValues(user);
        setClickListeners();

        return view;
    }

    // Set Ids
    private void setIds(View view) {
        logoutLay = view.findViewById(R.id.logoutLay);
        name = view.findViewById(R.id.name);
        email = view.findViewById(R.id.email);
        mobile = view.findViewById(R.id.mobile);
        orders = view.findViewById(R.id.myOrdersLay);
    }

    // Set Values
    private void setValues(User user) {
        // Name
        name.setText(user.getName());

        // Email
        email.setText(user.getEmail());

        // Mobile
        mobile.setText(user.getMobile());
    }

    // Set Click Listeners
    private void setClickListeners() {
        // My Orders
        orders.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MyOrders.class);
                startActivity(intent);
                getActivity().overridePendingTransition(0,0);
            }
        });

        // Logout
        logoutLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SessionManager sessionManager = new SessionManager(getActivity());
                sessionManager.clearPreferences();
                Intent intent = new Intent(getActivity(), SplashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finishActivityCallback.finishActivity();
            }
        });
    }
}
