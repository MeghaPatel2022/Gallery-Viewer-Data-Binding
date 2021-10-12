package gallery.photoapp.gallerypro.photoviewer.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import gallery.photoapp.gallerypro.photoviewer.AlbumViewActivity;
import gallery.photoapp.gallerypro.photoviewer.R;
import gallery.photoapp.gallerypro.photoviewer.constant.Util;
import gallery.photoapp.gallerypro.photoviewer.databinding.ImageViewBinding;
import gallery.photoapp.gallerypro.photoviewer.model.ImageFile;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class AlbumChildAdapter extends RecyclerView.Adapter<AlbumChildAdapter.MyClassView> {

    List<ImageFile> images;
    Activity activity;
    int direPos = 0;

    public AlbumChildAdapter(List<ImageFile> images, Activity activity, int i) {
        this.images = images;
        this.activity = activity;
        this.direPos = i;
    }

    @NonNull
    @Override
    public MyClassView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageViewBinding imageViewBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.image_view, parent, false);
        ViewGroup.LayoutParams params = imageViewBinding.getRoot().getLayoutParams();
        if (params != null) {
            WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
            int width = wm.getDefaultDisplay().getWidth();
            params.height = width / Util.COLUMN_TYPE;
        }
        return new MyClassView(imageViewBinding);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull MyClassView holder, int position) {

        ImageFile file = images.get(position);

        holder.imageViewBinding.mImage.setClipToOutline(true);

        RequestOptions options = new RequestOptions();
        if (file.getPath().endsWith(".PNG") || file.getPath().endsWith(".png")) {
            Glide.with(activity)
                    .load(file.getPath())
                    .apply(options.centerCrop()
                            .skipMemoryCache(true)
                            .priority(Priority.LOW)
                            .format(DecodeFormat.PREFER_ARGB_8888))
                    .transition(withCrossFade())
                    .transition(new DrawableTransitionOptions().crossFade(500))
                    .into(holder.imageViewBinding.mImage);
        } else {
            Glide.with(activity)
                    .load(file.getPath())
                    .apply(options.centerCrop()
                            .skipMemoryCache(true)
                            .priority(Priority.LOW))
                    .transition(withCrossFade())
                    .transition(new DrawableTransitionOptions().crossFade(500))
                    .into(holder.imageViewBinding.mImage);
        }

        holder.imageViewBinding.getRoot().setOnClickListener(v -> {
            Intent intent = new Intent(activity, AlbumViewActivity.class);
            intent.putExtra("Position", position);
            intent.putExtra("DirPos", direPos);
            intent.putExtra("from", "ImageFragment");
            activity.startActivity(intent);

        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class MyClassView extends RecyclerView.ViewHolder {

        private final ImageViewBinding imageViewBinding;

        public MyClassView(ImageViewBinding imageViewBinding) {
            super(imageViewBinding.getRoot());
            this.imageViewBinding = imageViewBinding;
        }
    }
}
