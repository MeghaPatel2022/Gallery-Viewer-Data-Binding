package gallery.photoapp.gallerypro.photoviewer;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

import gallery.photoapp.gallerypro.photoviewer.adapter.AlbumChildAdapter;
import gallery.photoapp.gallerypro.photoviewer.adapter.AlbumChildListAdapter;
import gallery.photoapp.gallerypro.photoviewer.constant.ConnectionDetector;
import gallery.photoapp.gallerypro.photoviewer.constant.Util;
import gallery.photoapp.gallerypro.photoviewer.databinding.ActivityAlbumBinding;
import gallery.photoapp.gallerypro.photoviewer.model.Directory;
import gallery.photoapp.gallerypro.photoviewer.model.ImageFile;
import pub.devrel.easypermissions.EasyPermissions;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.MediaColumns.BUCKET_DISPLAY_NAME;
import static android.provider.MediaStore.MediaColumns.BUCKET_ID;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;
import static android.provider.MediaStore.MediaColumns.ORIENTATION;
import static android.provider.MediaStore.MediaColumns.SIZE;
import static android.provider.MediaStore.MediaColumns.TITLE;

public class AlbumActivity extends AppCompatActivity {

    private static final String TAG = AlbumActivity.class.getSimpleName();

    ActivityAlbumBinding albumBinding;
    private boolean isInternetPresent;

    private List<ImageFile> imageFiles = new ArrayList<>();
    private AlbumChildAdapter albumChildAdapter;
    private AlbumChildListAdapter albumChildListAdapter;

    private AdView adView;
    private FirebaseAnalytics mFirebaseAnalytics;

