package com.example.jfm.myapplication;

import android.app.Activity;
import android.content.SyncStatusObserver;
import android.media.MediaRecorder;
import android.net.wifi.WifiInfo;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
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
import android.text.format.Formatter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpResponse;
import org.json.JSONObject;


import java.net.URL;
import java.util.Map;

public class MainActivity extends Activity {

    private Button btnInicio;
    private EditText nombreSalon;
    private TextView textoRuido;
    private LocationManager locationManager;
    private WifiManager wifiMgr;
    private WifiInfo wifiInfo;
    private DhcpInfo dhcpInfo;
    private MediaRecorder mRecorder = null;
    private Handler handler;
    private int num;
    /*Runnable runnable=new Runnable(){
        @Override
        public void run() {
            textoRuido.setText("num "+num);
            num++;
            handler.postDelayed(runnable, 250);
        }
    };*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Informacion WIFI
        wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
        //wifiInfo = wifiMgr.getConnectionInfo();
        //int ip = wifiInfo.getIpAddress();
        //String ipAddress = Formatter.formatIpAddress(ip);
        //Informacion WIFI
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        textoRuido = (TextView) findViewById(R.id.textSonido);
        /*handler=new Handler();
        //handler.post(runnable);*/
        num = 0;
        addListenerOnButton();
    }

    public void medicion()
    {
        //Texto de entrada para prueba
        nombreSalon = (EditText)findViewById(R.id.nombreSalon);
        String salon = nombreSalon.getText().toString();
        //RUIDO
        //WIFI
        wifiInfo = wifiMgr.getConnectionInfo();
        dhcpInfo = wifiMgr.getDhcpInfo();
        int ip = wifiInfo.getIpAddress();
        String mac = wifiInfo.getMacAddress();
        int netId = wifiInfo.getNetworkId();
        int dns1 = dhcpInfo.dns1;
        int dns2 = dhcpInfo.dns2;
        int contents = dhcpInfo.describeContents();
        int gateway = dhcpInfo.gateway;
        int ipdhcp = dhcpInfo.ipAddress;
        int leaseduration = dhcpInfo.leaseDuration;
        int netmask = dhcpInfo.netmask;
        int servaddress = dhcpInfo.serverAddress;
        String ipAddress = Formatter.formatIpAddress(ip);
        String netIdString = Formatter.formatIpAddress(netId);
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
        System.out.println( intToIp(servaddress));
        //POST
        makeHTTPPOSTRequest(salon,intToIp(gateway),intToIp(netmask));
        //GPS
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

        //while(true)
        //{
        //try {
        //    TextView tv1 = (TextView) findViewById(R.id.textSonido);
        //    tv1.setText("Hello");
        //}
        //catch(Exception e)
        //{
        //    System.out.println("- - - - - - - Error - - - - - - - - -");
        //    Log.i("RM", "error", e);
        //}
        //}
    }

    public String intToIp(int IpAddress) {
        /*
        return ((i >> 24 ) & 0xFF ) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ( i & 0xFF) ;
                   */
        return Formatter.formatIpAddress(IpAddress);
    }

    public void addListenerOnButton() {
        btnInicio = (Button) findViewById(R.id.btnDisplay);

        btnInicio.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Toast.makeText(MainActivity.this,
                        "En click", Toast.LENGTH_SHORT).show();

                new Thread(new Runnable() {
                    public void run() {
                        medicion();
                    }
                }).start();

            }

        });

    }

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

    public void makeHTTPPOSTRequest(String salon, String ipAP, String netmask) {
        try {
            String urlPost = "http://157.253.195.165:5000/api/marcas";
            HttpClient c = new DefaultHttpClient();
            HttpPost p = new HttpPost(urlPost);
            p.addHeader("content-type", "application/json");
            String jsonPost1 = "{\"codigo\":\"201116404\"," +
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
                    "\"infoAdd\":\"-\"}";
            String jsonPost2 = "{\"codigo\":\"201116404\"," +
                    "\"tiempo\":\"11:04:00 15/07/2015\"," +
                    "\"lugar\":\""+salon+"\"," +
                    "\"ip\":\"157.253.0.3\"," +
                    "\"ipaccesspoint\":\""+ipAP+"\"," +
                    "\"ruido\":\"1\"," +
                    "\"luz\":\"2\"," +
                    "\"musica\":\"JBalvin\"," +
                    "\"temperatura\":\"20\"," +
                    "\"humedad\":\"30\"," +
                    "\"grupo\":\"201113844\"," +
                    "\"infoAdd\":\""+netmask+"\"}";
            System.out.println("- - - - - - - JSON POST1 - - - - - - - -");
            System.out.println(jsonPost1);
            System.out.println("- - - - - - - JSON POST2 - - - - - - - -");
            System.out.println(jsonPost2);
            p.setEntity(new StringEntity(jsonPost2));
            HttpResponse r = c.execute(p);

            BufferedReader rd = new BufferedReader(new InputStreamReader(r.getEntity().getContent()));
            String line = "";
            while ((line = rd.readLine()) != null) {
                System.out.println(line);
            }
        }
        catch(ParseException e) {
            System.out.println(e);
        }
        catch(IOException e) {
            System.out.println(e);
        }
    }
}