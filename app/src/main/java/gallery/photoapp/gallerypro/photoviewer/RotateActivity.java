package gallery.photoapp.gallerypro.photoviewer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.isseiaoki.simplecropview.CropImageView;

import java.io.File;
import java.io.FileOutputStream;

import gallery.photoapp.gallerypro.photoviewer.databinding.ActivityRotateBinding;

public class RotateActivity extends BaseActivity {

    ActivityRotateBinding rotateBinding;
    MyClickHandlers myClickHandlers;

    private FirebaseAnalytics mFirebaseAnalytics;
    private String FilePath;

    void startAnim() {
        rotateBinding.rlLoading.setVisibility(View.VISIBLE);
        // or avi.smoothToShow();
    }

    void stopAnim() {
        rotateBinding.rlLoading.setVisibility(View.GONE);
        // or avi.smoothToHide();
    }


    @Override
    public void permissionGranted() {

    }

    private void fireAnalytics(String arg1, String arg2, String arg3) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, arg1);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, arg2);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, arg3);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rotateBinding = DataBindingUtil.setContentView(RotateActivity.this, R.layout.activity_rotate);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(RotateActivity.this);
        FilePath = getIntent().getStringExtra("filePath");
        Bitmap bitmap = BitmapFactory.decodeFile(new File(FilePath).getAbsolutePath());

        Glide.with(RotateActivity.this)
                .asBitmap()
                .load(bitmap)
                .thumbnail(0.5f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(rotateBinding.imgImages);

        myClickHandlers = new MyClickHandlers(RotateActivity.this);
        rotateBinding.setOnClickListener(myClickHandlers);

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

            Intent intent = new Intent(RotateActivity.this, EditActivity.class);
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

        public void onLeftRotate(View view) {
            fireAnalytics(EditActivity.class.getSimpleName(), "Image", "LeftRotate");
            rotateBinding.imgImages.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D);
        }

        public void onRightRotate(View view) {
            fireAnalytics(EditActivity.class.getSimpleName(), "Image", "RightRotate");
            rotateBinding.imgImages.rotateImage(CropImageView.RotateDegrees.ROTATE_90D);
        }

        public void onVerticalRotate(View view) {
            fireAnalytics(EditActivity.class.getSimpleName(), "Image", "VerticalRotate");
            Bitmap bInput = rotateBinding.imgImages.getCroppedBitmap();
            Matrix matrix = new Matrix();
            matrix.preScale(1.0f, -1.0f);
            Bitmap bOutput = Bitmap.createBitmap(bInput, 0, 0, bInput.getWidth(), bInput.getHeight(), matrix, true);

            Glide.with(RotateActivity.this)
                    .asBitmap()
                    .load(bOutput)
                    .thumbnail(0.5f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(rotateBinding.imgImages);
        }

        public void onHorizontalRotate(View view) {
            fireAnalytics(EditActivity.class.getSimpleName(), "Image", "HorizontalRotate");
            Bitmap bInput1 = rotateBinding.imgImages.getCroppedBitmap();
            Matrix matrix1 = new Matrix();
            matrix1.preScale(-1.0f, 1.0f);
            Bitmap bOutput1 = Bitmap.createBitmap(bInput1, 0, 0, bInput1.getWidth(), bInput1.getHeight(), matrix1, true);

            Glide.with(RotateActivity.this)
                    .asBitmap()
                    .load(bOutput1)
                    .thumbnail(0.5f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(rotateBinding.imgImages);
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
            SaveImage(rotateBinding.imgImages.getCroppedBitmap());
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            stopAnim();
        }
    }
}