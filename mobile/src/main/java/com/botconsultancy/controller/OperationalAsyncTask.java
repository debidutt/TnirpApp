package com.botconsultancy.controller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

/**
 * Created by debiduttprasad on 18/07/15.
 */
public abstract class OperationalAsyncTask extends AsyncTask<Void, Void, String> {

    private ProgressDialog pd;

    private Activity actv;

    private String txtProgressMessage = "Loading..";

    private boolean hideProgressDialog = false;

    public OperationalAsyncTask(Activity actv) {
        this.actv = actv;
    }

    /**
     * This function is used to set the message to be shown while loading
     * @param txtProgressMessage
     */
    public void setLoadingText(String txtProgressMessage){
        this.txtProgressMessage = txtProgressMessage;
    }

    @Override
    protected String doInBackground(Void... arg0) {

        if (!isCancelled()) {
            return performOperation();
        }else
        {
            return null;
        }
    }

    public abstract String performOperation();
    public abstract void preOperation();
    public abstract void postOperation(String result);

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (!hideProgressDialog) {
            if(pd==null || !pd.isShowing())
            {
                pd = ProgressDialog.show(actv, "", txtProgressMessage);
            }
        }


        preOperation();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if(pd!=null && pd.isShowing())
            pd.dismiss();
        postOperation(result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        if (!hideProgressDialog) {
            if(pd!=null && pd.isShowing())
                pd.dismiss();
        }
    }

    public boolean isHideProgressDialog() {
        return hideProgressDialog;
    }

    public void setHideProgressDialog(boolean hideProgressDialog) {
        this.hideProgressDialog = hideProgressDialog;
    }

}