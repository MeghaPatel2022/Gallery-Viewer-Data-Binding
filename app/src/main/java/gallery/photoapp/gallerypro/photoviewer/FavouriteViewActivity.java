package gallery.photoapp.gallerypro.photoviewer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfDocument;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import gallery.photoapp.gallerypro.photoviewer.adapter.CreateAlbumAdapter;
import gallery.photoapp.gallerypro.photoviewer.adapter.FavImageViewAdapter;
import gallery.photoapp.gallerypro.photoviewer.constant.Util;
import gallery.photoapp.gallerypro.photoviewer.databinding.ActivityFavouriteViewBinding;
import gallery.photoapp.gallerypro.photoviewer.model.Directory;
import gallery.photoapp.gallerypro.photoviewer.model.ImageFile;
import gallery.photoapp.gallerypro.photoviewer.model.RecycleModel;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.MediaColumns.BUCKET_DISPLAY_NAME;
import static android.provider.MediaStore.MediaColumns.BUCKET_ID;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;
import static android.provider.MediaStore.MediaColumns.ORIENTATION;
import static android.provider.MediaStore.MediaColumns.SIZE;
import static android.provider.MediaStore.MediaColumns.TITLE;

public class FavouriteViewActivity extends BaseActivity {

    private static final int position = 0;
    private static final String TAG = FavouriteViewActivity.class.getSimpleName();
    private static final int REQUEST_CODE_OPEN_DOCUMENT_TREE = 42;
    private static final String from = "";
    private static String ALBUMNAME = "";
    private static boolean isCreateAlbum = false;
    private static boolean isDeleteImg = false;
    private static boolean isCopyImg = false;
    private static int createAlbumPosition = 0;
    private final List<ImageFile> imageFiles = new ArrayList<>();
    public ArrayList<String> privateList = new ArrayList<>();
    public List<Directory<ImageFile>> dateFiles = new ArrayList<>();
    ActivityFavouriteViewBinding favouriteViewBinding;
    MyClickHandlers myClickHandlers;
    CreateAlbumAdapter createAlbumAdapter;
    BottomSheetBehavior mainPopUpBehaviour;
    BottomSheetBehavior albumPopUpBehaviour;
    BottomSheetBehavior albumCreatePopUpBehaviour;
    BottomSheetBehavior moveAlbumPopUpBehaviour;
    BottomSheetBehavior imageInfoUpBehaviour;
    BottomSheetBehavior imageRenameBehaviour;
    MediaScannerConnection msConn;
    Handler handler = new Handler();
    public Runnable calendarUpdater = new Runnable() {
        @Override
        public void run() {
            Animation fadeIn = AnimationUtils.loadAnimation(FavouriteViewActivity.this, R.anim.fade_in);
            Log.e("current item:", String.valueOf(favouriteViewBinding.imgViewPager.getCurrentItem()));
            if (favouriteViewBinding.imgViewPager.getCurrentItem() == privateList.size() - 1) {
                favouriteViewBinding.imgViewPager.setCurrentItem(0);
            } else {
                favouriteViewBinding.imgViewPager.setCurrentItem(favouriteViewBinding.imgViewPager.getCurrentItem() + 1);
            }
            favouriteViewBinding.imgViewPager.startAnimation(fadeIn);
            if (Util.SLIDE_TIME.equals(getString(R.string._1_sec)))
                handler.postDelayed(calendarUpdater, 1000);
            else if (Util.SLIDE_TIME.equals(getString(R.string._2_sec)))
                handler.postDelayed(calendarUpdater, 2000);
            else if (Util.SLIDE_TIME.equals(getString(R.string._3_sec)))
                handler.postDelayed(calendarUpdater, 3000);
            else if (Util.SLIDE_TIME.equals(getString(R.string._4_sec)))
                handler.postDelayed(calendarUpdater, 4000);
            else if (Util.SLIDE_TIME.equals(getString(R.string._5_sec)))
                handler.postDelayed(calendarUpdater, 5000);
            else
                handler.postDelayed(calendarUpdater, 1000);
        }
    };
    private FirebaseAnalytics mFirebaseAnalytics;
    private PdfDocument myPdfDocument;
    private int pageHeight;
    private int pageWidth;
    private FavImageViewAdapter viewPagerAdapter;

    private int finalPosition = 0;
    private boolean startSlideShow = false;

