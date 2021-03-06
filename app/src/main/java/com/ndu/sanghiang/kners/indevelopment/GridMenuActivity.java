package com.ndu.sanghiang.kners.indevelopment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ndu.sanghiang.kners.AboutActivity;
import com.ndu.sanghiang.kners.CodeMatchActivity;
import com.ndu.sanghiang.kners.DashboardProjectActivity;
import com.ndu.sanghiang.kners.R;
import com.ndu.sanghiang.kners.customlistview.ui.Main2Activity;
import com.ndu.sanghiang.kners.smartqap.inline.QcInlineActivity;

import java.util.Objects;

public class GridMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_manager);
        Toolbar tToolbar = findViewById(R.id.tToolbar);
        setSupportActionBar(tToolbar);
        //Menampilkan panah Back ←
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //gridview
        CustomGridViewActivity adapterViewAndroid = new CustomGridViewActivity(GridMenuActivity.this, gridViewString, gridViewImageId);
        androidGridView= findViewById(R.id.grid_view_image_text);
        androidGridView.setAdapter(adapterViewAndroid);
        androidGridView.setOnItemClickListener((parent, view, i, id) -> {
            //Toast.makeText(GridMenuActivity.this, "GridView Item: " + gridViewString[+i], Toast.LENGTH_SHORT).show();
            Intent myIntent = null;
            if(i == 0){
                myIntent = new Intent(view.getContext(), QcInlineActivity.class);
            }
            if(i == 1){
                myIntent = new Intent(view.getContext(), AboutActivity.class);
            }
            if(i == 2){
                myIntent = new Intent(view.getContext(), ProdukKnowledgeActivity.class);
            }
            if(i == 3){
                myIntent = new Intent(view.getContext(), CodeMatchActivity.class);
            }
            if(i == 4){
                myIntent = new Intent(view.getContext(), DashboardProjectActivity.class);
            }
            if(i == 5){
                myIntent = new Intent(view.getContext(), Main2Activity.class);
            }

            //lain2



            startActivity(myIntent);
        });
    }

    //gridview
    GridView androidGridView;

    String[] gridViewString = {
            "Admin", "MS Plant", "Quality Assurance", "Produksi", "Dashboard Project", "List View Test"

    };
    int[] gridViewImageId = {
            R.drawable.ic_launcher,
            R.drawable.ic_info_outline_black_24dp,
            R.drawable.ic_help_outline_black_24dp,
            R.drawable.ic_settings_white_24dp,
            R.drawable.ic_launcher_round,
            R.drawable.ic_settings_white_24dp

    };

}