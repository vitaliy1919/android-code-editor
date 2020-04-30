package com.example.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.myapplication.SyntaxHighlight.CPlusPlusHighlighter;
import com.example.myapplication.SyntaxHighlight.Styler.GeneralColorScheme;
import com.example.myapplication.SyntaxHighlight.Styler.GeneralStyler;
import com.example.myapplication.SyntaxHighlight.Styler.Styler;
import com.example.myapplication.SyntaxHighlight.Tokens.TokenList;
import com.example.myapplication.utils.ConverterKt;
import com.example.myapplication.views.FastScroll;
import com.example.myapplication.views.NumbersView;
import com.google.android.material.snackbar.Snackbar;

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
    private LinearLayout letters;
    private ProgressBar progressBar;
    private MultiAutoCompleteTextView codeEdit;
    private NumbersView numbersView;
    private LinearLayout mainLayout;
    private HorizontalScrollView wrapScroll;
    private ScrollView verticalScroll;
    private FastScroll fastScroll;
    private int currentLineNumber = -1;
    private boolean shouldUpdate = true;
    private int startHighlight = -1;
    private int endHighlight = -1;
    private boolean needsUpdate = true;
    private Handler handler;
    private CPlusPlusHighlighter highlighter;
    private Styler styler;
    private boolean highlightCode = true;
    private float prevScrollY = -1;
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
                        Snackbar.make(codeEdit, "Saved successfully", Snackbar.LENGTH_SHORT).show();

                    } catch (FileNotFoundException e) {
                        Snackbar.make(codeEdit, "File not found", Snackbar.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Snackbar.make(codeEdit, "Error while saving file", Snackbar.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.create_file:
                Intent createFileIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                createFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                createFileIntent.setType("*/*");
                createFileIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                if (createFileIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(createFileIntent, REQUEST_CREATE_FILE);
                }
                break;
            case R.id.highlight_code:
                highlightCode = !item.isChecked();
                if (!highlightCode) {
                    highlighter.parse(codeEdit.getText());
                } else {
                    Object spansToRemove[] = codeEdit.getText().getSpans(0, codeEdit.getLayout().getLineEnd(codeEdit.getLineCount() - 1), Object.class);
                    for (Object span : spansToRemove) {
                        if (span instanceof CharacterStyle)
                            codeEdit.getText().removeSpan(span);
                    }
                }
                item.setChecked(!item.isChecked());
        }
        return true;
    }
    private static final String[] COUNTRIES = new String[] {
            "Belgium", "France", "Italy", "Germany", "Spain"
    };
    private String symbols = "→;,.<>\"'={}&|!()+-*/[]#%^:_@?";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler(getMainLooper());
        setContentView(R.layout.activity_main_constraint);
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
        letters = findViewById(R.id.letters);
        for (int i = 0; i < symbols.length(); i++) {
            TextView letter = new TextView(this);
            int padding = (int)ConverterKt.dpToPx(5.0f, this);
            letter.setPadding(padding, padding, padding, padding);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(padding, padding, padding, padding);
            String letterS = Character.toString(symbols.charAt(i));
            letter.setTextSize(ConverterKt.spToPx(10, this));
            letter.setText(letterS);
            letter.setTextColor(ContextCompat.getColor(this,R.color.darkula_text));
            letter.setTypeface(ResourcesCompat.getFont(this, R.font.jetbrains_mono));
            letter.setLayoutParams(params);
            letter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = letterS.equals("→") ? "\t" : letterS;
                    codeEdit.getText().replace(codeEdit.getSelectionStart(), codeEdit.getSelectionEnd(), text);
                }
            });
            letters.addView(letter);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, COUNTRIES);
        codeEdit.setAdapter(adapter);
        codeEdit.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
//        codeEdit.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
//            @Override
//            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                //Log.d("Code editor", "Layout changed");
//
//                if (currentLineNumber != codeEdit.getLineCount() && shouldUpdate) {
//                    shouldUpdate = false;
//                    currentLineNumber = codeEdit.getLineCount();
//                    //Log.d("Code editor", "Numbers updated"+codeEdit.getLineCount());
////                    updateNumbersView();
//                    numbersView.update();
//
//                }
//            }
//        });
        styler = new GeneralStyler(codeEdit, highlighter,new GeneralColorScheme());
        //Log.d("process", "Hello");
        codeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                codeEdit.setCursorVisible(true);

            }
        });

        verticalScroll.getViewTreeObserver().addOnScrollChangedListener(() -> {
            Log.d("Scroll", verticalScroll.getScrollY() + "");
            int line;
            int startLine = codeEdit.getLayout().getLineForVertical((int) verticalScroll.getScrollY());
            int endLine = codeEdit.getLayout().getLineForVertical((int)verticalScroll.getScrollY() + verticalScroll.getHeight());
            int cursorLine = codeEdit.getLayout().getLineForOffset(codeEdit.getSelectionStart());
            if (prevScrollY > verticalScroll.getScrollY()) {
//                if (cursorLine > endLine - 2)
                    line = startLine + 2;
//                else line = -1;
            } else
                line = endLine - 2;
            if (line > 0 && line < codeEdit.getLineCount()) {
                int charNumber = codeEdit.getLayout().getLineStart(line);
//            int endCharNumber = codeEdit.getLayout().getLineEnd(line);
//            codeEdit.setCursorVisible(false);

                codeEdit.setSelection(charNumber);
            }
//            codeEdit.setFocusableInTouchMode(true);
            if (highlightCode)
                styler.updateStyling(verticalScroll.getScrollY(), verticalScroll.getHeight());
            prevScrollY = verticalScroll.getScrollY();
        });

        codeEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d("TextBefore", start+", "+count);
                TokenList tokens = highlighter.getTokens();
                TokenList.TokenNode iter = tokens.getHead();
                while (iter != null) {
                    Log.d("T", iter.getData().getType().toString());
                    iter = iter.getNext();
                    if (iter == tokens.getHead())
                        break;
                }
//                startHighlight  = ConverterKt.findCharBefore(s, Math.max(start - 1, 0), '\n');
//                    endHighlight = ConverterKt.findCharAfter(s, start+count - 1,'\n') + after + 1;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("TextOn", start+", "+count);
                highlighter.update();
//                needsUpdate = highlighter.checkNeedUpdate(s, start, start+count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (needsUpdate) {
                    needsUpdate = false;
                    codeEdit.getText().insert(1, "Test");
                }
//                    handler.post(() -> {

            long startTime = System.currentTimeMillis();

//            Object spansToRemove[] = s.getSpans(startHighlight, endHighlight, Object.class);
//            for (Object span : spansToRemove) {
//                if (span instanceof CharacterStyle)
//                    s.removeSpan(span);
//            }
//            Log.d("SpanUpdate", (System.currentTimeMillis() - startTime) / 1000.0 + "");
//            startTime = System.currentTimeMillis();
            Log.d("HightLightTime", (System.currentTimeMillis() - startTime) / 1000.0 + "");
//                    });
//                }

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
//                    //Log.d("URI result", fileURI.toString());
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
                    //Log.d("File read", "Time:" + difference/ 1000.0);

                    startTime = System.currentTimeMillis();
                    final String data1 = writer.toString();

                    difference = System.currentTimeMillis() - startTime;
                    //Log.d("File read", "Convert time:" + difference/ 1000.0);

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
                            highlighter.parse(codeEdit.getText());
                            //Log.d("File read", "Set time:" + difference/ 1000.0);

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
