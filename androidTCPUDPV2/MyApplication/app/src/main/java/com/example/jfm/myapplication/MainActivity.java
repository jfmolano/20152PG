package com.example.jfm.myapplication;

import android.app.Activity;
import android.media.MediaRecorder;
import android.net.wifi.WifiInfo;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
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

public class MainActivity extends Activity {
    //Constantes
    private final static int ITERACIONES_RUIDO = 1;
    private final static int T_INTERVALO_RUIDO = 1000;
    //Boton de comunicacion POST
    private Button btnPOST;
    private EditText nombreSalon;
    private LocationManager locationManager;
    private WifiManager wifiMgr;
    private WifiInfo wifiInfo;
    private DhcpInfo dhcpInfo;
    private MediaRecorder mRecorder = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Interfaz
        setContentView(R.layout.activity_main);

        //Informacion WIFI
        wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);

        //Informacion GPS
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Start audio
        //start();

        //Listener
        addListenerOnButton();
    }

    public void medicionTest()
    {
        // Metodo de prueba
    }

    public void medicion()
    {
        // TOAST=====
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {

                Toast.makeText(getApplicationContext(), "Inicia medicion", Toast.LENGTH_LONG).show();
            }
        });
        // TOAST=====

        //Texto de entrada para prueba tomado de campo de texto
        nombreSalon = (EditText)findViewById(R.id.nombreSalon);
        String salon = nombreSalon.getText().toString();

        //HORA - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss z");
        String hora = sdf.format(cal.getTime());

        //RUIDO - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

        double ruido = darRuido();
        double roundOff = Math.round(ruido * 100.0) / 100.0;
        String ruidoStr = ""+roundOff;

        //APPS ABIERTAS - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

        //TODO APPS ABIERTAS

        //MUSICA - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

        //TODO MUSICA

        //LUZ - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

        //TODO LUZ

        //REDES - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

        //Manejadores
        wifiInfo = wifiMgr.getConnectionInfo();
        dhcpInfo = wifiMgr.getDhcpInfo();
        //WIFI INFO
        int ip = wifiInfo.getIpAddress();
        String mac = wifiInfo.getMacAddress();
        int netId = wifiInfo.getNetworkId();
        String macAP = wifiInfo.getBSSID();
        String ipAddress = Formatter.formatIpAddress(ip);
        String netIdString = Formatter.formatIpAddress(netId);
        //DHCP INFO
        int dns1 = dhcpInfo.dns1;
        int dns2 = dhcpInfo.dns2;
        int contents = dhcpInfo.describeContents();
        int gateway = dhcpInfo.gateway;
        int ipdhcp = dhcpInfo.ipAddress;
        int leaseduration = dhcpInfo.leaseDuration;
        int netmask = dhcpInfo.netmask;
        int servaddress = dhcpInfo.serverAddress;

        //GPS - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        System.out.println("GPS");
        if(location!=null) {
            if (location.hasAltitude()) {
                double altitud = location.getAltitude();
                System.out.println("altitud");
                System.out.println(altitud);
            }
            if (location.hasSpeed()) {
                double velocidad = location.getSpeed();
                System.out.println("velocidad");
                System.out.println(velocidad);
            }
            if (true) {
                double longitud = location.getLongitude();
                System.out.println("longitud");
                System.out.println(longitud);
            }
            if (true) {
                double latitud = location.getLatitude();
                System.out.println("latitud");
                System.out.println(latitud);
            }
        }

        //- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        //- - - - - - - - - - - - - - - - - - - - - - - - - - - POST - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        //- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        makeHTTPPOSTRequest(salon,ipAddress,intToIp(gateway),intToIp(netmask),macAP,hora,ruidoStr);
    }

    //Funcion para pasar numeros raros a direcciones IP STRINGs
    public String intToIp(int IpAddress) {
        return Formatter.formatIpAddress(IpAddress);
    }

    public void makeHTTPPOSTRequest(String salon, String ip,String ipAP, String netmask, String macAP, String hora, String ruidoString) {
        try {
            String urlPost = "http://157.253.195.165:5000/api/marcas";
            HttpClient c = new DefaultHttpClient();
            HttpPost p = new HttpPost(urlPost);
            p.addHeader("content-type", "application/json");
            //JSON de PRUEBA
            /*String jsonPost1 = "{\"codigo\":\"201116404\"," +
                    "\"tiempo\":\"11:04:00 15/07/2015\"," +
                    "\"lugar\":\"ML009\"," +
                    "\"ip\":\"157.253.0.3\"," +
                    "\"ipaccesspoint\":\"157.253.0.1\"," +
                    "\"ruido\":\"1\"," +
                    "\"luz\":\"2\"," +
                    "\"musica\":\"JBalvin\"," +
                    "\"temperatura\":\"20\"," +
                    "\"humedad\":\"30\"," +
                    "\"grupo\":\"201113844\"," +
                    "\"infoAdd\":\"-\"}";*/
            String jsonPost = "{\"codigo\":\".\"," +
                    "\"tiempo\":\""+hora+"\"," +
                    "\"lugar\":\""+salon+"\"," +
                    "\"ip\":\""+ip+"\"," +
                    "\"ipaccesspoint\":\""+ipAP+"\"," +
                    "\"ruido\":\""+ruidoString+"\"," +
                    "\"luz\":\".\"," +
                    "\"musica\":\".\"," +
                    "\"temperatura\":\".\"," +
                    "\"humedad\":\".\"," +
                    "\"grupo\":\""+macAP+"\"," +
                    "\"infoAdd\":\""+netmask+"\"}";
            //IMPRIMIR PETICIONES REST
            //System.out.println("- - - - - - - JSON POST1 - - - - - - - -");
            //System.out.println(jsonPost1);
            //System.out.println("- - - - - - - JSON POST2 - - - - - - - -");
            //System.out.println(jsonPost);
            p.setEntity(new StringEntity(jsonPost));
            HttpResponse r = c.execute(p);

            BufferedReader rd = new BufferedReader(new InputStreamReader(r.getEntity().getContent()));
            String line = "";
            while ((line = rd.readLine()) != null) {
                System.out.println(line);
            }

            // TOAST=====
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {

                    Toast.makeText(getApplicationContext(), "Conexion exitosa", Toast.LENGTH_LONG).show();
                }
            });
            // TOAST=====

        }
        catch(ParseException e) {
            System.out.println(e);
        }
        catch(IOException e) {
            System.out.println(e);

            // TOAST=====
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {

                    Toast.makeText(getApplicationContext(), "ERROR de conexion", Toast.LENGTH_LONG).show();
                }
            });
            // TOAST=====
        }
    }

    //FUNCIONES RUIDOMETRO

    public void start() {
        try{
            if (mRecorder == null) {
                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mRecorder.setOutputFile("/dev/null");
                mRecorder.prepare();
                mRecorder.start();
            }
        }catch(Exception e){}

    }

    public void stop() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public double getAmplitude() {
        if (mRecorder != null)
            return  mRecorder.getMaxAmplitude();
        else
            return 0;

    }

    //Metodo para dar ruido promedio
    public double darRuido()
    {
        try {
            start();
            double ruido = 0;
            double ampMax;
            ampMax = getAmplitude();
            for (int i = 0; i < ITERACIONES_RUIDO; i++) {
                Thread.sleep(T_INTERVALO_RUIDO);
                ampMax = getAmplitude();
                ruido = (ruido*i+ampMax)/(i+1);
            }
            stop();
            return ruido;
        } catch(Exception e)
        {
            stop();
            return -1;
        }
    }

    //Activar boton de medicion
    public void addListenerOnButton() {
        btnPOST = (Button) findViewById(R.id.btnDisplay);

        btnPOST.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    public void run() {
                        medicion();
                        //Metodo de prueba
                        //medicionTest();
                    }
                }).start();

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