package com.example.gamecenter;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class LightsOut extends AppCompatActivity implements View.OnClickListener{
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private int moves;
    private TextView txt_moves;
    private TextView txt_best;
    private Button btn_solution;
    private Button newGame;
    private TextView[][] listaBotones;
    private Boolean[][] listaEncendidos;
    private int nfila_aleatorio;
    private int ncolumna_aleatorio;
    private String[][] solution;
    private Boolean showSolution;
    private String user;
    private DataBase db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lights_out);
        getSupportActionBar().hide();

        db = new DataBase(this);
        Intent intent = getIntent();
        user = intent.getStringExtra("usuario");

        listaEncendidos = new Boolean[5][5];
        listaBotones = new TextView[5][5];
        // Fila 1
        listaBotones[0][0] = (TextView) findViewById(R.id.btn1);
        listaBotones[0][1] = (TextView) findViewById(R.id.btn2);
        listaBotones[0][2] = (TextView) findViewById(R.id.btn3);
        listaBotones[0][3] = (TextView) findViewById(R.id.btn4);
        listaBotones[0][4] = (TextView) findViewById(R.id.btn5);
        // Fila 2
        listaBotones[1][0] = (TextView) findViewById(R.id.btn6);
        listaBotones[1][1] = (TextView) findViewById(R.id.btn7);
        listaBotones[1][2] = (TextView) findViewById(R.id.btn8);
        listaBotones[1][3] = (TextView) findViewById(R.id.btn9);
        listaBotones[1][4] = (TextView) findViewById(R.id.btn10);
        // Fila 3
        listaBotones[2][0] = (TextView) findViewById(R.id.btn11);
        listaBotones[2][1] = (TextView) findViewById(R.id.btn12);
        listaBotones[2][2] = (TextView) findViewById(R.id.btn13);
        listaBotones[2][3] = (TextView) findViewById(R.id.btn14);
        listaBotones[2][4] = (TextView) findViewById(R.id.btn15);
        // Fila 4
        listaBotones[3][0] = (TextView) findViewById(R.id.btn16);
        listaBotones[3][1] = (TextView) findViewById(R.id.btn17);
        listaBotones[3][2] = (TextView) findViewById(R.id.btn18);
        listaBotones[3][3] = (TextView) findViewById(R.id.btn19);
        listaBotones[3][4] = (TextView) findViewById(R.id.btn20);
        // Fila 5
        listaBotones[4][0] = (TextView) findViewById(R.id.btn21);
        listaBotones[4][1] = (TextView) findViewById(R.id.btn22);
        listaBotones[4][2] = (TextView) findViewById(R.id.btn23);
        listaBotones[4][3] = (TextView) findViewById(R.id.btn24);
        listaBotones[4][4] = (TextView) findViewById(R.id.btn25);


        showSolution = false;
        solution = new String[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                listaBotones[i][j].setOnClickListener(this);
                listaEncendidos[i][j] = false;
                solution[i][j] = "";
            }
        }

        txt_moves = (TextView) findViewById(R.id.txt_moves);
        txt_best = (TextView) findViewById(R.id.txt_best);
        txt_best.setText("BEST \n "+ db.getBestScore(user,"Light Out"));

        // Encender 5 botones al iniciar la aplicacion
        encenderInicio();

        btn_solution = (Button) findViewById(R.id.btn_solution);
        btn_solution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(showSolution == false){
                    //Añadir penalizacion por usar la solucion
                    moves = moves + 20;
                    txt_moves.setText("Moves \n "+ moves);
                    // Notificar al usuario de la penalizacion
                    Toast.makeText(
                            getApplicationContext(),
                            "PENALTY MOVES +20",
                            Toast.LENGTH_SHORT).show();
                    // Marcar la solucion en los textviews
                    for (int fila = 0; fila < 5; fila++) {
                        for (int columna = 0; columna < 5; columna++) {
                            if(solution[fila][columna].equals("S")){
                                listaBotones[fila][columna].setText("S");
                            }
                        }
                    }
                    showSolution = true;
                }else{
                    // Borrar la solucion de los textviews
                    for (int fila = 0; fila < 5; fila++) {
                        for (int columna = 0; columna < 5; columna++) {
                            listaBotones[fila][columna].setText("");
                        }
                    }
                    showSolution = false;
                }


            }
        });

        newGame = (Button) findViewById(R.id.new_game);
        newGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newGame();
            }
        });
    }

    public void encender(TextView[][] btn, Boolean[][] encender, int fila, int columna){
        moves++;
        txt_moves.setText("Moves \n "+ moves);
        // Compobar si el boton esta encendido
        if(encender[fila][columna]){
            // Apagar boton y apuntarlo en la lista de booleans
            encender[fila][columna] = false;
            btn[fila][columna].setBackgroundResource(R.drawable.boton_redondo);
            // Enciende o apaga los botones de los alrededores
            encenderAdyacentes(btn, encender, fila, columna);
            Log.d(LOG_TAG, encender[fila][columna].toString()+" "+btn[fila][columna].getId());

        } // Comprobar si el boton esta apagado
        else if(!encender[fila][columna]){
            // Encender boton y apuntarlo en la lista de booleans
            encender[fila][columna] = true;
            btn[fila][columna].setBackgroundResource(R.drawable.boton_presionado);
            // Enciende o apaga los botones de los alrededores
            encenderAdyacentes(btn, encender, fila, columna);
            Log.d(LOG_TAG, encender[fila][columna].toString()+" "+btn[fila][columna].getId());

        }

        // Actualizar la solucion
        if (showSolution == true){
            if (solution[fila][columna].equals("S")){
                solution[fila][columna] = "";
                listaBotones[fila][columna].setText("");
            }else{
                solution[fila][columna] = "S";
                listaBotones[fila][columna].setText("S");
            }
        }else{
            if (solution[fila][columna].equals("S")){
                solution[fila][columna] = "";
            }else{
                solution[fila][columna] = "S";
            }
        }

        comprobarFin();

    }

    public void encenderAdyacentes(TextView[][] btn, Boolean[][] encender, int fila, int columna){
        // Encender o apagar el boton de abajo
        int filaAbajo = fila+1;
        if(filaAbajo <= 4){ // Comprobar que la fila de abajo no sea mayor de 4
            // Comprobar si esta encendido y apagar el boton
            if(encender[filaAbajo][columna]){
                encender[filaAbajo][columna] = false;
                btn[filaAbajo][columna].setBackgroundResource(R.drawable.boton_redondo);
            } // Comprobar si esta apagado y encender el boton
            else if(!encender[filaAbajo][columna]){
                encender[filaAbajo][columna] = true;
                btn[filaAbajo][columna].setBackgroundResource(R.drawable.boton_presionado);
            }
        }

        // Encender o apagar el boton de arriba
        int filaArriba = fila-1;
        if(filaArriba >= 0){ // Comprobar que la fila de abajo no sea menor de 0
            // Comprobar si esta encendido y apagar el boton
            if(encender[filaArriba][columna]){
                encender[filaArriba][columna] = false;
                btn[filaArriba][columna].setBackgroundResource(R.drawable.boton_redondo);
            }// Comprobar si esta apagado y encender el boton
            else if(!encender[filaArriba][columna]){
                encender[filaArriba][columna] = true;
                btn[filaArriba][columna].setBackgroundResource(R.drawable.boton_presionado);
            }
        }

        // Encender o apagar el boton de la derecha
        int columnaDerecha = columna+1;
        if(columnaDerecha <= 4){// Comprobar que la columna derecha no sea mayor de 4
            // Comprobar si esta encendido y apagar el boton
            if(encender[fila][columnaDerecha]){
                encender[fila][columnaDerecha] = false;
                btn[fila][columnaDerecha].setBackgroundResource(R.drawable.boton_redondo);
            }// Comprobar si esta apagado y encender el boton
            else if(!encender[fila][columnaDerecha]){
                encender[fila][columnaDerecha] = true;
                btn[fila][columnaDerecha].setBackgroundResource(R.drawable.boton_presionado);
            }
        }

        // Encender o apagar el boton de la izquierda
        int columnaIzquierda = columna-1;
        if(columnaIzquierda >= 0){// Comprobar que la columna izquierda no sea mayor de 4
            // Comprobar si esta encendido y apagar el boton
            if(encender[fila][columnaIzquierda]){
                encender[fila][columnaIzquierda] = false;
                btn[fila][columnaIzquierda].setBackgroundResource(R.drawable.boton_redondo);
            }// Comprobar si esta apagado y encender el boton
            else if(!encender[fila][columnaIzquierda]){
                encender[fila][columnaIzquierda] = true;
                btn[fila][columnaIzquierda].setBackgroundResource(R.drawable.boton_presionado);
            }
        }

    }

    public void encenderInicio(){
        moves = 0;
        int cont = 0;
        while (cont < 5){
            // Generar posicion aleatoria para encender los botones
            nfila_aleatorio = (int) (Math.random() * (4 + 1 - 0)) + 0;
            ncolumna_aleatorio = (int) (Math.random() * (4 + 1 - 0)) + 0;
            // Encender el boton si este no esta encendido
            if (!listaEncendidos[nfila_aleatorio][ncolumna_aleatorio]){
                Log.d(LOG_TAG, " Fila: "+nfila_aleatorio+" Columna: "+ncolumna_aleatorio);
                // Encender el boton
                encender(listaBotones, listaEncendidos, nfila_aleatorio, ncolumna_aleatorio);
                // Sumar al contador tras haber encendido un boton
                cont++;
            }

        }
        moves = 0;
        txt_moves.setText("Moves \n "+ moves);
    }

    public void comprobarFin(){
        boolean fin = true;
        for (int fila = 0; fila < 5; fila++) {
            for (int columna = 0; columna < 5; columna++) {
                Log.d(LOG_TAG, "Boton: " + fila + ", " + columna + " : " + listaEncendidos[fila][columna]);
                if(listaEncendidos[fila][columna]){
                    fin = false;
                    break;
                }
            }
            if(!fin){
                break;
            }
        }

        if (fin){
            db.addScore(user,"Light Out", moves);
            dialogo_fin();
            newGame();
        }
    }

    public void dialogo_fin(){
        ArrayList<Score> score_list = db.getScores("Light Out");
        String ranking = "";

        for (int i = 0; i < score_list.size(); i++) {
            if (i==10){
                break;
            }
            ranking += (i+1) + "- " + score_list.get(i).getPlayer() + " " + score_list.get(i).getScore() +"\n";

        }

        txt_best.setText("BEST \n "+ db.getBestScore(user,"Light Out"));
        Dialogo dialogo = new Dialogo(moves,ranking);
        dialogo.show(getSupportFragmentManager(),"Dialogo");
    }

    public void newGame(){
        for (int fila = 0; fila < 5; fila++) {
            for (int columna = 0; columna < 5; columna++) {
                listaEncendidos[fila][columna] = false;
                solution[fila][columna] = "";
                listaBotones[fila][columna].setText("");
                listaBotones[fila][columna].setBackgroundResource(R.drawable.boton_redondo);
            }
        }
        showSolution = false;
        encenderInicio();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn1:
                encender(listaBotones, listaEncendidos, 0, 0);
                break;

            case R.id.btn2:
                encender(listaBotones, listaEncendidos, 0,1);
                break;

            case R.id.btn3:
                encender(listaBotones, listaEncendidos, 0,2);
                break;

            case R.id.btn4:
                encender(listaBotones, listaEncendidos, 0,3);
                break;

            case R.id.btn5:
                encender(listaBotones, listaEncendidos, 0,4);
                break;

            case R.id.btn6:
                encender(listaBotones, listaEncendidos, 1, 0);
                break;

            case R.id.btn7:
                encender(listaBotones, listaEncendidos, 1, 1);
                break;

            case R.id.btn8:
                encender(listaBotones, listaEncendidos, 1, 2);
                break;

            case R.id.btn9:
                encender(listaBotones, listaEncendidos, 1, 3);
                break;

            case R.id.btn10:
                encender(listaBotones, listaEncendidos, 1, 4);
                break;

            case R.id.btn11:
                encender(listaBotones, listaEncendidos, 2, 0);
                break;

            case R.id.btn12:
                encender(listaBotones, listaEncendidos, 2, 1);
                break;

            case R.id.btn13:
                encender(listaBotones, listaEncendidos, 2, 2);
                break;

            case R.id.btn14:
                encender(listaBotones, listaEncendidos, 2, 3);
                break;

            case R.id.btn15:
                encender(listaBotones, listaEncendidos, 2, 4);
                break;

            case R.id.btn16:
                encender(listaBotones, listaEncendidos, 3, 0);
                break;

            case R.id.btn17:
                encender(listaBotones, listaEncendidos, 3, 1);
                break;

            case R.id.btn18:
                encender(listaBotones, listaEncendidos, 3, 2);
                break;

            case R.id.btn19:
                encender(listaBotones, listaEncendidos, 3, 3);
                break;

            case R.id.btn20:
                encender(listaBotones, listaEncendidos, 3, 4);
                break;

            case R.id.btn21:
                encender(listaBotones, listaEncendidos, 4, 0);
                break;

            case R.id.btn22:
                encender(listaBotones, listaEncendidos, 4, 1);
                break;

            case R.id.btn23:
                encender(listaBotones, listaEncendidos, 4, 2);
                break;

            case R.id.btn24:
                encender(listaBotones, listaEncendidos, 4, 3);
                break;

            case R.id.btn25:
                encender(listaBotones, listaEncendidos, 4, 4);
                break;

            default:
                break;
        }
    }

}