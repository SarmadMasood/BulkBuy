package com.pk.bulkbuy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import com.pk.bulkbuy.database.DB_Handler;
import com.pk.bulkbuy.database.SessionManager;
import com.pk.bulkbuy.fragments.SignIn;
import com.pk.bulkbuy.fragments.SignUp;
import com.pk.bulkbuy.interfaces.FinishActivity;
import com.pk.bulkbuy.service.SyncDBService;
import com.pk.bulkbuy.utils.Constants;

/**
 * Created by Preeth on 1/3/2018
 */

public class SplashActivity extends AppCompatActivity implements FinishActivity {

    DB_Handler db_handler;
    Button signIn, signUp;
    Handler handler;
    TableLayout bottomLay;
    Snackbar snackbar = null;
    CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

//        // Service To Fetch Data From URL
//        setHandler();
//        startIntentService();

        // Initialize DB Handler
        db_handler = new DB_Handler(this);

        setIds();
        setClickListeners();
        //fetch some data here
        checkSession();
        // Service To Fetch Data From URL
        setHandler();
        startIntentService();
    }

    // Set Ids
    private void setIds() {
        signIn = findViewById(R.id.signin);
        signUp = findViewById(R.id.signup);
        bottomLay = findViewById(R.id.bottomLay);
        coordinatorLayout = findViewById(R.id.coordinatorLay);
    }

    // Set Click Listeners
    private void setClickListeners() {
        // Sign In
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
                ft.replace(R.id.fragment, new SignIn());
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        // Sign Up
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
                ft.replace(R.id.fragment, new SignUp());
                ft.addToBackStack(null);
                ft.commit();
            }
        });
    }

    // Start Intent Service To Fetch Data
    private void startIntentService() {
        Intent intent = new Intent(getApplicationContext(), SyncDBService.class);
        intent.putExtra("messenger", new Messenger(handler));
        startService(intent);
    }

    // Check Session
    private void checkSession() {
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.getSessionData(Constants.SESSION_EMAIL) != null && sessionManager.getSessionData(Constants.SESSION_EMAIL).trim().length() > 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadNextActivity();
                }
            }, 3000);
        } else {
            bottomLay.setVisibility(View.VISIBLE);
        }
    }

    // Load Next Activity
    private void loadNextActivity() {
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
    }

    // Handler To Receive Data From Service
    @SuppressLint("HandlerLeak")
    private void setHandler() {
        try {
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    Bundle reply = msg.getData();
                    if (reply.getString("message").equals("success")) {
                        checkSession();

                    } else {
                        // Show Error In Snack Bar
                        try {
                            String message = reply.getString("message");
                            assert message != null;
                            snackbar = Snackbar
                                    .make(coordinatorLayout, message, Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.retry, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            snackbar.dismiss();
                                            startIntentService();
                                        }
                                    });

                            // Changing message text color
                            snackbar.setActionTextColor(Color.RED);
                            snackbar.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finishActivity() {
        overridePendingTransition(0, 0);
        finish();
    }
}