    private void fireAnalyticsAds(String arg1, String arg2) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, arg1);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, arg2);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        albumBinding = DataBindingUtil.setContentView(AlbumActivity.this, R.layout.activity_album);
        albumBinding.setDirName(getIntent().getStringExtra("DirName"));

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(AlbumActivity.this);

        ConnectionDetector cd = new ConnectionDetector(AlbumActivity.this);
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Step 1 - Create an AdView and set the ad unit ID on it.
                    adView = new AdView(AlbumActivity.this);
                    fireAnalyticsAds("admob_banner", "Ad Request send");
                    adView.setAdUnitId(getString(R.string.banner_ad));
                    adView.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            super.onAdLoaded();
                            fireAnalyticsAds("admob_banner", "loaded");
                        }

                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            super.onAdFailedToLoad(loadAdError);
                            if (loadAdError.getMessage() != null)
                                fireAnalyticsAds("admob_banner_Error", loadAdError.getMessage());
                        }
                    });
                    albumBinding.bannerContainer.addView(adView);
                    loadBanner();
                }
            }, 2000);
        }

        albumBinding.imgBack.setOnClickListener(v -> onBackPressed());
    }

    private void loadBanner() {
        // Create an ad request. Check your logcat output for the hashed device ID
        // to get test ads on a physical device, e.g.,
        // "Use AdRequest.Builder.addTestDevice("ABCDE0123") to get test ads on this
        // device."
        AdRequest adRequest =
                new AdRequest.Builder().build();

        AdSize adSize = AdSize.SMART_BANNER;
        // Step 4 - Set the adaptive ad size on the ad view.
        adView.setAdSize(adSize);

        // Step 5 - Start loading the ad in the background.

        adView.loadAd(adRequest);
    }


    @Override
    protected void onResume() {
        super.onResume();
        boolean isGranted = EasyPermissions.hasPermissions(AlbumActivity.this, "android.permission.READ_EXTERNAL_STORAGE");
        if (isGranted)
            new LoadImages(AlbumActivity.this).execute();
    }

    void startAnim() {
        albumBinding.rlLoading.setVisibility(View.VISIBLE);
    }

    void stopAnim() {
        albumBinding.rlLoading.setVisibility(View.GONE);
    }


    class LoadImages extends AsyncTask<Void, Void, List<Directory<ImageFile>>> {

        @SuppressLint("StaticFieldLeak")
        FragmentActivity fragmentActivity;
        List<ImageFile> list1 = new ArrayList<>();

        public LoadImages(FragmentActivity fragmentActivity) {
            this.fragmentActivity = fragmentActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startAnim();
        }

        @Override
        protected List<Directory<ImageFile>> doInBackground(Void... voids) {

            String[] FILE_PROJECTION = {
                    //Base File
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.TITLE,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    DATE_ADDED,
                    MediaStore.Images.Media.ORIENTATION
            };

            String selection = MediaStore.MediaColumns.MIME_TYPE + "=? or " + MediaStore.MediaColumns.MIME_TYPE + "=? or " + MediaStore.MediaColumns.MIME_TYPE + "=? or " + MediaStore.MediaColumns.MIME_TYPE + "=?";

            String[] selectionArgs;
            selectionArgs = new String[]{"image/jpeg", "image/png", "image/jpg", "image/gif"};

            Cursor data = fragmentActivity.getContentResolver().query(MediaStore.Files.getContentUri("external"),
                    FILE_PROJECTION,
                    selection,
                    selectionArgs,
                    DATE_ADDED + " DESC");

            List<Directory<ImageFile>> directories = new ArrayList<>();
            List<Directory> directories1 = new ArrayList<>();

            if (data.getPosition() != -1) {
                data.moveToPosition(-1);
            }

            while (data.moveToNext()) {
                //Create a File instance
                ImageFile img = new ImageFile();

                img.setId(data.getLong(data.getColumnIndexOrThrow(_ID)));
                img.setName(data.getString(data.getColumnIndexOrThrow(TITLE)));
                img.setPath(data.getString(data.getColumnIndexOrThrow(DATA)));
                img.setSize(data.getLong(data.getColumnIndexOrThrow(SIZE)));
                img.setBucketId(data.getString(data.getColumnIndexOrThrow(BUCKET_ID)));
                img.setBucketName(data.getString(data.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME)));
                img.setDate(Util.convertTimeDateModified(data.getLong(data.getColumnIndexOrThrow(DATE_ADDED))));
                img.setOrientation(data.getInt(data.getColumnIndexOrThrow(ORIENTATION)));

                //Create a Directory
                Directory<ImageFile> directory = new Directory<>();
                directory.setId(img.getBucketId());
                directory.setName(img.getBucketName());
                directory.setPath(Util.extractPathWithoutSeparator(img.getPath()));

                if (!directories1.contains(directory)) {
                    directory.addFile(img);
                    directories.add(directory);
                    directories1.add(directory);
                } else {
                    directories.get(directories.indexOf(directory)).addFile(img);
                }
                imageFiles.add(img);
            }

            return directories;
        }

        @Override
        protected void onPostExecute(List<Directory<ImageFile>> directories) {
            super.onPostExecute(directories);
            Log.d(TAG, "onPostExecute: " + "done");
            imageFiles.clear();
            for (int i = 0; i < directories.size(); i++) {
                if (albumBinding.getDirName().equals(directories.get(i).getName())) {
                    imageFiles = directories.get(i).getFiles();
                    int finalI = i;
                    fragmentActivity.runOnUiThread(() -> {
                        Log.e("LLL_Date: ", albumBinding.getDirName() + "  Directory: " + directories.get(finalI).getName() + " Size: " + directories.get(finalI).getFiles().size());
                        if (!Util.isList) {
                            albumBinding.rvAlbumImage.setLayoutManager(new GridLayoutManager(AlbumActivity.this, Util.COLUMN_TYPE));
                            albumChildAdapter = new AlbumChildAdapter(imageFiles, AlbumActivity.this, finalI);
                            albumBinding.rvAlbumImage.setAdapter(albumChildAdapter);
                        } else {
                            albumBinding.rvAlbumImage.setLayoutManager(new LinearLayoutManager(AlbumActivity.this, RecyclerView.VERTICAL, false));
                            albumChildListAdapter = new AlbumChildListAdapter(imageFiles, AlbumActivity.this, finalI);
                            albumBinding.rvAlbumImage.setAdapter(albumChildListAdapter);
                        }
                    });
                }
            }

            stopAnim();

        }

    }

}