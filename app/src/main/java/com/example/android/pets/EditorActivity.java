package com.example.android.pets;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetDbHelper;

/**
 * Allows user to create a new pet or edits an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * EditText field to enter the pet's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the pet's breed
     */
    private EditText mBreedEditText;

    /**
     * EditText field to enter the pet's weight
     */
    private EditText mWeightEditText;

    /**
     * EditText field to enter the pet's gender
     */
    private Spinner mGenderSpinner;

    private Uri intentUri;
    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    private PetDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        dbHelper = new PetDbHelper(this);
        // Find all relevant views that we will need to; read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);
        setupSpinner();

        intentUri = getIntent().getData();
        if (intentUri != null) {
            getLoaderManager().initLoader(0, null, this);
            setTitle("Edit pet");
        }
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = 1; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = 2; // Female
                    } else {
                        mGender = 0; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                ContentValues dbValues = new ContentValues();

                dbValues.put(PetContract.PetEntry.COLUMN_PET_NAME, mNameEditText.getText().toString());
                dbValues.put(PetContract.PetEntry.COLUMN_PET_BREED, mBreedEditText.getText().toString());

                if (mGenderSpinner.getSelectedItemPosition() == 1)
                    dbValues.put(PetContract.PetEntry.COLUMN_PET_GENDER, PetContract.PetEntry.GENDER_MALE);

                else if (mGenderSpinner.getSelectedItemPosition() == 2)
                    dbValues.put(PetContract.PetEntry.COLUMN_PET_GENDER, PetContract.PetEntry.GENDER_FEMALE);

                else
                    dbValues.put(PetContract.PetEntry.COLUMN_PET_GENDER, PetContract.PetEntry.GENDER_UNKNOWN);

                dbValues.put(PetContract.PetEntry.COLUMN_PET_WEIGHT, mWeightEditText.getText().toString());

                Uri rowID = getContentResolver().insert(PetContract.PetEntry.CONTENT_URI, dbValues);

                if (rowID != null)
                {
                    Toast.makeText(this
                                    ,"Pet added successfully with id: " + rowID
                                    , Toast.LENGTH_SHORT)
                                    .show();
                    return true;

                }
                else
                    {
                    Toast toast = Toast.makeText(this, "Error Adding the pet", Toast.LENGTH_SHORT);
                    toast.show();
                }
                // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                getContentResolver().delete(PetContract.PetEntry.CONTENT_URI, null, null);
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = new String[]
                {
                        PetContract.PetEntry.COLUMN_PET_NAME,
                        PetContract.PetEntry.COLUMN_PET_BREED,
                        PetContract.PetEntry.COLUMN_PET_GENDER,
                        PetContract.PetEntry.COLUMN_PET_WEIGHT
                };

        return new CursorLoader(this,
                intentUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        } else {
            cursor.moveToNext();
            int namec,breedc,weightc,genderc;
            namec = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_NAME);
            breedc = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_BREED);
            genderc = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_GENDER);
            weightc = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_WEIGHT);
            mNameEditText.setText(cursor.getString(namec));
            mBreedEditText.setText(cursor.getString(breedc));
            mWeightEditText.setText(String.valueOf(cursor.getDouble(weightc)));
            int gender = cursor.getInt(genderc);
            switch (gender) {
                case PetContract.PetEntry.GENDER_MALE:
                    mGenderSpinner.setSelection(1);
                    break;
                case PetContract.PetEntry.GENDER_FEMALE:
                    mGenderSpinner.setSelection(2);
                    break;
                default:
                    mGenderSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0);
    }
}