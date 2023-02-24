package com.example.gamecenter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.DialogFragment;

public class Dialogo extends DialogFragment {
    String ranking;
    int puntuacion;
    TextView txt_score;
    TextView txt_ranking;

    public Dialogo(int puntuacion, String ranking) {
        this.puntuacion = puntuacion;
        this.ranking = ranking;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialogo, null);
        txt_score = (TextView) view.findViewById(R.id.txt_score);
        txt_score.setText("Your score:\n" + puntuacion);

        txt_ranking = (TextView) view.findViewById(R.id.txt_ranking);
        txt_ranking.setText("RANKING\n---------------------\n" + ranking);

        builder.setView(view);
        return builder.create();
    }

}
