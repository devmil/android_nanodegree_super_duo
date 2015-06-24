package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import de.greenrobot.event.EventBus;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.events.BookFetchingProgressEvent;
import it.jaschke.alexandria.services.BookService;


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";
    private EditText ean;
    private final int LOADER_ID = 1;
    private View rootView;
    private GridLayout resultContent;

    private final String EAN_CONTENT="eanContent";
    private static final String SCAN_FORMAT = "scanFormat";
    private static final String SCAN_CONTENTS = "scanContents";

    private String mScanFormat = "Format:";
    private String mScanContents = "Contents:";
    private TextView txtAdd_book_bookTitle;
    private TextView txtAdd_book_bookSubTitle;
    private TextView txtAdd_book_authors;
    private ImageView imgAdd_book_bookCover;
    private TextView txtAdd_book_categories;
    private View btnAdd_book_save_button;
    private View btnAdd_book_delete_button;
    private LinearLayout layoutAdd_book_progress_layout;

    public AddBook(){
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(BookFetchingProgressEvent e) {
        switch(e.getAction()) {
            case BookFetchingProgressEvent.ACTION_STARTED:
                setProgressActive(true);
                break;
            case BookFetchingProgressEvent.ACTION_FINISHED:
                setProgressActive(false);
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(ean!=null) {
            outState.putString(EAN_CONTENT, ean.getText().toString());
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        ean = (EditText) rootView.findViewById(R.id.add_book_ean);
        resultContent = (GridLayout)rootView.findViewById(R.id.add_book_result_content);
        txtAdd_book_bookTitle = (TextView) rootView.findViewById(R.id.add_book_bookTitle);
        txtAdd_book_bookSubTitle = (TextView) rootView.findViewById(R.id.add_book_bookSubTitle);
        txtAdd_book_authors = (TextView) rootView.findViewById(R.id.add_book_authors);
        imgAdd_book_bookCover = (ImageView) rootView.findViewById(R.id.add_book_bookCover);
        txtAdd_book_categories = (TextView) rootView.findViewById(R.id.add_book_categories);
        btnAdd_book_save_button = rootView.findViewById(R.id.add_book_save_button);
        btnAdd_book_delete_button = rootView.findViewById(R.id.add_book_delete_button);
        layoutAdd_book_progress_layout = (LinearLayout)rootView.findViewById(R.id.add_book_progress_layout);

        layoutAdd_book_progress_layout.setVisibility(View.GONE);

        ean.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String ean =s.toString();
                //catch isbn10 numbers
                if(ean.length()==10 && !ean.startsWith("978")){
                    ean="978"+ean;
                }
                if(ean.length()<13){
                    clearFields();
                    return;
                }
                //Once we have an ISBN, start a book intent
                fetchBook(ean);
            }
        });

        rootView.findViewById(R.id.add_book_scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                IntentIntegrator ii = IntentIntegrator.forSupportFragment(AddBook.this);
                ii.setDesiredBarcodeFormats(IntentIntegrator.PRODUCT_CODE_TYPES);
                ii.initiateScan();
            }
        });

        btnAdd_book_save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ean.setText("");
            }
        });

        btnAdd_book_delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteBook(ean.getText().toString());
                ean.setText("");
            }
        });

        if(savedInstanceState!=null){
            ean.setText(savedInstanceState.getString(EAN_CONTENT));
            ean.setHint("");
        }

        return rootView;
    }

    private void fetchBook(String ean) {
        Intent bookIntent = BookService.createFetchBookIntent(getActivity(), ean);
        getActivity().startService(bookIntent);
        restartLoader();
    }

    private void deleteBook(String ean) {
        Intent bookIntent = BookService.createDeleteBookIntent(getActivity(), ean);
        getActivity().startService(bookIntent);
    }

    private void setProgressActive(boolean isActive) {
        layoutAdd_book_progress_layout.setVisibility(isActive ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Activity activity = getActivity();
        if(activity == null) {
            return;
        }
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null && result.getFormatName() != null) {
            String content = result.getContents();
            if(content != null && content.length() >= 10) {
                fetchBook(result.getContents());
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(ean.getText().length()==0){
            return null;
        }
        String eanStr= ean.getText().toString();
        if(eanStr.length()==10 && !eanStr.startsWith("978")){
            eanStr="978"+eanStr;
        }
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        resultContent.setVisibility(View.VISIBLE);

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        txtAdd_book_bookTitle.setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        txtAdd_book_bookSubTitle.setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        String[] authorsArr = authors.split(",");

        txtAdd_book_authors.setLines(authorsArr.length);
        txtAdd_book_authors.setText(authors.replace(",", "\n"));
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            Glide.with(this).load(imgUrl).into(imgAdd_book_bookCover);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        txtAdd_book_categories.setText(categories);

        btnAdd_book_save_button.setVisibility(View.VISIBLE);
        btnAdd_book_delete_button.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private void clearFields(){
        txtAdd_book_bookTitle.setText("");
        txtAdd_book_bookSubTitle.setText("");
        txtAdd_book_authors.setText("");
        txtAdd_book_categories.setText("");
        resultContent.setVisibility(View.INVISIBLE);
        btnAdd_book_save_button.setVisibility(View.INVISIBLE);
        btnAdd_book_delete_button.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }
}
