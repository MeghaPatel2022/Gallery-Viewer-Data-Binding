package gallery.photoapp.gallerypro.photoviewer.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
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

import gallery.photoapp.gallerypro.photoviewer.AlbumActivity;
import gallery.photoapp.gallerypro.photoviewer.R;
import gallery.photoapp.gallerypro.photoviewer.databinding.ListAlbumListBinding;
import gallery.photoapp.gallerypro.photoviewer.model.Directory;
import gallery.photoapp.gallerypro.photoviewer.model.ImageFile;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class AlbumListAdapter extends RecyclerView.Adapter<AlbumListAdapter.MyClassView> {

    private static final String TAG = AlbumListAdapter.class.getSimpleName();
    private final RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
    List<Directory<ImageFile>> files;
    Activity activity;

    public AlbumListAdapter(List<Directory<ImageFile>> files, Activity activity) {
        this.files = files;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyClassView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(activity).inflate(R.layout.list_album_list, parent, false);
        ListAlbumListBinding albumListBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.list_album_list, parent, false);
        return new MyClassView(albumListBinding);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull MyClassView holder, int position) {
        Directory<ImageFile> DateName = files.get(position);

        holder.albumListBinding.setDirectory(DateName);

        ImageFile file = DateName.getFiles().get(0);

        holder.albumListBinding.imgAlbum.setClipToOutline(true);
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
                    .into(holder.albumListBinding.imgAlbum);
        } else {
            Glide.with(activity)
                    .load(file.getPath())
                    .apply(options.centerCrop()
                            .skipMemoryCache(true)
                            .priority(Priority.LOW))
                    .transition(withCrossFade())
                    .transition(new DrawableTransitionOptions().crossFade(500))
                    .into(holder.albumListBinding.imgAlbum);
        }

        holder.albumListBinding.getRoot().setOnClickListener(v -> {
            Intent intent = new Intent(activity, AlbumActivity.class);
            intent.putExtra("DirName", DateName.getName());
            activity.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public void addAll(List<Directory<ImageFile>> imgMain1DownloadList) {
        this.files.addAll(imgMain1DownloadList);
        if (this.files.size() >= 10)
            notifyItemRangeChanged(this.files.size() - 10, this.files.size());
        else
            notifyDataSetChanged();
    }

    public void clearData() {
        this.files.clear();
    }


    public class MyClassView extends RecyclerView.ViewHolder {

        private final ListAlbumListBinding albumListBinding;

        public MyClassView(ListAlbumListBinding albumListBinding) {
            super(albumListBinding.getRoot());
            this.albumListBinding = albumListBinding;
        }
    }
}
