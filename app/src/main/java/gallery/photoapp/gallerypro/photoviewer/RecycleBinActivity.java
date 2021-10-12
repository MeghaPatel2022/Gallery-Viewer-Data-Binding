package gallery.photoapp.gallerypro.photoviewer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import gallery.photoapp.gallerypro.photoviewer.Pref.SharedPrefrance;
import gallery.photoapp.gallerypro.photoviewer.adapter.RecycleBinAdapter;
import gallery.photoapp.gallerypro.photoviewer.constant.Util;
import gallery.photoapp.gallerypro.photoviewer.databinding.ActivityRecycleBinBinding;
import gallery.photoapp.gallerypro.photoviewer.model.RecycleModel;

public class RecycleBinActivity extends AppCompatActivity {

    public ArrayList<RecycleModel> pathList = new ArrayList<>();
    ActivityRecycleBinBinding recycleBinBinding;
    private RecycleBinAdapter recycleBinAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recycleBinBinding = DataBindingUtil.setContentView(RecycleBinActivity.this, R.layout.activity_recycle_bin);

    }

    @Override
    protected void onResume() {
        super.onResume();
        pathList = SharedPrefrance.getRecycleBinData(RecycleBinActivity.this);
        Log.e("LLL_size: ", String.valueOf(pathList.size()));

        if (pathList.size() == 0) {
            recycleBinBinding.llNoData.setVisibility(View.VISIBLE);
        } else {
            recycleBinBinding.llNoData.setVisibility(View.GONE);
        }

        if (!Util.isList) {
            final GridLayoutManager layoutManager = new GridLayoutManager(RecycleBinActivity.this, Util.COLUMN_TYPE);
            recycleBinBinding.rvImages.setLayoutManager(layoutManager);
        } else {
            final LinearLayoutManager layoutManager = new LinearLayoutManager(RecycleBinActivity.this, RecyclerView.VERTICAL, false);
            recycleBinBinding.rvImages.setLayoutManager(layoutManager);
        }
        recycleBinAdapter = new RecycleBinAdapter(pathList, RecycleBinActivity.this);
        recycleBinBinding.rvImages.setAdapter(recycleBinAdapter);

        recycleBinBinding.imgBack.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(RecycleBinActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}