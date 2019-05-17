package com.example.leedaehyung.smartbottle;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

public class LoginActivity extends AppCompatActivity {

    private long time= 0;
    Button login;
    Button signup;
    EditText id;
    EditText pass;
    private SessionManager sessionManager;


    @Override
    public void onBackPressed(){
        if(System.currentTimeMillis()-time>=2000){
            time=System.currentTimeMillis();
            Toast.makeText(getApplicationContext(),"뒤로 버튼을 한번 더 누르면 종료합니다.",Toast.LENGTH_SHORT).show();
        }else if(System.currentTimeMillis()-time<2000){
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

         sessionManager = new SessionManager(getApplicationContext());
         login= findViewById(R.id.login);
         signup = findViewById(R.id.signupbutton);
         id= findViewById(R.id.login_id);
         pass=findViewById(R.id.login_pass);
        login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //로그인 정보가 맞는지 서버에 정보 보내기..
                if(id.getText().toString().length() !=0 && pass.getText().toString().length() !=0)
                    new JSONTask().execute("http://ec2-52-79-237-177.ap-northeast-2.compute.amazonaws.com:65001/login");
                else Toast.makeText(getApplicationContext(), "아이디 또는 패스워드를 입력해주세요.",Toast.LENGTH_LONG).show();
            }
            });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),SignupActivity.class);
                startActivity(intent);
            }
        });

    }


    public class JSONTask extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... strings) {
            try{
                //JSONObject 를 만들고 키값 형식으로 저장해준다.

                JSONObject jsonObject=new JSONObject();

                jsonObject.accumulate("user_id",id.getText());
                jsonObject.accumulate("pass",pass.getText());
                HttpURLConnection con= null;
                BufferedReader reader=null;
                try {
                    URL url = new URL("http://ec2-52-79-237-177.ap-northeast-2.compute.amazonaws.com:65001/login");
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

            if(result.equals("OK")){
                //로그인 성공시
                sessionManager.createSession(id.getText().toString());

                Intent intent2 = new Intent(getApplicationContext(),MainActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent2);
                finish();
            }
            if(result.equals("NO")) {
                Toast.makeText(getApplicationContext(), "아이디 또는 비밀번호를 확인해주세요.",Toast.LENGTH_LONG).show();
            }

        }


    }


}
