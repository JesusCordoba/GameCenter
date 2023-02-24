package com.example.gamecenter;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DataBase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "db_GameCenter";
    private static final String DATABASE_TABLE = "tbl_score";

    public DataBase(Context context) {
        super(context, DATABASE_NAME, null, 33);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE if not exists "+ DATABASE_TABLE +"(id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "player TEXT,"
                + "game TEXT,"
                + "score INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS "+ DATABASE_TABLE);
        onCreate(db);
    }

    public void addScore(String player, String game, int score) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("player", player);
        contentValues.put("game", game);
        contentValues.put("score", score);
        db.insert(DATABASE_TABLE, null, contentValues);
        db.close();
    }

    public ArrayList<Score> getScores(String typeGame) {
        ArrayList<Score> score_list = new ArrayList<Score>();
        SQLiteDatabase db = this.getWritableDatabase();
        String order;
        if (typeGame.equals("2048")){
            order = "DESC";
        }else{
            order = "ASC";
        }
        Cursor cursor = db.rawQuery("select * from " + DATABASE_TABLE + " where game LIKE '"+ typeGame +"' GROUP BY score ORDER BY score " + order, null);
        if (cursor.getCount() != 0) {
            if (cursor.moveToFirst()) {
                do {
                    Score item = new Score();
                    @SuppressLint("Range") String player = cursor.getString(cursor.getColumnIndex("player"));
                    item.setPlayer(player);
                    @SuppressLint("Range") String game = cursor.getString(cursor.getColumnIndex("game"));
                    item.setGame(game);
                    @SuppressLint("Range") int score = Integer.parseInt(cursor.getString(cursor.getColumnIndex("score")));
                    item.setScore(score);

                    score_list.add(item);
                } while (cursor.moveToNext());
            }
        }
        cursor.close();
        db.close();
        return score_list;
    }

    public int getBestScore(String usuario, String game) {
        int best_score = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        String order;
        if (game.equals("2048")){
            order = "DESC";
        }else{
            order = "ASC";
        }
        //"select * from " + DATABASE_TABLE + "WHERE player LIKE "+ usuario +" GROUP BY score ORDER BY score DESC LIMIT 1"
        Cursor cursor = db.rawQuery("select * from " + DATABASE_TABLE + " where player LIKE '"+ usuario +"' AND game LIKE '"+ game +"' GROUP BY score ORDER BY score " + order +" LIMIT 1", null);
        if (cursor.getCount() != 0) {
            if (cursor.moveToFirst()) {
                do {

                    @SuppressLint("Range") int score = Integer.parseInt(cursor.getString(cursor.getColumnIndex("score")));
                    best_score = score;

                } while (cursor.moveToNext());
            }
        }
        cursor.close();
        db.close();
        return best_score;
    }
}
