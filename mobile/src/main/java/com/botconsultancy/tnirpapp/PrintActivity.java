package com.botconsultancy.tnirpapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.botconsultancy.Utilities.OSMConstants;
import com.botconsultancy.Utilities.Utility;
import com.botconsultancy.controller.OperationalAsyncTask;
import com.botconsultancy.controller.WebServiceRequest;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


public class PrintActivity extends ActionBarActivity {

    private EditText etOrderId;
    private EditText etCustomerName;
    private EditText etDesc;
    private Button btnPrint;
    private Button btnClear;
    private Button btnTakePhoto;
    private String customerID;
    private String cpclData;
    private Utility utility;
    private String printFormat;
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private String oldCpclData;
    private ImageView mImageView;


    private OperationalAsyncTask operationLoaderAsyncTask;

    private final UUID SerialPortServiceClass_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final BluetoothAdapter BA = BluetoothAdapter.getDefaultAdapter();
    //    private final String PrinterBsid = "00:22:58:3B:E0:6E"; // This is My Printer Bluetooth MAC Address
    private final String PrinterBsid = "00:22:58:3C:BA:F6";
    static final int REQUEST_TAKE_PHOTO = 1;
    String mCurrentPhotoPath;

    Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
            OutputStream sOut;
            BluetoothSocket socket;
            BA.cancelDiscovery();


            BluetoothDevice BD = BA.getRemoteDevice(PrinterBsid);
            try {
                socket = BD.createInsecureRfcommSocketToServiceRecord(SerialPortServiceClass_UUID);


                if (!socket.isConnected()) {
                    socket.connect();
                    Thread.sleep(1000); // <-- WAIT FOR SOCKET
                }
                sOut = socket.getOutputStream();

                Log.d("cpclData", cpclData);

                sOut.write(cpclData.getBytes());
                sOut.close();

                socket.close();
                BA.cancelDiscovery();


            } catch (IOException e) {
                Log.e("", "IOException");
                e.printStackTrace();
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);

        utility = new Utility(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etOrderId = (EditText) findViewById(R.id.et_order_id);
        etCustomerName = (EditText) findViewById(R.id.et_cust_name);
        etDesc = (EditText) findViewById(R.id.et_desc);
        btnPrint = (Button) findViewById(R.id.btn_print);
        btnClear = (Button) findViewById(R.id.btn_clear);
        btnTakePhoto = (Button) findViewById(R.id.btn_takephoto);

        editor = getPreferences(MODE_PRIVATE).edit();
        prefs = getPreferences(MODE_PRIVATE);

        etCustomerName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etCustomerName.getText().toString().length() > 0)
                    btnTakePhoto.setEnabled(true);
                else
                    btnTakePhoto.setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        if (etCustomerName.getText().toString().equalsIgnoreCase("") || etCustomerName.getText().toString().length() == 0
                || etOrderId.getText().toString().equalsIgnoreCase("") || etOrderId.getText().toString().length() == 0) {
            String restoredCustomerName = prefs.getString("customerName", null);
            String restoredOrderId = prefs.getString("orderId", null);
            if (restoredCustomerName != null) {
                etCustomerName.setText(restoredCustomerName, TextView.BufferType.EDITABLE);
                etOrderId.setText(restoredOrderId, TextView.BufferType.EDITABLE);
            }
        }

        printFormat = getIntent().getExtras().getString("printFormat");


