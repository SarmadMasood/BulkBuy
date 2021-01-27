package com.pk.bulkbuy.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pk.bulkbuy.MainActivity;
import com.pk.bulkbuy.R;
import com.pk.bulkbuy.database.DB_Handler;
import com.pk.bulkbuy.database.SessionManager;
import com.pk.bulkbuy.interfaces.FinishActivity;
import com.pk.bulkbuy.pojo.User;
import com.pk.bulkbuy.utils.Constants;
import com.pk.bulkbuy.utils.Util;

/**
 * Created by Preeth on 1/6/2018
 */

public class SignIn extends Fragment {

    Button signIn;
    RelativeLayout blockSignIn;
    LinearLayout signinView;
    ProgressBar activityIndicator;
    EditText email, password;
    ImageView back, showpassword;
    boolean isPasswordShown = false;
    FinishActivity finishActivityCallback;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        finishActivityCallback = (FinishActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.sign_in, container, false);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        setIds(view);
        setClickListeners();

        return view;
    }

    // Set Ids
    private void setIds(View view) {
        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
        signIn = view.findViewById(R.id.signin);
        back = view.findViewById(R.id.back);
        showpassword = view.findViewById(R.id.showpassword);
        activityIndicator = view.findViewById((R.id.activityIndicator));
        blockSignIn = view.findViewById(R.id.blockSignIn);
        signinView = view.findViewById(R.id.signinView);
    }

    // Set Click Listeners
    private void setClickListeners() {
        // Sign In
        signIn.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onClick(View view) {
                signinView.setClickable(false);
                blockSignIn.setVisibility(View.VISIBLE);
                activityIndicator.setVisibility(View.VISIBLE);
                activityIndicator.animate();

                // Validate Fields
//                if (user.getEmail().trim().length() > 0) {
//                    if (Util.isValidEmail(user.getEmail())) {
//                        if (user.getPassword().trim().length() > 0) {

                            // Sign In User
//
                            try {
//                                if (user.getEmail().trim().length() > 0) {



                                    //SignIn with firebase
                                    mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if (task.isSuccessful()) {
                                                        // Sign in success, update UI with the signed-in user's information
//                                                        Log.d(TAG, "signInWithEmail:success");
                                                        // Set Values To User Model
                                                        final FirebaseUser fuser = mAuth.getCurrentUser();


                                                        final DB_Handler db_handler = new DB_Handler(getActivity());
                                                        User dbuser = db_handler.getUser(email.getText().toString());

                                                        if (dbuser.getEmail()==null){
                                                            DatabaseReference myRef = database.getReference().child("Users").child(fuser.getUid());
                                                            myRef.addValueEventListener(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    // This method is called once with the initial value and again
                                                                    // whenever data at this location is updated.
                                                                    User user = new User();
                                                                    user = dataSnapshot.getValue(User.class);
                                                                    db_handler.registerUser(user.getName(), user.getEmail(), user.getMobile(), user.getPassword());
                                                                    // Save Session
                                                                    SessionManager sessionManager = new SessionManager(getActivity());
                                                                    sessionManager.saveSession(Constants.SESSION_EMAIL, fuser.getEmail());
                                                                    sessionManager.saveSession(Constants.SESSION_PASSWORD, password.getText().toString());

                                                                    //updateUI
                                                                    // Load Main Activity
                                                                    Intent i = new Intent(getActivity(), MainActivity.class);
                                                                    startActivity(i);
                                                                    finishActivityCallback.finishActivity();
                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError error) {
                                                                    // Failed to read value
                                                                  //  Log.w(TAG, "Failed to read value.", error.toException());
                                                                }
                                                            });

                                                        }
                                                        else {
                                                            // Save Session
                                                            SessionManager sessionManager = new SessionManager(getActivity());
                                                            sessionManager.saveSession(Constants.SESSION_EMAIL, fuser.getEmail());
                                                            sessionManager.saveSession(Constants.SESSION_PASSWORD, password.getText().toString());

                                                            //updateUI
                                                            // Load Main Activity
                                                            Intent i = new Intent(getActivity(), MainActivity.class);
                                                            startActivity(i);
                                                            finishActivityCallback.finishActivity();
                                                        }
                                                    } else {
                                                        // If sign in fails, display a message to the user.
//                                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                                        Toast.makeText(getActivity(),task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                        blockSignIn.setVisibility(View.INVISIBLE);
//                                                        updateUI(null);
                                                    }

                                                    // ...
                                                }
                                            });


//                                } else {
//                                    showInvalidUser();
//                                }
                            } catch (NullPointerException e) {
                                showInvalidUser();
                            }

//                        } else {
//                            showErrorToast(getActivity().getResources().getString(R.string.password));
//                        }
//
//                    } else {
//                        showErrorToastEmailNotValid();
//                    }
//                } else {
//                    showErrorToast(getActivity().getResources().getString(R.string.email));
//                }
            }
        });

        // Back Button Click
        back.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onClick(View view) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
                ft.replace(R.id.fragment, new BlankFragment());
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        // Show / Hide Password
        showpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPasswordShown) {
                    password.setTransformationMethod(new PasswordTransformationMethod());
                    showpassword.setImageResource(R.drawable.ic_eye_off_grey600_24dp);
                    isPasswordShown = false;
                } else {
                    password.setTransformationMethod(null);
                    showpassword.setImageResource(R.drawable.ic_eye_white_24dp);
                    isPasswordShown = true;
                }
            }
        });
    }

    // Show Error Toast
    private void showErrorToast(String value) {
        Toast.makeText(getActivity(), value + getResources().getString(R.string.BlankError), Toast.LENGTH_LONG).show();
    }

    // Show Error Toast - Email Not Valid
    private void showErrorToastEmailNotValid() {
        Toast.makeText(getActivity(), R.string.EmailError, Toast.LENGTH_LONG).show();
    }

    // Show Invalid User
    private void showInvalidUser() {
        Toast.makeText(getActivity(), R.string.InvalidUser, Toast.LENGTH_LONG).show();
    }
}
