package com.pk.bulkbuy;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pk.bulkbuy.activities.ShoppingCart;
import com.pk.bulkbuy.database.DB_Handler;
import com.pk.bulkbuy.database.SessionManager;
import com.pk.bulkbuy.fragments.Account;
import com.pk.bulkbuy.fragments.Categories;
import com.pk.bulkbuy.fragments.Products;
import com.pk.bulkbuy.fragments.Subcategories;
import com.pk.bulkbuy.fragments.WishList;
import com.pk.bulkbuy.interfaces.FinishActivity;
import com.pk.bulkbuy.interfaces.ShowBackButton;
import com.pk.bulkbuy.interfaces.ToolbarTitle;
import com.pk.bulkbuy.pojo.Category;
import com.pk.bulkbuy.utils.Constants;

import java.io.Serializable;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Subcategories.ChildCategories, ToolbarTitle, ShowBackButton, FinishActivity {

    BottomNavigationView navigation;
    DB_Handler db_handler;
    SessionManager sessionManager;
    Toolbar toolbar;
    TextView titleToolbar;
    int cartCount = 0;
    List<Category> childCategories;
    ImageView backButton;
    String subCategoryTitle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db_handler = new DB_Handler(this);
        sessionManager = new SessionManager(this);

        // Set Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set Title
        titleToolbar = findViewById(R.id.titleToolbar);
        titleToolbar.setText(R.string.TitleHome);

        // Back Button
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backButtonClick();
            }
        });

        // initialize bottom navigation view
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        callProductsFragment();
        setToolbarIconsClickListeners();
    }

    // Set Toolbar Icons Click Listeners
    private void setToolbarIconsClickListeners() {
        ImageView cart = findViewById(R.id.cart);
        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cartCount > 0) {
                    startActivity(new Intent(getApplicationContext(), ShoppingCart.class));
                    overridePendingTransition(0,0);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.cart_empty, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * BottomNavigationView Listener
     */
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            // Hide Back Button
            backButton.setVisibility(View.INVISIBLE);

            // Prevent Reload Same Fragment
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            switch (item.getItemId()) {
                case R.id.nav_home: // Home
                    // Prevent Reload
                    try {
                        if (!fm.findFragmentByTag("HOME").isVisible()) {
                            callProductsFragment();
                            titleToolbar.setText(R.string.TitleHome);
                        }
                    } catch (NullPointerException e) {
                        callProductsFragment();
                        titleToolbar.setText(R.string.TitleHome);
                    }
                    return true;

                case R.id.nav_categories: // Categories
                    ft.replace(R.id.content, new Categories());
                    ft.commit();
                    titleToolbar.setText(R.string.TitleCategories);
                    return true;

                case R.id.nav_shortlist: // Wish List
                    ft.replace(R.id.content, new WishList());
                    ft.commit();
                    titleToolbar.setText(R.string.TitleShortlist);
                    return true;

                case R.id.nav_account: // User Account
                    ft.replace(R.id.content, new Account());
                    ft.commit();
                    titleToolbar.setText(R.string.TitleAccount);
                    return true;
            }
            return false;
        }
    };

    // call products fragment
    private void callProductsFragment() {

        Bundle args = new Bundle();
        args.putInt(Constants.CAT_ID_KEY, 0);

        Products products = new Products();
        products.setArguments(args);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content, products, "HOME");
        ft.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Update Cart Count
        cartCount = db_handler.getCartItemCount(sessionManager.getSessionData(Constants.SESSION_EMAIL));
        TextView count = findViewById(R.id.count);
        if (cartCount > 0) {
            count.setVisibility(View.VISIBLE);
            count.setText(String.valueOf(cartCount));
        } else {
            count.setVisibility(View.GONE);
        }
    }

    // Back Button Click
    private void backButtonClick() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        try {
            if (fragmentManager.findFragmentByTag(Constants.FRAG_PDT).isVisible()) {
                // add bundle arguments
                Bundle bundle = new Bundle();
                bundle.putString(Constants.TITLE,subCategoryTitle);
                bundle.putSerializable(Constants.CAT_KEY, (Serializable) childCategories);

                Subcategories subcategories = new Subcategories();
                subcategories.setArguments(bundle);

                fragmentTransaction.replace(R.id.content, subcategories,Constants.FRAG_SUBCAT);
                fragmentTransaction.commit();
                return;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        try {
            if (fragmentManager.findFragmentByTag(Constants.FRAG_SUBCAT).isVisible()) {
                fragmentTransaction.replace(R.id.content, new Categories());
                fragmentTransaction.commit();
                titleToolbar.setText(R.string.TitleCategories);
                backButton.setVisibility(View.INVISIBLE);
            }
        } catch (NullPointerException e) {
            super.onBackPressed();
        }
    }

    /**
     * Save Child Categories From Subcategory Fragment
     * This is required by @backButtonClick method to restore state
     */
    @Override
    public void saveChildCategories(List<Category> childCategories) {
        this.childCategories = childCategories;
    }

    @Override
    public void onBackPressed() {
        backButtonClick();
    }

    // Set Toolbar Title
    @Override
    public void setToolbarTitle(String toolbarTitle) {
        titleToolbar.setText(toolbarTitle);
    }

    // show back button
    @Override
    public void showBackButton() {
        backButton.setVisibility(View.VISIBLE);
    }

    // Save Subcategory Title - Need for backButtonClick method
    @Override
    public void saveSubcategoryTitle(String toolbaTitle) {
        subCategoryTitle = toolbaTitle;
    }

    // Finish Activity From Fragment
    @Override
    public void finishActivity() {
        overridePendingTransition(0,0);
        finish();
    }
}
