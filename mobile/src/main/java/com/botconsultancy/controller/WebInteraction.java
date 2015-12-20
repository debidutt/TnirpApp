package com.botconsultancy.controller;

import android.content.Context;
import android.util.Log;

import com.botconsultancy.Utilities.OSMConstants;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Created by debiduttprasad on 18/07/15.
 */
class WebserviceInteraction {


    public String WebserviceInteractionService(Context context,String methodName, String request,String requestType,String[] strArray,boolean stat) {
        String strResult = null;
        DefaultHttpClient httpclient = null;
        try {

            HttpGet httpget = null;
            HttpPost httpPost = null;
            HttpResponse httpResponse = null;


            if(requestType.equalsIgnoreCase("get")) {


                if(stat == true || request != null) {
                    if(!stat) {
                        Log.d("get URL", OSMConstants.osmURL + methodName + "/" + request);
                        httpget = new HttpGet(
                                OSMConstants.osmURL + methodName + "/" + request);
                    }else{
                        Log.d("get URL", OSMConstants.osmURL + methodName);
                        httpget = new HttpGet(
                                OSMConstants.osmURL + methodName);
                    }
                }else{

                    StringBuilder sb = new StringBuilder();
                    String strRequestPost="/";

                    for(int i=0;i<strArray.length;i++){
                        sb.append(strRequestPost);
                        sb.append(strArray[i]);
                    }

                    String requestForGet = sb.toString();

                    Log.d("temporary url for post",""+OSMConstants.osmURL + methodName +requestForGet);

                    httpget = new HttpGet(
                            OSMConstants.osmURL+methodName+requestForGet.replace(' ','_'));

                }
                httpget.setHeader("Accept", "application/json");
                httpget.setHeader("Accept-Encoding", "gzip");
            }else{
                httpPost = new HttpPost(OSMConstants.osmURL+methodName);
                try {

//                    Log.d("JSON", jObj.toString());
//
//                    StringEntity stringEntity = new StringEntity(jObj.toString());

                    httpPost.setHeader("Accept", "application/json");
                    httpPost.setHeader("Content-Type", "application/json");
                    httpPost.setHeader("Accept-Encoding", "gzip");
//                    httpPost.setEntity(stringEntity);


                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            httpclient = new DefaultHttpClient();


            if(requestType.equalsIgnoreCase("get")) {

                 httpResponse = httpclient.execute(httpget);

            }else {

                 httpResponse = httpclient.execute(httpPost);
            }

            Log.d("http response",httpResponse.toString());

            InputStream inputStream = httpResponse.getEntity().getContent();

            Header contentEncoding = httpResponse
                    .getFirstHeader("Content-Encoding");

//            if (contentEncoding != null
//                    && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
//                inputStream = new GZIPInputStream(inputStream);
                strResult = convertInputStreamToString(inputStream);

                Log.d("Response",strResult);
//            }



        } catch (ConnectException e) {
            strResult = null;
        } catch (ConnectTimeoutException e) {
            strResult = null;
        } catch (SocketTimeoutException e) {
            strResult = null;
        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
        } catch (ClientProtocolException e) {

            e.printStackTrace();
        } catch (IllegalStateException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }
//     finally {
//            httpclient.getConnectionManager().shutdown();
//    }
        return strResult;
    }

    /**
     * This function converts the stream returned from the webservice to a
     * String format
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    private static String convertInputStreamToString(InputStream inputStream)
            throws IOException {
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
    }

}