package com.example.lostandfound2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateAdvertActivity extends AppCompatActivity {
    private EditText etName;
    private EditText etPhone;
    private EditText etDescription;
    private EditText etLocation;
    private Spinner spinnerCategory;
    private TextView tvDateValue;
    private ImageView ivPreview;
    private RadioGroup radioGroupType;

    private LostFoundDbHelper dbHelper;
    private String selectedImagePath = null;
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_advert);

        dbHelper = new LostFoundDbHelper(this);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etDescription = findViewById(R.id.etDescription);
        etLocation = findViewById(R.id.etLocation);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        tvDateValue = findViewById(R.id.tvDateValue);
        ivPreview = findViewById(R.id.ivPreview);
        radioGroupType = findViewById(R.id.radioGroupType);
        MaterialButton btnChooseImage = findViewById(R.id.btnChooseImage);
        MaterialButton btnSave = findViewById(R.id.btnSave);
        MaterialButton btnBack = findViewById(R.id.btnBackFromCreate);

        String[] categories = {"Electronics", "Pets", "Wallets", "Documents", "Others"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        tvDateValue.setText(formatDate(System.currentTimeMillis()));

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                String copiedPath = copyImageToInternalStorage(uri);
                if (copiedPath != null) {
                    selectedImagePath = copiedPath;
                    ivPreview.setImageURI(Uri.fromFile(new File(copiedPath)));
                } else {
                    Toast.makeText(this, "Image load failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnChooseImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> savePost());
    }

    private void savePost() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String postType = radioGroupType.getCheckedRadioButtonId() == R.id.rbFound ? "Found" : "Lost";

        if (name.isEmpty() || phone.isEmpty() || description.isEmpty() || location.isEmpty() || selectedImagePath == null) {
            Toast.makeText(this, "Please fill all fields and choose image", Toast.LENGTH_SHORT).show();
            return;
        }

        long postedAtMillis = System.currentTimeMillis();

        LostItem item = new LostItem(
                0L,
                postType,
                name,
                phone,
                description,
                category,
                location,
                selectedImagePath,
                postedAtMillis
        );

        long insertedId = dbHelper.insertItem(item);
        if (insertedId > 0) {
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ItemListActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
        }
    }

    private String copyImageToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }
            File outputFile = new File(getFilesDir(), "lf_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
            inputStream.close();
            outputStream.close();
            return outputFile.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    private String formatDate(long millis) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(millis));
    }
}
