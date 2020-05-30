package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.preference.PreferenceManager;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Files.FileIO;
import com.example.myapplication.SyntaxHighlight.CPlusPlusHighlighter;
import com.example.myapplication.SyntaxHighlight.Styler.GeneralColorScheme;
import com.example.myapplication.SyntaxHighlight.Styler.GeneralStyler;
import com.example.myapplication.SyntaxHighlight.Styler.Styler;
import com.example.myapplication.room.entities.TabData;
import com.example.myapplication.adapters.TabsAdapter;
import com.example.myapplication.databinding.ActivityMainConstraintBinding;
import com.example.myapplication.history.FileHistory;
import com.example.myapplication.settings.SettingsData;
import com.example.myapplication.utils.ConverterKt;
import com.example.myapplication.views.FastScroll;
import com.example.myapplication.views.NumbersView;
import com.example.myapplication.views.ScrollViewFlingCallback;
import com.example.myapplication.views.SuggestionsTextView;
import com.google.android.material.snackbar.Snackbar;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import static com.example.myapplication.utils.ConverterKt.spToPx;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    static final int REQUEST_OPEN_FILE = 1;
    private static final int REQUEST_CREATE_FILE = 2;
    private ActivityMainConstraintBinding binding;
    private Uri currentlyOpenedFile = null;
    boolean word_wrap = false;
    private LinearLayout letters;
    private ProgressBar progressBar;
    private SuggestionsTextView codeEdit;
    private NumbersView numbersView;
    private LinearLayout mainLayout;
    private boolean isFling = false;
    private HorizontalScrollView wrapScroll;
    private ScrollViewFlingCallback verticalScroll;
    private FastScroll fastScroll;
    private ConstraintLayout globalLayout;
    private SeekBar seekBar;
    private FileIO fileIO;
    private int currentLineNumber = -1;
    private boolean shouldUpdate = true;
    private int startHighlight = -1;
    private int endHighlight = -1;
    private String recentlyEnteredString = "";
    private boolean needsUpdate = true;
    private Handler handler;
    private SettingsData settingsData;
    private CPlusPlusHighlighter highlighter;
    private Styler styler;
    private boolean highlightCode = true;
    private float prevScrollY = -1;
    private boolean newDataSet = false;
    private FileHistory history = new FileHistory();
    private ArrayList<String> settingsChange = new ArrayList<>();
    private MenuItem undoItem;
    private MenuItem redoItem;
    private RecyclerView tabs;
    private TabsAdapter adapter;

    public int countChar(String str, char c) {
        int count = 0;

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c)
                count++;
        }

        return count;
    }



    private String symbols = "→;,.<>\"'={}&|!()+-*/[]#%^:_@?";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainConstraintBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        codeEdit = binding.codeEditor;
        numbersView = binding.numbersView;
        numbersView.initializeView(codeEdit);
        wrapScroll = binding.wrapHorizontalScroll;
        mainLayout = binding.mainLayout;
        progressBar = binding.progressBar;
        highlighter = new CPlusPlusHighlighter(this);
        fastScroll = binding.fastScroll;
        verticalScroll = binding.verticalScroll;
        fastScroll.initialize(codeEdit, verticalScroll);
        letters = binding.letters;
        globalLayout = binding.globalLayout;
        tabs = binding.tabs;
        tabs.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new TabsAdapter();
