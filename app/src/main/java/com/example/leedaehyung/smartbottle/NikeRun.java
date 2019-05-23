package com.example.leedaehyung.smartbottle;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.leedaehyung.smartbottle.sessionmanager.SessionManager;
import com.nhn.android.maps.NMapContext;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPathData;
import com.nhn.android.maps.overlay.NMapPathLineStyle;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPathDataOverlay;
import com.nhn.android.mapviewer.overlay.NMapResourceProvider;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by Lee DaeHyung on 2019-03-26.
 */

public class NikeRun extends NMapFragment{

    private View v = null;
    String getSpeedCal="";
    private SessionManager sm;
    private TextView tvTime;
    private TextView tvSpeed;
    private TextView tvCalory;
    private TextView tvDistance;
    private String locationProvider = null;
    private Location lastKnownLocation = null;
    private Button buttonMap;
    private NMapContext mMapContext;
    private NMapView nMapView;
    private NMapController mapController;
    private final int REQUEST_LOCATION_PERMISSION = 1;
    ArrayList<Double> list = new ArrayList<Double>();
    private Button startButton;
    private Button resetButton;
    private TextView myOutput;
    final static int Init = 0;
    final static int Run = 1;
    final static int Pause = 2;
    double longitude; //위도 127.xxx
    double latitude;  //경도 37.xxxx
    double distance=0;
    int getweight=0;
    LocationManager lm;