    private void fireAnalytics(String arg1, String arg2, String arg3) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, arg1);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, arg2);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, arg3);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    void startAnim() {
        favouriteViewBinding.rlLoading.setVisibility(View.VISIBLE);
    }

    void stopAnim() {
        favouriteViewBinding.rlLoading.setVisibility(View.GONE);
    }

    void startSlideShow() {
        favouriteViewBinding.llBottom.setVisibility(View.GONE);
        favouriteViewBinding.rlToolbar.setVisibility(View.GONE);
        startSlideShow = true;
        calendarUpdater.run();
    }

    void stopSlideShow() {
        favouriteViewBinding.llBottom.setVisibility(View.VISIBLE);
        favouriteViewBinding.rlToolbar.setVisibility(View.VISIBLE);
        startSlideShow = false;
        handler.removeCallbacks(calendarUpdater);
    }

    @Override
    public void permissionGranted() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        favouriteViewBinding = DataBindingUtil.setContentView(FavouriteViewActivity.this, R.layout.activity_favourite_view);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(FavouriteViewActivity.this);

        finalPosition = getIntent().getIntExtra("Position", finalPosition);

        createAlbumAdapter = new CreateAlbumAdapter(dateFiles, FavouriteViewActivity.this, position -> {
            ALBUMNAME = new File(dateFiles.get(position).getPath()).getName();
            isCreateAlbum = false;
            createAlbumPosition = position;
            albumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if (moveAlbumPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moveAlbumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                moveAlbumPopUpBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        favouriteViewBinding.rvListView.setLayoutManager(new LinearLayoutManager(FavouriteViewActivity.this, RecyclerView.VERTICAL, false));
        favouriteViewBinding.rvListView.setAdapter(createAlbumAdapter);

        new LongOperation().execute();
        new LoadAlbumImages(FavouriteViewActivity.this).execute();

        mainPopUpBehaviour = BottomSheetBehavior.from(favouriteViewBinding.llMainPopup);
        albumPopUpBehaviour = BottomSheetBehavior.from(favouriteViewBinding.llCreateAlbum);
        albumCreatePopUpBehaviour = BottomSheetBehavior.from(favouriteViewBinding.llCreateAnAlbum);
        moveAlbumPopUpBehaviour = BottomSheetBehavior.from(favouriteViewBinding.llMoveAlbum);
        imageInfoUpBehaviour = BottomSheetBehavior.from(favouriteViewBinding.rlImageInfo);
        imageRenameBehaviour = BottomSheetBehavior.from(favouriteViewBinding.llRename);

        myClickHandlers = new MyClickHandlers(FavouriteViewActivity.this);
        favouriteViewBinding.setOnClickListener(myClickHandlers);
        favouriteViewBinding.imgFav.setSelected(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void slideShowPopUp() {
        String[] radio1 = {getResources().getString(R.string._1_sec),
                getResources().getString(R.string._2_sec),
                getResources().getString(R.string._3_sec),
                getResources().getString(R.string._4_sec),
                getResources().getString(R.string._5_sec)};

        final Dialog dial = new Dialog(FavouriteViewActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dial.requestWindowFeature(1);
        dial.setContentView(R.layout.dialog_slideshow);
        dial.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dial.setCanceledOnTouchOutside(true);

        RadioGroup rg_sorting1 = dial.findViewById(R.id.rg_sorting1);

        for (int i = 0; i < radio1.length; i++) {
            String sorting1 = radio1[i];
            RadioButton radioButton = new RadioButton(this);
            radioButton.setPadding(30, 30, 7, 30);
            radioButton.setText(sorting1);
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            radioButton.setTextColor(getResources().getColor(R.color.menu_text_color));
            radioButton.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_200)));
            radioButton.setId(i);

            if (Util.SLIDE_TIME.equals("")) {
                if (i == 0) {
                    radioButton.setChecked(true);
                    Util.SLIDE_TIME = radioButton.getText().toString();
                }
            } else if (radioButton.getText().equals(Util.SLIDE_TIME)) {
                radioButton.setChecked(true);
            }

            rg_sorting1.addView(radioButton);
        }

        //set listener to radio button group
        rg_sorting1.setOnCheckedChangeListener((group, checkedId) -> {
            int checkedRadioButtonId = group.getCheckedRadioButtonId();
            RadioButton radioBtn = dial.findViewById(checkedRadioButtonId);
            Util.SLIDE_TIME = radioBtn.getText().toString();
            Log.e("LLLLL_Refill_time: ", Util.SLIDE_TIME);
            startSlideShow();

            fireAnalytics(FavouriteViewActivity.class.getSimpleName(), "Image", "Slide show");

            dial.dismiss();
        });

        if (mainPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        dial.show();

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void printDocument(ArrayList<String> totalDoc) {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

        String jobName = getString(R.string.app_name) +
                " Document";

        printManager.print(jobName, new MyPrintDocumentAdapter(FavouriteViewActivity.this, totalDoc),
                null);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean pageInRange(PageRange[] pageRanges, int page) {
        for (int i = 0; i < pageRanges.length; i++) {
            if ((page >= pageRanges[i].getStart()) &&
                    (page <= pageRanges[i].getEnd()))
                return true;
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void drawPage(PdfDocument.Page page, String Path) {

        File directory = new File(Path);
        Uri uri = Uri.parse("file://" + directory.getAbsolutePath());
        File file1 = new File(uri.getPath());
        Bitmap bitmap1 = BitmapFactory.decodeFile(file1.getAbsolutePath());
        Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap1,
                page.getCanvas().getWidth(),
                page.getCanvas().getHeight(), true
        );
        int centreX = (page.getCanvas().getWidth() - bitmap2.getWidth()) >> 1;
        int centreY = (page.getCanvas().getWidth() - bitmap2.getWidth()) >> 1;
        Canvas canvas = page.getCanvas();
        canvas.drawBitmap(bitmap2, centreX, centreY, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void SaveImage(Bitmap finalBitmap, File samplefile) {

        File file = new File(samplefile.getParentFile().getAbsolutePath(), "." + samplefile.getName());
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            ArrayList<String> hideFileList = SharedPrefrance.getHideFileList(FavouriteViewActivity.this);
            hideFileList.add(file.getAbsolutePath());
            SharedPrefrance.setHideFileList(FavouriteViewActivity.this, hideFileList);

            Toast.makeText(this, "Hide " + samplefile.getName(), Toast.LENGTH_LONG).show();
            if (samplefile.exists()) {
                Util.delete(FavouriteViewActivity.this, samplefile);
            }

            ArrayList<String> favFileList = SharedPrefrance.getFavouriteFileList(FavouriteViewActivity.this);
            favFileList.remove(samplefile.getAbsolutePath());
            SharedPrefrance.setFavouriteFileList(FavouriteViewActivity.this, new ArrayList<>());
            SharedPrefrance.setFavouriteFileList(FavouriteViewActivity.this, favFileList);

            scanPhoto(samplefile.toString());

            privateList.remove(favouriteViewBinding.imgViewPager.getCurrentItem());
            viewPagerAdapter.notifyDataSetChanged();
            if (privateList.isEmpty())
                onBackPressed();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setWallpaper(String s) {
        Bitmap bitmap = BitmapFactory.decodeFile(s);
        WallpaperManager manager = WallpaperManager.getInstance(getApplicationContext());
        try {
            manager.setBitmap(bitmap);
            Toast.makeText(this, "Wallpaper set!", Toast.LENGTH_SHORT).show();
            stopAnim();

        } catch (IOException e) {
            Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareImage(File file) {
        fireAnalytics(FavouriteViewActivity.class.getSimpleName(), "Image", "Share");
        Uri uri = FileProvider.getUriForFile(FavouriteViewActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
        Intent shareIntent = new Intent("android.intent.action.SEND");
        shareIntent.setType("image/*");
        shareIntent.putExtra("android.intent.extra.TEXT", getResources().getString(R.string.app_name) + " Create By : https://play.google.com/store/apps/details?id=" + getPackageName());
        shareIntent.putExtra("android.intent.extra.STREAM", uri);
        startActivity(Intent.createChooser(shareIntent, "Share Image using"));
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void deleteFile() {
        final Dialog dial = new Dialog(FavouriteViewActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
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

            File sourceFile = new File(privateList.get(favouriteViewBinding.imgViewPager.getCurrentItem()));
            File destinationfile = new File(file, sourceFile.getName());

            File sampleFile = Environment.getExternalStorageDirectory();
            File file1 = new File(dateFiles.get(createAlbumPosition).getPath());

            ArrayList<String> favFileList = SharedPrefrance.getFavouriteFileList(FavouriteViewActivity.this);
            favFileList.remove(sourceFile.getAbsolutePath());
            SharedPrefrance.setFavouriteFileList(FavouriteViewActivity.this, new ArrayList<>());
            SharedPrefrance.setFavouriteFileList(FavouriteViewActivity.this, favFileList);

            if (file1.getAbsolutePath().contains(sampleFile.getAbsolutePath())) {
                if (sourceFile.exists()) {
                    moveImageToRecycleBin(sourceFile, destinationfile);
                }
            } else {
                if (!SharedPrefrance.getSharedPreference(FavouriteViewActivity.this).contains(file1.getParentFile().getAbsolutePath())) {
                    Intent intent = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
                    }

                    startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);
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

    private void moveImage(Bitmap finalBitmap, File sourceFile, boolean isCreateAlbum, int position) {

        String[] parts = (sourceFile.getAbsolutePath()).split("/");

        File file;
        if (!isCreateAlbum) {
            file = new File(dateFiles.get(position).getPath(), sourceFile.getName());
        } else {
            File mainDir = Environment.getExternalStorageDirectory();
            File destinationFile = new File(mainDir, ALBUMNAME);

            if (!destinationFile.exists()) {
                destinationFile.mkdirs();
            }

            file = new File(destinationFile, sourceFile.getName());
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            ArrayList<String> favFileList = SharedPrefrance.getFavouriteFileList(FavouriteViewActivity.this);
            favFileList.remove(sourceFile.getAbsolutePath());
            favFileList.add(file.getAbsolutePath());
            SharedPrefrance.setFavouriteFileList(FavouriteViewActivity.this, new ArrayList<>());
            SharedPrefrance.setFavouriteFileList(FavouriteViewActivity.this, favFileList);

            Toast.makeText(this, "Move File to " + file.getParentFile().getAbsolutePath(), Toast.LENGTH_LONG).show();
            if (sourceFile.exists()) {
                Util.delete(FavouriteViewActivity.this, sourceFile);
            }

            scanPhoto(file.toString());

            if (favouriteViewBinding.imgViewPager.getCurrentItem() == privateList.size() - 1) {
                privateList.remove(favouriteViewBinding.imgViewPager.getCurrentItem());
                onBackPressed();
            } else {
                privateList.remove(favouriteViewBinding.imgViewPager.getCurrentItem());
                viewPagerAdapter.notifyDataSetChanged();
            }

            if (privateList.size() == 0)
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

            ArrayList<String> favFileList = SharedPrefrance.getFavouriteFileList(FavouriteViewActivity.this);
            if (favFileList.contains(sourceFile.getAbsolutePath())) {
                favFileList.remove(sourceFile.getAbsolutePath());
                SharedPrefrance.setFavouriteFileList(FavouriteViewActivity.this, new ArrayList<>());
                SharedPrefrance.setFavouriteFileList(FavouriteViewActivity.this, favFileList);
            }

            Toast.makeText(this, "Move File to Recycle Bin", Toast.LENGTH_LONG).show();
            ArrayList<RecycleModel> recycleList = SharedPrefrance.getRecycleBinData(FavouriteViewActivity.this);

            if (recycleList == null)
                recycleList = new ArrayList<>();

            RecycleModel recycleModel = new RecycleModel();
            recycleModel.setImageName(sourceFile.getName());
            recycleModel.setOldImagePath(sendFile.getAbsolutePath());
            recycleModel.setNewImagePath(sourceFile.getAbsolutePath());

            recycleList.add(recycleModel);
            SharedPrefrance.setRecycleBinData(FavouriteViewActivity.this, new ArrayList<>());
            SharedPrefrance.setRecycleBinData(FavouriteViewActivity.this, recycleList);

            if (sendFile.exists()) {
                boolean isDelete = sendFile.delete();
                if (!isDelete) {
                    isDelete = Util.delete(FavouriteViewActivity.this, sendFile);
                }
                Log.e("LLLL_del: ", String.valueOf(isDelete));
            }

            scanPhoto(sendFile.toString());

            if (favouriteViewBinding.imgViewPager.getCurrentItem() == privateList.size() - 1) {
                privateList.remove(favouriteViewBinding.imgViewPager.getCurrentItem());
                onBackPressed();
            } else {
                privateList.remove(favouriteViewBinding.imgViewPager.getCurrentItem());
                viewPagerAdapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyImage(Bitmap finalBitmap, File sourceFile, boolean isCreateAlbum, int position) {

        String[] parts = (sourceFile.getAbsolutePath()).split("/");
        File file;
        if (!isCreateAlbum) {
            file = new File(dateFiles.get(position).getPath(), sourceFile.getName());
        } else {
            File mainDir = Environment.getExternalStorageDirectory();
            File destinationFile = new File(mainDir, ALBUMNAME);

            if (!destinationFile.exists()) {
                destinationFile.mkdirs();
            }
            Log.e("LLL_Name: ", destinationFile.getAbsolutePath());

            file = new File(destinationFile, sourceFile.getName());
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            Toast.makeText(this, "Copy File to " + file.getParentFile().getAbsolutePath(), Toast.LENGTH_LONG).show();

            scanPhoto(file.toString());

        } catch (Exception e) {
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
        if (albumPopUpBehaviour.getState() == BottomSheetBehavior.STATE_HALF_EXPANDED) {
            albumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (albumPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            albumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (mainPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (albumCreatePopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            albumCreatePopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (moveAlbumPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            moveAlbumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (imageInfoUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            imageInfoUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            if (startSlideShow)
                stopSlideShow();
            else {
                super.onBackPressed();

            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (resultCode == RESULT_OK) {

            File file1 = new File(privateList.get(favouriteViewBinding.imgViewPager.getCurrentItem()));
            String[] parts = (file1.getAbsolutePath()).split("/");

            DocumentFile documentFile = DocumentFile.fromTreeUri(this, resultData.getData());
            documentFile = documentFile.findFile(parts[parts.length - 1]);
            if (documentFile == null) {
                File sourceFile = new File(privateList.get(favouriteViewBinding.imgViewPager.getCurrentItem()));
                File file = new File(dateFiles.get(createAlbumPosition).getPath());

                DocumentFile documentFile1 = DocumentFile.fromTreeUri(this, resultData.getData());

                SharedPrefrance.setSharedPreferenceUri(FavouriteViewActivity.this, documentFile1.getUri());
                SharedPrefrance.setSharedPreference(FavouriteViewActivity.this, file1.getParentFile().getAbsolutePath());

                if (isCopyImg) {
                    isCopyImg = false;
                    copySDImage(sourceFile, isCreateAlbum, createAlbumPosition);
                } else if (isDeleteImg) {
                    isDeleteImg = false;
                    if (sourceFile.exists()) {
                        File file2 = new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/.nomedia");

                        if (!file2.exists())
                            file2.mkdirs();

                        File destinations = new File(file2, sourceFile.getName());

                        moveSDImageToRecycleBin(sourceFile, destinations);
                    }

                } else {
                    moveSDImage(sourceFile, isCreateAlbum, createAlbumPosition);
                }
            } else {

                SharedPrefrance.setSharedPreferenceUri(FavouriteViewActivity.this, documentFile.getUri());
                SharedPrefrance.setSharedPreference(FavouriteViewActivity.this, file1.getParentFile().getAbsolutePath());

                getData(file1);
            }

        }
    }

    private void getData(File file1) {

        File file2 = new File(file1.getParentFile().getParentFile().getAbsolutePath(), "." + file1.getName());
        File file3 = new File(file1.getParentFile().getAbsolutePath(), "." + file1.getName());

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

            ArrayList<String> hideFileList = SharedPrefrance.getHideFileList(FavouriteViewActivity.this);
            hideFileList.add(file3.getAbsolutePath());
            SharedPrefrance.setHideFileList(FavouriteViewActivity.this, hideFileList);

            ArrayList<String> favFileList = SharedPrefrance.getFavouriteFileList(FavouriteViewActivity.this);
            favFileList.remove(file1.getAbsolutePath());
            SharedPrefrance.setFavouriteFileList(FavouriteViewActivity.this, new ArrayList<>());
            SharedPrefrance.setFavouriteFileList(FavouriteViewActivity.this, favFileList);

            if (file1.exists()) {
                Util.delete(FavouriteViewActivity.this, file1);
            }

            privateList.remove(favouriteViewBinding.imgViewPager.getCurrentItem());
            viewPagerAdapter.notifyDataSetChanged();
            if (privateList.size() == 0)
                onBackPressed();
            Toast.makeText(this, "Hide " + file1.getName(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "something went wrong" + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void moveSDImage(File sourceFile, boolean isCreateAlbum, int position) {

        File file;
        if (!isCreateAlbum) {
            file = new File(dateFiles.get(position).getPath(), sourceFile.getName());
        } else {
            File mainDir = Environment.getExternalStorageDirectory();
            File destinationFile = new File(mainDir, ALBUMNAME);

            if (!destinationFile.exists()) {
                destinationFile.mkdirs();
            }

            file = new File(destinationFile, sourceFile.getName());
        }

        OutputStream fileOutupStream = null;
        File file2 = new File(file.getParentFile().getParentFile().getAbsolutePath(), sourceFile.getName());
        DocumentFile targetDocument = getDocumentFile(file2, false);

        try {
            fileOutupStream = getContentResolver().openOutputStream(targetDocument.getUri());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(sourceFile.getAbsolutePath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutupStream);
            fileOutupStream.flush();
            fileOutupStream.close();

            ArrayList<String> favFileList = SharedPrefrance.getFavouriteFileList(FavouriteViewActivity.this);
            favFileList.remove(sourceFile.getAbsolutePath());
            favFileList.add(file.getAbsolutePath());
            SharedPrefrance.setFavouriteFileList(FavouriteViewActivity.this, new ArrayList<>());
            SharedPrefrance.setFavouriteFileList(FavouriteViewActivity.this, favFileList);

            if (sourceFile.exists()) {
                Util.delete(FavouriteViewActivity.this, sourceFile);
            }

            scanPhoto(file.toString());

            if (favouriteViewBinding.imgViewPager.getCurrentItem() == privateList.size() - 1) {
                privateList.remove(favouriteViewBinding.imgViewPager.getCurrentItem());
                onBackPressed();
            } else {
                privateList.remove(favouriteViewBinding.imgViewPager.getCurrentItem());
                viewPagerAdapter.notifyDataSetChanged();
            }
            if (privateList.isEmpty())
                onBackPressed();
            Toast.makeText(this, "Move " + sourceFile.getName(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "something went wrong" + e.getMessage(), Toast.LENGTH_SHORT).show();
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

            ArrayList<String> favFileList = SharedPrefrance.getFavouriteFileList(FavouriteViewActivity.this);
            if (favFileList.contains(sourceFile.getAbsolutePath())) {
                favFileList.remove(sourceFile.getAbsolutePath());
                SharedPrefrance.setFavouriteFileList(FavouriteViewActivity.this, new ArrayList<>());
                SharedPrefrance.setFavouriteFileList(FavouriteViewActivity.this, favFileList);
            }

            Toast.makeText(this, "Move File to Recycle Bin", Toast.LENGTH_LONG).show();
            ArrayList<RecycleModel> recycleList = SharedPrefrance.getRecycleBinData(FavouriteViewActivity.this);
            if (recycleList == null)
                recycleList = new ArrayList<>();

            RecycleModel recycleModel = new RecycleModel();
            recycleModel.setImageName(sourceFile.getName());
            recycleModel.setOldImagePath(sentFile.getAbsolutePath());
            recycleModel.setNewImagePath(sourceFile.getAbsolutePath());

            recycleList.add(recycleModel);
            SharedPrefrance.setRecycleBinData(FavouriteViewActivity.this, new ArrayList<>());
            SharedPrefrance.setRecycleBinData(FavouriteViewActivity.this, recycleList);

            if (sentFile.exists()) {
                boolean isDelete = sentFile.delete();
                if (!isDelete) {
                    Util.delete(FavouriteViewActivity.this, sentFile);
                }
            }

            scanPhoto(sentFile.toString());

            if (favouriteViewBinding.imgViewPager.getCurrentItem() == privateList.size() - 1) {
                privateList.remove(favouriteViewBinding.imgViewPager.getCurrentItem());
                onBackPressed();
            } else {
                privateList.remove(favouriteViewBinding.imgViewPager.getCurrentItem());
                viewPagerAdapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            Toast.makeText(this, "something went wrong" + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void copySDImage(File sourceFile, boolean isCreateAlbum, int position) {

        File file;
        if (!isCreateAlbum) {
            file = new File(dateFiles.get(position).getPath(), sourceFile.getName());
        } else {
            File mainDir = Environment.getExternalStorageDirectory();
            File destinationFile = new File(mainDir, ALBUMNAME);

            if (!destinationFile.exists()) {
                destinationFile.mkdirs();
            }

            file = new File(destinationFile, sourceFile.getName());
        }

        OutputStream fileOutupStream = null;
        File file2 = new File(file.getParentFile().getParentFile().getAbsolutePath(), sourceFile.getName());
        DocumentFile targetDocument = getDocumentFile(file2, false);

        try {
            fileOutupStream = getContentResolver().openOutputStream(targetDocument.getUri());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(sourceFile.getAbsolutePath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutupStream);
            fileOutupStream.flush();
            fileOutupStream.close();

            scanPhoto(file.toString());

            Toast.makeText(this, "Copy " + sourceFile.getName(), Toast.LENGTH_LONG).show();

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

        Uri treeUri = Uri.parse(SharedPrefrance.getSharedPreferenceUri(FavouriteViewActivity.this));

        if (treeUri == null) {
            return null;
        }

        // start with root of SD card and then parse through document tree.
        DocumentFile document = DocumentFile.fromTreeUri(FavouriteViewActivity.this, treeUri);

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

    @Override
    protected void onResume() {
        super.onResume();
    }

    public class MyClickHandlers {
        Context context;

        public MyClickHandlers(Context context) {
            this.context = context;
        }

        public void onImageEditListener(View view) {
            fireAnalytics(FavouriteViewActivity.class.getSimpleName(), "Image", "Edit");
            File file = new File(privateList.get(favouriteViewBinding.imgViewPager.getCurrentItem())).getParentFile().getAbsoluteFile();
            Util.FOLDER_NAME = file.getAbsolutePath();

            Log.e("LLLL_Log: ", Util.FOLDER_NAME + "    : ");

            Intent intent = new Intent(FavouriteViewActivity.this, EditActivity.class);
            intent.putExtra("filePath", privateList.get(favouriteViewBinding.imgViewPager.getCurrentItem()));
            startActivity(intent);
        }

        public void onImageDeleteListener(View view) {
            fireAnalytics(FavouriteViewActivity.class.getSimpleName(), "Image", "Delete");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                deleteFile();
            }
        }

        public void onProtectListener(View view) {
            fireAnalytics(FavouriteViewActivity.class.getSimpleName(), "Image", "Hide");
            File sampleFile = Environment.getExternalStorageDirectory();
            File file1 = new File(privateList.get(favouriteViewBinding.imgViewPager.getCurrentItem()));
            Uri uri = Uri.fromFile(file1);

            if (file1.getAbsolutePath().contains(sampleFile.getAbsolutePath())) {
                Bitmap bitmap = BitmapFactory.decodeFile(file1.getAbsolutePath());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    SaveImage(bitmap, file1);
                }
            } else {
                if (!SharedPrefrance.getSharedPreference(FavouriteViewActivity.this).contains(file1.getParentFile().getAbsolutePath())) {
                    Intent intent = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
                    }

                    startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);
                } else {
                    getData(file1);
                }
            }
        }

        public void onPrivateListener(View view) {
            fireAnalytics(FavouriteViewActivity.class.getSimpleName(), "ImageAlbum", "PrivateAlbum");
            if (albumPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                albumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                albumPopUpBehaviour.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
            }

            File sampleFile = Environment.getExternalStorageDirectory();
            File file1 = new File(privateList.get(favouriteViewBinding.imgViewPager.getCurrentItem()));
            Uri uri = Uri.fromFile(file1);

            if (file1.getAbsolutePath().contains(sampleFile.getAbsolutePath())) {
                Bitmap bitmap = BitmapFactory.decodeFile(file1.getAbsolutePath());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    SaveImage(bitmap, file1);
                }
            } else {
                if (!SharedPrefrance.getSharedPreference(FavouriteViewActivity.this).contains(file1.getParentFile().getAbsolutePath())) {
                    Intent intent = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
                    }

                    startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);
                } else {
                    getData(file1);
                }
            }
        }

        public void onMoreListener(View view) {
            if (mainPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }

        public void onAddAlbumListener(View view) {
            mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if (albumPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                albumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                albumPopUpBehaviour.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
            }
        }

        public void onAlbumViewCancelListener(View view) {
            if (albumPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                albumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                albumPopUpBehaviour.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
            }
        }

        public void onCreateAlbumListener(View view) {
            albumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            isCreateAlbum = true;
            createAlbumPosition = 0;
            if (albumCreatePopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                albumCreatePopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                albumCreatePopUpBehaviour.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
            }
        }

        public void onOkListener(View view) {
            fireAnalytics(FavouriteViewActivity.class.getSimpleName(), "ImageAlbum", "Create New Album");
            if (albumCreatePopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                albumCreatePopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            if (moveAlbumPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moveAlbumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                moveAlbumPopUpBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
            }

            if (favouriteViewBinding.etAlbumName.length() > 0) {
                /*Util.hideKeyboard(FavouriteViewActivity.this);*/
                ALBUMNAME = favouriteViewBinding.etAlbumName.getText().toString();
            }
        }

        public void onCancelListener(View view) {
            if (albumCreatePopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                albumCreatePopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            if (albumCreatePopUpBehaviour.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                albumCreatePopUpBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }

        public void onMoveAlbumListener(View view) {
            fireAnalytics(FavouriteViewActivity.class.getSimpleName(), "Image", "Move to another album");
            File sourceFile = new File(privateList.get(favouriteViewBinding.imgViewPager.getCurrentItem()));
            Bitmap bitmap = BitmapFactory.decodeFile(sourceFile.getAbsolutePath());
            File file1;
            File sampleFile = Environment.getExternalStorageDirectory();
            if (isCreateAlbum)
                file1 = new File(sampleFile, ALBUMNAME);
            else
                file1 = new File(dateFiles.get(createAlbumPosition).getPath());

            if (file1.getAbsolutePath().contains(sampleFile.getAbsolutePath())) {
                moveImage(bitmap, sourceFile, isCreateAlbum, createAlbumPosition);
            } else {
                if (!SharedPrefrance.getSharedPreference(FavouriteViewActivity.this).contains(file1.getParentFile().getAbsolutePath())) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                            | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                    intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
                    startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);
                } else {
                    File file = new File(dateFiles.get(position).getPath());
                    moveSDImage(sourceFile, isCreateAlbum, createAlbumPosition);
                }
            }


            if (moveAlbumPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moveAlbumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }

        public void onCopyListener(View view) {
            fireAnalytics(FavouriteViewActivity.class.getSimpleName(), "Image", "Copy to other album");
            File sourceFile = new File(privateList.get(favouriteViewBinding.imgViewPager.getCurrentItem()));
            Bitmap bitmap = BitmapFactory.decodeFile(sourceFile.getAbsolutePath());

            isCopyImg = true;

            File sampleFile = Environment.getExternalStorageDirectory();
            File file1;

            if (isCreateAlbum)
                file1 = new File(sampleFile, ALBUMNAME);
            else
                file1 = new File(dateFiles.get(createAlbumPosition).getPath());

            if (file1.getAbsolutePath().contains(sampleFile.getAbsolutePath())) {
                copyImage(bitmap, sourceFile, isCreateAlbum, createAlbumPosition);
            } else {
                if (!SharedPrefrance.getSharedPreference(FavouriteViewActivity.this).contains(file1.getParentFile().getAbsolutePath())) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                            | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                    intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
                    startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);
                } else {
                    File file = new File(dateFiles.get(position).getPath());
                    copySDImage(sourceFile, isCreateAlbum, createAlbumPosition);
                }
            }

            if (moveAlbumPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moveAlbumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public void onSlideShowListener(View view) {
            slideShowPopUp();
        }

        public void onDetailsListener(View view) {
            fireAnalytics(FavouriteViewActivity.class.getSimpleName(), "Image", "Information");
            if (mainPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            if (imageInfoUpBehaviour.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                imageInfoUpBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }

        public void onSetWallpaperListener(View view) {
            fireAnalytics(FavouriteViewActivity.class.getSimpleName(), "Image", "set wallpaper");
            if (mainPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            startAnim();
            setWallpaper(imageFiles.get(favouriteViewBinding.imgViewPager.getCurrentItem()).getPath());
        }

        public void onShareListener(View view) {
            shareImage(new File(privateList.get(favouriteViewBinding.imgViewPager.getCurrentItem())));
        }

        public void onRenameListener(View view) {
            fireAnalytics(FavouriteViewActivity.class.getSimpleName(), "Image", "Rename");
            if (imageRenameBehaviour.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                imageRenameBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
            if (mainPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }

        public void onRenameOkListener(View view) {
            if (imageRenameBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                imageRenameBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            Log.e("LLLL_Name: ", favouriteViewBinding.etRename.getText().toString());
            if (favouriteViewBinding.etRename.length() > 0) {
                File file = new File(privateList.get(favouriteViewBinding.imgViewPager.getCurrentItem()));
                String extension = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
                File sdcard = new File(file.getParentFile().getAbsolutePath());
                File newFileName = new File(sdcard, favouriteViewBinding.etRename.getText().toString() + "." + extension);
                boolean isRename = file.renameTo(newFileName);
                if (isRename) {
                    ContentResolver resolver = getApplicationContext().getContentResolver();
                    resolver.delete(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA +
                                    " =?", new String[]{file.getAbsolutePath()});
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(Uri.fromFile(newFileName));
                    getApplicationContext().sendBroadcast(intent);
                }

                ArrayList<String> hideFileList = SharedPrefrance.getHideFileList(FavouriteViewActivity.this);
                if (hideFileList.contains(file.getAbsolutePath())) {
                    hideFileList.remove(file.getAbsolutePath());
                    hideFileList.add(newFileName.getAbsolutePath());
                    SharedPrefrance.setHideFileList(FavouriteViewActivity.this, new ArrayList<>());
                    SharedPrefrance.setHideFileList(FavouriteViewActivity.this, hideFileList);
                }

                ArrayList<String> favFileList = SharedPrefrance.getFavouriteFileList(FavouriteViewActivity.this);
                if (favFileList.contains(file.getAbsolutePath())) {
                    favFileList.remove(file.getAbsolutePath());
                    favFileList.add(newFileName.getAbsolutePath());
                    SharedPrefrance.setFavouriteFileList(FavouriteViewActivity.this, new ArrayList<>());
                    SharedPrefrance.setFavouriteFileList(FavouriteViewActivity.this, favFileList);
                }

                favouriteViewBinding.tvFilename.setText(newFileName.getName());
            }
        }

        public void onRenameCanListener(View view) {
            if (imageRenameBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                imageRenameBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }

        public void onPrintListener(View view) {
            fireAnalytics(FavouriteViewActivity.class.getSimpleName(), "Image", "Print");
            if (mainPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            File printFile = new File(privateList.get(favouriteViewBinding.imgViewPager.getCurrentItem()));
            ArrayList<String> printFileLsit = new ArrayList<String>();
            printFileLsit.add(printFile.getAbsolutePath());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                printDocument(printFileLsit);
            }
        }

        public void onBackListener(View view) {
            onBackPressed();
        }

    }

    // Print PDF Documents
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public class MyPrintDocumentAdapter extends PrintDocumentAdapter {
        Context context;
        ArrayList<String> path;

        public MyPrintDocumentAdapter(Context context, ArrayList<String> path) {
            this.context = context;
            this.path = path;
        }

        @Override
        public void onLayout(PrintAttributes oldAttributes,
                             PrintAttributes newAttributes,
                             CancellationSignal cancellationSignal,
                             LayoutResultCallback callback,
                             Bundle metadata) {

            myPdfDocument = new PrintedPdfDocument(context, newAttributes);

            pageHeight =
                    newAttributes.getMediaSize().getHeightMils() / 1000 * 72;
            pageWidth =
                    newAttributes.getMediaSize().getWidthMils() / 1000 * 72;

            if (cancellationSignal.isCanceled()) {
                callback.onLayoutCancelled();
                return;
            }

            if (path.size() > 0) {
                PrintDocumentInfo.Builder builder = new PrintDocumentInfo
                        .Builder("print_output.pdf").setContentType(
                        PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(path.size());

                PrintDocumentInfo info = builder.build();
                callback.onLayoutFinished(info, true);
            } else {
                callback.onLayoutFailed("Page count is zero.");
            }

        }

        @Override
        public void onWrite(final PageRange[] pageRanges,
                            final ParcelFileDescriptor destination,
                            final CancellationSignal
                                    cancellationSignal,
                            final WriteResultCallback callback) {

            for (int i = 0; i < path.size(); i++) {
                if (pageInRange(pageRanges, i)) {
                    android.graphics.pdf.PdfDocument.PageInfo newPage = new android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth,
                            pageHeight, i).create();

                    PdfDocument.Page page =
                            myPdfDocument.startPage(newPage);

                    if (cancellationSignal.isCanceled()) {
                        callback.onWriteCancelled();
                        myPdfDocument.close();
                        myPdfDocument = null;
                        return;
                    }
                    drawPage(page, path.get(i));
                    myPdfDocument.finishPage(page);
                }
            }

            try {
                myPdfDocument.writeTo(new FileOutputStream(
                        destination.getFileDescriptor()));
            } catch (IOException e) {
                callback.onWriteFailed(e.toString());
                return;
            } finally {
                myPdfDocument.close();
                myPdfDocument = null;
            }

            callback.onWriteFinished(pageRanges);

        }
    }

    private final class LongOperation extends AsyncTask<Void, Void, ArrayList<String>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            ArrayList<String> pathList;
            pathList = SharedPrefrance.getFavouriteFileList(FavouriteViewActivity.this);
            return pathList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            privateList = result;
            int finalPosition1 = finalPosition;
            runOnUiThread(() -> {
                viewPagerAdapter = new FavImageViewAdapter(FavouriteViewActivity.this, result, "");
                favouriteViewBinding.imgViewPager.setAdapter(viewPagerAdapter);
                favouriteViewBinding.imgViewPager.setCurrentItem(finalPosition1);

                ArrayList<String> favList = SharedPrefrance.getFavouriteFileList(FavouriteViewActivity.this);
                favouriteViewBinding.imgFav.setSelected(favList.contains(privateList.get(favouriteViewBinding.imgViewPager.getCurrentItem())));

                favouriteViewBinding.imgFav.setOnClickListener(v -> {
                    ArrayList<String> favList1 = SharedPrefrance.getFavouriteFileList(FavouriteViewActivity.this);
                    if (v.isSelected()) {
                        favList1.remove(privateList.get(favouriteViewBinding.imgViewPager.getCurrentItem()));
                        privateList.remove(privateList.get(favouriteViewBinding.imgViewPager.getCurrentItem()));
                        viewPagerAdapter.notifyDataSetChanged();
                        if (privateList.isEmpty())
                            onBackPressed();
                    }
                    SharedPrefrance.setFavouriteFileList(FavouriteViewActivity.this, new ArrayList<>());
                    SharedPrefrance.setFavouriteFileList(FavouriteViewActivity.this, favList1);
                });

                File file = new File(result.get(finalPosition1));
                favouriteViewBinding.setFileName(file.getName());
                String time = DateFormat.format("dd MMM yyyy hh:mm a", file.lastModified()).toString();
                favouriteViewBinding.setFileDateTime(time);

                // File Information
                favouriteViewBinding.setFilePath(file.getAbsolutePath());
                favouriteViewBinding.setFileModified(Util.convertTimeDateModifiedShown(file.lastModified()));
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                int imageHeight = options.outHeight;
                int imageWidth = options.outWidth;
                favouriteViewBinding.setFileResolution(imageWidth + " x " + imageHeight);
                favouriteViewBinding.setFileSize(Util.getFileSize(file.length()));

                favouriteViewBinding.imgViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                        if (mainPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                            mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        } else if (albumPopUpBehaviour.getState() == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                            albumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        } else if (albumPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                            albumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        } else if (albumCreatePopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                            albumCreatePopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        } else if (moveAlbumPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                            moveAlbumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        }
                    }

                    @Override
                    public void onPageSelected(int position) {
                        File file = new File(result.get(position));
                        favouriteViewBinding.setFileName(file.getName());
                        String time = DateFormat.format("dd MMM yyyy hh:mm a", file.lastModified()).toString();
                        favouriteViewBinding.setFileDateTime(time);
                        favouriteViewBinding.imgFav.setSelected(true);
                        favouriteViewBinding.setFilePath(file.getAbsolutePath());
                        favouriteViewBinding.setFileModified(Util.convertTimeDateModifiedShown(file.lastModified()));
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                        int imageHeight = options.outHeight;
                        int imageWidth = options.outWidth;
                        favouriteViewBinding.setFileResolution(imageWidth + " x " + imageHeight);
                        favouriteViewBinding.setFileSize(Util.getFileSize(file.length()));
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });

            });
        }
    }

    class LoadAlbumImages extends AsyncTask<Void, Void, List<Directory<ImageFile>>> {

        @SuppressLint("StaticFieldLeak")
        FragmentActivity fragmentActivity;
        List<ImageFile> list1 = new ArrayList<>();

        public LoadAlbumImages(FragmentActivity fragmentActivity) {
            this.fragmentActivity = fragmentActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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

            fragmentActivity.runOnUiThread(() -> {
                createAlbumAdapter.clearData();
                createAlbumAdapter.addAll(directories);
            });
        }
    }
}