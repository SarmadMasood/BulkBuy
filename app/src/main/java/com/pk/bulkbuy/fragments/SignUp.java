package com.pk.bulkbuy.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
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

import java.util.concurrent.TimeUnit;

/**
 * Created by Preeth on 1/6/2018
 */

public class SignUp extends Fragment {

    EditText name, email, password, mobile, codeEditText;
    TextView loading;
    RelativeLayout blockSignUp;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    Button signUp, verify;
    ImageView back, showpassword;
    boolean isPasswordShown = false;
    FinishActivity finishActivityCallback;
    String mVerificationId, code;
    PhoneAuthProvider.ForceResendingToken mResendToken;

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
        loading = view.findViewById(R.id.loading);
    }

    // Set Click Listeners
    private void setClickListeners() {
        // Sign Up

        signUp.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onClick(View view) {
                String phoneNumber = Util.getFormattedPhonenumber(mobile.getText().toString());
                showLoading();

                PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                            signUpAndSaveUser(credential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException exception) {
                        Toast.makeText(getActivity(), exception.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        hideLoading();
                        loading.setText("Verifying...");

                        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                            // Invalid request
                        } else if (exception instanceof FirebaseTooManyRequestsException) {
                            // The SMS quota for the project has been exceeded
                        }
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        // Save verification ID and resending token so we can use them later
                        loading.setText("Signing up...");
                        mVerificationId = verificationId;
                        mResendToken = token;
                        inputCodeAndVerify();
                    }
                };

                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                                .setPhoneNumber(phoneNumber)       // Phone number to verify
                                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                .setActivity(getActivity())                 // Activity (for callback binding)
                                .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                                .build();
                
                PhoneAuthProvider.verifyPhoneNumber(options);
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

    protected void inputCodeAndVerify(){

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        View view = getLayoutInflater().inflate(R.layout.dialog_verification, null);
        verify = (Button) view.findViewById(R.id.verify);
        codeEditText = (EditText) view.findViewById(R.id.codeEditText);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                hideLoading();
                loading.setText("Verifying...");
            }
        });
        dialog.show();
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                code = codeEditText.getText().toString();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                if(credential.getSmsCode()!=null){
                    signUpAndSaveUser(credential);
                }else{
                    hideLoading();
                    Toast.makeText(getContext(),"Error during sign up, please try again",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    protected void signUpAndSaveUser(PhoneAuthCredential credential){
        mAuth.signInWithCredential(credential).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    mAuth.signOut();
                    signUpWithEmailAndPassword();
                    // ...
                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        loading.setText("Verifying...");
                        hideLoading();
                        Toast.makeText(getContext(),"Incorrect Verification Code",Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    protected void signUpWithEmailAndPassword(){
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
                            hideLoading();
                        }

                        // ...
                    }
                });
    }

    protected void showLoading(){
        email.setEnabled(false);
        name.setEnabled(false);
        mobile.setEnabled(false);
        password.setEnabled(false);
        signUp.setEnabled(false);
        blockSignUp.setVisibility(View.VISIBLE);
    }

    protected void hideLoading(){
        email.setEnabled(true);
        name.setEnabled(true);
        mobile.setEnabled(true);
        password.setEnabled(true);
        signUp.setEnabled(true);
        blockSignUp.setVisibility(View.GONE);
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
