package com.engtoolsdev.popmov;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.engtoolsdev.popmov.models.Movie;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    MainActivityFragment movieFragment;
    TextView titleView;
    SwitchCompat faveSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);

        titleView = (TextView) toolbar.findViewById(R.id.main_toolbar_title);
        faveSwitch = (SwitchCompat) findViewById(R.id.fave_switch);

        movieFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        setSupportActionBar(toolbar);
    }

    public void loadMovie(Movie movie){
        if(movieFragment != null){
            movieFragment.loadMovie(movie);
        }
    }

}
