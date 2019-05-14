package com.example.leedaehyung.smartbottle;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private long time= 0;
    final Fragment fa= new WaterLeftFragment();
    final Fragment fb= new GraphFragment();
    final Fragment fc= new NikeRun();
    Fragment active = fa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
////////////////////////////////////////////////////////////////////////////////////////
        if(savedInstanceState!=null){
            String aaa=savedInstanceState.getString("aaa","");
        }
        /////////////////////////////////////////////////////////////////////////////
        BottomNavigationView bottomNav=findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        fragmentManager=getSupportFragmentManager();

        fragmentManager.beginTransaction().add(R.id.fragment_container,fa,"1").commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container,fc,"3").hide(fc).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container,fb,"2").hide(fb).commit();

    }

    @Override
    public void onBackPressed(){
        if(System.currentTimeMillis()-time>=2000){
            time=System.currentTimeMillis();
            Toast.makeText(getApplicationContext(),"뒤로 버튼을 한번 더 누르면 종료합니다.",Toast.LENGTH_SHORT).show();
        }else if(System.currentTimeMillis()-time<2000){
            finish();
        }
    }
    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch(item.getItemId()){
                case R.id.nav_home:
                    fragmentManager.beginTransaction().hide(active).show(fa).commit();
                    active=fa;
                    return true;

                case R.id.nav_graph:
                    fragmentManager.beginTransaction().hide(active).show(fb).commit();
                    active=fb;
                    return true;

                case R.id.nav_running:
                    fragmentManager.beginTransaction().hide(active).show(fc).commit();
                    active=fc;
                    return true;
            }
            return false;

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
