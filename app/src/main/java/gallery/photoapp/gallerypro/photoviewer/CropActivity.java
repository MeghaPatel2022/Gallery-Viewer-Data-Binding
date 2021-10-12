package gallery.photoapp.gallerypro.photoviewer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.isseiaoki.simplecropview.CropImageView;

import java.io.File;
import java.io.FileOutputStream;

import gallery.photoapp.gallerypro.photoviewer.databinding.ActivityCropBinding;

public class CropActivity extends BaseActivity {

    ActivityCropBinding cropBinding;
    MyClickHandlers myClickHandlers;

    private String FilePath;

    void startAnim() {
        cropBinding.rlLoading.setVisibility(View.VISIBLE);
        // or avi.smoothToShow();
    }

    void stopAnim() {
        cropBinding.rlLoading.setVisibility(View.GONE);
        // or avi.smoothToHide();
    }


    @Override
    public void permissionGranted() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cropBinding = DataBindingUtil.setContentView(CropActivity.this, R.layout.activity_crop);

        FilePath = getIntent().getStringExtra("filePath");

        Bitmap bitmap = BitmapFactory.decodeFile(new File(FilePath).getAbsolutePath());

        Glide.with(CropActivity.this)
                .asBitmap()
                .load(bitmap)
                .thumbnail(0.5f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(cropBinding.imgImages);

        cropBinding.imgImages.setAnimationEnabled(true);
        cropBinding.imgImages.setAnimationDuration(300);
        cropBinding.imgImages.setInterpolator(new AccelerateDecelerateInterpolator());

        myClickHandlers = new MyClickHandlers(CropActivity.this);
        cropBinding.setOnClickListener(myClickHandlers);
    }

    private void SaveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/" + getString(R.string.app_name));
        File mySubDir = new File(myDir.getAbsolutePath() + "/.temp");
        String path = "";
        Log.d("Data", "data: " + path);

        if (!myDir.exists()) {
            Log.d("Data", "data: myDir " + myDir.mkdirs());
        }

        if (!mySubDir.exists()) {
            Log.d("Data", "data: mySubDir " + mySubDir.mkdirs());
        }

        File file = new File(mySubDir, "image_temp.jpeg");
        path = file.getAbsolutePath();

        if (file.exists()) file.delete();

        try {
            file.createNewFile();
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            Intent intent = new Intent(CropActivity.this, EditActivity.class);
            intent.putExtra("filePath", path);
            startActivity(intent);
            finish();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();

    }

    public class MyClickHandlers {

        Context context;

        public MyClickHandlers(Context context) {
            this.context = context;
        }

        public void onNoneClickListener(View view) {
            cropBinding.imgImages.setCropEnabled(false);
        }

        public void onImageFreeListener(View view) {
            cropBinding.imgImages.setCropEnabled(true);
            cropBinding.imgImages.setCropMode(CropImageView.CropMode.FREE);
        }

        public void onImageEclipseListener(View view) {
            cropBinding.imgImages.setCropEnabled(true);
            cropBinding.imgImages.setCropMode(CropImageView.CropMode.CIRCLE);
        }

        public void onImage11Listener(View view) {
            cropBinding.imgImages.setCropEnabled(true);
            cropBinding.imgImages.setCustomRatio(1, 1);
        }

        public void onImage34Listener(View view) {
            cropBinding.imgImages.setCropEnabled(true);
            cropBinding.imgImages.setCropMode(CropImageView.CropMode.RATIO_3_4);
        }

        public void onImage43Listener(View view) {
            cropBinding.imgImages.setCropEnabled(true);
            cropBinding.imgImages.setCropMode(CropImageView.CropMode.RATIO_4_3);
        }

        public void onImage23Listener(View view) {
            cropBinding.imgImages.setCropEnabled(true);
            cropBinding.imgImages.setCustomRatio(2, 3);
        }

        public void onImage916Listener(View view) {
            cropBinding.imgImages.setCropEnabled(true);
            cropBinding.imgImages.setCropMode(CropImageView.CropMode.RATIO_9_16);
        }

        public void onImage169Listener(View view) {
            cropBinding.imgImages.setCropEnabled(true);
            cropBinding.imgImages.setCropMode(CropImageView.CropMode.RATIO_16_9);
        }

        public void onDoneListener(View view) {
            new LongOperation().execute();
        }

        public void onBackListener(View view) {
            onBackPressed();
        }
    }

    private final class LongOperation extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startAnim();
        }

        @Override
        protected String doInBackground(Void... params) {
            SaveImage(cropBinding.imgImages.getCroppedBitmap());
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            stopAnim();
        }
    }
}