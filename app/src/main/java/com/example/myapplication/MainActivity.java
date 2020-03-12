package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.views.AutoCompleteTextViewWithNumbers;
import com.example.myapplication.views.FastScroll;
import com.example.myapplication.views.NumbersView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_GET = 1;
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

//    public void updateNumbersView() {
//        int lineNumber = 1;
//        Layout codeLayout = codeEdit.getLayout();
//        CharSequence text = codeEdit.getText();
//        StringBuilder numberBuilder = new StringBuilder();
//        int lineStart = 0;
//        int linesCount = codeLayout.getLineCount();
//        for (int i = 0; i < linesCount; i++) {
//            if (word_wrap) {
//                if (i == lineStart) {
//                    numberBuilder.append(lineNumber).append('\n');
//                    lineNumber++;
//                } else
//                    numberBuilder.append('\n');
//                if (i == linesCount - 1 || text.charAt(codeLayout.getLineEnd(i) - 1) == '\n' )
//                    lineStart = i + 1;
//            } else {
//                numberBuilder.append(i+1).append('\n');
//            }
//        }
//        numbersView.setText(numberBuilder.toString());
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

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
//                codeEdit.setVisibility(View.INVISIBLE);
//                codeEdit.setVisibility(View.VISIBLE);
                item.setChecked(!item.isChecked());
                break;
            case R.id.open_file:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                    return true;
                }
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_IMAGE_GET);
                }
                break;

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

//        codeEdit.initPaints();
//        codeEdit.setMovementMethod(new ScrollingMovementMethod());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, COUNTRIES);
//        MultiAutoCompleteTextView textView = findViewById(R.id.);
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

//        try{
//            Process su = Runtime.getRuntime().exec("ls /storage/self/primary");
//            BufferedReader errorReader = new BufferedReader(
//                    new InputStreamReader(su.getErrorStream()));
//            BufferedReader reader = new BufferedReader(
//                    new InputStreamReader(su.getInputStream()));
//
//            int read;
//            char[] buffer = new char[4096];
//            StringBuffer output = new StringBuffer();
//            StringBuffer err = new StringBuffer();
//            while ((read = errorReader.read(buffer)) > 0) {
//                err.append(buffer, 0, read);
//            }
//            while ((read = reader.read(buffer)) > 0) {
//                output.append(buffer, 0, read);
//            }
//            reader.close();
//            Log.d("process", output.toString());
//            Log.d("process", err.toString());
//
////            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
////            outputStream.writeBytes("exit\n");
////            outputStream.flush();
//            su.waitFor();
//        }catch(IOException e){
//           e.printStackTrace();
//        }catch(InterruptedException e){
////            throw new Exception(e);
//            e.printStackTrace();
//        }
//        numbersView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d("numbers on click", "click happend");
//
//            }
//        });
        codeEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                String changedText = s.subSequence(start, start + count).toString();
//                Log.d("beforeTextChanged", changedText);
//                int newLines = countChar(changedText, '\n');
//                Log.d("beforeTextChanged", "removed " + newLines + " new lines");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                String changedText = s.subSequence(start, start + count).toString();
//                Log.d("onTextChanged", changedText);
//                int newLines = countChar(changedText, '\n');
//                Log.d("onTextChanged", "added " + newLines + " new lines");
            }

            @Override
            public void afterTextChanged(Editable s) {
                Object spansToRemove[] = s.getSpans(0, s.length(), Object.class);
                for(Object span: spansToRemove){
                    if(span instanceof CharacterStyle)
                        s.removeSpan(span);
                }
//                Pattern.compile("\\\\")
                highlighter.hightliht(s);
//                numbersView.invalidate();

//                String data = s.toString();
                shouldUpdate = true;
//                Log.d("Height", codeEdit.getHeight() + " " + verticalScroll.getHeight());
//                int index = data.indexOf("int");
//                while (index >= 0) {
//                    s.setSpan(new ForegroundColorSpan(Color.RED), index, index+3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
////                    System.out.println(index);
//                    index = data.indexOf("int", index + 1);
//                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            final Uri fileURI = data.getData();
//            final Uri canonized = getContentResolver().canonicalize(fileURI);
            progressBar.setVisibility(View.VISIBLE);
            Thread readFIle = new Thread(new Runnable() {
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
            readFIle.start();

//            txt.setText(buf.toString());

            // Do work with photo saved at fullPhotoUri
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case 1: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    // permission was granted, yay! Do the
//                    // contacts-related task you need to do.
//                } else {
//
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
//                }
//                return;
//            }
//
//            // other 'case' lines to check for other
//            // permissions this app might request
//        }
//    }
}
