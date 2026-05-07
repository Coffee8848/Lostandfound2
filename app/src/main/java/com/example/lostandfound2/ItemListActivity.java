package com.example.lostandfound2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ItemListActivity extends AppCompatActivity {
    private Spinner spinnerFilter;
    private ListView listViewItems;
    private LostFoundDbHelper dbHelper;
    private LostItemAdapter itemAdapter;
    private final List<LostItem> currentItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        dbHelper = new LostFoundDbHelper(this);
        spinnerFilter = findViewById(R.id.spinnerFilterCategory);
        listViewItems = findViewById(R.id.listViewItems);
        MaterialButton btnBack = findViewById(R.id.btnBackFromList);

        String[] filterOptions = {"All", "Electronics", "Pets", "Wallets", "Documents", "Others"};
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filterOptions);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);

        itemAdapter = new LostItemAdapter(this, currentItems);
        listViewItems.setAdapter(itemAdapter);

        spinnerFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                loadItems();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        listViewItems.setOnItemClickListener((parent, view, position, id) -> {
            LostItem item = currentItems.get(position);
            Intent intent = new Intent(ItemListActivity.this, ItemDetailActivity.class);
            intent.putExtra("item_id", item.getId());
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void loadItems() {
        String filter = spinnerFilter.getSelectedItem().toString();
        List<LostItem> result = dbHelper.getItems(filter);
        currentItems.clear();
        currentItems.addAll(result);
        itemAdapter.setItemList(currentItems);
    }
}

