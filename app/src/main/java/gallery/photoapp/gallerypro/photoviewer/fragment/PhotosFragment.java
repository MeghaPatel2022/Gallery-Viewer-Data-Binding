package gallery.photoapp.gallerypro.photoviewer.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import gallery.photoapp.gallerypro.photoviewer.R;
import gallery.photoapp.gallerypro.photoviewer.adapter.ImageAdapter;
import gallery.photoapp.gallerypro.photoviewer.constant.Util;
import gallery.photoapp.gallerypro.photoviewer.databinding.FragmentPhotosBinding;
import gallery.photoapp.gallerypro.photoviewer.model.Directory;
import gallery.photoapp.gallerypro.photoviewer.model.ImageFile;
import pub.devrel.easypermissions.EasyPermissions;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.MediaColumns.BUCKET_DISPLAY_NAME;
import static android.provider.MediaStore.MediaColumns.BUCKET_ID;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_MODIFIED;
import static android.provider.MediaStore.MediaColumns.ORIENTATION;
import static android.provider.MediaStore.MediaColumns.SIZE;
import static android.provider.MediaStore.MediaColumns.TITLE;

public class PhotosFragment extends BaseFragment {

    private static final String TAG = PhotosFragment.class.getSimpleName();
    private final List<ImageFile> imageFiles = new ArrayList<>();
    private final List<Object> objects = new ArrayList<>();
    public List<Directory<ImageFile>> dateFiles = new ArrayList<>();
    public ArrayList<String> dateList = new ArrayList<>();
    FragmentPhotosBinding photosBinding;
    //    ImageParentAdapter imageParentAdapter;
    ImageAdapter imageAdapter;
    GridLayoutManager linearLayoutManager;
    private boolean isSet = false;
    private MyReceiver myReceiver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myReceiver = new MyReceiver();
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(myReceiver,
                new IntentFilter("TAG_REFRESH"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        photosBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_photos, container, false);

        linearLayoutManager = new GridLayoutManager(getContext(), Util.COLUMN_TYPE);
        photosBinding.rvImages.setLayoutManager(linearLayoutManager);
        photosBinding.rvImages.setScrollContainer(true);
        photosBinding.rvImages.setLayoutAnimation(null);
        photosBinding.rvImages.setItemAnimator(null);

        imageAdapter = new ImageAdapter(new ArrayList<>(), getActivity());
        photosBinding.rvImages.setAdapter(imageAdapter);

        return photosBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean isGranted = EasyPermissions.hasPermissions(getContext(), "android.permission.READ_EXTERNAL_STORAGE");
        if (isGranted) {
            isSet = true;
            new LoadImages(getActivity()).execute();
        }

    }

    @Override
    void permissionGranted() {

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(myReceiver);
    }

    void startAnim() {
        photosBinding.rlLoading.setVisibility(View.VISIBLE);
    }

    void stopAnim() {
        photosBinding.rlLoading.setVisibility(View.GONE);
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
            imageFiles.clear();
            dateList.clear();
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
                    DATE_MODIFIED,
                    MediaStore.Images.Media.ORIENTATION
            };

            String selection = MediaStore.MediaColumns.MIME_TYPE + "=? or " + MediaStore.MediaColumns.MIME_TYPE + "=? or " + MediaStore.MediaColumns.MIME_TYPE + "=? or " + MediaStore.MediaColumns.MIME_TYPE + "=?";

            String[] selectionArgs;
            selectionArgs = new String[]{"image/jpeg", "image/png", "image/jpg", "image/gif"};

            Cursor data = requireActivity().getContentResolver().query(MediaStore.Files.getContentUri("external"),
                    FILE_PROJECTION,
                    selection,
                    selectionArgs,
                    DATE_MODIFIED + " DESC");

            List<Directory<ImageFile>> directories = new ArrayList<>();

            if (data.getPosition() != -1) {
                data.moveToPosition(-1);
            }

            int i = 0;
            int position = 0;

            while (data.moveToNext()) {
                //Create a File instance
                if (data.getString(data.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME)) != null) {
                    ImageFile img = new ImageFile();

                    i = data.getPosition();

                    if (!data.getString(data.getColumnIndexOrThrow(TITLE)).startsWith(".")) {
                        img.setId(data.getLong(data.getColumnIndexOrThrow(_ID)));
                        img.setName(data.getString(data.getColumnIndexOrThrow(TITLE)));
                        img.setPath(data.getString(data.getColumnIndexOrThrow(DATA)));
                        img.setSize(data.getLong(data.getColumnIndexOrThrow(SIZE)));
                        img.setBucketId(data.getString(data.getColumnIndexOrThrow(BUCKET_ID)));
                        img.setBucketName(data.getString(data.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME)));
                        img.setDate(Util.convertTimeDateModified(data.getLong(data.getColumnIndexOrThrow(DATE_MODIFIED))));
                        img.setOrientation(data.getInt(data.getColumnIndexOrThrow(ORIENTATION)));
                        img.setDateTitle(img.getDate());

                        if (!dateList.contains(img.getDate())) {
                            dateList.add(img.getDate());
                            img.setDirectory(true);
                            data.moveToPosition(i - 1);
                            img.setPosition(position);
                            position = position + 1;
                        } else {
                            img.setDirectory(false);
                        }

                        imageFiles.add(img);
                    }
                }
            }

            data.close();

            return directories;
        }

        @Override
        protected void onPostExecute(List<Directory<ImageFile>> directories) {
            super.onPostExecute(directories);
            Log.d(TAG, "onPostExecute: " + "done");

            fragmentActivity.runOnUiThread(() -> {

                imageAdapter.addAll(imageFiles);
                imageAdapter.notifyDataSetChanged();
                linearLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        if (imageAdapter != null) {
                            if (imageAdapter.getItemViewType(position) == ImageAdapter.DATE_TYPE) {
                                return Util.COLUMN_TYPE;
                            }
                        }
                        return 1;
                    }
                });
                photosBinding.rvImages.setLayoutManager(linearLayoutManager);
            });
            stopAnim();

        }
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            linearLayoutManager = new GridLayoutManager(requireContext(), Util.COLUMN_TYPE);
            photosBinding.rvImages.setLayoutManager(linearLayoutManager);
            photosBinding.rvImages.setScrollContainer(true);
            photosBinding.rvImages.setLayoutAnimation(null);
            photosBinding.rvImages.setItemAnimator(null);

            imageAdapter = new ImageAdapter(new ArrayList<>(), getActivity());
            photosBinding.rvImages.setAdapter(imageAdapter);
            new LoadImages(getActivity()).execute();
        }
    }
}
