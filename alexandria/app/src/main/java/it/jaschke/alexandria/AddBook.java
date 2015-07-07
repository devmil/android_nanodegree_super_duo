package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    private TextView txtAdd_book_bookTitle;
    private TextView txtAdd_book_bookSubTitle;
    private TextView txtAdd_book_authors;
    private ImageView imgAdd_book_bookCover;
    private TextView txtAdd_book_categories;
    private View btnAdd_book_save_button;
    private View btnAdd_book_delete_button;
    private LinearLayout layoutAdd_book_progress_layout;
    private Button btnAdd_book_scan_button;
    private TextView txtAdd_book_txt_error;

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
                setErrorText(e.getResult());
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
        btnAdd_book_scan_button = (Button)rootView.findViewById(R.id.add_book_scan_button);
        txtAdd_book_txt_error = (TextView)rootView.findViewById(R.id.add_book_txt_error);

        layoutAdd_book_progress_layout.setVisibility(View.GONE);

        InputFilter eanInputFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                boolean isOK = true;
                StringBuilder fixedResult = new StringBuilder(end - start);
                for(int i=start; i<end; i++) {
                    char currentChar = source.charAt(i);
                    int currentLength = dstart + fixedResult.length();
                    if(Character.isDigit(currentChar)
                            && currentLength < 13) {
                        fixedResult.append(currentChar);
                    } else {
                        isOK = false;
                    }
                }
                if(isOK) {
                    return null;
                }
                return fixedResult;
            }
        };
        ean.setFilters(new InputFilter [] { eanInputFilter });

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

        btnAdd_book_scan_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFields();
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
                //don't delete the ISBN text content as there might be a typo
                clearFields();
            }
        });

        clearFields();

        return rootView;
    }

    private void setErrorText(@BookFetchingProgressEvent.Results int result) {
        switch(result) {
            case BookFetchingProgressEvent.RESULT_OK:
                txtAdd_book_txt_error.setText("");
                break;
            case BookFetchingProgressEvent.RESULT_ERROR_BAD_FORMAT:
                txtAdd_book_txt_error.setText(R.string.add_book_result_bad_format);
                break;
            case BookFetchingProgressEvent.RESULT_ERROR_CONNECTION:
                txtAdd_book_txt_error.setText(R.string.add_book_result_error_connection);
                break;
            case BookFetchingProgressEvent.RESULT_NOT_FOUND:
                txtAdd_book_txt_error.setText(R.string.add_book_result_not_found);
                break;
        }
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
                //this triggers the search
                ean.setText(result.getContents());
                //fetchBook(result.getContents());
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void restartLoader(){
        if(getLoaderManager() != null) {
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }
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
        long eanLong;
        try {
            eanLong = Long.parseLong(eanStr);
        } catch(NumberFormatException e) {
            eanLong = 0;
        }
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(eanLong),
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

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));

        populateFields(bookTitle, bookSubTitle, authors, imgUrl, categories);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private void populateFields(String bookTitle, String bookSubTitle, String authors, String imgUrl, String categories) {
        if(bookTitle == null) {
            bookTitle = "";
        }
        if(bookSubTitle == null) {
            bookSubTitle = "";
        }
        if(authors == null) {
            authors = "";
        }
        if(imgUrl == null) {
            imgUrl = "";
        }
        if(categories == null) {
            categories = "";
        }

        ean.setEnabled(false);

        btnAdd_book_scan_button.setEnabled(false);

        resultContent.setVisibility(View.VISIBLE);

        txtAdd_book_bookTitle.setText(bookTitle);
        txtAdd_book_bookSubTitle.setText(bookSubTitle);

        String[] authorsArr = authors.split(",");

        txtAdd_book_authors.setLines(authorsArr.length);
        txtAdd_book_authors.setText(authors.replace(",", "\n"));

        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            Glide.with(this).load(imgUrl).into(imgAdd_book_bookCover);
        }

        txtAdd_book_categories.setText(categories);

        btnAdd_book_save_button.setVisibility(View.VISIBLE);
        btnAdd_book_delete_button.setVisibility(View.VISIBLE);
    }

    private void clearFields(){
        ean.setEnabled(true);

        btnAdd_book_scan_button.setEnabled(true);
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