//        adapter.getTabsNames().add("Test.js");
//        ada pter.getTabsNames().add("Untitled.js");
        adapter.addOnItemListener(new TabsAdapter.OnItemChange() {
            @Override
            public void onItemClosed(int position) {

            }

            @Override
            public void onItemActive(int previousPosition, int position) {
                if (position == previousPosition)
                    return;
                TabData data = adapter.get(position);
                if (data.getFileUri() != null)
                    openFile(data.getFileName(), data.getFileUri());
                else
                    codeEdit.updateText("");
            }
        });
        tabs.setAdapter(adapter);

        fileIO = new FileIO(this);
        history.addChangeOccuredListener(new FileHistory.ChangeOccured() {
            @Override
            public void onInsertHappen() {

            }

            @Override
            public void onChange(boolean undoAvailable, boolean redoAvailable) {
                if (undoItem != null)
                    menuItemChangeState(undoItem, undoAvailable);
                if (redoItem != null)
                    menuItemChangeState(redoItem, redoAvailable);
            }
        });
        SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(this);
        settingsData = new SettingsData(this);
        settingsData.fromSharedPreferenses(pref);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

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
            letter.setTextSize(spToPx(8, this));
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
        styler = new GeneralStyler(codeEdit, highlighter,new GeneralColorScheme());
        codeEdit.initialize(highlighter, verticalScroll,settingsData, styler, history);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        undoItem = menu.findItem(R.id.undo);
        redoItem = menu.findItem(R.id.redo);

        menuItemChangeState(undoItem, false);
        menuItemChangeState(redoItem, false);

        return true;
    }

    public void menuItemChangeState(MenuItem item, boolean enabled) {
        Drawable d = item.getIcon();
        d = d.mutate();
        d = DrawableCompat.wrap(d);
        item.setEnabled(enabled);
        if (enabled)
            DrawableCompat.setTint(d, getResources().getColor(R.color.itemActive));
        else
            DrawableCompat.setTint(d, getResources().getColor(R.color.itemNotActive));
        item.setIcon(d);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.undo:
               history.undo(codeEdit.getText());
               break;
            case R.id.redo:
                history.redo(codeEdit.getText());
                break;
//                d.setTint();
//            case R.id.wrap_content:
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
//                item.setChecked(!item.isChecked());
//                break;
//                break;
            case R.id.open_file:
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                intent.setType("*/*");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_OPEN_FILE);
                }
                break;
            case R.id.save_file:
                if (currentlyOpenedFile != null) {
                    try {
                        fileIO.saveFile(currentlyOpenedFile, codeEdit.getText().toString());
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
//            case R.id.highlight_code:
//                highlightCode = !item.isChecked();
//                if (!highlightCode) {
//                    highlighter.parse(codeEdit.getText());
//                } else {
//                    Object spansToRemove[] = codeEdit.getText().getSpans(0, codeEdit.getLayout().getLineEnd(codeEdit.getLineCount() - 1), Object.class);
//                    for (Object span : spansToRemove) {
//                        if (span instanceof CharacterStyle)
//                            codeEdit.getText().removeSpan(span);
//                    }
//                }
//                item.setChecked(!item.isChecked());
//                break;
            case R.id.settings_action:
                Intent settingIntent = new Intent(this , SettingsActivity.class);
                startActivity(settingIntent);
                break;
        }
        return true;
    }
    private static final String[] COUNTRIES = new String[] {
            "Belgium", "France", "Italy", "Germany", "Spain"
    };


    private void openFile(String fileName, Uri fileURI) {
        progressBar.setVisibility(View.VISIBLE);
        Thread readFile = new Thread(() -> {
            try {
                String data1 = fileIO.openFile(fileURI);
                runOnUiThread(() -> {
                    newDataSet = true;
                    codeEdit.updateText(data1);
                    progressBar.setVisibility(View.INVISIBLE);
                });
            } catch (FileNotFoundException e) {
                Snackbar.make(codeEdit, "File not found", Snackbar.LENGTH_SHORT).show();
            } catch (IOException e) {
                Snackbar.make(codeEdit, "Error while opening file", Snackbar.LENGTH_SHORT).show();
            }

        });
        readFile.start();
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
        adapter.addTab(new TabData(fileName, fileURI));
        adapter.setActive(adapter.getItemCount() - 1);
        getSupportActionBar().setSubtitle(fileName);
        if (requestCode == REQUEST_OPEN_FILE ) {
            openFile(fileName, fileURI);
        } else if (requestCode == REQUEST_CREATE_FILE) {
            codeEdit.setText("");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);

    }


    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        for (String key: settingsChange) {
            settingsData.updateValue(key, sharedPreferences);
        }
        settingsChange.clear();
//        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    public void changeWordWrap(boolean value) {
        if (value) {
            wrapScroll.removeView(codeEdit);
            mainLayout.removeView(wrapScroll);
            mainLayout.removeView(codeEdit);
            mainLayout.addView(codeEdit);
        } else {
            wrapScroll.removeView(codeEdit);
            mainLayout.removeView(codeEdit);
            mainLayout.removeView(wrapScroll);
            mainLayout.addView(wrapScroll);
            wrapScroll.addView(codeEdit);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        settingsChange.add(key);
    }

    public SuggestionsTextView getCodeEdit() {
        return codeEdit;
    }

    public LinearLayout getMainLayout() {
        return mainLayout;
    }

    public HorizontalScrollView getWrapScroll() {
        return wrapScroll;
    }


    public NumbersView getNumbersView() {
        return numbersView;
    }
}
