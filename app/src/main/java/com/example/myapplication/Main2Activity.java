package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Main2Activity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_GET = 1;
    private EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        editText = findViewById(R.id.editText1);
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.wrap_content:
//                word_wrap = !item.isChecked();
//                if (word_wrap) {
//                    wrapScroll.removeView(codeEdit);
//                    mainLayout.removeView(wrapScroll);
//                    mainLayout.addView(codeEdit);
//                } else {
//                    mainLayout.removeView(codeEdit);
//                    mainLayout.addView(wrapScroll);
//                    wrapScroll.addView(codeEdit);
//                }
//                shouldUpdate = true;
//                codeEdit.setVisibility(View.INVISIBLE);
//                codeEdit.setVisibility(View.VISIBLE);
                item.setChecked(!item.isChecked());
                break;
            case R.id.open_file:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Main2Activity.this,
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            final Uri fileURI = data.getData();
            editText.setText("Loading...");
//            final Uri canonized = getContentResolver().canonicalize(fileURI);
//            progressBar.setVisibility(View.VISIBLE);
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
                            editText.setText(data);
                            Log.d("File read", "Set time:" + difference/ 1000.0);

//                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }


            });
            readFIle.start();

//            txt.setText(buf.toString());

            // Do work with photo saved at fullPhotoUri
        }
    }
}
