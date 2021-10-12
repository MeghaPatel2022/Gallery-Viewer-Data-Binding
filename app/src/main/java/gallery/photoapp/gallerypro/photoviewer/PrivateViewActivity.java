package gallery.photoapp.gallerypro.photoviewer;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.documentfile.provider.DocumentFile;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import gallery.photoapp.gallerypro.photoviewer.Pref.SharedPrefrance;
import gallery.photoapp.gallerypro.photoviewer.adapter.PrivateViewPagerAdapter;
import gallery.photoapp.gallerypro.photoviewer.constant.Util;
import gallery.photoapp.gallerypro.photoviewer.databinding.ActivityPrivateViewBinding;
import gallery.photoapp.gallerypro.photoviewer.model.RecycleModel;

public class PrivateViewActivity extends BaseActivity {

    private static final String TAG = PrivateViewActivity.class.getSimpleName();
    private static final int REQUEST_CODE_OPEN_DOCUMENT_TREE = 42;
    public ArrayList<String> privateList = new ArrayList<>();
    ActivityPrivateViewBinding privateViewBinding;
    MyClickHandlers myClickHandlers;
    MediaScannerConnection msConn;
    BottomSheetBehavior imageInfoUpBehaviour;
    private FirebaseAnalytics mFirebaseAnalytics;
    private PrivateViewPagerAdapter viewPagerAdapter;
    private int finalPosition = 0;
    private boolean isDeleteImg = false;

