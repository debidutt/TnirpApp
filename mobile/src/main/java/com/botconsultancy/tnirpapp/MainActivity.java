package com.botconsultancy.tnirpapp;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.botconsultancy.Utilities.OSMConstants;
import com.botconsultancy.Utilities.Utility;
import com.botconsultancy.controller.OperationalAsyncTask;
import com.botconsultancy.controller.*;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MainActivity extends Activity {

    private OperationalAsyncTask operationLoaderAsyncTask;
    private Utility utility;
    private EditText etUserName;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvShowHide;
    private String printFormat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        utility = new Utility(getApplicationContext());

        etUserName = (EditText)findViewById(R.id.et_username);
        etPassword = (EditText)findViewById(R.id.et_password);
        btnLogin = (Button)findViewById(R.id.btn_login);
        tvShowHide = (TextView)findViewById(R.id.tv_show_hide);

        callPrintFormat();

        tvShowHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tvShowHide.getText().toString().equalsIgnoreCase(getResources().getString(R.string.txt_btn_show))) {
                    tvShowHide.setText(getResources().getString(R.string.txt_btn_hide));
                    etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else{
                    tvShowHide.setText(getResources().getString(R.string.txt_btn_show));
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Intent i = new Intent(MainActivity.this, PrintActivity.class);
//
//                i.putExtra("printFormat",printFormat);
//
//                startActivity(i);

                if(!etUserName.getText().toString().equalsIgnoreCase("") && !etPassword.getText().toString().equalsIgnoreCase("")) {
                    String[] arrString = new String[2];

                    arrString[0] = etUserName.getText().toString();
                    arrString[1] = etPassword.getText().toString();

                    if(utility.isNetworkOnline()) {

                        Log.d("isNetworkOnline","true");

                        callOSMservice(arrString);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "No Network !", Toast.LENGTH_SHORT).show();
                    }


                }else{

                    if(etUserName.getText().toString().equalsIgnoreCase(""))
                        Toast.makeText(getApplicationContext(),"Please enter the Username",Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getApplicationContext(),"Please enter the Password",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    @Override
    public void onPause() {
        super.onPause();

        etUserName.setText("");
        etPassword.setText("");
    }

    @Override
    public void onResume() {
        super.onResume();

        etUserName.setText("");
        etPassword.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void callPrintFormat() {


        operationLoaderAsyncTask = new OperationalAsyncTask(MainActivity.this) {

            @Override
            public void preOperation() {

                Log.d("preOperation", "true");
            }

            @Override
            public void postOperation(String result) {

                Log.d("PostOperation", "true");
                try {

                    printFormat = result;

                    Log.d("printFormat %%",printFormat);

                }catch(Exception e){
                    Toast.makeText(getApplicationContext(),"Login Authentication Failed !",Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

            }

            @Override
            public String performOperation() {

                Log.d("performOperation", "true");

                String result = null;

                Log.d("Making a connection", "true");


                result = new WebServiceRequest(MainActivity.this).getPrintFormat(OSMConstants.getPrintFormat,"get");

                Log.d("Response ###", result);

                return result;
            }

        };

        operationLoaderAsyncTask.execute();

    }


    private void callOSMservice(final String[] arrString) {

        operationLoaderAsyncTask = new OperationalAsyncTask(MainActivity.this) {

            @Override
            public void preOperation() {

                Log.d("preOperation", "true");
            }

            @Override
            public void postOperation(String result) {

                Log.d("PostOperation", "true");
                try {
                    JSONObject jObj = new JSONObject(result);

                    Log.d("Error state",""+jObj.get("ErrorInfo").toString().equalsIgnoreCase(""));

                    if(jObj.get("ErrorInfo").toString().equalsIgnoreCase("") && jObj.get("isActive").toString().equalsIgnoreCase("1")) {
                        Toast.makeText(getApplicationContext(), "Login Successful !", Toast.LENGTH_SHORT).show();

                        Intent i = new Intent(MainActivity.this, PrintActivity.class);

                        i.putExtra("printFormat",printFormat);

                        startActivity(i);
                    }
                    else
                        Toast.makeText(getApplicationContext(), "Login Authentication Failed ! Please Try Again !", Toast.LENGTH_LONG).show();

                }catch(Exception e){
                    Toast.makeText(getApplicationContext(),"Login Authentication Failed !",Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

            }

            @Override
            public String performOperation() {

                Log.d("performOperation", "true");

                String result = null;
//                JSONStringer jsonStringer = null;
//
//                try {
//                     jsonStringer = new JSONStringer().object().key("strUserID")
//                            .value(username).key("strPassword").value(password).endObject();
//                }catch (Exception e){
//                    e.printStackTrace();
//                }



                Log.d("Making a connection", "true");


                result = new WebServiceRequest(MainActivity.this).UserAuthenticationSaveOrderOnline(OSMConstants.login,"get",arrString);

                Log.d("Response", result);

                return result;
            }

        };

        operationLoaderAsyncTask.execute();
    }

}


