package com.example.leedaehyung.smartbottle;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.leedaehyung.smartbottle.sessionmanager.SessionManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.json.JSONArray;
import org.json.JSONException;
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
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

import butterknife.ButterKnife;


/**
 * Created by Lee DaeHyung on 2019-03-20.
 */

public class GraphFragment extends Fragment implements OnChartValueSelectedListener {

    private View rootView = null;
    private SessionManager sm;
    Integer[] daySum = new Integer[7];
    TextView tv_date;
    TextView tv_chartMessage;
    TextView tv_total;
    TextView tv_average;
    BarChart barChart;
    String jsonresult;
    int dateCount = -DateUtils.getDateDay(DateUtils.getFarDay(0), "yyyy-MM-dd");
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.content_graph, container, false);
        ButterKnife.bind(this,rootView);
        sm= new SessionManager(getContext());
        this.rootView = rootView;
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ////////////////////////////서버로부터 마신량 받아오기/////////////////////////////
        try {
            Toast.makeText(getContext(), "서버랑 연동", Toast.LENGTH_LONG).show();
            jsonresult = new DateTask().execute("http://ec2-52-79-237-177.ap-northeast-2.compute.amazonaws.com:65001/day_drank").get();
            if (jsonresult!= null) {
                Log.e("json값",jsonresult);
            } else Toast.makeText(getContext(), "데이터 없음", Toast.LENGTH_LONG).show();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ////////////////////////////서버로부터 마신량 받아오기/////////////////////////////

        //initializeChart(DateUtils.getDateDay(DateUtils.getFarDay(0), "yyyy-MM-dd"));
        initializeChart(DateUtils.getDateDay(DateUtils.getFarDay(0), "yyyy-MM-dd")+1);
        String date1= String.valueOf(DateUtils.getDateDay(DateUtils.getFarDay(0)+1, "yyyy-MM-dd"));
        Log.e("날짜값",date1);
        ImageButton nextWeek = rootView.findViewById(R.id.chart_ibtn_next);
        ImageButton lastWeek = rootView.findViewById(R.id.chart_ibtn_back);

        nextWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateMoveNextButton();
            }
        });
        lastWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateMoveBackButton();
            }
        });
    }

    private void DateMoveBackButton() {
        DateUtils DayNum = new DateUtils();
        String farday = DayNum.getFarDay(0);
        String dateFormat = "yyyy-MM-dd";
        DayNum.getDateDay(farday, dateFormat);
        dateCount -= 7;
        Log.e("보낸날짜", DateUtils.getFarDay(dateCount));
        ////////////////////서버로부터 마신량 받아오기/////////////////////////////
        try {
            Toast.makeText(getContext(), "서버랑 연동", Toast.LENGTH_SHORT).show();
            jsonresult = new DateTask().execute("http://ec2-52-79-237-177.ap-northeast-2.compute.amazonaws.com:65001/day_drank").get();
            if (jsonresult!= null) {
                Log.e("json값",jsonresult);
            } else Toast.makeText(getContext(), "데이터 없음", Toast.LENGTH_SHORT).show();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ////////////////////////////서버로부터 마신량 받아오기/////////////////////////////
        initializeChart(7);
    }

    private void DateMoveNextButton() {
        int DayNum = new DateUtils().getDateDay(DateUtils.getFarDay(0), "yyyy-MM-dd");
        if (dateCount == -DayNum) {
            Toast.makeText(getActivity(), "다음주의 기록은 볼 수 없습니다.", Toast.LENGTH_SHORT).show();
        } else {
            dateCount += 7;
            ////////////////////서버로부터 마신량 받아오기/////////////////////////////
            try {
                new DateTask().execute("http://ec2-52-79-237-177.ap-northeast-2.compute.amazonaws.com:65001/day_drank");
                Toast.makeText(getContext(), "서버랑 연동", Toast.LENGTH_LONG).show();
                jsonresult = new DateTask().execute("http://ec2-52-79-237-177.ap-northeast-2.compute.amazonaws.com:65001/day_drank").get();
                if (jsonresult!= null) {
                    Log.e("json값",jsonresult);
                } else Toast.makeText(getContext(), "데이터 없음", Toast.LENGTH_LONG).show();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ////////////////////////////서버로부터 마신량 받아오기/////////////////////////////
            if (dateCount == -DayNum)
                initializeChart(DayNum);
            else
                initializeChart(7);
        }
        initializeChart(7);
    }

    ////////////////////////////////initializeChart///////////////////////////////////////////////////////
    @SuppressLint("SetTextI18n")
    private void initializeChart(int dayCount) {
        float TotalAmount = 0f;
        float Max = 0f;
        float sumCount = 0f;
        ArrayList<BarEntry> entries = new ArrayList<>();
        barChart = rootView.findViewById(R.id.bargraph);


        ///////////////////////////그래프에 값 추가하기///////////////////////////////


        String drankamount = null;

        if (jsonresult != null) {
            StringTokenizer st = new StringTokenizer(jsonresult, "/");
            Integer[] entry_element = {0,0,0,0,0,0,0};
            int size=0;
            size= st.countTokens();
            for (int i = 0; i < size ; i++) {

                    entry_element[i] = Integer.parseInt(st.nextToken());
                    Log.e("일일량", String.valueOf(entry_element[i]));
                    Log.e("사이즈값", String.valueOf(size));

            }
            for (int i = 0; i < dayCount; i++) {
                //이 부분을 val daySum = sqliteManager!!.getDayDrinkAmount(DateUtils.getFarDay(dateCount + i))
                //daySum=22;
                if(entry_element[i]!=null) {
                    entries.add((BarEntry) new BarEntry(entry_element[i], i));
                }else entries.add((BarEntry) new BarEntry(0,i));
            }
        }


        ///////////////////////////그래프에 값 추가하기///////////////////////////////


        TextView tv_date = rootView.findViewById(R.id.chart_tv_weekdate);
        TextView tv_chartMessage = rootView.findViewById(R.id.chart_tv_message);
        tv_date.setText(DateUtils.getFarDay(dateCount) + " ~ " + DateUtils.getFarDay(dateCount + 6));
//      TextView tv_total= getView().findViewById(R.id.chart_tv_totaldrink);
//      tv_total.setText(String.format("%.1f",TotalAmount/1000f/sumCount)+"L");
        TextView tv_average = getView().findViewById(R.id.chart_tv_averagedrink);
//        if(sumCount!=0f)
//            tv_average.setText(String.format("%.1f",TotalAmount/1000f/sumCount)+"L");
//        else
        try {
            if (new AverageTask().execute("http://ec2-52-79-237-177.ap-northeast-2.compute.amazonaws.com:65001/week_drank").get() != null) {
                String average = new AverageTask().execute("http://ec2-52-79-237-177.ap-northeast-2.compute.amazonaws.com:65001/week_drank").get();
                Toast.makeText(getActivity(), average, Toast.LENGTH_LONG).show();
                tv_average.setText(average + "mL");
            } else Toast.makeText(getActivity(), "평균 데이터 안와쪄요", Toast.LENGTH_SHORT).show();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ArrayList<String> labels = new ArrayList<>();
        labels.add("일");
        labels.add("월");
        labels.add("화");
        labels.add("수");
        labels.add("목");
        labels.add("금");
        labels.add("토");

        BarDataSet dataset = new BarDataSet(entries, "마신 물의 양");
        BarData data = new BarData(labels, dataset);
        barChart.setData(data);
        barChart.setOnChartValueSelectedListener(this);

        barChart.setDescription("");
        barChart.setDescriptionTextSize(16f);
        barChart.setDescriptionColor(Color.BLACK);
        barChart.getLegend().setEnabled(true);
        barChart.getLegend().setTextColor(Color.TRANSPARENT);
        barChart.getLegend().setCustom(new int[]{Color.TRANSPARENT}, new String[]{""});

        barChart.setDrawGridBackground(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setDrawGridLines(false);
        barChart.getAxisLeft().setTextColor(Color.TRANSPARENT);
        barChart.getXAxis().setDrawGridLines(false);

        barChart.setPinchZoom(false);
        barChart.setDragEnabled(false);
        barChart.setScaleEnabled(false);
        barChart.animateY(1700);

        barChart.getXAxis().setTextSize(14f);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setTextColor(ColorTemplate.rgb("#000000"));

        barChart.getAxisLeft().setTextColor(ColorTemplate.rgb("#000000"));
        barChart.getAxisLeft().setTextSize(14f);
        barChart.getAxisLeft().setStartAtZero(true);
        barChart.getAxisLeft().setSpaceTop(45f);

        if (TotalAmount > 0)
            tv_chartMessage.setVisibility(View.INVISIBLE);
        else
            tv_chartMessage.setVisibility(View.VISIBLE);

        dataset.setDrawValues(true);
        dataset.setValueTextSize(13f);
        dataset.setValueTextColor(Color.BLACK);
        dataset.setValueFormatter(new DataSetValueFormatter());


    }


    private class DataSetValueFormatter implements ValueFormatter {
        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return String.format(Math.round(value) + "");
        }

    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        DateUtils dName = new DateUtils();
        TextView tv_selected_anyday = rootView.findViewById(R.id.selected_anyday);
        TextView tvselectedday = rootView.findViewById(R.id.chart_tv_selectday);
//        Toast.makeText(getActivity(), e.getXIndex(),Toast.LENGTH_SHORT).show();
        int position = e.getXIndex();
        final String x = barChart.getXAxis().getValues().get(position);
        tv_selected_anyday.setText(x);
        tvselectedday.setText(String.format("%.0f", e.getVal()) + "ml");
    }

    @Override
    public void onNothingSelected() {

    }



    //////////////////////평균 마신량을 서버로부터 받기////////////////////////////
    @SuppressLint("StaticFieldLeak")
    public class AverageTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                //JSONObject 를 만들고 키값 형식으로 저장해준다.

                JSONObject jsonObject = new JSONObject();

                jsonObject.accumulate("id",sm.getse());
                jsonObject.accumulate("date", DateUtils.getFarDay(dateCount));
                HttpURLConnection con = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL("http://ec2-52-79-237-177.ap-northeast-2.compute.amazonaws.com:65001/week_drank");
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

        @SuppressLint("DefaultLocale")
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // 서버로부터 받을값을 출력
            //daySum= Integer.parseInt(result);
        }

    }

    //////////////////////평균 마신량을 서버로부터 받기////////////////////////////
//////////////////////날짜 보내고 마신량 받아버리기///////////////////////////
    @SuppressLint("StaticFieldLeak")
    public class DateTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                //JSONObject 를 만들고 키값 형식으로 저장해준다.

                JSONObject jsonObject = new JSONObject();

                jsonObject.accumulate("id",sm.getse());
                jsonObject.accumulate("date", DateUtils.getFarDay(dateCount));
                HttpURLConnection con = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL("http://ec2-52-79-237-177.ap-northeast-2.compute.amazonaws.com:65001/day_drank");
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
                    int i=0;
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



        }
    }
    //////////////////////날짜 보내고 마신량 받아버리기///////////////////////////

}