    int cur_Status = Init; //현재의 상태를 저장할변수를 초기화함.
    int myCount = 1;
    long myBaseTime;
    long myPauseTime;
    Button gpslocation;


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("aaa", String.valueOf(NikeRun.this));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMapContext = new NMapContext(super.getActivity());
        mMapContext.onCreate();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.content_nikerun, container, false);
        //this.v = v;
        sm = new SessionManager(getContext());
        tvSpeed = v.findViewById(R.id.setspeed);
        tvCalory = v.findViewById(R.id.setcalory);
        tvDistance = v.findViewById(R.id.distance);

        startButton = v.findViewById(R.id.button_start);
        resetButton = v.findViewById(R.id.button_reset);
        myOutput = v.findViewById(R.id.timer);

        nMapView = (NMapView) v.findViewById(R.id.mapView);
        nMapView.setNcpClientId("szm3hgcaxr");
        nMapView.setClickable(true);
        gpslocation = v.findViewById(R.id.button_navermap);
        /////////////////////위치 받자///////////////////////
        lm = (LocationManager) getLayoutInflater().getContext().getSystemService(Context.LOCATION_SERVICE);
        assert lm != null;
        /////////////////////초기 현재 위치 받자///////////////////////


        ///////////////////////////////////////////////
        startButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                switch (cur_Status) {
                    case Init:
                        Log.e("로그", "위치발동");
                        WeightDialog dialog = WeightDialog.newInstance(new WeightDialog.NameInputListener(){
                            @Override
                            public void onNameInputComplete(int weight) {
                                getweight=weight;
                                if(getweight!=0){
                                    ///서버로 몸무게 보내기
                                    new WeightTask().execute("http://ec2-52-79-237-177.ap-northeast-2.compute.amazonaws.com:65001/weight");
                                    Log.e("서버로 몸무게","몸무게 송신 완료");
                                }
                                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    requestLocationPermission();
                                } else {
                                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                            2000,      //최소 시간 간격(ms)
                                            2,        //최소 거리(m)
                                            mLocationListener);

                                }
                                myBaseTime = SystemClock.elapsedRealtime();
                                System.out.println(myBaseTime);
                                //myTimer이라는 핸들러를 빈 메세지를 보내서 호출
                                myTimer.sendEmptyMessage(0);
                                startButton.setText("멈춤"); //버튼의 문자"시작"을 "멈춤"으로 변경
                                //myBtnRec.setEnabled(true); //기록버튼 활성
                                cur_Status = Run; //현재상태를 런상태로 변경
                            }
                        });
                        dialog.show(getFragmentManager(),"addDialog");

                        break;
                    case Run:
                        myTimer.removeMessages(0); //핸들러 메세지 제거
                        myPauseTime = SystemClock.elapsedRealtime();
                        startButton.setText("시작");
                        //myBtnRec.setText("리셋");
                        cur_Status = Pause;
                        break;
                    case Pause:
                        long now = SystemClock.elapsedRealtime();
                        myTimer.sendEmptyMessage(0);
                        myBaseTime += (now - myPauseTime);
                        startButton.setText("멈춤");
                        //myBtnRec.setText("기록");
                        cur_Status = Run;
                        break;


                }
            }
        });
        gpslocation.setOnClickListener(new View.OnClickListener(){
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onClick(View v) {
                /////////////////폴리라인그리기////////////////////////////////
                NMapPathData pathData = new NMapPathData(list.size());
                pathData.initPathData();
                for (int i = 0; i < list.size(); i += 2) {
                    pathData.addPathPoint(list.get(i), list.get(i + 1), NMapPathLineStyle.DATA_TYPE_POLYLINE);
                }
                NMapPathLineStyle pathLineStyle = new NMapPathLineStyle(nMapView.getContext());
                pathLineStyle.setLineColor(0xfffbb33,0xff);
                Toast.makeText(getContext(), String.valueOf(list.size()), Toast.LENGTH_SHORT).show();
                pathData.endPathData();
                NMapPathDataOverlay pathDataOverlay = mOverlayManager.createPathDataOverlay(pathData);
                pathDataOverlay.showAllPathData(0);
                tvDistance.setText(String.format("%.2f",distance)+"m");
                /////////////////폴리라인그리기/////////////////////////////////
                try {
                    getSpeedCal= new TimeDistanceTask().execute("http://ec2-52-79-237-177.ap-northeast-2.compute.amazonaws.com:65001/time_distance").get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.e("속력칼로리",getSpeedCal);
                if(getSpeedCal!="") {
                    StringTokenizer speedCal = new StringTokenizer(getSpeedCal,"/");

                    tvSpeed.setText(String.format("%.2f",Double.parseDouble(speedCal.nextToken()))+"km/h");
                    tvCalory.setText(String.format("%.2f",Double.parseDouble(speedCal.nextToken()))+"kcal");
                }
            }
        });
        resetButton.setOnClickListener(new View.OnClickListener(){
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                //핸들러를 멈춤
                myTimer.removeMessages(0);

                lm.removeUpdates(mLocationListener);    //리셋 누르면 위치 업데이트 중지

                startButton.setText("시작");
                //myBtnRec.setText("기록");
                //서버로 time과 distance값 보내기
                //그 후 0초로 초기화
                myOutput.setText("00:00:00");
                tvDistance.setText("0m");
                cur_Status = Init;
                myCount = 1;

                //myRec.setText("");
            }
        });

        ButterKnife.bind(this, v);
        return v;
    }


    private NMapOverlayManager mOverlayManager;
    private NMapResourceProvider mMapViewerResourceProvider;

    @Override
    public void onStart() {
        super.onStart();

        nMapView.setOnMapStateChangeListener(this);
        nMapView.setBuiltInZoomControls(true, null);
        mapController = nMapView.getMapController();
        mapController.setMapCenter(127.12937838381717,37.45025807626762, 11);
        mOverlayManager = new NMapOverlayManager(getActivity(), nMapView, null);


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onMapInitHandler(NMapView nMapView, NMapError nMapError) {
        if (nMapError == null) {//success

            mapController.setMapCenter(new NGeoPoint(127.12937838381717, 37.45025807626762), 11);

            //set POI data
            NMapPOIdata poIdata = new NMapPOIdata(2, null);
            poIdata.beginPOIdata(2);
            poIdata.addPOIitem(127.12937838381717, 37.45025807626762, "대형이", 0, 0);
            poIdata.addPOIitem(127.12937838381717, 37.45025807626762, "안녕", 0, 0);
            poIdata.endPOIdata();
        }
        super.onMapInitHandler(nMapView, nMapError);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private final LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            //위치 값이 바뀌면 이벤트 발생
            Location locationA = new Location("pointA");
            Location locationB = new Location("pointB");

            if(list.size()!=0) {
                locationA.setLongitude(longitude);
                locationA.setLatitude(latitude);
            }

            longitude = location.getLongitude(); //위도 127.xxx
            latitude = location.getLatitude();  //경도 37.xxxx

            locationB.setLongitude(longitude);
            locationB.setLatitude(latitude);
            //두 값의 거리를 구함
            if(list.size()>=4){
                distance += locationA.distanceTo(locationB);
                Log.e("AB 거리", String.valueOf(distance));
            }

            //갱신될 때마다 ArrayList에 추가했으면 좋겠다..
            Toast.makeText(getContext(), "위치 갱신됨", Toast.LENGTH_SHORT).show();
            list.add(longitude);
            list.add(latitude);
            for (int i = 0; i < list.size() / 2; i += 2) {

                Log.e("127", String.valueOf(list.get(i)));
                Log.e("37", String.valueOf(list.get(i + 1)));
            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    public void requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (EasyPermissions.hasPermissions(getContext(), perms)) {
            Toast.makeText(getContext(), "Permission already granted", Toast.LENGTH_SHORT).show();
        } else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
        }
    }

    @SuppressLint("HandlerLeak")
    Handler myTimer = new Handler() {
        public void handleMessage(Message msg) {
            myOutput.setText(getTimeOut());

            //sendEmptyMessage 는 비어있는 메세지를 Handler 에게 전송하는겁니다.
            myTimer.sendEmptyMessage(0);
        }
    };

    //현재시간을 계속 구해서 출력하는 메소드
    String getTimeOut() {
        long now = SystemClock.elapsedRealtime(); //애플리케이션이 실행되고나서 실제로 경과된 시간(??)^^;
        long outTime = now - myBaseTime;
        @SuppressLint("DefaultLocale") String easy_outTime = String.format("%02d:%02d:%02d", (outTime / 1000 / 3600) , (outTime / 1000 / 60)% 60, (outTime / 1000) % 60);
        return easy_outTime;

    }

    ///////////////////////////몸무게 서버로 보내기////////////////////////////////
    @SuppressLint("StaticFieldLeak")
    public class WeightTask extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... strings) {
            try{
                //JSONObject 를 만들고 키값 형식으로 저장해준다.

                JSONObject jsonObject=new JSONObject();

                //jsonObject.accumulate("id",sm.getse());
                jsonObject.accumulate("weight",getweight);
                HttpURLConnection con= null;
                BufferedReader reader=null;
                try {
                    URL url = new URL("http://ec2-52-79-237-177.ap-northeast-2.compute.amazonaws.com:65001/weight");
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");//POST방식으로 보냄
                    con.setRequestProperty("Cache-Control", "no-cache");//캐시설정
                    con.setRequestProperty("Content-Type", "application/json");//application JSON 형식으로 전송
                    con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                    con.setDoOutput(true);//OutStream으로 post데이터를 넘겨주곘다.
                    con.setDoInput(true);//InputStream으로 서버로부터 응답을 받겠다.
                    con.connect();
                    //서버로 보내기 위해 스트림 생성
                    OutputStream outStream = con.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                    writer.write(jsonObject.toString());
                    writer.flush();
                    writer.close();
                    //서버로부터 데이터를 받음
                    InputStream stream = con.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuffer buffer = new StringBuffer();
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    return buffer.toString();   //서버로부터 받은 값을 리턴해줌
                }catch (MalformedURLException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(con != null){
                        con.disconnect();
                    }
                    try {
                        if(reader != null){
                            reader.close();//버퍼를 닫아줌
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // 서버로부터 받을값을 출력
        }


    }

    ///////////////////////////시간과 거리 서버로 보내기////////////////////////////////
    @SuppressLint("StaticFieldLeak")
    public class TimeDistanceTask extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                //JSONObject 를 만들고 키값 형식으로 저장해준다.

                JSONObject jsonObject = new JSONObject();
                StringTokenizer time = new StringTokenizer(myOutput.getText().toString(),":");
                String hour = time.nextToken();
                String min = time.nextToken();
                String sec = time.nextToken();
                double hours = Double.parseDouble(hour) + (Double.parseDouble(min)*60 + Double.parseDouble(sec))/3600;


                jsonObject.accumulate("jogtime", hours);
                jsonObject.accumulate("distance", distance);
                HttpURLConnection con = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL("http://ec2-52-79-237-177.ap-northeast-2.compute.amazonaws.com:65001/time_distance");
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");//POST방식으로 보냄
                    con.setRequestProperty("Cache-Control", "no-cache");//캐시설정
                    con.setRequestProperty("Content-Type", "application/json");//application JSON 형식으로 전송
                    con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                    con.setDoOutput(true);//OutStream으로 post데이터를 넘겨주곘다.
                    con.setDoInput(true);//InputStream으로 서버로부터 응답을 받겠다.
                    con.connect();
                    //서버로 보내기 위해 스트림 생성
                    OutputStream outStream = con.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                    writer.write(jsonObject.toString());
                    writer.flush();
                    writer.close();
                    //서버로부터 데이터를 받음
                    InputStream stream = con.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuffer buffer = new StringBuffer();
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    return buffer.toString();   //서버로부터 받은 값을 리턴해줌
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                    try {
                        if (reader != null) {
                            reader.close();//버퍼를 닫아줌
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // 서버로부터 받을값을 출력
            Log.e("보내줘",result);
        }
    }

}