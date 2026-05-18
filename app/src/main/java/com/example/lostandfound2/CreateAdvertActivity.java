package com.example.lostandfound2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateAdvertActivity extends AppCompatActivity {
    private EditText etName;
    private EditText etPhone;
    private EditText etDescription;
    private EditText etLocation;
    private Spinner spinnerCategory;
    private RadioGroup radioGroupType;
    private ImageView ivPreview;

    private LostFoundDbHelper dbHelper;
    private FusedLocationProviderClient fusedLocationClient;

    private String selectedImagePath = null;
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;

    private ActivityResultLauncher<String> imagePickerLauncher;
    private ActivityResultLauncher<Intent> autocompleteLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_advert);

        dbHelper = new LostFoundDbHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etDescription = findViewById(R.id.etDescription);
        etLocation = findViewById(R.id.etLocation);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        radioGroupType = findViewById(R.id.radioGroupType);
        ivPreview = findViewById(R.id.ivPreview);
        TextView tvDateValue = findViewById(R.id.tvDateValue);

        MaterialButton btnChooseImage = findViewById(R.id.btnChooseImage);
        MaterialButton btnGetCurrentLocation = findViewById(R.id.btnGetCurrentLocation);
        MaterialButton btnSave = findViewById(R.id.btnSave);
        MaterialButton btnBack = findViewById(R.id.btnBackFromCreate);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        String[] categories = {"Electronics", "Pets", "Wallets", "Documents", "Others"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        tvDateValue.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()));

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri == null) return;
            selectedImagePath = copyImageToInternalStorage(uri);
            ivPreview.setImageURI(Uri.fromFile(new File(selectedImagePath)));
        });

        autocompleteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK || result.getData() == null) return;
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    selectedLatitude = place.getLatLng().latitude;
                    selectedLongitude = place.getLatLng().longitude;
                    etLocation.setText(place.getAddress());
                }
        );

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean fine = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                    Boolean coarse = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                    if (Boolean.TRUE.equals(fine) || Boolean.TRUE.equals(coarse)) {
                        getCurrentLocation();
                    } else {
                        Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        etLocation.setOnClickListener(v -> openAutocomplete());
        btnChooseImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnGetCurrentLocation.setOnClickListener(v -> requestCurrentLocation());
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> savePost());
    }

    private void openAutocomplete() {
        List<Place.Field> fields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this);
        autocompleteLauncher.launch(intent);
    }

    private void requestCurrentLocation() {
        boolean hasPermission =
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (hasPermission) {
            getCurrentLocation();
        } else {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void getCurrentLocation() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location == null) return;
                    selectedLatitude = location.getLatitude();
                    selectedLongitude = location.getLongitude();
                    etLocation.setText(toAddress(location));
                });
    }

    private String toAddress(Location location) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> list = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (list != null && !list.isEmpty() && list.get(0).getAddressLine(0) != null) {
                return list.get(0).getAddressLine(0);
            }
        } catch (Exception ignored) {
        }
        return String.format(Locale.getDefault(), "Lat %.6f, Lng %.6f", location.getLatitude(), location.getLongitude());
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
        if (selectedLatitude == 0.0 && selectedLongitude == 0.0) {
            Toast.makeText(this, "Please choose location", Toast.LENGTH_SHORT).show();
            return;
        }

        LostItem item = new LostItem(
                0L,
                postType,
                name,
                phone,
                description,
                category,
                location,
                selectedImagePath,
                System.currentTimeMillis(),
                selectedLatitude,
                selectedLongitude
        );

        dbHelper.insertItem(item);
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, ItemListActivity.class));
        finish();
    }

    private String copyImageToInternalStorage(Uri uri) {
        try {
            InputStream input = getContentResolver().openInputStream(uri);
            File outputFile = new File(getFilesDir(), "lf_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream output = new FileOutputStream(outputFile);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = input.read(buffer)) > 0) {
                output.write(buffer, 0, len);
            }
            input.close();
            output.close();
            return outputFile.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }
}

