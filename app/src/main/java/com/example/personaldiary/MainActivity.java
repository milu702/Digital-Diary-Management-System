```java
package com.example.personaldiary;

import android.app.AlertDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private LinearLayout entriesContainer;
    private EditText searchBox;
    private TextView countText;

    private String currentSearch = "";

    private final int purple = Color.rgb(118, 85, 181);
    private final int dark = Color.rgb(48, 39, 71);
    private final int grey = Color.rgb(121, 114, 134);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        entriesContainer = findViewById(R.id.entriesContainer);
        searchBox = findViewById(R.id.searchBox);
        countText = findViewById(R.id.countText);

        Button addButton = findViewById(R.id.addButton);

        addButton.setOnClickListener(v ->
                showEntryDialog(-1, "", ""));

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                currentSearch = s.toString().trim();
                loadEntries(currentSearch);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        loadEntries("");
    }

    private String getCurrentDate() {
        return new SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                Locale.getDefault()
        ).format(new Date());
    }

    private GradientDrawable makeBackground(int color) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(20 * getResources()
                .getDisplayMetrics().density);
        return shape;
    }

    private TextView createText(
            String value, int size, int color, boolean bold) {

        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);

        if (bold) {
            text.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }

        return text;
    }

    private void loadEntries(String keyword) {
        entriesContainer.removeAllViews();

        Cursor cursor;

        if (keyword.isEmpty()) {
            cursor = dbHelper.getAllEntries();
        } else {
            cursor = dbHelper.searchEntries(keyword);
        }

        int count = 0;

        try {
            while (cursor != null && cursor.moveToNext()) {
                count++;

                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow("id"));

                String title = cursor.getString(
                        cursor.getColumnIndexOrThrow("title"));

                String content = cursor.getString(
                        cursor.getColumnIndexOrThrow("content"));

                String date = cursor.getString(
                        cursor.getColumnIndexOrThrow("date"));

                entriesContainer.addView(
                        createEntryCard(id, title, content, date));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        countText.setText(count + (
                count == 1
                        ? " diary entry found"
                        : " diary entries found"
        ));

        if (count == 0) {
            TextView empty = createText(
                    keyword.isEmpty()
                            ? "✍️\n\nNo diary entries yet.\nTap the button above to write your first entry!"
                            : "No matching entries found.",
                    16,
                    grey,
                    false
            );

            empty.setGravity(Gravity.CENTER);
            empty.setPadding(20, 35, 20, 35);

            entriesContainer.addView(empty);
        }
    }

    private View createEntryCard(
            int id, String title, String content, String date) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(18, 18, 18, 18);
        card.setBackground(makeBackground(Color.WHITE));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin = 14;
        card.setLayoutParams(cardParams);
        card.setElevation(3);

        TextView titleView = createText(title, 19, dark, true);
        card.addView(titleView);

        TextView dateView = createText(
                "📅 " + date, 12, grey, false);

        LinearLayout.LayoutParams dateParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        dateParams.topMargin = 8;
        dateParams.bottomMargin = 12;
        card.addView(dateView, dateParams);

        TextView contentView = createText(
                content, 15, Color.DKGRAY, false);

        contentView.setMaxLines(5);
        card.addView(contentView);

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setGravity(Gravity.END);

        Button editButton = new Button(this);
        editButton.setText("Edit");
        editButton.setAllCaps(false);
        editButton.setTextColor(purple);

        Button deleteButton = new Button(this);
        deleteButton.setText("Delete");
        deleteButton.setAllCaps(false);
        deleteButton.setTextColor(Color.rgb(190, 55, 75));

        buttons.addView(editButton);
        buttons.addView(deleteButton);

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        buttonParams.topMargin = 10;
        card.addView(buttons, buttonParams);

        editButton.setOnClickListener(v ->
                showEntryDialog(id, title, content));

        deleteButton.setOnClickListener(v ->
                confirmDelete(id));

        card.setOnClickListener(v ->
                showEntryDialog(id, title, content));

        return card;
    }

    private void showEntryDialog(
            int id, String oldTitle, String oldContent) {

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(24, 8, 24, 8);

        EditText titleInput = new EditText(this);
        titleInput.setHint("Enter diary title");
        titleInput.setSingleLine(true);
        titleInput.setText(oldTitle);
        form.addView(titleInput);

        EditText contentInput = new EditText(this);
        contentInput.setHint("Write your thoughts here...");
        contentInput.setText(oldContent);
        contentInput.setGravity(Gravity.TOP | Gravity.START);
        contentInput.setMinLines(5);
        form.addView(contentInput);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(id == -1
                        ? "New Diary Entry"
                        : "Edit Diary Entry")
                .setView(form)
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton(
                        id == -1 ? "Save" : "Update", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button saveButton =
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            saveButton.setOnClickListener(v -> {
                String title =
                        titleInput.getText().toString().trim();

                String content =
                        contentInput.getText().toString().trim();

                if (title.isEmpty()) {
                    titleInput.setError("Enter a title");
                    titleInput.requestFocus();
                    return;
                }

                if (content.isEmpty()) {
                    contentInput.setError("Write something");
                    contentInput.requestFocus();
                    return;
                }

                boolean success;

                if (id == -1) {
                    success = dbHelper.addEntry(
                            title, content, getCurrentDate());
                } else {
                    success = dbHelper.updateEntry(
                            id, title, content, getCurrentDate());
                }

                if (success) {
                    Toast.makeText(
                            MainActivity.this,
                            id == -1
                                    ? "Entry saved successfully"
                                    : "Entry updated successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    dialog.dismiss();
                    loadEntries(currentSearch);
                } else {
                    Toast.makeText(
                            MainActivity.this,
                            "Could not save entry",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        });

        dialog.show();
    }

    private void confirmDelete(int id) {
        new AlertDialog.Builder(this)
                .setTitle("Delete diary entry?")
                .setMessage(
                        "Are you sure you want to delete this entry?")
                .setNegativeButton("Cancel", (dialog, which) ->
                        dialog.dismiss())
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean deleted = dbHelper.deleteEntry(id);

                    if (deleted) {
                        Toast.makeText(
                                this,
                                "Entry deleted",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadEntries(currentSearch);
                    } else {
                        Toast.makeText(
                                this,
                                "Could not delete entry",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
```
