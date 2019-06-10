package com.rafagire.orderapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class QRReaderActivity extends AppCompatActivity implements View.OnClickListener {

    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private SurfaceView cameraView;
    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1;

    private Button bTableNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView in onResume
        //setContentView(R.layout.activity_qrreader);

    }

    protected void onResume(){
        setContentView(R.layout.activity_qrreader);

        cameraView = (SurfaceView) findViewById(R.id.camera_view);
        bTableNumber = (Button) findViewById(R.id.bTableNumber);
        bTableNumber.setOnClickListener(this);

        initQR();

        //Check keys of SharedPreferences
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();

        if(!pref.contains(getString(R.string.first_access_key))){
            editor.putBoolean(getString(R.string.first_access_key), true);
            editor.commit();
        }

        if(pref.getBoolean(getString(R.string.first_access_key), true)){
            //Necessary actions for the first access
            initDB();
            editor.putBoolean(getString(R.string.first_access_key), false);
            editor.commit();
        }

        if(pref.contains(getString(R.string.current_code_key))){
            Intent intent = new Intent(QRReaderActivity.this, MenuGeneral.class);
            Bundle bundle = new Bundle();
            bundle.putString("code", pref.getString(getString(R.string.current_code_key), null));
            intent.putExtras(bundle);
            startActivity(intent);
        }

        super.onResume();
    }

    public void initQR(){
        // Create the QR detector
        //barcodeDetector = new BarcodeDetector.Builder(QRReaderActivity.this).setBarcodeFormats(Barcode.ALL_FORMATS).build();
        barcodeDetector = new BarcodeDetector.Builder(QRReaderActivity.this).setBarcodeFormats(Barcode.QR_CODE).build();

        // Prepare it
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {
                    Log.d("QR ERROR", "Si que entra");

                    String code = barcodes.valueAt(0).displayValue.toString();

                    barcodeDetector.release();

                    Intent intent = new Intent(QRReaderActivity.this, MenuGeneral.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("code", code);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });


        // Create the camera source
        //cameraSource = new CameraSource.Builder(QRReaderActivity.this, barcodeDetector).setRequestedPreviewSize(300, 300).setAutoFocusEnabled(true).build();
        cameraSource = new CameraSource.Builder(QRReaderActivity.this, barcodeDetector).setAutoFocusEnabled(true).build();

        // Lifecycle listener of the camera
        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // Check if the user has granted permissions to the camera
                if (ActivityCompat.checkSelfPermission(QRReaderActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        // Check the Android version (must be greater than M for showing the dialog)
                        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA));
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
                    }
                    return;
                } else {
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException ie) {
                        Log.e("CAMERA SOURCE", ie.getMessage());
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

    }

    public void initDB(){
        DBAccess dbAccess = new DBLocal(this, "DB_OrderApp", null, 1);
        dbAccess.deleteTable();
        dbAccess.createTable();
        try {
            InputStream fraw = getResources().openRawResource(R.raw.initial_database_content);
            BufferedReader brin = new BufferedReader(new InputStreamReader(fraw));

            String line;
            JSONParser parser=new JSONParser();
            while ( (line = brin.readLine()) != null ) {
                Object o=parser.parse(line);
                JSONObject siteJSON=(JSONObject)o;

                Product product = new Product();
                product.id = ((Long) siteJSON.get("id")).intValue();
                product.type = (String) siteJSON.get("type");
                product.name = (String) siteJSON.get("name");
                product.price = ((Double) siteJSON.get("price")).floatValue();

                dbAccess.add(product);
            }
            fraw.close();
        }
        catch (Exception ex) {
            Log.d("DATABASE", ex.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        barcodeDetector.release();

        switch(v.getId()){
            case (R.id.bTableNumber):
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle(getString(R.string.QRReader_dialog_title));
                alertDialog.setMessage(getString(R.string.QRReader_dialog_text));

                final EditText eText = new EditText(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                eText.setLayoutParams(lp);
                eText.setTextColor(Color.BLACK);
                alertDialog.setView(eText);

                alertDialog.setPositiveButton(getString(R.string.QRReader_dialog_positiveButton),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String tableNumber = eText.getText().toString();
                                if (!tableNumber.equals("")) {
                                    Intent intent = new Intent(QRReaderActivity.this, MenuGeneral.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putString("code", tableNumber);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                }
                                else{
                                    Toast.makeText(getApplicationContext(), getString(R.string.QRReader_toast_noCode), Toast.LENGTH_SHORT).show();
                                    onResume();
                                }
                            }
                        });

                alertDialog.setNegativeButton(getString(R.string.QRReader_dialog_negativeButton),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                onResume();
                            }
                        });

                alertDialog.show();
                break;
        }
    }
}