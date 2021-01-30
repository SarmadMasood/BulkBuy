package com.pk.bulkbuy.utils;

import android.content.Context;
import android.text.TextUtils;

import com.pk.bulkbuy.R;
import com.pk.bulkbuy.service.SyncDBService;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Preeth on 1/5/18
 */

// Utilities Class
public class Util {

    // Format Double Value To Remove Unnecessary Zero
    public static String formatDouble(double num) {
        if (num == (long) num)
            return String.format(Locale.US, "%d", (long) num);
        else
            return String.format(Locale.US, "%s", num);
    }

    // Get inClause String For Array Parameters In DB
    public static String getInClause(List<String> array) {
        String inClause = array.toString();

        //replace the brackets with parentheses
        inClause = inClause.replace("[", "(");
        inClause = inClause.replace("]", ")");

        return inClause;
    }

    // Check email valid or not
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Get Error Message
    public static String getErrorMessage(Throwable t, Context context) {
        if (t instanceof SocketTimeoutException || t instanceof UnknownHostException || t instanceof ConnectException) {
            return context.getResources().getString(R.string.NoInternet);
        } else {
            return context.getResources().getString(R.string.Error500);
        }
    }

    public static String getFormattedPhonenumber(String phone){
        if (phone.charAt(0)=='0'){
            phone = SyncDBService.charRemoveAt(phone,0);
            phone = "+92"+phone;
        }
        else if(phone.charAt(0)=='+' && phone.charAt(1)=='9' && phone.charAt(2)=='2'){
            return phone;
        }
        else if(phone.charAt(0)=='9' && phone.charAt(1)=='2'){
            phone = "+"+phone;
        }
        return phone;
    }
}
