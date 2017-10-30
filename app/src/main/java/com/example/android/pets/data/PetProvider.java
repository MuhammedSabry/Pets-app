package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import static android.content.UriMatcher.NO_MATCH;

/**
 * s
 * Created by Muhammed on 9/29/2017.
 */

public class PetProvider extends ContentProvider {
    PetDbHelper petHelper;

    SQLiteDatabase dbRead, dbWrite;

    private static final UriMatcher sUriMatcher = new UriMatcher(NO_MATCH);
    private static final int PETS = 100, PETS_ID = 101;

    static {

        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, "pets", PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, "pets/#", PETS_ID);
    }

    @Override
    public boolean onCreate() {
        petHelper = new PetDbHelper(getContext());
        dbRead = petHelper.getReadableDatabase();
        dbWrite = petHelper.getWritableDatabase();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionargs, @Nullable String orderby) {
        int i = sUriMatcher.match(uri);
        Cursor cursor;
        switch (i) {
            case PETS:
                cursor = dbRead.query(PetContract.PetEntry.TABLE_NAME
                        , projection, selection, selectionargs, null, null, orderby);
                break;
            case PETS_ID:
                selection = PetContract.PetEntry._ID + "=?";
                selectionargs = new String[]{ String.valueOf(ContentUris.parseId(uri)) };
                cursor = dbRead.query(PetContract.PetEntry.TABLE_NAME
                        , projection
                        , selection
                        , selectionargs
                        , null
                        , null
                        , orderby);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI" + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri))
        {
            case PETS:
                return PetContract.PetEntry.CONTENT_LIST_TYPE;
            case PETS_ID:
                return PetContract.PetEntry.CONTENT_ITEM_TYPE;
            default:
                return "Invalid uri";
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        if (!DataValid(contentValues))
            throw new IllegalArgumentException("Invalid Data to insert to the database ");
        else {
            if (sUriMatcher.match(uri) == PETS) {
                getContext().getContentResolver().notifyChange(uri,null);
                return ContentUris.withAppendedId(PetContract.PetEntry.CONTENT_URI
                        , dbWrite.insert(PetContract.PetEntry.TABLE_NAME, null, contentValues));
            } else
                throw new IllegalArgumentException("Invalid Uri to insert " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        switch (sUriMatcher.match(uri)) {
            case PETS:
                getContext().getContentResolver().notifyChange(uri,null);

                return dbWrite.delete(PetContract.PetEntry.TABLE_NAME, selection, selectionArgs);
            case PETS_ID:
                getContext().getContentResolver().notifyChange(uri,null);

                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return dbWrite.delete(PetContract.PetEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Invalid uri doesn't match database " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        switch (sUriMatcher.match(uri)) {
            case PETS:
                if (!updateValid(contentValues))
                    throw new IllegalArgumentException("Invalid Data to insert to the database ");
                else {
                    getContext().getContentResolver().notifyChange(uri,null);
                    return dbWrite.update(PetContract.PetEntry.TABLE_NAME
                            , contentValues
                            , null
                            , null);
                }
            case PETS_ID:
                if (!updateValid(contentValues))
                    throw new IllegalArgumentException("Invalid Data to insert to the database ");
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                getContext().getContentResolver().notifyChange(uri,null);
                return dbWrite.update(PetContract.PetEntry.TABLE_NAME
                        , contentValues
                        , selection
                        , selectionArgs);
            default:
                throw new IllegalArgumentException("Invalid uri doesn't match database " + uri);
        }
    }

    private boolean updateValid(ContentValues values) {
        if (values.size() == 0)
            return false;
        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_NAME)) {
            if (!validName(values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME)))
                return false;
        }
        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_GENDER)) {
            if (!validGender(values.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER)))
                return false;
        }
        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_WEIGHT)) {
            if (!validWeight(values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT)))
                return false;
        }
        return true;
    }

    private boolean DataValid(ContentValues contents) {
        String name = contents.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
        Integer gender = contents.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
        Integer weight = contents.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
        if (!validGender(gender)
                || !validName(name)
                || !validWeight(weight))
            return false;
        else
            return true;
    }

    private boolean validGender(Integer s) {
        if (s == PetContract.PetEntry.GENDER_FEMALE || s == PetContract.PetEntry.GENDER_MALE || s == PetContract.PetEntry.GENDER_UNKNOWN)
            return true;
        else
            return false;
    }

    private boolean validName(String s) {
        if (s.trim().isEmpty())
            return false;
        return true;
    }

    private boolean validWeight(Integer l) {
        if (l == null)
            return false;
        if (l > 0.0 || l <= 100.0)
            return true;
        else
            return false;
    }
}
