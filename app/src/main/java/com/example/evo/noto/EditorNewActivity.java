package com.example.evo.noto;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;



/*
This activity handle all what will happen when we interact with the presentation that shown when we need to add new nore
 */
public class EditorNewActivity extends AppCompatActivity {
    private EditText editor, tagEditor;
    private TextView note_info, note_infoLable;

    //We use this string to store the specific action
    // that we want to the provider to do it like add new note when we finish our editing
    private String action;

    //This string hold the information of note like creation time
    private String dateCreated;

    //This string is used to store note id we use it when we want to show a specific note
    private String noteFilter;

    //String that hold the inserted text
    private String oldText="", oldNoteTag="";
    private String newText, newNoteTag;

    //Those button relate to poups dialog
    private Button yesBtnSD, noBtnSD;
    private Dialog save_Dialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_new);

        editor = (EditText) findViewById(R.id.editText);
        tagEditor = (EditText) findViewById(R.id.note_tag);
        note_info = (TextView) findViewById(R.id.note_info);
        note_infoLable = (TextView) findViewById(R.id.noteInfo);
        Intent intent = getIntent();


        //Retrieve the rui that has been send when the intent started
        Uri uri = intent.getParcelableExtra(NotesProvider.CONTENT_ITEM_TYPE);

        action = Intent.ACTION_INSERT;
        setTitle(getString(R.string.new_note));


        save_Dialog = new Dialog(this);
        save_Dialog.setContentView(R.layout.confirm_save_dialog);
        yesBtnSD = (Button) save_Dialog.findViewById(R.id.yes_btnSD);
        noBtnSD = (Button) save_Dialog.findViewById(R.id.no_btnSD);
        //setting confirmation confirm editing dialog


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (action.equals(Intent.ACTION_INSERT)) {
            getMenuInflater().inflate(R.menu.menu_editor_type2, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finishEditing();
                break;
            case R.id.action_save:
                finishEditingWOQ();
                break;
        }

        return true;
    }

    private void deleteNote() {
        getContentResolver().delete(NotesProvider.CONTENT_URI,
                noteFilter, null);
        Toast.makeText(this, getString(R.string.note_deleted),
                Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();


    }

    private void finishEditing() {
        newText = editor.getText().toString().trim();
        newNoteTag = tagEditor.getText().toString();
        switch (action) {
            case Intent.ACTION_INSERT:
                if (newText.length() == 0) {
                    setResult(RESULT_CANCELED);
                    finish();
                } else
                if (!oldText.equals(newText) || !oldNoteTag.equals(newNoteTag)) {
                    //                    insertNote(newText);
                    save_Dialog.show();
                    yesBtnSD.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            insertNote(newText, newNoteTag);
                            save_Dialog.dismiss();
                            finish();
                        }
                    });
                    noBtnSD.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            save_Dialog.dismiss();
                            finish();
                        }
                    });
                }else {
                    finish();
                }
                break;
        }
//        finish();
    }

    private void finishEditingWOQ() {
        newText = editor.getText().toString().trim();
        newNoteTag = tagEditor.getText().toString().trim();

        switch (action) {
            case Intent.ACTION_INSERT:
                if (newText.length() == 0) {
                } else {
                    save_Dialog.show();
                    yesBtnSD.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            insertNote(newText, newNoteTag);
                            updateAbstractContent();
                            save_Dialog.dismiss();


                        }
                    });
                    noBtnSD.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            save_Dialog.dismiss();
                        }
                    });
                }
                break;


        }
    }

    private void updateNote(String noteText) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        getContentResolver().update(NotesProvider.CONTENT_URI, values, noteFilter, null);
        Toast.makeText(this, getString(R.string.note_updated), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    private void insertNote(String noteText, String noteTag) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        values.put(DBOpenHelper.NOTE_TAG, noteTag);
        getContentResolver().insert(NotesProvider.CONTENT_URI, values);
        Toast.makeText(this, getString(R.string.note_inserted), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    @Override
    public void onBackPressed() {
        finishEditing();
    }

    public void grtSpeechInput(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());


        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Editable currentText = editor.getText();

                    int cursorPosition = editor.getSelectionStart();


                    CharSequence enteredText = editor.getText().toString();
                    CharSequence startToCursor = enteredText.subSequence(0, cursorPosition);


                    CharSequence cursorToEnd = enteredText.subSequence(cursorPosition, enteredText.length());
                    editor.setText(startToCursor + " " + result.get(0) + " " + cursorToEnd);


                    int currentCursorPosition = cursorPosition + result.get(0).length() + 2;
                    editor.setSelection(currentCursorPosition);

                }
                break;
        }


    }



    public void updateAbstractContent() {
        this.oldText = this.newText;
        this.oldNoteTag = this.newNoteTag;
        Log.i("oldText", this.oldText);
        Log.i("newText", this.newText);
        Log.i("oldNoteTag", this.oldNoteTag);
        Log.i("newNoteTag", this.newNoteTag);
    }
}