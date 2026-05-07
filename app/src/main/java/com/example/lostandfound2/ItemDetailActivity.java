package com.example.lostandfound2;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ItemDetailActivity extends AppCompatActivity {
    private LostFoundDbHelper dbHelper;
    private LostItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        dbHelper = new LostFoundDbHelper(this);
        long itemId = getIntent().getLongExtra("item_id", -1L);
        if (itemId == -1L) {
            finish();
            return;
        }

        item = dbHelper.getItemById(itemId);
        if (item == null) {
            finish();
            return;
        }

        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvAge = findViewById(R.id.tvDetailAge);
        TextView tvLocation = findViewById(R.id.tvDetailLocation);
        TextView tvPhone = findViewById(R.id.tvDetailPhone);
        TextView tvDescription = findViewById(R.id.tvDetailDescription);
        TextView tvDateExact = findViewById(R.id.tvDetailDateExact);
        ImageView ivDetailImage = findViewById(R.id.ivDetailImage);
        MaterialButton btnRemove = findViewById(R.id.btnRemove);
        MaterialButton btnBack = findViewById(R.id.btnBackFromDetail);

        tvTitle.setText(item.getPostType() + " " + item.getName());
        tvAge.setText(getTimeAgo(item.getPostedAtMillis()));
        tvLocation.setText("At " + item.getLocation());
        tvPhone.setText("Phone: " + item.getPhone());
        tvDescription.setText(item.getDescription());
        tvDateExact.setText("Posted: " + formatDate(item.getPostedAtMillis()));

        File imageFile = new File(item.getImagePath());
        if (imageFile.exists()) {
            ivDetailImage.setImageBitmap(BitmapFactory.decodeFile(item.getImagePath()));
        }

        btnBack.setOnClickListener(v -> finish());
        btnRemove.setOnClickListener(v -> removeItem());
    }

    private void removeItem() {
        boolean deleted = dbHelper.deleteItem(item.getId());
        if (deleted) {
            File imageFile = new File(item.getImagePath());
            if (imageFile.exists()) {
                imageFile.delete();
            }
            Toast.makeText(this, "Removed", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Remove failed", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatDate(long millis) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(millis));
    }

    private String getTimeAgo(long postedMillis) {
        long diff = System.currentTimeMillis() - postedMillis;
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        if (days > 0) {
            return days + " days ago";
        }
        if (hours > 0) {
            return hours + " hours ago";
        }
        return "Just now";
    }
}

