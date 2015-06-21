package it.jaschke.alexandria.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import it.jaschke.alexandria.MainActivity;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.api.books.GoogleBooksAPI;
import it.jaschke.alexandria.data.AlexandriaContract;
import retrofit.RestAdapter;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class BookService extends IntentService {

    private final String LOG_TAG = BookService.class.getSimpleName();

    public static final String FETCH_BOOK = "it.jaschke.alexandria.services.action.FETCH_BOOK";
    public static final String DELETE_BOOK = "it.jaschke.alexandria.services.action.DELETE_BOOK";

    public static final String EAN = "it.jaschke.alexandria.services.extra.EAN";

    public BookService() {
        super("Alexandria");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (FETCH_BOOK.equals(action)) {
                final String ean = intent.getStringExtra(EAN);
                fetchBook(ean);
            } else if (DELETE_BOOK.equals(action)) {
                final String ean = intent.getStringExtra(EAN);
                deleteBook(ean);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void deleteBook(String ean) {
        if(ean!=null) {
            getContentResolver().delete(AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(ean)), null, null);
        }
    }

    /**
     * Handle action fetchBook in the provided background thread with the provided
     * parameters.
     */
    private void fetchBook(String ean) {

        if(ean.length()!=13){
            return;
        }

        Cursor bookEntry = getContentResolver().query(
                AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(ean)),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        if(bookEntry.getCount()>0){
            bookEntry.close();
            return;
        }

        bookEntry.close();

        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(GoogleBooksAPI.GOOGLE_APIS_BASE_URL)
                .build();

        GoogleBooksAPI api = adapter.create(GoogleBooksAPI.class);
        try {
            GoogleBooksAPI.SearchResponse response = api.searchItems(String.format("isbn:%s", ean));

            if(response.totalItems <= 0
                    || response.items == null
                    || response.items.size() <= 0) {
                handleNoValidBookFound();
                return;
            }

            GoogleBooksAPI.Item resultItem = response.items.get(0);

            if(resultItem == null
                    || resultItem.volumeInfo == null) {
                handleNoValidBookFound();
                return;
            }

            String title = resultItem.volumeInfo.title;
            String subtitle = resultItem.volumeInfo.subtitle;
            String desc = resultItem.volumeInfo.description;
            String imgUrl = "";
            if(resultItem.volumeInfo.imageLinks != null) {
                imgUrl = resultItem.volumeInfo.imageLinks.thumbnail;
            }

            writeBackBook(ean, title, subtitle, desc, imgUrl);

            if(resultItem.volumeInfo.authors != null
                    && resultItem.volumeInfo.authors.size() > 0) {
                writeBackAuthors(ean, resultItem.volumeInfo.authors);
            }
            if(resultItem.volumeInfo.categories != null
                    && resultItem.volumeInfo.categories.size() > 0) {
                writeBackCategories(ean, resultItem.volumeInfo.categories);
            }

        } catch(Exception e) {
            //TODO: save error state so that it can be displayed in the UI
            Log.e(LOG_TAG, "Error ", e);
        }

    }

    private void handleNoValidBookFound() {
        Intent messageIntent = new Intent(MainActivity.MESSAGE_EVENT);
        messageIntent.putExtra(MainActivity.MESSAGE_KEY,getResources().getString(R.string.not_found));
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
    }

    private void writeBackBook(String ean, String title, String subtitle, String desc, String imgUrl) {
        ContentValues values= new ContentValues();
        values.put(AlexandriaContract.BookEntry._ID, ean);
        values.put(AlexandriaContract.BookEntry.TITLE, title);
        values.put(AlexandriaContract.BookEntry.IMAGE_URL, imgUrl);
        values.put(AlexandriaContract.BookEntry.SUBTITLE, subtitle);
        values.put(AlexandriaContract.BookEntry.DESC, desc);
        getContentResolver().insert(AlexandriaContract.BookEntry.CONTENT_URI,values);
    }

    private void writeBackAuthors(String ean, List<String> authors) {
        ContentValues values= new ContentValues();
        for (int i = 0; i < authors.size(); i++) {
            values.put(AlexandriaContract.AuthorEntry._ID, ean);
            values.put(AlexandriaContract.AuthorEntry.AUTHOR, authors.get(i));
            getContentResolver().insert(AlexandriaContract.AuthorEntry.CONTENT_URI, values);
            values= new ContentValues();
        }
    }

    private void writeBackCategories(String ean, List<String> categories) {
        ContentValues values= new ContentValues();
        for (int i = 0; i < categories.size(); i++) {
            values.put(AlexandriaContract.CategoryEntry._ID, ean);
            values.put(AlexandriaContract.CategoryEntry.CATEGORY, categories.get(i));
            getContentResolver().insert(AlexandriaContract.CategoryEntry.CONTENT_URI, values);
            values= new ContentValues();
        }
    }
 }