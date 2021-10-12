package gallery.photoapp.gallerypro.photoviewer.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
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

import java.util.List;

import gallery.photoapp.gallerypro.photoviewer.AlbumViewActivity;
import gallery.photoapp.gallerypro.photoviewer.R;
import gallery.photoapp.gallerypro.photoviewer.constant.Util;
import gallery.photoapp.gallerypro.photoviewer.databinding.ListImageListBinding;
import gallery.photoapp.gallerypro.photoviewer.model.ImageFile;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class AlbumChildListAdapter extends RecyclerView.Adapter<AlbumChildListAdapter.MyClassView> {

    List<ImageFile> images;
    Activity activity;
    int direPos = 0;

    public AlbumChildListAdapter(List<ImageFile> images, Activity activity, int direPos) {
        this.images = images;
        this.activity = activity;
        this.direPos = direPos;
    }

    @NonNull
    @Override
    public MyClassView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListImageListBinding listBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.list_image_list, parent, false);
        return new MyClassView(listBinding);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull MyClassView holder, int position) {
        ImageFile file = images.get(position);

        holder.listBinding.setImageFile(file);

        holder.listBinding.imgAlbum.setClipToOutline(true);
        holder.listBinding.tvImageSize.setText(Util.getFileSize(file.getSize()));

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
                    .into(holder.listBinding.imgAlbum);
        } else {
            Glide.with(activity)
                    .load(file.getPath())
                    .apply(options.centerCrop()
                            .skipMemoryCache(true)
                            .priority(Priority.LOW))
                    .transition(withCrossFade())
                    .transition(new DrawableTransitionOptions().crossFade(500))
                    .into(holder.listBinding.imgAlbum);
        }

        holder.itemView.setOnClickListener(v -> {
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

    public class MyClassView extends RecyclerView.ViewHolder {

        private final ListImageListBinding listBinding;

        public MyClassView(ListImageListBinding listBinding) {
            super(listBinding.getRoot());

            this.listBinding = listBinding;
        }
    }
}
