package com.example.gamecenter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;

public class Ranking extends AppCompatActivity {

    private TextView r_2048;
    private TextView r_lightsOut;
    private DataBase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);
        getSupportActionBar().hide();

        db = new DataBase(this);

        r_2048 = (TextView) findViewById(R.id.ranking_2048);
        r_2048.setText(crearRanking("2048"));

        r_lightsOut = (TextView) findViewById(R.id.ranking_lightsOut);
        r_lightsOut.setText(crearRanking("Light Out"));

    }

    public String crearRanking(String game){
        ArrayList<Score> score_list = db.getScores(game);
        String ranking = "\n";

        for (int i = 0; i < score_list.size(); i++) {
            if (i==10){
                break;
            }
            ranking += (i+1) + " - " + score_list.get(i).getPlayer() + ": " + score_list.get(i).getScore() +"\n";

        }
        return ranking;
    }

}