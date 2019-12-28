package com.example.evo.noto;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Console;

public class SearchResultsActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {


    private CursorAdapter cursorAdapter;

    private static final int EDITOR_REQUEST_CODE = 1001;


    private String noteFilter;

    //this string used as tag of search
    private String searchTag;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        handleIntent(getIntent());


        cursorAdapter = new NotesCursorAdapter(this, null, 0);

        ListView list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(cursorAdapter);

        emptyText = (TextView) findViewById(R.id.noteAlert);



        //this treatment describe if there is not result of our research
        if (list.getAdapter().getCount() == 0) {
            emptyText.setText(
                    getString(
                            R.string.search_alertBegin) +
                            this.searchTag +
                            getString(R.string.search_alert1End)
            );
            Log.i("itemList", String.valueOf(list.getAdapter().getCount()) + emptyText.getText());
            list.setEmptyView(emptyText);
        }

        TextView emptyText = (TextView) findViewById(R.id.noteAlert);
        list.setEmptyView(emptyText);

        //show editor to edit a specific note
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            Intent intent = new Intent(SearchResultsActivity.this, EditorActivity.class);
                                            Uri uri = Uri.parse(NotesProvider.CONTENT_URI + "/" + id);
                                            intent.putExtra(NotesProvider.CONTENT_ITEM_TYPE, uri);
                                            startActivityForResult(intent, EDITOR_REQUEST_CODE);
                                        }
                                    }

        );

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search_main, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }



    //this function will called when search activity will appear **we store a needed search key
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search
            Log.i("SearchTag", query);
            this.searchTag = query;
        }
    }


    private void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }



    //this will called when we need to fetch our cursor from database
    //so our query launched with a specific criteria that indicated by search tag **query
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        noteFilter = DBOpenHelper.NOTE_TAG + "=" + this.searchTag;
        return new CursorLoader(this, NotesProvider.CONTENT_URI,
                null, noteFilter, null, null);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDITOR_REQUEST_CODE && resultCode == RESULT_OK) {
            restartLoader();
        }
    }
}
