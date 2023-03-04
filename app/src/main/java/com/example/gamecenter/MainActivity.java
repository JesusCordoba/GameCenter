package com.example.gamecenter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private EditText txt_user;
    private String user;
    private Button btn_2048;
    private Button btn_LightsOut;
    private Button btn_ranking;
    private Context context;
    private SharedPreferences usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Oculta la barra de accion
        getSupportActionBar().hide();

        // Obtenemos contexto de la aplicacion
        context = getBaseContext();
        usuario = getSharedPreferences("datos",Context.MODE_PRIVATE);

        // Inicializamos el edittext y escribimos el usuario guardado
        txt_user = (EditText) findViewById(R.id.user);
        txt_user.setText(usuario.getString("usuario",""));

        // Comprobamos si el usuairo guardado esta vacio
        if(usuario.getString("usuario","").isEmpty()){
            // Si esta vacio escribimos Guest como usuario y lo guardamos
            txt_user.setText("Guest");
            user = "Guest";
        }else{
            // Si no esta vacio guardamos el nombre de usuario
            user = usuario.getString("usuario","");
        }

        txt_user.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Guardamos el usuario cambiado
                user = txt_user.getText().toString();

                // Comprobamos si el editext esta vacio
                if (user.isEmpty()){ // El edittext esta vacio
                    // Deshabilitamos los botones y escribimos el texto en rojo
                    btn_2048.setEnabled(false);
                    btn_2048.setTextColor(Color.RED);
                    btn_LightsOut.setEnabled(false);
                    btn_LightsOut.setTextColor(Color.RED);
                    btn_ranking.setEnabled(false);
                    btn_ranking.setTextColor(Color.RED);
                }else{ // El edittext NO esta vacio
                    // Se guarda el nuevo nombre de usuario y habilitamos los botones
                    SharedPreferences.Editor editor=usuario.edit();
                    editor.putString("usuario", user);
                    editor.commit();
                    btn_2048.setEnabled(true);
                    btn_2048.setTextColor(Color.WHITE);
                    btn_LightsOut.setEnabled(true);
                    btn_LightsOut.setTextColor(Color.WHITE);
                    btn_ranking.setEnabled(true);
                    btn_ranking.setTextColor(Color.WHITE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btn_2048 = (Button) findViewById(R.id.btn_2048);
        btn_2048.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_2048 = new Intent(context,G_2048.class);
                intent_2048.putExtra("usuario", user);
                startActivity(intent_2048);
            }
        });

        btn_LightsOut = (Button) findViewById(R.id.btn_lightsOut);
        btn_LightsOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_LightsOut = new Intent(context,LightsOut.class);
                intent_LightsOut.putExtra("usuario", user);
                startActivity(intent_LightsOut);
            }
        });

        btn_ranking = (Button) findViewById(R.id.btn_ranking);
        btn_ranking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_Ranking = new Intent(context,Ranking.class);
                startActivity(intent_Ranking);
            }
        });



    }
}