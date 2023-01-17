package com.mobilestyx.jlrmaximizer.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

public class AppUtils {

    private static final String TAG = "AppUtils";

    //to check if the internet is on
    public static boolean isInternetOn(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    //to show a alert dialog in the app
    public static AlertDialog createInfoDialog(@NonNull Context context, String title, @NonNull String message, DialogInterface.OnClickListener listener) {

        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNeutralButton("Ok", listener)
                .create();
    }

    //to show a alert dialog in the app
    public static AlertDialog createInfoDialog(@NonNull Context context, String title, @NonNull String message) {

        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
    }

    public static AlertDialog showAlertDialog(final Context context, String title, String message,
                                              final Boolean status) {
        if (status) {
            return new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setNeutralButton("Update", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("market://details?id=com.mobilestyx.JLRMaximizer"));
                                context.startActivity(intent);

                            } catch (android.content.ActivityNotFoundException anfe) {
                                //  viewInBrowser(context, "https://play.google.com/store/apps/details?id=com.mobilestyx.JLRMaximizer");
                            }
                        }
                    })
                    .create();
        } else {
            return new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create();
        }
    }

}
