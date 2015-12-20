package com.botconsultancy.controller;

import android.content.Context;

import org.apache.http.NameValuePair;
import org.json.JSONStringer;

import java.util.List;

/**
 * Created by debiduttprasad on 19/07/15.
 */
public class WebServiceRequest{

    private Context mContext;

    public WebServiceRequest(Context context) {
        this.mContext = context;
    }


    public String UserAuthenticationSaveOrderOnline(String methodName,String requestType,String[] strArray)
    {


        // Making a web service interaction call

        String strResult = new WebserviceInteraction().WebserviceInteractionService(mContext,methodName, null,requestType,strArray,false);

        return strResult;
    }

    public String fetchOrderIDbyUserName(String methodName,String requestType,String orderID){


        String strResult = new WebserviceInteraction().WebserviceInteractionService(mContext,methodName, orderID,requestType,null,false);


        return strResult;
    }


    public String getPrintFormat(String methodName,String requestType)
    {

        String strResult = new WebserviceInteraction().WebserviceInteractionService(mContext,methodName, null,requestType,null,true);

        return strResult;
    }

}
