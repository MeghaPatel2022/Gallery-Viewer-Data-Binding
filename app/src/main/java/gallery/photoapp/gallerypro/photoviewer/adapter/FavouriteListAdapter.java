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

import java.io.File;
import java.util.ArrayList;

import gallery.photoapp.gallerypro.photoviewer.FavouriteViewActivity;
import gallery.photoapp.gallerypro.photoviewer.R;
import gallery.photoapp.gallerypro.photoviewer.constant.Util;
import gallery.photoapp.gallerypro.photoviewer.databinding.ListImageListBinding;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class FavouriteListAdapter extends RecyclerView.Adapter<FavouriteListAdapter.MyClassView> {

    ArrayList<String> pathList;
    Activity activity;

    public FavouriteListAdapter(ArrayList<String> pathList, Activity activity) {
        this.pathList = pathList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyClassView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListImageListBinding listImageListBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.list_image_list, parent, false);
        return new MyClassView(listImageListBinding);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull MyClassView holder, int position) {
        String filePath = pathList.get(position);

        holder.listImageListBinding.imgAlbum.setClipToOutline(true);
        File newFile = new File(filePath);
        holder.listImageListBinding.tvImageName.setText(newFile.getName());
        holder.listImageListBinding.tvImageDate.setText(Util.getDurationString(newFile.lastModified()));
        holder.listImageListBinding.tvImageSize.setText(Util.getFileSize(newFile.length()));

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
                    .into(holder.listImageListBinding.imgAlbum);
        } else {
            Glide.with(activity)
                    .load(filePath)
                    .apply(options.centerCrop()
                            .skipMemoryCache(true)
                            .priority(Priority.LOW))
                    .transition(withCrossFade())
                    .transition(new DrawableTransitionOptions().crossFade(500))
                    .into(holder.listImageListBinding.imgAlbum);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(activity, FavouriteViewActivity.class);
            intent.putExtra("Position", position);
            intent.putExtra("from", "FavImageFragment");
            intent.putExtra("directoryPosition", 0);
            activity.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return pathList.size();
    }

    public class MyClassView extends RecyclerView.ViewHolder {

        private final ListImageListBinding listImageListBinding;

        public MyClassView(ListImageListBinding listImageListBinding) {
            super(listImageListBinding.getRoot());

            this.listImageListBinding = listImageListBinding;
        }
    }
}