    private void fireAnalytics(String arg1, String arg2, String arg3) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, arg1);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, arg2);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, arg3);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    @Override
    public void permissionGranted() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        privateViewBinding = DataBindingUtil.setContentView(PrivateViewActivity.this, R.layout.activity_private_view);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(PrivateViewActivity.this);

        finalPosition = getIntent().getIntExtra("Position", finalPosition);
        new LongOperation().execute();

        imageInfoUpBehaviour = BottomSheetBehavior.from(privateViewBinding.rlImageInfo);
        myClickHandlers = new MyClickHandlers(PrivateViewActivity.this);
        privateViewBinding.setOnClick(myClickHandlers);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void deleteFile() {
        final Dialog dial = new Dialog(PrivateViewActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dial.requestWindowFeature(1);
        dial.setContentView(R.layout.dialog_delete);
        dial.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dial.setCanceledOnTouchOutside(true);

        TextView title = dial.findViewById(R.id.tvTitleDel);
        title.setText("Move 1 image to the Recycle bin?");

        TextView del = dial.findViewById(R.id.delete_yes);
        del.setText("Move to Recycle bin");

        del.findViewById(R.id.delete_yes).setOnClickListener(view -> {
            isDeleteImg = true;

            File file = new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/.nomedia");

            if (!file.exists())
                file.mkdirs();

            File sourceFile = new File(privateList.get(privateViewBinding.imgViewPager.getCurrentItem()));
            File destinationfile = new File(file, sourceFile.getName());

            File sampleFile = Environment.getExternalStorageDirectory();

            ArrayList<String> favFileList = SharedPrefrance.getFavouriteFileList(PrivateViewActivity.this);
            favFileList.remove(sourceFile.getAbsolutePath());
            SharedPrefrance.setFavouriteFileList(PrivateViewActivity.this, new ArrayList<>());
            SharedPrefrance.setFavouriteFileList(PrivateViewActivity.this, favFileList);

            if (destinationfile.getAbsolutePath().contains(sampleFile.getAbsolutePath())) {
                if (sourceFile.exists()) {
                    moveImageToRecycleBin(sourceFile, destinationfile);
                }
            } else {
                if (!SharedPrefrance.getSharedPreference(PrivateViewActivity.this).contains(destinationfile.getParentFile().getAbsolutePath())) {
                    Intent intent = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
                        startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);
                    }

                } else {
                    if (sourceFile.exists()) {
                        moveSDImageToRecycleBin(sourceFile, destinationfile);
                    }
                }
            }

            if (privateList.size() == 0)
                onBackPressed();

            dial.dismiss();
        });
        dial.findViewById(R.id.delete_no).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dial.dismiss();
            }
        });
        dial.show();
    }

    private void SaveImage(Bitmap finalBitmap, File samplefile) {


        String[] parts = (samplefile.getAbsolutePath()).split("/");

        String string1 = parts[parts.length - 1].substring(0, parts[parts.length - 1].lastIndexOf('.'));
        String fileName = string1.replace(".", "");

        String extension = parts[parts.length - 1].substring(parts[parts.length - 1].lastIndexOf("."));
        Log.e("LLL_Name: ", extension);

        Log.e("LLL_Name: ", fileName);

        File file = new File(samplefile.getParentFile().getAbsolutePath(), fileName + extension);

        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            ArrayList<String> hideFileList = SharedPrefrance.getHideFileList(PrivateViewActivity.this);
            hideFileList.remove(samplefile.getAbsolutePath());
            SharedPrefrance.setHideFileList(PrivateViewActivity.this, new ArrayList<>());
            SharedPrefrance.setHideFileList(PrivateViewActivity.this, hideFileList);

            Toast.makeText(this, "Unhidden " + samplefile.getName(), Toast.LENGTH_LONG).show();
            if (samplefile.exists()) {
                samplefile.delete();
            }

            scanPhoto(file.toString());

            privateList.remove(privateViewBinding.imgViewPager.getCurrentItem());
            viewPagerAdapter.notifyDataSetChanged();
            if (privateList.isEmpty())
                onBackPressed();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void moveImageToRecycleBin(File sendFile, File sourceFile) {
        isDeleteImg = false;
        try {

            Bitmap finalBitmap = BitmapFactory.decodeFile(sendFile.getAbsolutePath());

            FileOutputStream out = new FileOutputStream(sourceFile);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            ArrayList<String> favFileList = SharedPrefrance.getFavouriteFileList(PrivateViewActivity.this);
            if (favFileList.contains(sourceFile.getAbsolutePath())) {
                favFileList.remove(sourceFile.getAbsolutePath());
                SharedPrefrance.setFavouriteFileList(PrivateViewActivity.this, new ArrayList<>());
                SharedPrefrance.setFavouriteFileList(PrivateViewActivity.this, favFileList);
            }

            Toast.makeText(this, "Move File to Recycle Bin", Toast.LENGTH_LONG).show();
            ArrayList<RecycleModel> recycleList = SharedPrefrance.getRecycleBinData(PrivateViewActivity.this);

            if (recycleList == null)
                recycleList = new ArrayList<>();

            RecycleModel recycleModel = new RecycleModel();
            recycleModel.setImageName(sourceFile.getName());
            recycleModel.setOldImagePath(sendFile.getAbsolutePath());
            recycleModel.setNewImagePath(sourceFile.getAbsolutePath());

            recycleList.add(recycleModel);
            SharedPrefrance.setRecycleBinData(PrivateViewActivity.this, new ArrayList<>());
            SharedPrefrance.setRecycleBinData(PrivateViewActivity.this, recycleList);

            if (sendFile.exists()) {
                boolean isDelete = sendFile.delete();
                if (!isDelete) {
                    isDelete = Util.delete(PrivateViewActivity.this, sendFile);
                }
                Log.e("LLLL_del: ", String.valueOf(isDelete));
            }

            scanPhoto(sendFile.toString());

            if (privateViewBinding.imgViewPager.getCurrentItem() == privateList.size() - 1) {
                privateList.remove(privateViewBinding.imgViewPager.getCurrentItem());
                onBackPressed();
            } else {
                privateList.remove(privateViewBinding.imgViewPager.getCurrentItem());
                viewPagerAdapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void moveSDImageToRecycleBin(File sentFile, File sourceFile) {
        isDeleteImg = false;

        try {

            Bitmap finalBitmap = BitmapFactory.decodeFile(sentFile.getAbsolutePath());

            FileOutputStream out = new FileOutputStream(sourceFile);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            ArrayList<String> favFileList = SharedPrefrance.getFavouriteFileList(PrivateViewActivity.this);
            if (favFileList.contains(sourceFile.getAbsolutePath())) {
                favFileList.remove(sourceFile.getAbsolutePath());
                SharedPrefrance.setFavouriteFileList(PrivateViewActivity.this, new ArrayList<>());
                SharedPrefrance.setFavouriteFileList(PrivateViewActivity.this, favFileList);
            }

            Toast.makeText(this, "Move File to Recycle Bin", Toast.LENGTH_LONG).show();
            ArrayList<RecycleModel> recycleList = SharedPrefrance.getRecycleBinData(PrivateViewActivity.this);
            if (recycleList == null)
                recycleList = new ArrayList<>();

            RecycleModel recycleModel = new RecycleModel();
            recycleModel.setImageName(sourceFile.getName());
            recycleModel.setOldImagePath(sentFile.getAbsolutePath());
            recycleModel.setNewImagePath(sourceFile.getAbsolutePath());

            recycleList.add(recycleModel);
            SharedPrefrance.setRecycleBinData(PrivateViewActivity.this, new ArrayList<>());
            SharedPrefrance.setRecycleBinData(PrivateViewActivity.this, recycleList);

            if (sentFile.exists()) {
                boolean isDelete = sentFile.delete();
                if (!isDelete) {
                    isDelete = Util.delete(PrivateViewActivity.this, sentFile);
                }
                Log.e("LLLL_del: ", String.valueOf(isDelete));
            }

            scanPhoto(sentFile.toString());

            if (privateViewBinding.imgViewPager.getCurrentItem() == privateList.size() - 1) {
                privateList.remove(privateViewBinding.imgViewPager.getCurrentItem());
                onBackPressed();
            } else {
                privateList.remove(privateViewBinding.imgViewPager.getCurrentItem());
                viewPagerAdapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            Toast.makeText(this, "something went wrong" + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    public void scanPhoto(final String imageFileName) {
        msConn = new MediaScannerConnection(this, new MediaScannerConnection.MediaScannerConnectionClient() {
            public void onMediaScannerConnected() {
                msConn.scanFile(imageFileName, null);
                Log.i("msClient obj", "connection established");
            }

            public void onScanCompleted(String path, Uri uri) {
                msConn.disconnect();
                Log.i("msClient obj", "scan completed");
            }
        });
        this.msConn.connect();
    }

    @Override
    public void onBackPressed() {
        if (imageInfoUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            imageInfoUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            Intent intent = new Intent(PrivateViewActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (resultCode == RESULT_OK) {

            File file1 = new File(privateList.get(privateViewBinding.imgViewPager.getCurrentItem()));
            String[] parts = (file1.getAbsolutePath()).split("/");

            DocumentFile documentFile = DocumentFile.fromTreeUri(this, resultData.getData());
            documentFile = documentFile.findFile(parts[parts.length - 1]);

            Log.e("LLL_Data: ", String.valueOf(documentFile.getUri()));

            if (documentFile == null) {
                File sourceFile = new File(privateList.get(privateViewBinding.imgViewPager.getCurrentItem()));
                DocumentFile documentFile1 = DocumentFile.fromTreeUri(this, resultData.getData());

                SharedPrefrance.setSharedPreferenceUri(PrivateViewActivity.this, documentFile1.getUri());
                SharedPrefrance.setSharedPreference(PrivateViewActivity.this, file1.getParentFile().getAbsolutePath());

                if (isDeleteImg) {
                    isDeleteImg = false;
                    if (sourceFile.exists()) {
                        File file2 = new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/.nomedia");

                        if (!file2.exists())
                            file2.mkdirs();

                        File destinationfile = new File(file2, sourceFile.getName());

                        moveSDImageToRecycleBin(sourceFile, destinationfile);
                    }

                } else {
                    getData(file1);
                }
            } else {
                File sourceFile = new File(privateList.get(privateViewBinding.imgViewPager.getCurrentItem()));
                Log.e("LLL_Data: ", String.valueOf(documentFile.getUri()));

                SharedPrefrance.setSharedPreferenceUri(PrivateViewActivity.this, documentFile.getUri());
                SharedPrefrance.setSharedPreference(PrivateViewActivity.this, file1.getParentFile().getAbsolutePath());
                if (isDeleteImg) {
                    isDeleteImg = false;
                    if (sourceFile.exists()) {
                        File file2 = new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/.nomedia");

                        if (!file2.exists())
                            file2.mkdirs();

                        File destinationfile = new File(file2, sourceFile.getName());

                        moveSDImageToRecycleBin(sourceFile, destinationfile);
                    }

                } else {
                    getData(file1);
                }
            }
        }
    }

    private void getData(File file1) {

        String[] parts = (file1.getAbsolutePath()).split("/");
        Log.e("LLL_Name: ", parts[parts.length - 1]);
        String string1 = parts[parts.length - 1].substring(0, parts[parts.length - 1].lastIndexOf('.'));
        String fileName = string1.replace(".", "");

        String extension = parts[parts.length - 1].substring(parts[parts.length - 1].lastIndexOf("."));
        Log.e("LLL_Name: ", extension);

        File file2 = new File(file1.getParentFile().getParentFile().getAbsolutePath(), fileName + extension);
        File file3 = new File(file1.getParentFile().getAbsolutePath(), fileName + extension);

        OutputStream fileOutupStream = null;
        DocumentFile targetDocument = getDocumentFile(file2, false);

        try {
            fileOutupStream = getContentResolver().openOutputStream(targetDocument.getUri());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(file1.getAbsolutePath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutupStream);
            fileOutupStream.flush();
            fileOutupStream.close();

            ArrayList<String> hideFileList = SharedPrefrance.getHideFileList(PrivateViewActivity.this);
            hideFileList.remove(file1.getAbsolutePath());
            SharedPrefrance.setHideFileList(PrivateViewActivity.this, new ArrayList<>());
            SharedPrefrance.setHideFileList(PrivateViewActivity.this, hideFileList);

            if (file1.exists()) {
                boolean isDelete = Util.delete(PrivateViewActivity.this, file1);
                Log.e("LLLL_Del: ", String.valueOf(isDelete));
            }

            privateList.remove(privateViewBinding.imgViewPager.getCurrentItem());
            viewPagerAdapter.notifyDataSetChanged();
            if (privateList.isEmpty())
                onBackPressed();
            Toast.makeText(this, "Unhide " + file1.getName(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "something went wrong" + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public DocumentFile getDocumentFile(final File file, final boolean isDirectory) {
        String baseFolder = getExtSdCardFolder(file);

        if (baseFolder == null) {
            return null;
        }

        String relativePath = null;
        try {
            String fullPath = file.getCanonicalPath();
            relativePath = fullPath.substring(baseFolder.length() + 1);
        } catch (IOException e) {
            return null;
        }

        Uri treeUri = Uri.parse(SharedPrefrance.getSharedPreferenceUri(PrivateViewActivity.this));

        if (treeUri == null) {
            return null;
        }

        // start with root of SD card and then parse through document tree.
        DocumentFile document = DocumentFile.fromTreeUri(PrivateViewActivity.this, treeUri);

        String[] parts = relativePath.split("\\/");
        for (int i = 0; i < parts.length; i++) {
            DocumentFile nextDocument = document.findFile(parts[i]);

            if (nextDocument == null) {
                if ((i < parts.length - 1) || isDirectory) {
                    nextDocument = document.createDirectory(parts[i]);
                } else {
                    nextDocument = document.createFile("image", parts[i]);
                }
            }
            document = nextDocument;
        }

        return document;
    }

    public String getExtSdCardFolder(final File file) {
        String[] extSdPaths = getExtSdCardPaths();
        try {
            for (int i = 0; i < extSdPaths.length; i++) {
                if (file.getCanonicalPath().startsWith(extSdPaths[i])) {
                    return extSdPaths[i];
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    /**
     * Get a list of external SD card paths. (Kitkat or higher.)
     *
     * @return A list of external SD card paths.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private String[] getExtSdCardPaths() {
        List<String> paths = new ArrayList<>();
        for (File file : getExternalFilesDirs("external")) {
            if (file != null && !file.equals(getExternalFilesDir("external"))) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w(TAG, "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        return paths.toArray(new String[paths.size()]);
    }

    /**
     * Retrieve the application context.
     *
     * @return The (statically stored) application context
     */
    private void shareImage(File file) {
        fireAnalytics(FavouriteViewActivity.class.getSimpleName(), "Image", "Share");
        Uri uri = FileProvider.getUriForFile(PrivateViewActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
        Intent shareIntent = new Intent("android.intent.action.SEND");
        shareIntent.setType("image/*");
        shareIntent.putExtra("android.intent.extra.TEXT", getResources().getString(R.string.app_name) + " Create By : https://play.google.com/store/apps/details?id=" + getPackageName());
        shareIntent.putExtra("android.intent.extra.STREAM", uri);
        startActivity(Intent.createChooser(shareIntent, "Share Image using"));
    }

    private final class LongOperation extends AsyncTask<Void, Void, ArrayList<String>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            ArrayList<String> pathList;
            pathList = SharedPrefrance.getHideFileList(PrivateViewActivity.this);
            return pathList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            privateList = result;
            int finalPosition1 = finalPosition;
            runOnUiThread(() -> {
                viewPagerAdapter = new PrivateViewPagerAdapter(PrivateViewActivity.this, result, "");
                privateViewBinding.imgViewPager.setAdapter(viewPagerAdapter);
                privateViewBinding.imgViewPager.setCurrentItem(finalPosition1);

                File file = new File(result.get(finalPosition1));
                privateViewBinding.tvImgName.setText(file.getName());
                String time = DateFormat.format("dd MMM yyyy hh:mm a", file.lastModified()).toString();
                privateViewBinding.tvDateTime.setText(time);

                // File Information
                privateViewBinding.tvFilename.setText(file.getName());
                privateViewBinding.tvPath.setText(file.getAbsolutePath());
                privateViewBinding.tvDate.setText(Util.convertTimeDateModifiedShown(file.lastModified()));
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                int imageHeight = options.outHeight;
                int imageWidth = options.outWidth;
                privateViewBinding.tvResolution.setText(imageWidth + " x " + imageHeight);
                privateViewBinding.tvSize.setText(Util.getFileSize(file.length()));

                privateViewBinding.imgViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        File file = new File(result.get(position));
                        privateViewBinding.tvImgName.setText(file.getName());
                        String time = DateFormat.format("dd MMM yyyy hh:mm a", file.lastModified()).toString();
                        privateViewBinding.tvDateTime.setText(time);

                        // File Information
                        privateViewBinding.tvFilename.setText(file.getName());
                        privateViewBinding.tvPath.setText(file.getAbsolutePath());
                        privateViewBinding.tvDate.setText(Util.convertTimeDateModifiedShown(file.lastModified()));
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                        int imageHeight = options.outHeight;
                        int imageWidth = options.outWidth;
                        privateViewBinding.tvResolution.setText(imageWidth + " x " + imageHeight);
                        privateViewBinding.tvSize.setText(Util.getFileSize(file.length()));
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });

            });
        }
    }

    public class MyClickHandlers {
        Context context;

        public MyClickHandlers(Context context) {
            this.context = context;
        }

        public void onImageEditListener(View view) {
            fireAnalytics(PrivateViewActivity.class.getSimpleName(), "Image", "Edit");
            File file = new File(privateList.get(privateViewBinding.imgViewPager.getCurrentItem())).getParentFile().getAbsoluteFile();
            Util.FOLDER_NAME = file.getAbsolutePath();

            Log.e("LLLL_Log: ", Util.FOLDER_NAME + "    : ");

            Intent intent = new Intent(PrivateViewActivity.this, EditActivity.class);
            intent.putExtra("filePath", privateList.get(privateViewBinding.imgViewPager.getCurrentItem()));
            startActivity(intent);
        }

        public void onImageDeleteListener(View view) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                deleteFile();
            }
        }

        public void onProtectListener(View view) {
            fireAnalytics(PrivateViewActivity.class.getSimpleName(), "Image", "Unhide");
            File sampleFile = Environment.getExternalStorageDirectory();
            File file1 = new File(privateList.get(privateViewBinding.imgViewPager.getCurrentItem()));
            Uri uri = Uri.fromFile(file1);

            if (file1.getAbsolutePath().contains(sampleFile.getAbsolutePath())) {
                Bitmap bitmap = BitmapFactory.decodeFile(file1.getAbsolutePath());
                SaveImage(bitmap, file1);
            } else {
                if (!SharedPrefrance.getSharedPreference(PrivateViewActivity.this).contains(file1.getParentFile().getAbsolutePath())) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                            | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                    intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
                    startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);
                } else {
                    getData(file1);
                }
            }
        }

        public void onDetailsListener(View view) {
            fireAnalytics(PrivateViewActivity.class.getSimpleName(), "Image", "Information");
            if (imageInfoUpBehaviour.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                imageInfoUpBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }

        public void onShareClick(View view) {
            shareImage(new File(privateList.get(privateViewBinding.imgViewPager.getCurrentItem())));
        }

        public void onBackListener(View view) {
            onBackPressed();
        }

    }

}