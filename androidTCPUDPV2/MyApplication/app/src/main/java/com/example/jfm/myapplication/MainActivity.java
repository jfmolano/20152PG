package com.example.jfm.myapplication;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Location;
import android.content.Context;
import android.location.LocationManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpResponse;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends Activity {
    //Constantes
    private final static int ITERACIONES_RUIDO = 1;
    private final static int T_INTERVALO_RUIDO = 1000;
    //Boton de comunicacion POST
    private Button btnPOST;
    private Button btnStart;
    private Button btnActualizarCod;
    private EditText codigoEntrada;
    private TextView vistaCodigo;
    //GPS
    private LocationManager locationManager;
    //WIFI
    private WifiManager wifiMgr;
    private WifiInfo wifiInfo;
    private DhcpInfo dhcpInfo;
    //WIFI2
    private List<ScanResult> scanResults;
    //RUIDO
    private MediaRecorder mRecorder = null;
    //LUZ
    DataCollection mDataCollection = null;
    //MUSICA
    public static final String SERVICECMD = "com.android.music.musicservicecommand";
    public static final String CMDNAME = "command";
    public static final String CMDTOGGLEPAUSE = "togglepause";
    public static final String CMDSTOP = "stop";
    public static final String CMDPAUSE = "pause";
    public static final String CMDPREVIOUS = "previous";
    public static final String CMDNEXT = "next";
    private String artista;
    private String album;
    private String pista;
    private String apps;
    private String enReproduccion;
    private boolean listoWifiScan;
    private WifiManager mWifiManager;
    private int j;

    //Preferencias
    private SharedPreferences sharedPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Preferencias
        sharedPref = getApplicationContext().getSharedPreferences(
                "com.jfm.appMT.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);

        //Interfaz
        setContentView(R.layout.activity_main);

        //Codigo
        codigoEntrada = (EditText) findViewById(R.id.codigoEdText);

        //Codigo vista
        vistaCodigo = (TextView) findViewById(R.id.textoCodigo);

        //Listener
        addListenerOnButton();
    }

    //Activar boton de medicion
    public void addListenerOnButton() {
        btnPOST = (Button) findViewById(R.id.btnDisplay);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnActualizarCod = (Button) findViewById(R.id.btnActualizarCod);
        btnPOST.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Intent intent = new Intent(getBaseContext(), Medidor.class);
                        PendingIntent sender = PendingIntent.getService(getBaseContext(), 0,
                                intent, 0);
                        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                        am.cancel(sender);
                        Toast.makeText(getBaseContext(), "Medicion detenida", Toast.LENGTH_LONG).show();
                    }
                });
            }

        });

        btnStart.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Intent intent = new Intent(getBaseContext(), Medidor.class);
                        PendingIntent sender = PendingIntent.getService(getBaseContext(), 0,
                                intent, 0);

                        // We want the alarm to go off 30 seconds from now.
                        long firstTime = SystemClock.elapsedRealtime();
                        firstTime += 15 * 1000;

                        // Schedule the alarm!
                        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime,
                                15 * 1000 * 60, sender);
                        Toast.makeText(getBaseContext(), "Medicion establecida", Toast.LENGTH_LONG).show();
                    }
                });
            }

        });

        btnActualizarCod.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        //
                        String codigo = codigoEntrada.getText().toString();
                        //
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("CodigoUni", codigo);
                        editor.commit();
                        //
                        //
                        String codStored = sharedPref.getString("CodigoUni", "No Codigo");
                        //
                        vistaCodigo.setText("Codigo: " + codStored);
                        Toast.makeText(getBaseContext(), "Codigo actualizado", Toast.LENGTH_LONG).show();
                    }
                });
            }

        });
    }
}

//CODIGO PARA IMPRIMIR INFO WIFI
/*
        System.out.println("- - - - - IP - - - - -");
        System.out.println(ipAddress);
        System.out.println("- - - - - MAC - - - - -");
        System.out.println(mac);
        System.out.println("- - - - - ID de Net - - - - -");
        System.out.println(netIdString);
        System.out.println("- - - - - dns1 - - - - -");
        System.out.println( intToIp(dns1));
        System.out.println("- - - - - dns2 - - - - -");
        System.out.println( intToIp(dns2));
        System.out.println("- - - - - gateway - - - - -");
        System.out.println( intToIp(gateway));
        System.out.println("- - - - - ipdhcp - - - - -");
        System.out.println( intToIp(ipdhcp));
        System.out.println("- - - - - leaseduration - - - - -");
        System.out.println(leaseduration);
        System.out.println("- - - - - netmask - - - - -");
        System.out.println( intToIp(netmask));
        System.out.println("- - - - - servaddress - - - - -");
        System.out.println(intToIp(servaddress));*/