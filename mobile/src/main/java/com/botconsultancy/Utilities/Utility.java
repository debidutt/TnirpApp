package com.botconsultancy.Utilities;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by debiduttprasad on 20/07/15.
 */
public class Utility {

    private Context mContext;


    public Utility(Context context) {
        this.mContext = context;
    }

    /**
     * This function is used to check if the device is online
     *
     * @return boolean
     */
    public boolean isNetworkOnline() {

        ConnectivityManager conMan = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (conMan.getActiveNetworkInfo() != null
                && conMan.getActiveNetworkInfo().isConnectedOrConnecting()) {
            return true;
        } else {

            return false;
        }
    }
}