        etOrderId.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {

                        case KeyEvent.KEYCODE_ENTER:
                            if (utility.isNetworkOnline()) {
                                callCustomerNameByOrderID(etOrderId.getText().toString());

                            } else {
                                Toast.makeText(getApplicationContext(), "No Network !", Toast.LENGTH_SHORT).show();
                            }

                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });


        etDesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etDesc.getText().toString().length() > 0) {
                    setTitle("Get Ready to Print !");
                    btnPrint.setEnabled(true);
                } else {
                    setTitle("Please Enter Description !");
                    btnPrint.setEnabled(false);
                }


            }

            @Override
            public void afterTextChanged(Editable s) {
                if (etDesc.getText().toString().length() > 0) {
                    setTitle("Get Ready to Print !");
                    btnPrint.setEnabled(true);
                } else {
                    setTitle("Please Enter Description !");
                    btnPrint.setEnabled(false);
                }


            }
        });

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();

            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etCustomerName.setText("");
                etOrderId.setText("");
                etDesc.setText("");
            }
        });

        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isBluetoothEnabled()) {
                    if (utility.isNetworkOnline()) {

                        setTitle("Enter another Order ID :)");
                        String[] arrString = new String[2];

                        arrString[0] = etOrderId.getText().toString();
                        arrString[1] = etDesc.getText().toString();

                        SaveOrderLine(arrString);


                    } else {
                        Toast.makeText(getApplicationContext(), "No Network !", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please Enable Bluetooth !", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_print, menu);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode != RESULT_CANCELED) {
            if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK && data != null) {

                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                mImageView.setImageBitmap(imageBitmap);


            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        String imageFileName = prefs.getString("customerID", null);
        String fileName = imageFileName+timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory("/OSTM App");
        File image = File.createTempFile(
                fileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

//        Toast.makeText(this,"File Name is "+imageFileName,Toast.LENGTH_LONG).show();

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }


    public boolean isBluetoothEnabled() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter.isEnabled();

    }

    private String addSpace(int magicNumber, char c[]) {
        StringBuilder resultStrBuilder = new StringBuilder();

        for (int i = 0; i < magicNumber; i++) {

            Log.d("Length ", " " + c.length);

            if (i < c.length) {
                Log.d("characters got " + i, "" + c[i]);
                resultStrBuilder.append(c[i]);
            } else {
                Log.d("characters added " + i, " ");
                resultStrBuilder.append(" ");
            }
        }


        return resultStrBuilder.toString();
    }

    String SaveOrderLine(final String[] arrString) {

        operationLoaderAsyncTask = new OperationalAsyncTask(PrintActivity.this) {

            @Override
            public void preOperation() {

                Log.d("preOperation", "true");
            }

            @Override
            public void postOperation(String result) {

                Log.d("PostOperation", "true");
                try {
                    JSONObject jObj = new JSONObject(result);

                    Log.d("Error state", "" + jObj.get("ErrorInfo").toString().equalsIgnoreCase(""));

                    if (jObj.get("ErrorInfo").toString().equalsIgnoreCase("") && !jObj.get("BarCodeID").toString().equalsIgnoreCase("")) {

                        char[] displayBarcodeID = jObj.get("DisplayBarCodeID").toString().toCharArray();

                        char[] customerName = etCustomerName.getText().toString().toCharArray();

                        Log.d("CustomerName ", addSpace(62, customerName) + "check");

                        Log.d("displayBarcodeID ", addSpace(64, displayBarcodeID) + "check");

                        cpclData = printFormat.replace("{BARCODEID}", " " + jObj.get("BarCodeID").toString()).replace("{DISPLAYBARCODEID}", " " + addSpace(68, displayBarcodeID)).replace("{CustomerName}", " " + addSpace(64, customerName)).replace("{DESCRIPTION}", " " + etDesc.getText().toString()).replace("\\\\", "\\").replace("\\r\\n", System.getProperty("line.separator")).replace("\"", " ");


                        Log.d("cpclData  **", cpclData);

                        t.run();

                        Toast.makeText(getApplicationContext(), "Orders Saved Successfully !", Toast.LENGTH_SHORT).show();

                        if (editor.putString("customerName", etCustomerName.getText().toString()).equals(null) && etOrderId.getText().toString().equals(null)) {
                            etOrderId.setText("");
                            etCustomerName.setText("");
                            etDesc.setText("");
                            etOrderId.requestFocus();
                        } else {

                            editor.putString("customerName", etCustomerName.getText().toString());
                            editor.putString("orderId", etOrderId.getText().toString());
                        }

                        editor.apply();

                    } else
                        Toast.makeText(getApplicationContext(), "Saving Orders Failed ! Please Try Again !", Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Saving Orders Failed !", Toast.LENGTH_LONG).show();
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


                result = new WebServiceRequest(PrintActivity.this).UserAuthenticationSaveOrderOnline(OSMConstants.saveOrderLine, "get", arrString);

                Log.d("Response", result);

                return result;
            }

        };

        operationLoaderAsyncTask.execute();

        return "";
    }


    String callCustomerNameByOrderID(final String orderID) {

        operationLoaderAsyncTask = new OperationalAsyncTask(PrintActivity.this) {

            @Override
            public void preOperation() {

                Log.d("preOperation", "true");
            }

            @Override
            public void postOperation(String result) {

                Log.d("PostOperation", "true");

                try {

                    JSONObject jObj = new JSONObject(result);

                    Log.d("Error state", "" + jObj.get("ErrorInfo").toString().equalsIgnoreCase(""));

                    if (jObj.get("ErrorInfo").toString().equalsIgnoreCase("")) {

                        etCustomerName.setText(jObj.get("CustomerName").toString());
                        setTitle("Please Enter Description !");
                        customerID = jObj.getString("CustomerID");
                        editor.putString("customerID", customerID);
                        editor.apply();
                        etDesc.setEnabled(true);
                        etDesc.requestFocus();
                    } else
                        Toast.makeText(getApplicationContext(), "Customer Name couldn't be Fetched ! Please Try Again !", Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Server is not Responding ! Please Try Again !", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

            }

            @Override
            public String performOperation() {

                Log.d("performOperation", "true");

                String result;

                Log.d("Making a connection", "true");


                result = new WebServiceRequest(PrintActivity.this).fetchOrderIDbyUserName(OSMConstants.getCustomerName, "get", orderID);


                return result;
            }

        };

        operationLoaderAsyncTask.execute();


        return "";
    }


}
