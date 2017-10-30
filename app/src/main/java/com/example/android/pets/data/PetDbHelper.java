package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * Created by Muhammed on 9/29/2017.s
 */

public class PetDbHelper extends SQLiteOpenHelper {
    private final static String DBName = "shelter.db";
    private final static int DBVersion = 1;
    private final static String CREATE_DATABASE_TABLE =
            "CREATE TABLE "
                    + PetContract.PetEntry.TABLE_NAME
                    + "( "
                    + PetContract.PetEntry._ID + " INTEGER"
                    + " PRIMARY KEY "
                    + ", "
                    + PetContract.PetEntry.COLUMN_PET_BREED + " TEXT, "
                    + PetContract.PetEntry.COLUMN_PET_NAME + " TEXT, "
                    + PetContract.PetEntry.COLUMN_PET_GENDER + " INTEGER NOT NULL, "
                    + PetContract.PetEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";

    public PetDbHelper(Context context) {
        super(context, DBName, null, DBVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DATABASE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        onCreate(sqLiteDatabase);
    }
}
