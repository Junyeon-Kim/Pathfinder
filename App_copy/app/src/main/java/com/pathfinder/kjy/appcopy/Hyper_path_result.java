package com.pathfinder.kjy.appcopy;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import static java.lang.Double.parseDouble;
import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import java.util.Locale;

public class Hyper_path_result extends FragmentActivity implements OnMapReadyCallback{

    private Marker currentMarker = null;

    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;

    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소

    Location mCurrentLocatiion;
    LatLng currentPosition;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;

    private View mLayout;

    private final static String QUEUE_NAME = "coor";

    private GoogleMap mMap;
    private String Origin_latitude;
    private String Origin_longitude;
    private String Desti_latitude;
    private String Desti_longitude;
    private String Origin;
    private String Destination;

    private String HPMessage;

    private LatLng hyper_point;
    private LatLng hyper_point2;
    private PolylineOptions polylineOptions;
    private PolylineOptions polylineOptions2;
    private PolylineOptions polylineOptions3;

    private String messages;

    private String[][] hyper_route2;
    private int[] hyper_path;
    private String[] bus_name;
    private double[] hyper_latitude;
    private double[] hyper_longitude;
    private String[] bus_station_name;
    private int[] transfer_boolean;
    private int[] expected_travel_time;


    private ArrayList<LatLng> polylines;
    private ArrayList<String> group_polyline;
    private List<Integer> list10;

    private LatLng location2;

    private ArrayList<String[]> theend;
    private ArrayList<LatLng> revised_LatLng;

    private List<Polyline> line;
    private List<Polyline> line2;

    private ArrayList<Marker> marker;
    private Marker marker12;

    private ArrayList<Double> latitude12;
    private ArrayList<Double> longitude12;
    private ArrayList<String> condition;
    private ArrayList<String> group_condition;

