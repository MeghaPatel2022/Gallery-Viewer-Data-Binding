package gallery.photoapp.gallerypro.photoviewer;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import gallery.photoapp.gallerypro.photoviewer.Pref.SharedPrefrance;
import gallery.photoapp.gallerypro.photoviewer.adapter.FavouriteAdapter;
import gallery.photoapp.gallerypro.photoviewer.adapter.FavouriteListAdapter;
import gallery.photoapp.gallerypro.photoviewer.constant.Util;
import gallery.photoapp.gallerypro.photoviewer.databinding.ActivityFavouriteBinding;

public class FavouriteActivity extends AppCompatActivity {

    public ArrayList<String> pathList = new ArrayList<>();
    ActivityFavouriteBinding favouriteBinding;
    MyClickHandlers myClickHandlers;
    FavouriteAdapter favouriteAdapter;
    FavouriteListAdapter favouriteListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        favouriteBinding = DataBindingUtil.setContentView(FavouriteActivity.this, R.layout.activity_favourite);
        myClickHandlers = new MyClickHandlers(FavouriteActivity.this);
        favouriteBinding.setOnBackListener(myClickHandlers);
    }

    @Override
    protected void onResume() {
        super.onResume();
        pathList = SharedPrefrance.getFavouriteFileList(FavouriteActivity.this);

        if (pathList.size() == 0) {
            favouriteBinding.llNoData.setVisibility(View.VISIBLE);
        } else {
            favouriteBinding.llNoData.setVisibility(View.GONE);
        }

        if (!Util.isList) {
            final GridLayoutManager layoutManager = new GridLayoutManager(FavouriteActivity.this, Util.COLUMN_TYPE);
            favouriteBinding.rvImages.setLayoutManager(layoutManager);
            favouriteAdapter = new FavouriteAdapter(pathList, FavouriteActivity.this);
            favouriteBinding.rvImages.setAdapter(favouriteAdapter);
        } else {
            final LinearLayoutManager layoutManager = new LinearLayoutManager(FavouriteActivity.this, RecyclerView.VERTICAL, false);
            favouriteBinding.rvImages.setLayoutManager(layoutManager);
            favouriteListAdapter = new FavouriteListAdapter(pathList, FavouriteActivity.this);
            favouriteBinding.rvImages.setAdapter(favouriteListAdapter);
        }
    }

    public class MyClickHandlers {
        Context context;

        public MyClickHandlers(Context context) {
            this.context = context;
        }

        public void onBackListener(View view) {
            onBackPressed();
        }
    }

}