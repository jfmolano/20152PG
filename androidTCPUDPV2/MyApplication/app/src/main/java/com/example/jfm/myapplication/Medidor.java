package com.example.jfm.myapplication;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
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

public class Medidor extends Service {
    //Constantes
    private final static int ITERACIONES_RUIDO = 1;
    private final static int T_INTERVALO_RUIDO = 1000;
    //Boton de comunicacion POST
    private Button btnPOST;
    private EditText nombreSalon;
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

    @Override
    public void onCreate() {
        System.out.println(" - - - - - - - - - - - Inicia el servicio - - - - - - - - - - -");

        //Informacion WIFI
        wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);

        //Informacion GPS
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Start audio
        //start();

        //Luz
        mDataCollection = new DataCollection(this);

        //Musica
        artista = "";
        album = "";
        IntentFilter iF = new IntentFilter();
        iF.addAction("com.android.music.metachanged");
        iF.addAction("com.android.music.playstatechanged");
        iF.addAction("com.android.music.playbackcomplete");
        iF.addAction("com.android.music.queuechanged");
        iF.addAction("com.android.music.metachanged");
        //Nuevas lineas
        iF.addAction("com.htc.music.metachanged");
        iF.addAction("fm.last.android.metachanged");
        iF.addAction("com.sec.android.app.music.metachanged");
        iF.addAction("com.nullsoft.winamp.metachanged");
        iF.addAction("com.amazon.mp3.metachanged");
        iF.addAction("com.miui.player.metachanged");
        iF.addAction("com.real.IMP.metachanged");
        iF.addAction("com.sonyericsson.music.metachanged");
        iF.addAction("com.rdio.android.metachanged");
        iF.addAction("com.samsung.sec.android.MusicPlayer.metachanged");
        iF.addAction("com.andrew.apollo.metachanged");
        registerReceiver(mReceiver, iF);

        //Redes 2
        iF.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(new BroadcastReceiver() {

            @Override

            public void onReceive(Context context, Intent intent) {
                mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                mWifiManager.getScanResults();
                scanResults = mWifiManager.getScanResults();
                listoWifiScan = true;
            }
        }
                , iF);
        listoWifiScan = false;

        new Thread(new Runnable() {
            public void run() {
                medicion();
                //Metodo de prueba
                //medicionTest();
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    public ScanResult darMasPoderoso(){
        ScanResult resp = null;
        mWifiManager.startScan();
        while(!listoWifiScan){}
        listoWifiScan = false;
        Iterator<ScanResult> it = scanResults.iterator();
        int maximo = Integer.MIN_VALUE;
        while(it.hasNext())
        {
            ScanResult sR = it.next();
            if(sR.SSID.equals("SENECA"))
            {
                int potencia = sR.level;
                if(potencia > maximo)
                {
                    maximo = potencia;
                    resp = sR;
                }
            }/*
            System.out.println("SSID: "+sR.SSID);
            System.out.println("BSSID: "+sR.BSSID);
            System.out.println("Nivel: "+sR.level);*/
        }

        return resp;
    }

    public void medicionTest()
    {
        // Metodo de prueba
        try {
            escribirSDSalonMAC();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // TOAST=====
        /*
        Medidor.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), "Apps: "+apps, Toast.LENGTH_LONG).show();
            }
        });
        */
        // TOAST=====
    }

    public void medicion()
    {

        String salon = "Salon";

        //HORA - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss z");
        String hora = sdf.format(cal.getTime());

        //RUIDO - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

        double ruido = darRuido();
        double roundOff = Math.round(ruido * 100.0) / 100.0;
        String ruidoStr = ""+roundOff;

        //APPS ABIERTAS - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

        ActivityManager actvityManager = (ActivityManager)
                this.getSystemService( ACTIVITY_SERVICE );
        List<ActivityManager.RunningTaskInfo> packageNameList = actvityManager.getRunningTasks(1);
        Iterator<ActivityManager.RunningTaskInfo> it = packageNameList.iterator();
        apps = "";
        while(it.hasNext())
        {
            ActivityManager.RunningTaskInfo task = it.next();
            apps = apps+";"+task.topActivity.getPackageName();
        }

        //MUSICA - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

        AudioManager manager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        enReproduccion = "enReproduccion";
        if(!manager.isMusicActive())
        {
            enReproduccion = "NOenReproduccion";
        }

        //LUZ - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

        float luz = mDataCollection.darLuzActual();
        String luzStr = luz + "";

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
        //BSSID mas poderoso
        String scanRes = darBSSIDMasPoderoso();

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
        makeHTTPPOSTRequest(salon,ipAddress,intToIp(gateway),intToIp(netmask),scanRes,hora,ruidoStr,luzStr);
    }

    //Funcion para pasar numeros raros a direcciones IP STRINGs
    public String intToIp(int IpAddress) {
        return Formatter.formatIpAddress(IpAddress);
    }

    String darBSSIDMasPoderoso()
    {
        ScanResult scRes = darMasPoderoso();
        String scanRes = "No SENECA";
        if(scRes!=null)
        {
            scanRes = scRes.BSSID;
        }
        return scanRes;
    }

    public void makeHTTPPOSTRequest(String salon, String ip,String ipAP, String netmask, String macAP, String hora, String ruidoString, String luzString) {
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
                    "\"luz\":\""+luzString+"\"," +
                    "\"musica\":\""+enReproduccion+";"+pista+";"+artista+"\"," +
                    "\"temperatura\":\".\"," +
                    "\"humedad\":\""+apps+"\"," +
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

        }
        catch(ParseException e) {
            System.out.println(e);
        }
        catch(IOException e) {
            System.out.println(e);
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

    //onReceive musica
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            String cmd = intent.getStringExtra("command");
            artista = intent.getStringExtra("artist");
            album = intent.getStringExtra("album");
            pista = intent.getStringExtra("track");
        }
    };

    public void escribirSDSalonMAC() throws IOException {
        File folder = new File("/storage/extSdCard/TesisSalones");
        boolean var = false;
        if (!folder.exists())
            var = folder.mkdir();

        System.out.println("" + var);

        final String filename = folder.toString() + "/" + "Test.csv";

        // show waiting screen
        CharSequence contentTitle = getString(R.string.app_name);

        FileWriter fw = new FileWriter(filename);

        fw.append("Salon");
        fw.append(',');

        fw.append("MAC");
        fw.append(',');

        fw.append('\n');

        String salon = "";
        String mac = "";
        j = 0;
        while(!salon.equals("FIN")) {
            salon = "Salon";
            System.out.println(salon);
            fw.append(salon);
            fw.append(',');

            fw.append(darBSSIDMasPoderoso());
            fw.append(',');

            fw.append('\n');
            j++;
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        fw.close();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
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