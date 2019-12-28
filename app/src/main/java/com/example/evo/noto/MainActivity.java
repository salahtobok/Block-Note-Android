package com.example.evo.noto;


import android.app.Dialog;
import android.app.SearchManager;
import android.support.v7.widget.SearchView;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {


    private TextView emptyText;
    //This string is used to store note id we use it when we want to show a specific note
    private String noteFilter;


    //The dialog that appear when we want to delete our note from main or home application
    private Dialog del_Dialog;
    private Button yesBtnDD, noBtnDD;

    //This menu item represent a delete button that will appear when on long click at item list
    private MenuItem delete_item;


    private static final int EDITOR_REQUEST_CODE = 1001;


    //Cursor adapter use to fetch result of query as menu list items
    private CursorAdapter cursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Use a noteCursorAdapter as our list adapter
        cursorAdapter = new NotesCursorAdapter(this, null, 0);

        //Reference to a list view
        ListView list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(cursorAdapter);

        emptyText = (TextView) findViewById(R.id.noteAlert);


        //A specific treatment used when a list is empty
        if (list.getAdapter().getCount() == 0) {
            emptyText.setText(
                    getString(
                            R.string.no_note_alert)
            );
            Log.i("itemList", String.valueOf(list.getAdapter().getCount()) + emptyText.getText());
            list.setEmptyView(emptyText);
        }


        //Show a note that we intent to display
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            delete_item.setVisible(false);
                                            Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                                            Uri uri = Uri.parse(NotesProvider.CONTENT_URI + "/" + id);
                                            intent.putExtra(NotesProvider.CONTENT_ITEM_TYPE, uri);
                                            startActivityForResult(intent, EDITOR_REQUEST_CODE);
                                        }
                                    }
        );


        //setting confirmation delete dialog
        del_Dialog = new Dialog(this);
        del_Dialog.setContentView(R.layout.confirm_delete_dialog);
        yesBtnDD = (Button) del_Dialog.findViewById(R.id.yes_btnDD);
        noBtnDD = (Button) del_Dialog.findViewById(R.id.no_btnDD);


        //Show item menu that present delete menu
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("selectedNote", String.valueOf(id));
                noteFilter = DBOpenHelper.NOTE_ID + "=" + id;
                delete_item.setVisible(true);
                return true;
            }
        });


        getLoaderManager().initLoader(0, null, this);
    }


    //That function called when we need to delete a note
    private void insertNote(String noteText) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        Uri noteUri = getContentResolver().insert(NotesProvider.CONTENT_URI,
                values);
        Log.d("MainActivity", "Inserted note " + noteUri.getLastPathSegment());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //Reference to item menu that describe a delete button
        delete_item = menu.findItem(R.id.action_delete);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    //Handle menu item"s functions
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_delete_all:
                deleteAllNotes();
                break;

            case R.id.action_delete:

                del_Dialog.show();

                yesBtnDD.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        deleteNote();
                        del_Dialog.dismiss();
                        delete_item.setVisible(false);

                    }
                });


                noBtnDD.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        del_Dialog.dismiss();
                    }
                });
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    //That method called when we need to delete all notes
    private void deleteAllNotes() {

        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {


                            getContentResolver().delete(
                                    NotesProvider.CONTENT_URI, null, null
                            );
                            restartLoader();

                            Toast.makeText(MainActivity.this,
                                    getString(R.string.all_deleted),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.are_you_sure))
                .setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener)
                .show();
    }


    private void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, NotesProvider.CONTENT_URI,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }


    //That method called when we need to add a new node *-start new note activity
    public void openEditorForNewNote(View view) {
        delete_item.setVisible(false);
        Intent intent = new Intent(this, EditorNewActivity.class);
        startActivityForResult(intent, EDITOR_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDITOR_REQUEST_CODE && resultCode == RESULT_OK) {
            restartLoader();
        }
    }


    //This function called when we need to delete a specific note
    private void deleteNote() {
        getContentResolver().delete(NotesProvider.CONTENT_URI,
                noteFilter, null);
        Toast.makeText(this, getString(R.string.note_deleted),
                Toast.LENGTH_SHORT).show();
        restartLoader();


    }


}