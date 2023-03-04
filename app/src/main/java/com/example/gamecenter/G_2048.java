package com.example.gamecenter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class G_2048 extends AppCompatActivity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{//activity_g2048

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private GestureDetector gestureDetector;
    // Tiempo que se mantiene el gesto
    private static final int SWIPE_THRESHOLD = 100;
    // Velocidad del gesto
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    private TextView[][] listaCasillas;
    private TextView[][] listaCasillasAnterior;
    private Button newGame;
    private Button btn_anterior;
    private TextView score;
    private TextView best_score;
    private int valor_score;
    private int valor_score_anterior;
    // Fila y columna de numero generado aleatoriamente en el tablero
    private int nfila_aleatorio;
    private int ncolumna_aleatorio;
    // Boolean para comprobar si un numero se ha movido en el tablero
    private boolean mover = false;
    private DataBase db;
    private String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_g2048);
        getSupportActionBar().hide();

        db = new DataBase(this);
        this.gestureDetector = new GestureDetector(this, this);
        gestureDetector.setOnDoubleTapListener(this);

        // Recibir nombre de usuario del menu
        user = "Guest";
        Intent intent = getIntent();
        user = intent.getStringExtra("usuario");

        // Ponemos el valor de la puntuacion a 0
        score = (TextView) findViewById(R.id.score);
        valor_score = 0;
        valor_score_anterior = 0;
        best_score = (TextView) findViewById(R.id.best_score);
        best_score.setText("BEST \n "+ db.getBestScore(user,"2048"));

        //Inicializar lista de casillas anterior
        listaCasillasAnterior = new TextView[4][4];
        for (int fila = 0; fila < 4; fila++) {
            for (int columna = 0; columna < 4; columna++) {
                listaCasillasAnterior[fila][columna]= new TextView(this);
            }
        }

        listaCasillas = new TextView[4][4];
        // Fila 1
        listaCasillas[0][0] = (TextView) findViewById(R.id.txt0);
        listaCasillas[0][1] = (TextView) findViewById(R.id.txt1);
        listaCasillas[0][2] = (TextView) findViewById(R.id.txt2);
        listaCasillas[0][3] = (TextView) findViewById(R.id.txt3);
        // Fila 2
        listaCasillas[1][0] = (TextView) findViewById(R.id.txt4);
        listaCasillas[1][1] = (TextView) findViewById(R.id.txt5);
        listaCasillas[1][2] = (TextView) findViewById(R.id.txt6);
        listaCasillas[1][3] = (TextView) findViewById(R.id.txt7);
        // Fila 3
        listaCasillas[2][0] = (TextView) findViewById(R.id.txt8);
        listaCasillas[2][1] = (TextView) findViewById(R.id.txt9);
        listaCasillas[2][2] = (TextView) findViewById(R.id.txt10);
        listaCasillas[2][3] = (TextView) findViewById(R.id.txt11);
        // Fila 4
        listaCasillas[3][0] = (TextView) findViewById(R.id.txt12);
        listaCasillas[3][1] = (TextView) findViewById(R.id.txt13);
        listaCasillas[3][2] = (TextView) findViewById(R.id.txt14);
        listaCasillas[3][3] = (TextView) findViewById(R.id.txt15);

        // Generar un numero en la cuadricula
        generarNumero();

        // Resetear el juego para volver a empezar
        newGame = (Button) findViewById(R.id.new_game);
        newGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Borrar y generar un nuevo juego
                newGame();
                // Bloquear el boton de posicion anterior
                btn_anterior.setEnabled(false);
                // Pintar tablero
                for (int fila = 0; fila < 4; fila++) {
                    for (int columna = 0; columna < 4; columna++) {
                        pintar(listaCasillas[fila][columna]);
                    }
                }
            }

        });

        // Poner los textview en la posicion que estaban antes de la ultima interaccion
        btn_anterior = (Button) findViewById(R.id.anterior);
        btn_anterior.setEnabled(false);// Bloquear boton por defecto
        btn_anterior.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Cambiar la puntuacion a la puntacion anterior
                valor_score = valor_score_anterior;
                score.setText("SCORE \n " + valor_score_anterior);

                // Colocar en el tablero los textview con su posicion anterior y pintar el tablero
                for (int fila = 0; fila < 4; fila++) {
                    for (int columna = 0; columna < 4; columna++) {
                        listaCasillas[fila][columna].setText(listaCasillasAnterior[fila][columna].getText().toString());
                        pintar(listaCasillas[fila][columna]);
                    }
                }
                //Bloquear el boton anterior
                btn_anterior.setEnabled(false);
            }
        });

        // Pintar el tablero con los numero generados al inicio
        for (int fila = 0; fila < 4; fila++) {
            for (int columna = 0; columna < 4; columna++) {
                pintar(listaCasillas[fila][columna]);
            }
        }

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        boolean result = false;

        // Guardar la posicion de los textview antes de que cambien
        gCasillasAnteriores();
        // Desbloquear boton anterior
        btn_anterior.setEnabled(true);

        try {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
            // Si la cordenada x es mayor a la y el deslizamiento ha sido horizontal
            if (Math.abs(diffX) > Math.abs(diffY)) {
                // El deslizamiento debe cumplir unas condiciones de distancia y tiempo
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    // Deslizar a la derecha
                    if (diffX > 0) {
                        moverDerecha();

                    // Deslizar a la izquierda
                    } else {
                        moverIzquierda();
                    }
                }
                result = true;
            }// el deslizamiento vertical tambien debe cumplir unas condiciones de distancia y tiempo
            else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                // Deslizar abajo
                if (diffY > 0) {
                    moverAbajo();

                // Deslizar arriba
                } else {
                    moverArriba();
                }
            }
            // Generar un nuevo numero, si se ha movido un numero
            if (mover) {
                generarNumero();
                mover = false;
            }

            // Comprobar si el juego ha finalizado
            comprobarFin();

            result = true;

        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return result;
    }

    public void mostrar_toast(String texto) {
        Context context = getApplicationContext();
        CharSequence text = texto;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public void moverDerecha(){
        Log.d(LOG_TAG, "--------Derecha--------");
        // Comprobar si el numero se puede mover desde la primera fila
        for (int iFila = 0; iFila < 4; iFila++) {
            Log.d(LOG_TAG, "Fila: " + iFila);
            // Comprobar si el numero se puede mover desde la ultima columna
            for (int iColumna = 3; iColumna >= 0; iColumna--) {
                Log.d(LOG_TAG, "Fila: " + iFila + " Columna: " + iColumna);
                // Comprobar si hay numero en la casilla derecha
                if (!listaCasillas[iFila][iColumna].getText().toString().isEmpty()) {
                    moverNumDerecha(iFila, iColumna);
                }
            }
        }
        Log.d(LOG_TAG, "----------------------");
    }

    public void moverIzquierda(){
        Log.d(LOG_TAG, "--------Izquierda--------");
        // Comprobar si el numero se puede mover desde la primera fila
        for (int iFila = 0; iFila < 4; iFila++) {
            Log.d(LOG_TAG, "Fila: " + iFila);
            // Comprobar si el numero se puede mover desde la primera columna
            for (int iColumna = 0; iColumna < 4; iColumna++) {
                Log.d(LOG_TAG, "Columna: " + iColumna);
                // Comprobar si hay numero en la casilla derecha
                if (!listaCasillas[iFila][iColumna].getText().toString().isEmpty()) {
                    moverNumIzquierda(iFila, iColumna);
                }
            }
        }
        Log.d(LOG_TAG, "----------------------");
    }

    public void moverAbajo(){
        Log.d(LOG_TAG, "--------Abajo--------");
        // Comprobar si el numero se puede mover desde la ultima fila
        for (int iFila = 3; iFila >= 0; iFila--) {
            Log.d(LOG_TAG, "Fila: " + iFila);
            // Comprobar si el numero se puede mover desde la primera columna
            for (int iColumna = 0; iColumna < 4; iColumna++) {
                Log.d(LOG_TAG, "Columna: " + iColumna);
                // Comprobar si hay numero en la casilla derecha
                if (!listaCasillas[iFila][iColumna].getText().toString().isEmpty()) {
                    moverNumAbajo(iFila, iColumna);
                }

            }
        }
        Log.d(LOG_TAG, "----------------------");
    }

    public void moverArriba(){
        Log.d(LOG_TAG, "--------Arriba--------");
        // Comprobar si el numero se puede mover desde la primera fila
        for (int iFila = 0; iFila < 4; iFila++) {
            Log.d(LOG_TAG, "Fila: " + iFila);
            // Comprobar si el numero se puede mover desde la primera columna
            for (int iColumna = 0; iColumna < 4; iColumna++) {
                Log.d(LOG_TAG, "Columna: " + iColumna);
                // Comprobar si hay numero en la casilla derecha
                if (!listaCasillas[iFila][iColumna].getText().toString().isEmpty()) {
                    moverNumArriba(iFila, iColumna);
                }
            }
        }
        Log.d(LOG_TAG, "----------------------");
    }

    public void moverNumDerecha(int nfila, int ncolumna) {
        int ncolumna_original = ncolumna;
        int columna_anterior = ncolumna-1;
        String valor_original = (String) listaCasillas[nfila][ncolumna_original].getText();

        // Comprobar si se puede sumar
        if (ncolumna > 0){
            while (columna_anterior>=0){
                // Comprobar si el textview anterior al textview esta vacio
                if (!listaCasillas[nfila][columna_anterior].getText().toString().isEmpty()){
                    String valor_anterior = (String) listaCasillas[nfila][columna_anterior].getText();
                    // Comprobar si el textview anterior es igual al original
                    if (Integer.parseInt(valor_original) == Integer.parseInt(valor_anterior)) {
                        // Sumar los dos valores de los textview
                        int suma = Integer.parseInt(valor_original) + Integer.parseInt(valor_anterior);
                        Log.d(LOG_TAG, "SUMA: " + suma);
                        // Crear animacion que pasa el textview anterior a la posicion del textview original
                        animacion(listaCasillas[nfila][columna_anterior], listaCasillas[nfila][ncolumna_original],"X");
                        // Vaciar el textview anterior
                        listaCasillas[nfila][columna_anterior].setText("");
                        //Poner en el textview original el valor de la suma de los dos textview
                        listaCasillas[nfila][ncolumna_original].setText(suma + "");
                        valor_original = suma+"";
                        // Sumar la puntuacion
                        sumar_score(suma);
                        // Poner que se ha movido un textview
                        mover = true;
                        break;
                    }else{
                        break;
                    }
                }
                columna_anterior--;
            }
        }

        // Comprobar si se puede mover el numero
        while (ncolumna < 3) {
            ncolumna++;
            // Si estamos en la ultima columna y esta vacia pintar numero
            if (ncolumna == 3 && listaCasillas[nfila][ncolumna].getText().toString().isEmpty()) {
                animacion(listaCasillas[nfila][ncolumna_original], listaCasillas[nfila][ncolumna],"X");
                listaCasillas[nfila][ncolumna_original].setText("");
                listaCasillas[nfila][ncolumna].setText(valor_original);
            }// Comprobar si hay numero en la casilla derecha
            else if (!listaCasillas[nfila][ncolumna].getText().toString().isEmpty()) {
                Log.d(LOG_TAG, "OCUPADA Fila: " + nfila + " Columna: " + ncolumna);
                // Colocar el numero en la posicion anterior a la ocupada
                ncolumna--;
                animacion(listaCasillas[nfila][ncolumna_original], listaCasillas[nfila][ncolumna],"X");
                listaCasillas[nfila][ncolumna_original].setText("");
                listaCasillas[nfila][ncolumna].setText(valor_original);
                break;
            }
        }

        // Comprobar si el numero se ha movido
        if (ncolumna != ncolumna_original) {
            mover = true;
        }
    }

    public void moverNumIzquierda(int nfila, int ncolumna) {
        int ncolumna_original = ncolumna;
        int columna_siguiente = ncolumna+1;
        String valor_original = (String) listaCasillas[nfila][ncolumna_original].getText();

        // Comprobar si se puede sumar
        if (ncolumna < 3){
            while (columna_siguiente<=3){
                String valor_siguiente = (String) listaCasillas[nfila][columna_siguiente].getText();
                if (!listaCasillas[nfila][columna_siguiente].getText().toString().isEmpty()){
                    if (Integer.parseInt(valor_original) == Integer.parseInt(valor_siguiente)) {
                        int suma = Integer.parseInt(valor_original) + Integer.parseInt(valor_siguiente);
                        Log.d(LOG_TAG, "SUMA: " + suma);
                        animacion(listaCasillas[nfila][columna_siguiente], listaCasillas[nfila][ncolumna_original],"X");
                        listaCasillas[nfila][columna_siguiente].setText("");
                        listaCasillas[nfila][ncolumna_original].setText(suma + "");
                        // Sumar numeros a la puntuacion total
                        sumar_score(suma);
                        valor_original = suma+"";
                        mover = true;
                        break;
                    }else{
                        break;
                    }
                }
                columna_siguiente++;
            }
        }

        // Comprobar si se puede mover el numero
        while (ncolumna > 0) {
            ncolumna--;
            // Si estamos en la ultima columna y esta vacia pintar numero
            if (ncolumna == 0 && listaCasillas[nfila][ncolumna].getText().toString().isEmpty()) {
                animacion(listaCasillas[nfila][ncolumna_original], listaCasillas[nfila][ncolumna],"X");
                listaCasillas[nfila][ncolumna_original].setText("");
                listaCasillas[nfila][ncolumna].setText(valor_original);
            }// Comprobar si hay numero en la casilla derecha
            else if (!listaCasillas[nfila][ncolumna].getText().toString().isEmpty()) {
                Log.d(LOG_TAG, "OCUPADA Fila: " + nfila + " Columna: " + ncolumna);
                    // Colocar el numero en la posicion anterior a la ocupada
                    ncolumna++;
                    animacion(listaCasillas[nfila][ncolumna_original], listaCasillas[nfila][ncolumna],"X");
                    listaCasillas[nfila][ncolumna_original].setText("");
                    listaCasillas[nfila][ncolumna].setText(valor_original);
                    break;
            }
        }

        // Comprobar si el numero se ha movido
        if (ncolumna != ncolumna_original) {
            mover = true;
        }
    }

    public void moverNumAbajo(int nfila, int ncolumna) {
        int nfila_original = nfila;
        int fila_anterior = nfila-1;
        String valor_original = (String) listaCasillas[nfila_original][ncolumna].getText();

        // Comprobar si se puede sumar
        if (nfila > 0){
            while (fila_anterior>=0){
                if (!listaCasillas[fila_anterior][ncolumna].getText().toString().isEmpty()){
                    String valor_anterior = (String) listaCasillas[fila_anterior][ncolumna].getText();
                    if (Integer.parseInt(valor_original) == Integer.parseInt(valor_anterior)) {
                        int suma = Integer.parseInt(valor_original) + Integer.parseInt(valor_anterior);
                        Log.d(LOG_TAG, "SUMA: " + suma);
                        animacion(listaCasillas[fila_anterior][ncolumna], listaCasillas[nfila_original][ncolumna],"Y");
                        listaCasillas[fila_anterior][ncolumna].setText("");
                        listaCasillas[nfila_original][ncolumna].setText(suma + "");
                        // Sumar numeros a la puntuacion total
                        sumar_score(suma);
                        valor_original = suma+"";
                        mover = true;
                        break;
                    }else{
                        break;
                    }
                }
                fila_anterior--;
            }
        }

        // Comprobar si se puede mover el numero
        while (nfila < 3) {
            nfila++;
            // Si estamos en la ultima columna y esta vacia pintar numero
            if (nfila == 3 && listaCasillas[nfila][ncolumna].getText().toString().isEmpty()) {
                animacion(listaCasillas[nfila_original][ncolumna], listaCasillas[nfila][ncolumna],"Y");
                listaCasillas[nfila_original][ncolumna].setText("");
                listaCasillas[nfila][ncolumna].setText(valor_original);
            }// Comprobar si hay numero en la casilla derecha
            else if (!listaCasillas[nfila][ncolumna].getText().toString().isEmpty()) {
                Log.d(LOG_TAG, "OCUPADA Fila: " + nfila + " Columna: " + ncolumna);
                    nfila--;
                    animacion(listaCasillas[nfila_original][ncolumna], listaCasillas[nfila][ncolumna],"Y");
                    listaCasillas[nfila_original][ncolumna].setText("");
                    listaCasillas[nfila][ncolumna].setText(valor_original);
                    break;
            }
        }

        // Comprobar si el numero se ha movido
        if (nfila != nfila_original) {
            mover = true;
        }
    }

    public void moverNumArriba(int nfila, int ncolumna) {
        int nfila_original = nfila;
        int fila_siguiente = nfila+1;
        String valor_original = (String) listaCasillas[nfila_original][ncolumna].getText();

        // Comprobar si se puede sumar
        if (nfila < 3){
            while (fila_siguiente<=3){
                String valor_siguiente = (String) listaCasillas[fila_siguiente][ncolumna].getText();
                if (!listaCasillas[fila_siguiente][ncolumna].getText().toString().isEmpty()){
                    if (Integer.parseInt(valor_original) == Integer.parseInt(valor_siguiente)) {
                        int suma = Integer.parseInt(valor_original) + Integer.parseInt(valor_siguiente);
                        Log.d(LOG_TAG, "SUMA: " + suma);
                        animacion(listaCasillas[fila_siguiente][ncolumna], listaCasillas[nfila_original][ncolumna],"Y");
                        listaCasillas[fila_siguiente][ncolumna].setText("");
                        listaCasillas[nfila_original][ncolumna].setText(suma + "");
                        // Sumar numeros a la puntuacion total
                        sumar_score(suma);
                        valor_original = suma+"";
                        mover = true;
                        break;
                    }else{
                        break;
                    }
                }
                fila_siguiente++;
            }
        }

        // Comprobar si se puede mover el numero
        while (nfila > 0) {
            nfila--;
            // Si estamos en la ultima columna y esta vacia pintar numero
            if (nfila == 0 && listaCasillas[nfila][ncolumna].getText().toString().isEmpty()) {
                animacion(listaCasillas[nfila_original][ncolumna], listaCasillas[nfila][ncolumna],"Y");
                listaCasillas[nfila_original][ncolumna].setText("");
                listaCasillas[nfila][ncolumna].setText(valor_original);
            }// Comprobar si hay numero en la casilla derecha
            else if (!listaCasillas[nfila][ncolumna].getText().toString().isEmpty()) {
                Log.d(LOG_TAG, "OCUPADA Fila: " + nfila + " Columna: " + ncolumna);
                    // Colocar el numero en la posicion anterior a la ocupada
                    nfila++;
                    animacion(listaCasillas[nfila_original][ncolumna], listaCasillas[nfila][ncolumna],"Y");
                    listaCasillas[nfila_original][ncolumna].setText("");
                    listaCasillas[nfila][ncolumna].setText(valor_original);
                    break;
            }
        }

        // Comprobar si el numero se ha movido
        if (nfila != nfila_original) {
            mover = true;
        }
    }

    public void generarNumero() {
        boolean numero_puesto = false;
        while (!numero_puesto) {
            nfila_aleatorio = (int) (Math.random() * (3 + 1 - 0)) + 0;
            ncolumna_aleatorio = (int) (Math.random() * (3 + 1 - 0)) + 0;
            if (!listaCasillas[nfila_aleatorio][ncolumna_aleatorio].getText().toString().isEmpty()) {
                Log.d(LOG_TAG, "OCUPADA Fila: " + nfila_aleatorio + " Columna: " + ncolumna_aleatorio);
            } else {
                    listaCasillas[nfila_aleatorio][ncolumna_aleatorio].setText("2");
                animacionAparecer(listaCasillas[nfila_aleatorio][ncolumna_aleatorio]);
                numero_puesto = true;
                Log.d(LOG_TAG, " Creado Fila: " + nfila_aleatorio + " Columna: " + ncolumna_aleatorio);
            }
        }
    }

    public void animacionAparecer(TextView numero) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(0f, 1f, 0f, 1f, 100, 100);
        scaleAnimation.setDuration(100);
        numero.startAnimation(scaleAnimation);
    }

    public void comprobarFin(){
        boolean fin_col = true;
        boolean fin_fil = true;
        boolean fin_v = true;
        for (int fila = 0; fila < listaCasillas.length; fila++) {
            for (int columna = 0; columna < listaCasillas[fila].length; columna++) {

                // Comprobar que el numero de al lado sea igual
                if(columna<=2){
                    int sig_columna = columna + 1;
                    if (listaCasillas[fila][columna].getText().toString().equals(listaCasillas[fila][sig_columna].getText().toString())){
                        fin_col = false;
                        break;
                    }
                }

                // Comprobar que el numero de debajo no sea igual
                if (fila<=2){
                    int fila_abajo = fila + 1;
                    if (listaCasillas[fila][columna].getText().toString().equals(listaCasillas[fila_abajo][columna].getText().toString())){
                        fin_fil = false;
                        break;
                    }
                }

                // Comprobar que no haya casillas vacias
                if(listaCasillas[fila][columna].getText().toString().isEmpty()){
                    fin_v = false;
                    break;
                }

            }
        }
        if (fin_col == true && fin_fil == true && fin_v == true){
            db.addScore(user,"2048",valor_score);
            dialogo_fin();

        }
    }

    // Suma la puntuacion cuando dos numeros se suman
    public void sumar_score(int suma_numeros) {
        // Sumar numeros a la puntuacion
        valor_score = valor_score + suma_numeros;
        // Mostrar por pantalla la puntuacion
        score.setText("SCORE \n " + valor_score);
    }

    public void newGame(){
        // Borrar todos los numeros
        for (int fila = 0; fila < listaCasillas.length; fila++) {
            for (int columna = 0; columna < listaCasillas[fila].length; columna++) {
                listaCasillas[fila][columna].setText("");
            }
        }
        // Cambiar la puntuacion a 0
        valor_score = 0;
        score.setText("SCORE \n 0");

        // Generar un numero para volver a empezar a jugar
        generarNumero();
    }

    public void dialogo_fin(){
        ArrayList<Score> score_list = db.getScores("2048");
        String ranking = "";

        for (int i = 0; i < score_list.size(); i++) {
            if (i==10){
                break;
            }
            ranking += (i+1) + "- " + score_list.get(i).getPlayer() + " " + score_list.get(i).getScore() +"\n";

        }

        best_score.setText("BEST \n "+ db.getBestScore(user, "2048"));
        Dialogo dialogo = new Dialogo(valor_score,ranking);
        dialogo.show(getSupportFragmentManager(),"Dialogo");
        newGame();
    }

    public void animacion(TextView original ,TextView fin, String orientacion){
        float distanciaX = 0;
        float distanciaY = 0;
        if (orientacion.equals("X")){
            distanciaX = fin.getX() - original.getX();
        }else {
            distanciaY = fin.getY() - original.getY();
        }

        Animation animation = new TranslateAnimation(0, distanciaX,0, distanciaY);
            animation.setDuration(100);//150
            original.startAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Pintar el tablero al acabar la animacion
                for (int fila = 0; fila < 4; fila++) {
                    for (int columna = 0; columna < 4; columna++) {
                        pintar(listaCasillas[fila][columna]);
                    }
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    // Pintar los textview segun el numero que contiene
    public void pintar(TextView textView) {
        String numero = textView.getText().toString();

        if (!numero.equals("")) {
            textView.setVisibility(View.VISIBLE);
        }else if (numero.equals("")) {
            textView.setVisibility(View.INVISIBLE);
        }

        switch (numero) {
            case "2":
                textView.setBackgroundResource(R.drawable.c2);
                break;
            case "4":
                textView.setBackgroundResource(R.drawable.c4);
                break;
            case "8":
                textView.setBackgroundResource(R.drawable.c8);
                break;
            case "16":
                textView.setBackgroundResource(R.drawable.c16);
                break;
            case "32":
                textView.setBackgroundResource(R.drawable.c32);
                break;
            case "64":
                textView.setBackgroundResource(R.drawable.c64);
                break;
            case "128":
                textView.setBackgroundResource(R.drawable.c128);
                break;
            case "256":
                textView.setBackgroundResource(R.drawable.c256);
                break;
            case "512":
                textView.setBackgroundResource(R.drawable.c512);
                break;
            case "1024":
                textView.setBackgroundResource(R.drawable.c1024);
                break;
            case "2048":
                textView.setBackgroundResource(R.drawable.c2048);
                break;
        }

    }

    public void gCasillasAnteriores(){
        valor_score_anterior = valor_score;

        for (int fila = 0; fila < 4; fila++) {
            for (int columnas = 0; columnas < 4; columnas++) {
                listaCasillasAnterior[fila][columnas].setText(listaCasillas[fila][columnas].getText().toString());
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
    }


}