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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

public class SignUp extends Fragment {

    EditText name, email, password, mobile;
    RelativeLayout blockSignUp;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    Button signUp;
    ImageView back, showpassword;
    boolean isPasswordShown = false;
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
        View view = inflater.inflate(R.layout.sign_up, container, false);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        setIds(view);
        setClickListeners();


        return view;
    }

    // Set Ids
    private void setIds(View view) {
        name = view.findViewById(R.id.name);
        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
        mobile = view.findViewById(R.id.mobile);
        signUp = view.findViewById(R.id.signup);
        back = view.findViewById(R.id.back);
        showpassword = view.findViewById(R.id.showpassword);
        blockSignUp = view.findViewById(R.id.blockSignUp);
    }

    // Set Click Listeners
    private void setClickListeners() {
        // Sign Up
        signUp.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onClick(View view) {
            blockSignUp.setVisibility(View.VISIBLE);
                // Set Values To User Model

                // Validate Fields
//                if (user.getName().trim().length() > 0) {
//                    if (user.getEmail().trim().length() > 0) {
//                        if (Util.isValidEmail(user.getEmail())) {
//                            if (user.getMobile().trim().length() > 0) {
//                                if (user.getPassword().trim().length() >= 6) {

//
                               //     if (isInserted != -1) {




                                        //save user in firebase
                                        mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                                                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                                        if (task.isSuccessful()) {
                                                            // Sign in success, update UI with the signed-in user's information
                                                            FirebaseUser fuser = mAuth.getCurrentUser();
                                                            User user = new User();
                                                            user.setName(name.getText().toString());
                                                            user.setEmail(email.getText().toString());
                                                            user.setMobile(mobile.getText().toString());
                                                            user.setPassword(password.getText().toString());

                                                            DatabaseReference myRef = database.getReference().child("Users");
                                                            myRef.child(fuser.getUid()).setValue(user);

                                                            // Register User
                                                            DB_Handler db_handler = new DB_Handler(getActivity());
                                                            long isInserted = db_handler.registerUser(user.getName(), user.getEmail(), user.getMobile(), user.getPassword());

                                                            // Save Session
                                                            SessionManager sessionManager = new SessionManager(getActivity());
                                                            sessionManager.saveSession(Constants.SESSION_EMAIL, email.getText().toString());
                                                            sessionManager.saveSession(Constants.SESSION_PASSWORD, password.getText().toString());

                                                            Intent i = new Intent(getActivity(), MainActivity.class);
                                                            startActivity(i);
                                                            finishActivityCallback.finishActivity();
                                                            //updateUI(user);
                                                        } else {
                                                            // If sign in fails, display a message to the user.
                                                           Toast.makeText(getActivity(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                            blockSignUp.setVisibility(View.INVISIBLE);
                                                        }

                                                        // ...
                                                    }
                                                });

                                        // Load Main Activity

//                                    } else {
//                                        showErrorToastEmailExists();
//                                    }
//
//                                } else {
//                                    showErrorToast(getActivity().getResources().getString(R.string.password));
//                                }
//                            } else {
//                                showErrorToast(getActivity().getResources().getString(R.string.mobile));
//                            }
//                        } else {
//                            showErrorToastEmailNotValid();
//                        }
//                    } else {
//                        showErrorToast(getActivity().getResources().getString(R.string.email));
//                    }
//                } else {
//                    showErrorToast(getActivity().getResources().getString(R.string.name));
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

        // Show Password
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

    // Show Error Toast - Email Exists
    private void showErrorToastEmailExists() {
        Toast.makeText(getActivity(), R.string.EmailExistsError, Toast.LENGTH_LONG).show();
    }
}