    private ArrayList<String> group_latitude12;
    private ArrayList<String> group_longitude12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.hyper_path_result);

        mLayout = findViewById(R.id.drawer);

        Log.d(TAG, "onCreate");

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent1 = getIntent();
        Origin_latitude = intent1.getExtras().getString("Origin_latitude");
        Origin_longitude = intent1.getExtras().getString("Origin_longitude");
        Origin = intent1.getExtras().getString("Origin");
        Desti_latitude = intent1.getExtras().getString("Desti_latitude");
        Desti_longitude = intent1.getExtras().getString("Desti_longitude");
        Destination = intent1.getExtras().getString("Destination");

        Button buttonOpen = (Button) findViewById(R.id.listview_go) ;
        buttonOpen.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer) ;
                if (!drawer.isDrawerOpen(Gravity.LEFT)) {
                    drawer.openDrawer(Gravity.LEFT) ;
                }
            }
        });
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                //location = locationList.get(0);

                currentPosition
                        = new LatLng(location.getLatitude(), location.getLongitude());

                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                        + " 경도:" + String.valueOf(location.getLongitude());
                Log.d(TAG, "onLocationResult : " + markerSnippet);

                //현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, markerTitle, markerSnippet);
                mCurrentLocatiion = location;
            }
        }
    };

    private void startLocationUpdates() {
        if (!checkLocationServicesStatus()) {
            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        }else {
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);

            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }

            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            if (checkPermission())
                mMap.setMyLocationEnabled(true);
        }
    }

    class TestThread2 implements Runnable {
        @Override
        public void run() {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("220.67.127.124");
            factory.setPort(5672);
            factory.setUsername("pathfinder");
            factory.setPassword("pathfinder");
            factory.setVirtualHost("/");

            try {
                Connection connection = factory.newConnection();
                Channel channel= connection.createChannel();
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);

                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), "UTF-8");
                    HPMessage = message;
                    handler.sendEmptyMessage(0);
                };
                channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        }
    }

    final Handler handler = new Handler(){
        @Override
        public void handleMessage (Message msg){
            if (msg.what ==0) {

                String revise = HPMessage.replace("[", "");
                String revise2 = revise.replace("]", "");
                String revise3 = revise2.replace("'", "");
                String[] str = revise3.split(", ");

                hyper_route2 = new String[str.length/7][7];

                for(int i=0; i<str.length/7; i++) {
                    hyper_route2[i][0] = str[7*i];
                    hyper_route2[i][1] = str[7*i+1];
                    hyper_route2[i][2] = str[7*i+2];
                    hyper_route2[i][3] = str[7*i+3];
                    hyper_route2[i][4] = str[7*i+4];
                    hyper_route2[i][5] = str[7*i+5];
                    hyper_route2[i][6] = str[7*i+6];
                }
                hyper_path = new int[hyper_route2.length];
                bus_name = new String[hyper_route2.length];
                hyper_latitude = new double[hyper_route2.length];
                hyper_longitude = new double[hyper_route2.length];
                bus_station_name = new String[hyper_route2.length];
                transfer_boolean = new int[hyper_route2.length];
                expected_travel_time = new int[hyper_route2.length];

                for(int i = 0; i < hyper_route2.length; i++) {
                    hyper_path[i] = Integer.parseInt(hyper_route2[i][0]);
                    bus_name[i] = hyper_route2[i][1];
                    hyper_latitude[i] = Double.parseDouble(hyper_route2[i][2]);
                    hyper_longitude[i] = Double.parseDouble(hyper_route2[i][3]);
                    bus_station_name[i] = hyper_route2[i][4];
                    transfer_boolean[i] = Integer.parseInt(hyper_route2[i][5]);
                    expected_travel_time[i] = Integer.parseInt(hyper_route2[i][6]);
                }

                TextView t1 = (TextView) findViewById(R.id.expected_time);
                t1.setText("예상 소요시간 :  " + Integer.toString(expected_travel_time[0]) + "분");


                String[] depart_bus = new String[hyper_route2.length];

                for(int i=0; i<hyper_route2.length-1; i++){
                    if(!bus_name[i].equals(bus_name[0])){
                        depart_bus[i] = (bus_name[i]);
                        break;
                    }
                }

                List<String> list = new ArrayList<String>();
                List<String[]> list2 = new ArrayList<String[]>();
                List<String> list3 = new ArrayList<String>();
                List<String> list4 = new ArrayList<String>();
                List<String> list5 = new ArrayList<String>();
                List<String> list6 = new ArrayList<String>();
                List<String> list7 = new ArrayList<String>();
                List<String> list8 = new ArrayList<String>();
                List<Integer> list9 = new ArrayList<Integer>();
                list10 = new ArrayList<>();

                for(int i=0; i<hyper_route2.length; i++){
                    list9.add(hyper_path[i]);
                }

                int t = 0;
                while (t < 1000) {
                    for (int k = 0; k < list9.size(); k++) {
                        if (!list10.contains(list9.get(k))) {
                            list10.add(list9.get(k));
                        }
                    }
                    t++;
                }

                for(int i = 0; i < hyper_route2.length-1; i++) {
                    if(hyper_path[i] != hyper_path[i+1]){
                        for(int j=i+1; j< hyper_route2.length-2; j++){
                            if(!bus_name[j].equals(bus_name[0])){
                                depart_bus[j] = bus_name[j];
                                break;
                            }
                        }
                    }
                }

                for(String s : depart_bus) {
                    if(s != null && s.length() > 0) {
                        list.add(s);
                    }
                }

                int y = 0;
                while (y < 1000) {
                    for (int k = 0; k < list.size(); k++) {
                        if (!list6.contains(list.get(k))) {
                            list6.add(list.get(k));
                        }
                    }
                    y++;
                }

                polylines = new ArrayList<LatLng>();
                group_polyline = new ArrayList<String>();
                line = new ArrayList<>();
                line2 = new ArrayList<>();
                latitude12 = new ArrayList<>();
                longitude12 = new ArrayList<>();
                group_latitude12 = new ArrayList<>();
                group_longitude12 = new ArrayList<>();
                condition = new ArrayList<>();
                group_condition = new ArrayList<>();

                marker = new ArrayList<>();

                for(int i = 0; i < hyper_route2.length-1; i++) {
                    if(hyper_path[i] == hyper_path[i+1]) {
                        hyper_point = new LatLng(hyper_latitude[i], hyper_longitude[i]);

                        latitude12.add(hyper_latitude[i]);
                        longitude12.add(hyper_longitude[i]);

                        condition.add(bus_name[i] + "x" + bus_station_name[i]);

                        polylines.add(hyper_point);
                    } else{
                        hyper_point2 = new LatLng(hyper_latitude[i], hyper_longitude[i]);
                        latitude12.add(hyper_latitude[i]);
                        longitude12.add(hyper_longitude[i]);
                        condition.add(bus_name[i] + "x" + bus_station_name[i]);

                        group_latitude12.add(latitude12.toString());
                        group_longitude12.add(longitude12.toString());
                        group_condition.add(condition.toString());

                        polylines.add(hyper_point2);

                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.RED);
                        polylineOptions.width(15);
                        polylineOptions.addAll(polylines);

                        line.add(mMap.addPolyline(polylineOptions));

                        group_polyline.add(polylines.toString());

                        polylines.clear();
                        latitude12.clear();
                        longitude12.clear();
                        condition.clear();
                    }
                }
                group_latitude12.add(latitude12.toString());
                group_longitude12.add(longitude12.toString());
                group_condition.add(condition.toString());

                group_polyline.add(polylines.toString());

                polylineOptions2 = new PolylineOptions();
                polylineOptions2.color(Color.RED);
                polylineOptions2.width(15);
                polylineOptions2.addAll(polylines);

                line2.add(mMap.addPolyline(polylineOptions2));

                theend = new ArrayList<>();

                for(int i=0; i<group_polyline.size(); i++) {
                    String test2 = group_polyline.get(i);
                    String revised = test2.replace("[", "");
                    String revised2 = revised.replace("]", "");
                    String revised3 = revised2.replace("'", "");
                    String[] strd = revised3.split(", ");

                    theend.add(strd);
                }

                for(int i = 0; i < hyper_route2.length-1; i++) {
                    if(transfer_boolean[i] == 1){
                        String[][] transfer_bus = new String[hyper_route2.length][2];
                        transfer_bus[i][0] = bus_name[i];
                        transfer_bus[i][1] = bus_station_name[i];

                        list2.add(new String[]{transfer_bus[i][0], transfer_bus[i][1]});
                    } else continue;
                }

                if(!list2.isEmpty()){
                    list3.add(list2.get(0)[1]);
                }

                if(list2.size() > 1) {
                    for(int i=0; i<list2.size()-1; i++) {
                        if(list2.get(i)[1].equals(list2.get(i+1)[1])) {
                            continue;
                        }else list3.add(list2.get(i+1)[1]);
                    }
                }

                int x = 0;
                while (x < 1000) {
                    for (int k = 0; k < list3.size(); k++) {
                        if (!list5.contains(list3.get(k))) {
                            list5.add(list3.get(k));
                        }
                    }
                    x++;
                }

                if(list2.size() > 1) {
                    for(int i=0; i<list5.size(); i++) {
                        for(int j=0; j<list2.size(); j++) {
                            if(list5.get(i).equals(list2.get(j)[1])) {
                                list4.add(list2.get(j)[0]);
                            }
                        }
                        for(int k=0; k<hyper_route2.length; k++){
                            if(list5.get(i) == bus_station_name[k]){
                                hyper_point = new LatLng(hyper_latitude[k], hyper_longitude[k]);
                                MarkerOptions hyper_Transfer_point = new MarkerOptions();
                                hyper_Transfer_point.title(bus_station_name[k]);
                                hyper_Transfer_point.snippet("후보 List : " + list4);
                                hyper_Transfer_point.position(hyper_point);
                                hyper_Transfer_point.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                                marker12 = mMap.addMarker(hyper_Transfer_point);
                                marker12.setTag(hyper_path[k]);
                                marker.add(marker12);
                            }
                        }
                        list4.clear();
                    }
                } else {
                    for(int k=0; k<hyper_route2.length; k++){
                        if(!list3.isEmpty()) {
                            if (list3.get(0) == bus_station_name[k]) {
                                hyper_point = new LatLng(hyper_latitude[k], hyper_longitude[k]);
                                MarkerOptions hyper_Transfer_point = new MarkerOptions();
                                hyper_Transfer_point.title(bus_station_name[k]);
                                hyper_Transfer_point.snippet("후보 List : [" + bus_name[k] + "]");
                                hyper_Transfer_point.position(hyper_point);
                                hyper_Transfer_point.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                                marker12 = mMap.addMarker(hyper_Transfer_point);
                                marker12.setTag(hyper_path[k]);
                                marker.add(marker12);
                            }
                        }
                    }
                }

                for(int i=0; i<hyper_route2.length-1; i++) {
                    if(!bus_name[i].equals(bus_name[0])) {
                        LatLng origin_point = new LatLng(hyper_latitude[i], hyper_longitude[i]);
                        MarkerOptions mOptions1 = new MarkerOptions();
                        mOptions1.title(bus_station_name[i]);
                        mOptions1.snippet("후보 List : " + list6);
                        mOptions1.position(origin_point);
                        marker12 = mMap.addMarker(mOptions1);
                        marker12.setTag(777);
                        marker.add(marker12);
                        break;
                    }
                }

                LatLng destination_point = new LatLng(parseDouble(Desti_latitude), parseDouble(Desti_longitude));
                MarkerOptions mOptions2 = new MarkerOptions();
                mOptions2.title(Destination);
                mOptions2.position(destination_point);
                marker12 = mMap.addMarker(mOptions2);
                marker12.setTag(333);
                marker.add(marker12);

                LatLng hyper = new LatLng((parseDouble(Origin_latitude) + parseDouble(Desti_latitude))*0.5, (parseDouble(Origin_longitude) + parseDouble(Desti_longitude))*0.5);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hyper, 12));

                ListView listview;
                ListViewAdaptor adapter;

                // Adapter 생성
                adapter = new ListViewAdaptor();

                // 리스트뷰 참조 및 Adapter달기
                listview = (ListView) findViewById(R.id.route_detail);
                listview.setAdapter(adapter);

                for(int i=0; i<hyper_route2.length; i++){
                    list7.add(Integer.toString(hyper_path[i]));
                }

                int z = 0;
                while (z < 1000) {
                    for (int k = 0; k < list7.size(); k++) {
                        if (!list8.contains(list7.get(k))) {
                            list8.add(list7.get(k));
                        }
                    }
                    z++;
                }

                List<String> path = new ArrayList<String>();
                List<String> path2 = new ArrayList<String>();
                List<String> depart_stop = new ArrayList<String>();
                List<String> transfer_stop = new ArrayList<String>();
                List<String> arrive_stop = new ArrayList<String>();
                List<String> departure_bus = new ArrayList<String>();

                for(int i=0; i<list8.size(); i++) {
                    for(int j=0; j<hyper_path.length-1; j++) {
                        if(list8.get(i).equals(Integer.toString(hyper_path[j]))) {
                            path.add(bus_station_name[j]);
                            path2.add(bus_name[j]);
                            if(transfer_boolean[j] == 1) {
                                transfer_stop.add(bus_station_name[j] + " " + bus_name[j]);
                            }
                        }
                    }

                    for(int r=0; r<hyper_route2.length-1; r++){
                        if(!bus_name[r].equals(bus_name[0])){
                            depart_stop.add(path.get(r) + " " + path2.get(r));
                            break;
                        }
                    }

                    arrive_stop.add(path.get(path.size()-2));

                    if(transfer_stop.isEmpty()){
                        transfer_stop.add("환승 없음");
                    }

                    String a = depart_stop.toString();
                    String b = transfer_stop.toString();
                    String c = arrive_stop.toString();

                    adapter.addItem(a, b, c);

                    path.clear();
                    path2.clear();
                    transfer_stop.clear();
                    depart_stop.clear();
                    departure_bus.clear();
                    arrive_stop.clear();
                }

            }
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Thread thread = new Thread(new TestThread2());
        thread.start();

        mMap = googleMap;

        setDefaultLocation();

        messages = "['0', 'w', '37.486518499999995', '127.07065720000001', 'r', '0', '43', '0', '420', '37.48383029836474', '127.0691287253194', '주공4단지', '0', '43', '0', '420', '37.4867451919951', '127.06740777662675', '개포주공5단지경기여고', '0', '43', '0', '420', '37.48863910860667', '127.06647963464657', '개포동역.개포시장', '0', '43', '0', '420', '37.49277570160585', '127.06429091707821', '대치역강남구민회관', '0', '43', '0', '420', '37.49277570160585', '127.06429091707821', '대치역강남구민회관', '0', '43', '0', 'w', '37.494576993540704', '127.063462172234', '대치역', '0', '43', '0', '3호선', '37.494576993540704', '127.063462172234', '대치역', '1', '43', '0', '3호선', '37.4909171535387', '127.055402372225', '도곡역', '0', '43', '0', '3호선', '37.4870418535309', '127.04695407221901', '매봉역', '0', '43', '0', '3호선', '37.4845477535219', '127.033958072211', '양재역', '0', '43', '0', '3호선', '37.4849183935085', '127.016290172208', '남부터미널역', '0', '43', '0', '3호선', '37.4849183935085', '127.016290172208', '남부터미널역', '0', '43', '0', 'w', '37.48119562708348', '127.0141766143708', '예술의전당', '0', '43', '0', 'w', '37.4789929', '127.01162179999999', 's', '0', '43', '1', 'w', '37.486518499999995', '127.07065720000001', 'r', '0', '43', '1', '461', '37.48383029836474', '127.0691287253194', '주공4단지', '0', '43', '1', '461', '37.4867451919951', '127.06740777662675', '개포주공5단지경기여고', '0', '43', '1', '461', '37.48863910860667', '127.06647963464657', '개포동역.개포시장', '0', '43', '1', '461', '37.49277570160585', '127.06429091707821', '대치역강남구민회관', '0', '43', '1', '461', '37.495593648086576', '127.06285350184922', '은마아파트', '0', '43', '1', '461', '37.49875163907115', '127.05998673977727', '은마아파트입구사거리', '0', '43', '1', '461', '37.497652844268906', '127.05665295509527', '베스티안병원', '0', '43', '1', '461', '37.49678381159031', '127.0538931373053', '한티역2번출구.서울강남고용노동지청', '0', '43', '1', '461', '37.49597185613767', '127.05151716843436', '한티역7번출구', '0', '43', '1', '461', '37.49465204341267', '127.04762099419649', '역삼중학교.강남세브란스병원', '0', '43', '1', '461', '37.49284804912675', '127.04180867497291', '도곡1차IPARK.모커리한방병원', '0', '43', '1', '461', '37.49184959934925', '127.03868899617486', '역삼럭키아파트.역삼월드메르디앙아파트', '0', '43', '1', '461', '37.49032266567607', '127.0337555780698', '뱅뱅사거리기쁨병원', '0', '43', '1', '461', '37.48898974072346', '127.02943048147316', '뱅뱅사거리', '0', '43', '1', '461', '37.488156490144334', '127.02671606908692', '서초동무지개아파트', '0', '43', '1', '461', '37.486525561863715', '127.02125304031073', '서일초등학교', '0', '43', '1', '461', '37.48546050549669', '127.01777293570605', '국제전자센터', '0', '43', '1', '461', '37.48546050549669', '127.01777293570605', '국제전자센터', '0', '43', '1', 'w', '37.4849183935085', '127.016290172208', '남부터미널역', '0', '43', '1', 'w', '37.482012081398224', '127.01700392295', '남부터미널', '0', '43', '1', '서초22', '37.482012081398224', '127.01700392295', '남부터미널', '1', '43', '1', '서초22', '37.48119562708348', '127.0141766143708', '예술의전당', '0', '43', '1', '서초22', '37.47978932561959', '127.0108596417038', '신중초등학교', '0', '43', '1', '서초22', '37.47978932561959', '127.0108596417038', '신중초등학교', '0', '43', '1', 'w', '37.4789929', '127.01162179999999', 's', '0', '43']";

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {
            startLocationUpdates();
        }else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                        ActivityCompat.requestPermissions( Hyper_path_result.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                Log.d( TAG, "onMapClick :");
            }
        });
        googleMap.setOnInfoWindowClickListener(infoWindowClickListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        if (checkPermission()) {
            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            if (mMap!=null)
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFusedLocationClient != null) {
            Log.d(TAG, "onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    public String getCurrentAddress(LatLng latlng) {
        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }
    }

    public void setDefaultLocation() {
        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";

        if (currentMarker != null) currentMarker.remove();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 12);
        mMap.moveCamera(cameraUpdate);
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {
        if (currentMarker != null) currentMarker.remove();

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
    }

    private boolean checkPermission() {
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if ( check_result ) {
                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                startLocationUpdates();
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }else {
                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }
            }
        }
    }

    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(Hyper_path_result.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    GoogleMap.OnInfoWindowClickListener infoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
            if(!marker.getTitle().equals(Destination)){
                String getList4 = marker.getSnippet();
                String a = getList4.replace("후보 List : ", "");
                String b = a.replace("[", "");
                String c = b.replace("]", "");
                String[] d = c.split(", ");

                location2 = marker.getPosition();
                double loc_latitude = location2.latitude;
                double loc_longitude = location2.longitude;
                LatLng loc_position = location2;

                Intent intent2 = new Intent(Hyper_path_result.this, PopupActivity.class);
                intent2.putExtra("data", d);
                intent2.putExtra("loc_position", loc_position.toString());
                intent2.putExtra("loc_latitude", Double.toString(loc_latitude));
                intent2.putExtra("loc_longitude", Double.toString(loc_longitude));
                intent2.putExtra("Tag", marker.getTag().toString());
                intent2.putExtra("marker_name", marker.getTitle());
                startActivityForResult(intent2, 1);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                //데이터 받기
                String result = data.getStringExtra("choosed_bus");
                String marker_position = data.getStringExtra("loc_position");
                String marker_latitude = data.getStringExtra("loc_latitude");
                String marker_longitude = data.getStringExtra("loc_longitude");
                String marker_tag = data.getStringExtra("Tag");
                String marker_name = data.getStringExtra("marker_name");

                for(int i=0; i<group_condition.size(); i++) {
                    String x1 = group_condition.get(i);
                    String y1 = x1.replace("[", "");
                    String z1 = y1.replace("]", "");
                    String[] w1 = z1.split(", ");

                    boolean check_condition = Arrays.asList(w1).contains(result + "x" + marker_name);

                    if (check_condition == false) {
                        for (int j = 0; j < theend.get(i).length; j++) {
                            theend.get(i)[j] = "lat/lng: (0,0)";
                        }
                        for(int u=0; u<marker.size(); u++){
                            if(marker.get(u).getTag().toString().equals(Integer.toString(i))){
                                marker.get(u).setVisible(false);
                            }
                        }
                    }
                }

                revised_LatLng = new ArrayList<LatLng>();
                String revised_latitude;
                String revised_longitude;
                LatLng cc;

                for(Polyline Line : line){
                    Line.remove();
                }

                for(Polyline Line2 : line2){
                    Line2.remove();
                }

                for(int i=0; i<theend.size(); i++){
                    for(int j=0; j<theend.get(i).length; j++) {
                        polylineOptions3 = new PolylineOptions();
                        polylineOptions3.color(Color.RED);
                        polylineOptions3.width(15);

                        String a = theend.get(i)[j].replace("lat/lng: (", "");
                        String b = a.replace(")", "");
                        String[] c = b.split(",");

                        revised_latitude = c[0];
                        revised_longitude = c[1];

                        cc = new LatLng(Double.parseDouble(revised_latitude), Double.parseDouble(revised_longitude));

                        if(!revised_latitude.equals("0") && !revised_longitude.equals("0")) {
                            revised_LatLng.add(cc);
                            polylineOptions3.addAll(revised_LatLng);
                            line.add(this.mMap.addPolyline(polylineOptions3));
                        }
                    }
                    revised_LatLng.clear();
                }
            }
        }
    }
}