package com.example.leedaehyung.smartbottle;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.widget.Toast;
import java.io.OutputStreamWriter;

public class SignupActivity extends AppCompatActivity {

    EditText name ;
    EditText pass ;
    EditText repass;
    EditText email ;
    EditText id ;
    Button signup;
    Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


         name = findViewById(R.id.sign_name);
         pass = findViewById(R.id.sign_pass);
         repass= findViewById(R.id.sign_repass);
         email = findViewById(R.id.sign_email);
         id = findViewById(R.id.sign_id);
         signup= findViewById(R.id.sign_signupbutton);
         cancel= findViewById(R.id.cancelbutton);
         signup.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //회원가입 조건 짜기
                if(id.getText().toString().length() !=0 && name.getText().toString().length() !=0 && pass.getText().toString().length() !=0 && email.getText().toString().length() !=0){
                    if(pass.getText().toString().equals(repass.getText().toString())){
                        new JSONTask().execute("http://ec2-52-79-237-177.ap-northeast-2.compute.amazonaws.com.com:65001/register");
                        Toast.makeText(getApplicationContext(), "회원가입 완료.",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                    }
                    else Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다.",Toast.LENGTH_LONG).show();

                }
                else Toast.makeText(getApplicationContext(), "빈칸없이 작성해주세요.",Toast.LENGTH_LONG).show();

            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    public class JSONTask extends AsyncTask<String,String,String>{
        @Override
        protected String doInBackground(String... strings) {
            try{
                //JSONObject 를 만들고 키값 형식으로 저장해준다.

                JSONObject jsonObject=new JSONObject();

                jsonObject.accumulate("user_id",id.getText());
                jsonObject.accumulate("name",name.getText());
                jsonObject.accumulate("pass",pass.getText());
                jsonObject.accumulate("email",email.getText());
                HttpURLConnection con= null;
                BufferedReader reader=null;
                try {
                    URL url = new URL("http://ec2-52-79-237-177.ap-northeast-2.compute.amazonaws.com:65001/register");
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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // 서버로부터 받을값을 출력

        }
    }
}
