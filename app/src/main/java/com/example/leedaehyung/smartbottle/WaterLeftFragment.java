package com.example.leedaehyung.smartbottle;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.leedaehyung.smartbottle.sessionmanager.SessionManager;

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
import java.util.concurrent.ExecutionException;

import butterknife.ButterKnife;
import me.itangqi.waveloadingview.WaveLoadingView;

public class WaterLeftFragment extends Fragment{
    private SessionManager sm;
    double percent=0;
    double today_amount=0;
    WaveLoadingView waveLoadingView ;
    Button setpurposeButton ;
    EditText editWater ;
    TextView tv_todayamount;
    private TextView purpose_value;
    double getpurpose=0;
    String getfromserver_purpose = "";
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.content_water_left, container, false);
        ButterKnife.bind(this,v);
        waveLoadingView = v.findViewById(R.id.waveLoadingView);
        setpurposeButton = (Button) v.findViewById(R.id.setPurpose);
        editWater = v.findViewById(R.id.weightSet);
        purpose_value = v.findViewById(R.id.purpose_value);
        tv_todayamount=v.findViewById(R.id.today_amount);
        //서버로부터 데이터 받아옴.
        sm = new SessionManager(getContext());
        Log.e("id",sm.getse());
        setpurposeButton.setOnClickListener(new View.OnClickListener() {    //목표량 설정버튼 클릭시 실행 리스너
            @Override
            public void onClick(View v) {
                //목표량 가져오기
                PurposeDialog dialog = PurposeDialog.newInstance(new PurposeDialog.NameInputListener() {
                    @Override
                    public void onNameInputComplete(String purpose) {
                        if(purpose !=  null){
                            //목표량...
                            getpurpose=Double.parseDouble(purpose);
                            purpose_value.setText(purpose);
                            ///서버로 목표량 보내기
                            new PurposeTask().execute("http://ec2-52-79-237-177.ap-northeast-2.compute.amazonaws.com:65001/purpose");
                            try {
                                today_amount = Double.parseDouble(new amountTask().execute("http://ec2-52-79-237-177.ap-northeast-2.compute.amazonaws.com:65001/todayamount").get());
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        refresh();
                    }
                });
                dialog.show(getFragmentManager(),"addDialog");
            }
        });


            try {
                getfromserver_purpose = new getPurposeTask().execute("http://ec2-52-79-237-177.ap-northeast-2.compute.amazonaws.com:65001/getpurpose").get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            Log.e("목표량(서버로부터)", getfromserver_purpose);
            if (getfromserver_purpose != null) {
                try {
                    String amount123 = new amountTask().execute("http://ec2-52-79-237-177.ap-northeast-2.compute.amazonaws.com:65001/todayamount").get();
                    today_amount = Integer.parseInt(amount123);
                    percent = ((today_amount / Double.parseDouble(getfromserver_purpose))) * 100;
                    Log.e("today_amount", String.valueOf(today_amount));
                    Log.e("목표량", getfromserver_purpose);
                    Log.e("percent", String.valueOf(percent));
                    purpose_value.setText(getfromserver_purpose);
                    tv_todayamount.setText(String.valueOf(today_amount));
                    waveLoadingView.setProgressValue((int) percent);
                    if (percent < 50) {
                        waveLoadingView.setBottomTitle(String.format("%d%%", (int) percent));
                        waveLoadingView.setCenterTitle("");
                        waveLoadingView.setTopTitle("");
                    } else if (percent < 80) {
                        waveLoadingView.setBottomTitle("");
                        waveLoadingView.setCenterTitle(String.format("%d%%", (int) percent));
                        waveLoadingView.setTopTitle("");
                    } else {
                        waveLoadingView.setBottomTitle("");
                        waveLoadingView.setCenterTitle("");
                        waveLoadingView.setTopTitle(String.format("%d%%", (int) percent));
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        else waveLoadingView.setProgressValue(0);

        waveLoadingView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                refresh();     //그림 클릭하면 refresh
            }
        });
//        if(savedInstanceState != null){
//            purpose_value.setText(savedInstanceState.getInt("purpose"));
//        }

        return v;   //이거때문에 오류걸렸었음.

    }

//    @Override
//    public void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
//        purpose_value=getView().findViewById(R.id.purpose_value);
//        int a= Integer.parseInt(purpose_value.getText().toString());
//        outState.putInt("purpose",a);
//    }

//    @Override
//    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
//        super.onViewStateRestored(savedInstanceState);
//        if(savedInstanceState != null){
//            purpose_value.setText(savedInstanceState.getString("purpose"));
//        }
//
//    }

    public void openDialog() {
        DialogFragment waterdialog = new DialogFragment();
        waterdialog.show(getFragmentManager(), "water setting dialog");
    }
    //이 메소드를 호출하면 fragment 새로고침;
    private void refresh(){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.detach(this).attach(this).commit();
        Toast.makeText(getActivity(),"페이지 새로고침",Toast.LENGTH_LONG).show();
    }

    //////////////////////오늘 amount를 서버로부터 받기////////////////////////////
    @SuppressLint("StaticFieldLeak")
    public class amountTask extends AsyncTask<String,String,String> {
    @Override
    protected String doInBackground(String... strings) {
        try {
            //JSONObject 를 만들고 키값 형식으로 저장해준다.

            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("id",sm.getse());
            HttpURLConnection con= null;
            BufferedReader reader=null;
            try {
                URL url = new URL("http://ec2-52-79-237-177.ap-northeast-2.compute.amazonaws.com:65001/todayamount");
                con = (HttpURLConnection) url.openConnection();

                con.setRequestMethod("POST");//POST방식으로 보냄
                con.setRequestProperty("Cache-Control", "no-cache");//캐시설정
                con.setRequestProperty("Content-Type", "application/json");//application JSON 형식으로 전송
                con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                con.setDoOutput(true);//OutStream으로 post데이터를 넘겨주곘다.
                con.setDoInput(true);//InputStream으로 서버로부터 응답을 받겠다.
                con.connect();
                //서버로부터 데이터를 받음
                OutputStream outStream = con.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                writer.write(jsonObject.toString());
                writer.flush();
                writer.close();
                con.connect();
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

    @SuppressLint("DefaultLocale")
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

    }

}
//////////////////////마신량을 서버로부터 받기////////////////////////////


///////////////////////////목표량 서버로 보내기////////////////////////////////
    @SuppressLint("StaticFieldLeak")
    public class PurposeTask extends AsyncTask<String,String,Integer> {
        @Override
        protected Integer doInBackground(String... strings) {
            try{
                //JSONObject 를 만들고 키값 형식으로 저장해준다.

                JSONObject jsonObject=new JSONObject();

                jsonObject.accumulate("id",sm.getse());
                jsonObject.accumulate("purpose",getpurpose);
                HttpURLConnection con= null;
                BufferedReader reader=null;
                try {
                    URL url = new URL("http://ec2-52-79-237-177.ap-northeast-2.compute.amazonaws.com:65001/purpose");
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
                    return Integer.parseInt(buffer.toString());   //서버로부터 받은 값을 리턴해줌
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
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            // 서버로부터 받을값을 출력
        }
    }
///////////////////목표량 서버로 보내기//////////////////////////////////////

    //////////////////////오늘 purpose db서버로부터 받기////////////////////////////
    @SuppressLint("StaticFieldLeak")
    public class getPurposeTask extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                //JSONObject 를 만들고 키값 형식으로 저장해준다.

                JSONObject jsonObject = new JSONObject();

                jsonObject.accumulate("id",sm.getse());
                HttpURLConnection con= null;
                BufferedReader reader=null;
                try {
                    URL url = new URL("http://ec2-52-79-237-177.ap-northeast-2.compute.amazonaws.com:65001/getpurpose");
                    con = (HttpURLConnection) url.openConnection();

                    con.setRequestMethod("POST");//POST방식으로 보냄
                    con.setRequestProperty("Cache-Control", "no-cache");//캐시설정
                    con.setRequestProperty("Content-Type", "application/json");//application JSON 형식으로 전송
                    con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                    con.setDoOutput(true);//OutStream으로 post데이터를 넘겨주곘다.
                    con.setDoInput(true);//InputStream으로 서버로부터 응답을 받겠다.
                    con.connect();
                    //서버로부터 데이터를 받음
                    OutputStream outStream = con.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                    writer.write(jsonObject.toString());
                    writer.flush();
                    writer.close();

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

        @SuppressLint("DefaultLocale")
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

        }

    }
//////////////////////purpose 서버로부터 받기////////////////////////////
}
