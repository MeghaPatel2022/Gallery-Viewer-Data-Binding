package gallery.photoapp.gallerypro.photoviewer.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.util.ArrayList;

import gallery.photoapp.gallerypro.photoviewer.PrivateViewActivity;
import gallery.photoapp.gallerypro.photoviewer.R;
import gallery.photoapp.gallerypro.photoviewer.constant.Util;
import gallery.photoapp.gallerypro.photoviewer.databinding.ListImageListBinding;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class PrivateListImageAdapter extends RecyclerView.Adapter<PrivateListImageAdapter.MyClassView> {

    private final FirebaseAnalytics mFirebaseAnalytics;
    ArrayList<String> pathList;
    Activity activity;

    public PrivateListImageAdapter(ArrayList<String> pathList, Activity activity) {
        this.pathList = pathList;
        this.activity = activity;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
    }

    @NonNull
    @Override
    public MyClassView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListImageListBinding imageListBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.list_image_list, parent, false);
        return new MyClassView(imageListBinding);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull MyClassView holder, int position) {
        String filePath = pathList.get(position);

        holder.imageListBinding.imgAlbum.setClipToOutline(true);
        File newFile = new File(filePath);
        holder.imageListBinding.tvImageName.setText(newFile.getName());
        holder.imageListBinding.tvImageDate.setText(Util.getDurationString(newFile.lastModified()));
        holder.imageListBinding.tvImageSize.setText(Util.getFileSize(newFile.length()));

        RequestOptions options = new RequestOptions();
        if (filePath.endsWith(".PNG") || filePath.endsWith(".png")) {
            Glide.with(activity)
                    .load(filePath)
                    .apply(options.centerCrop()
                            .skipMemoryCache(true)
                            .priority(Priority.LOW)
                            .format(DecodeFormat.PREFER_ARGB_8888))
                    .transition(withCrossFade())
                    .transition(new DrawableTransitionOptions().crossFade(500))
                    .into(holder.imageListBinding.imgAlbum);
        } else {
            Glide.with(activity)
                    .load(filePath)
                    .apply(options.centerCrop()
                            .skipMemoryCache(true)
                            .priority(Priority.LOW))
                    .transition(withCrossFade())
                    .transition(new DrawableTransitionOptions().crossFade(500))
                    .into(holder.imageListBinding.imgAlbum);
        }

        holder.itemView.setOnClickListener(v -> {
            fireAnalytics(PrivateListImageAdapter.class.getSimpleName(), "ListPrivateImage", "Open");
            Intent intent = new Intent(activity, PrivateViewActivity.class);
            intent.putExtra("Position", position);
            intent.putExtra("from", "PrivateImageFragment");
            intent.putExtra("directoryPosition", 0);
            activity.startActivity(intent);
            activity.finish();
        });
    }

    @Override
    public int getItemCount() {
        return pathList.size();
    }

    private void fireAnalytics(String arg1, String arg2, String arg3) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, arg1);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, arg2);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, arg3);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    public class MyClassView extends RecyclerView.ViewHolder {

        private final ListImageListBinding imageListBinding;

        public MyClassView(ListImageListBinding imageListBinding) {
            super(imageListBinding.getRoot());

            this.imageListBinding = imageListBinding;
        }
    }
}
