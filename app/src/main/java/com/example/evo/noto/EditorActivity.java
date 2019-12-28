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


public class EditorActivity extends AppCompatActivity {
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
    private String oldText, oldNoteTag;
    private String newText, newNoteTag;

    //Those button relate to poups dialog
    private Button yesBtnDD, noBtnDD, yesBtnED, noBtnED;
    private Dialog del_Dialog, edit_Dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        editor = (EditText) findViewById(R.id.editText);
        tagEditor = (EditText) findViewById(R.id.tagEditor);
        note_info = (TextView) findViewById(R.id.note_info);
        note_infoLable = (TextView) findViewById(R.id.noteInfo);
        Intent intent = getIntent();

        //Retrieve the rui that has been send when the intent started
        Uri uri = intent.getParcelableExtra(NotesProvider.CONTENT_ITEM_TYPE);


        action = Intent.ACTION_EDIT;
        noteFilter = DBOpenHelper.NOTE_ID + "=" + uri.getLastPathSegment();

        Cursor cursor = getContentResolver().query(uri,
                DBOpenHelper.ALL_COLUMNS, noteFilter, null, null);
        cursor.moveToFirst();
        oldText = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT));

        oldNoteTag = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TAG));

        dateCreated = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_CREATED));

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date date = dateFormat.parse(dateCreated);
            Timestamp timestamp = new java.sql.Timestamp(date.getTime());
            String day = timestamp.toString().substring(0, 10);
            String time = timestamp.toString().substring(11, 16);
            String info;
            info = "Creation : " + "\n" + day + "\n" + time;
            note_info.setText(info);
            note_infoLable.setText(getString(R.string.note_info));
            Log.d("info", info);
            Log.d("day", day);
            Log.d("time", time);
        } catch (Exception e) {

        }

        tagEditor.setText(oldNoteTag);

        editor.setText(oldText);
        editor.requestFocus();


        //setting confirmation delete dialog
        del_Dialog = new Dialog(this);
        del_Dialog.setContentView(R.layout.confirm_delete_dialog);
        yesBtnDD = (Button) del_Dialog.findViewById(R.id.yes_btnDD);
        noBtnDD = (Button) del_Dialog.findViewById(R.id.no_btnDD);

        edit_Dialog = new Dialog(this);
        edit_Dialog.setContentView(R.layout.confirm_edit_dialog);
        yesBtnED = (Button) edit_Dialog.findViewById(R.id.yes_btnED);
        noBtnED = (Button) edit_Dialog.findViewById(R.id.no_btnED);

        //setting confirmation confirm editing dialog


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (action.equals(Intent.ACTION_EDIT)) {
            getMenuInflater().inflate(R.menu.menu_editor, menu);
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
            case R.id.action_delete:


                del_Dialog.show();


                yesBtnDD.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        deleteNote();
                        del_Dialog.dismiss();
                        finish();
                    }
                });


                noBtnDD.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        del_Dialog.dismiss();
                    }
                });


                break;
            case R.id.action_save:
                finishEditingWOQ();
                break;

        }

        return true;
    }


    //method that handle note deleting
    private void deleteNote() {
        getContentResolver().delete(NotesProvider.CONTENT_URI,
                noteFilter, null);
        Toast.makeText(this, getString(R.string.note_deleted),
                Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();


    }
    //method that handle confirmation treatment when we want to quit your editor
    private void finishEditing() {
        newText = editor.getText().toString().trim();
        newNoteTag = tagEditor.getText().toString().trim();

        switch (action) {
            case Intent.ACTION_EDIT:
                if (newText.length() == 0) {


                    del_Dialog.show();


                    yesBtnDD.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            deleteNote();
                            del_Dialog.dismiss();
                            finish();
                        }
                    });


                    noBtnDD.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            del_Dialog.dismiss();
                            finish();
                        }
                    });


                } else if (oldText.equals(newText) && oldNoteTag.equals(newNoteTag)) {
                    setResult(RESULT_CANCELED);
                    finish();
                } else {
                    edit_Dialog.show();
                    yesBtnED.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            updateNote(newText, newNoteTag);
                            edit_Dialog.dismiss();
                            finish();
                        }
                    });

                    noBtnED.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            edit_Dialog.dismiss();
                            finish();
                        }
                    });

                }

        }
    }



    //method that handle confirmation treatment when we want to persist all data existing in our layout wo quite

    private void finishEditingWOQ() {
        newText = editor.getText().toString().trim();
        newNoteTag = tagEditor.getText().toString().trim();

        switch (action) {
            case Intent.ACTION_EDIT:
                if (newText.length() == 0) {

                    del_Dialog.show();

                    yesBtnDD.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            deleteNote();
                            del_Dialog.dismiss();
                            finish();
                        }
                    });


                    noBtnDD.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            del_Dialog.dismiss();
                        }
                    });


                } else if (oldText.equals(newText) && oldNoteTag.equals(newNoteTag)) {
                } else {
                    edit_Dialog.show();
                    yesBtnED.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            updateNote(newText, newNoteTag);
                            edit_Dialog.dismiss();
                            updateAbstractContent();
                        }
                    });

                    noBtnED.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            edit_Dialog.dismiss();
                        }
                    });

                }

        }
    }

    private void updateNote(String noteText, String noteTag) {
        ContentValues values = new ContentValues();

        //set a new attributes of our note : newNote if it changed ; newContent if it changed
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        values.put(DBOpenHelper.NOTE_TAG, noteTag);
        getContentResolver().update(NotesProvider.CONTENT_URI, values, noteFilter, null);
        Toast.makeText(this, getString(R.string.note_updated), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    @Override
    public void onBackPressed() {
        finishEditing();
    }


    //this method will; launch when the user want to make speech as input method
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

    //this method will; launch when the user end up with its speech
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




    //update all our layout variable before if we persist data
    public void updateAbstractContent() {
        this.oldText = this.newText;
        this.oldNoteTag = this.newNoteTag;
        Log.i("oldText", this.oldText);
        Log.i("newText", this.newText);
        Log.i("oldNoteTag", this.oldNoteTag);
        Log.i("newNoteTag", this.newNoteTag);
    }
}
