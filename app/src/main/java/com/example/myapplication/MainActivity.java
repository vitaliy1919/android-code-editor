package com.example.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.views.FastScroll;
import com.example.myapplication.views.NumbersView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_OPEN_FILE = 1;
    private static final int REQUEST_CREATE_FILE = 2;
    private Uri currentlyOpenedFile = null;
    boolean word_wrap = false;
    private ProgressBar progressBar;
    private MultiAutoCompleteTextView codeEdit;
    private NumbersView numbersView;
    private LinearLayout mainLayout;
    private HorizontalScrollView wrapScroll;
    private ScrollView verticalScroll;
    private FastScroll fastScroll;
    private int currentLineNumber = -1;
    private boolean shouldUpdate = true;
    private CPlusPlusHighlighter highlighter;
    public int countChar(String str, char c) {
        int count = 0;

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c)
                count++;
        }

        return count;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.wrap_content:
                word_wrap = !item.isChecked();
                if (word_wrap) {
                    wrapScroll.removeView(codeEdit);
                    mainLayout.removeView(wrapScroll);
                    mainLayout.addView(codeEdit);
                } else {
                    mainLayout.removeView(codeEdit);
                    mainLayout.addView(wrapScroll);
                    wrapScroll.addView(codeEdit);
                }
                shouldUpdate = true;
                item.setChecked(!item.isChecked());
                break;
            case R.id.open_file:
//                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                        != PackageManager.PERMISSION_GRANTED) {
//                    ActivityCompat.requestPermissions(MainActivity.this,
//                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                            1);
//                    return true;
//                }
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                intent.setType("application/pdf");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
//                startActivityForResult(intent, EDIT_REQUEST);
                intent.setType("*/*");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_OPEN_FILE);
                }
                break;
            case R.id.save_file:
                if (currentlyOpenedFile != null) {
                    try {
                        OutputStream stream = getContentResolver().openOutputStream(currentlyOpenedFile);
                        OutputStreamWriter writer = new OutputStreamWriter(stream);
                        writer.write(String.valueOf(codeEdit.getText()));
                        writer.close();
                        Toast.makeText(this, "Saved succesfully", Toast.LENGTH_SHORT).show();

                    } catch (FileNotFoundException e) {
                        Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(this, "Error while saving file", Toast.LENGTH_SHORT).show();

                    }
                }
            case R.id.create_file:
                Intent createFileIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                createFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                createFileIntent.setType("*/*");
                createFileIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                if (createFileIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(createFileIntent, REQUEST_CREATE_FILE);
                }
        }
        return true;
    }
    private static final String[] COUNTRIES = new String[] {
            "Belgium", "France", "Italy", "Germany", "Spain"
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        codeEdit = findViewById(R.id.code_editor);
        numbersView = findViewById(R.id.numbers_view);
        numbersView.initializeView(codeEdit);
        wrapScroll = findViewById(R.id.wrap_horizontal_scroll);
        mainLayout = findViewById(R.id.main_layout);
        progressBar = findViewById(R.id.progress_bar);
        highlighter = new CPlusPlusHighlighter(this);
        fastScroll = findViewById(R.id.fast_scroll);
        verticalScroll = findViewById(R.id.vertical_scroll);
        fastScroll.initialize(codeEdit, verticalScroll);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, COUNTRIES);
        codeEdit.setAdapter(adapter);
        codeEdit.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        codeEdit.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                Log.d("Code editor", "Layout changed");

                if (currentLineNumber != codeEdit.getLineCount() && shouldUpdate) {
                    shouldUpdate = false;
                    currentLineNumber = codeEdit.getLineCount();
                    Log.d("Code editor", "Numbers updated"+codeEdit.getLineCount());
//                    updateNumbersView();
                    numbersView.update();

                }
            }
        });
        Log.d("process", "Hello");
        codeEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Object spansToRemove[] = s.getSpans(0, s.length(), Object.class);
                for(Object span: spansToRemove){
                    if(span instanceof CharacterStyle)
                        s.removeSpan(span);
                }

                highlighter.hightliht(s);
                shouldUpdate = true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null)
            return;
        final Uri fileURI = data.getData();
        if (fileURI == null)
            return;
        currentlyOpenedFile = fileURI;
        Cursor returnCursor = getContentResolver().query(fileURI, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String fileName = returnCursor.getString(nameIndex);
        returnCursor.close();
        getSupportActionBar().setSubtitle(fileName);
        if (requestCode == REQUEST_OPEN_FILE ) {
            progressBar.setVisibility(View.VISIBLE);
            Thread readFile = new Thread(new Runnable() {
                @Override
                public void run() {
                    long startTime = System.currentTimeMillis();
//                    Log.d("URI result", fileURI.toString());
                    InputStream inputStream = null;
                    String str = "";
                    final StringBuilder buf = new StringBuilder();
                    try {
                        inputStream = getContentResolver().openInputStream(fileURI);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    int lines = 0;
                    BufferedInputStream reader = new BufferedInputStream(inputStream);
                    final ByteArrayOutputStream writer = new ByteArrayOutputStream();
                    byte[] bytes = new byte[1024*1024];

                    int chunkSize = 1;
                    try {
                        while (chunkSize > 0) {
                            chunkSize = reader.available();
                            chunkSize = reader.read(bytes, 0, chunkSize);
                            writer.write(bytes, 0, chunkSize);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    long difference = System.currentTimeMillis() - startTime;
                    Log.d("File read", "Time:" + difference/ 1000.0);

                    startTime = System.currentTimeMillis();
                    final String data1 = writer.toString();

                    difference = System.currentTimeMillis() - startTime;
                    Log.d("File read", "Convert time:" + difference/ 1000.0);

                    try {
                        inputStream.close();
                        reader.close();
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            long startTime = System.currentTimeMillis();
                            final String data = writer.toString();

                            long difference = System.currentTimeMillis() - startTime;
                            codeEdit.setText(data);
                            Log.d("File read", "Set time:" + difference/ 1000.0);

                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }


            });
            readFile.start();
        } else if (requestCode == REQUEST_CREATE_FILE) {
            codeEdit.setText("");
        }
    }

}
