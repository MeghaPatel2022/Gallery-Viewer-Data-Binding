package gallery.photoapp.gallerypro.photoviewer.adapter;

import android.app.Activity;
import android.os.Build;
import android.util.Log;
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

import gallery.photoapp.gallerypro.photoviewer.R;
import gallery.photoapp.gallerypro.photoviewer.databinding.ListAlbumListBinding;
import gallery.photoapp.gallerypro.photoviewer.model.Directory;
import gallery.photoapp.gallerypro.photoviewer.model.ImageFile;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class CreateAlbumAdapter extends RecyclerView.Adapter<CreateAlbumAdapter.MyClassView> {

    private static final String TAG = CreateAlbumAdapter.class.getSimpleName();
    private final RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
    private final ItemClickListener mItemClickListener;
    List<Directory<ImageFile>> files;
    Activity activity;

    public CreateAlbumAdapter(List<Directory<ImageFile>> files, Activity activity, ItemClickListener itemClickListener) {
        this.files = files;
        this.activity = activity;
        this.mItemClickListener = itemClickListener;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull MyClassView holder, int position) {
        Log.e("LLLL_Name: ", files.get(position).getName());
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

        holder.itemView.setOnClickListener(v -> {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(position);
            }
        });
    }

    @NonNull
    @Override
    public MyClassView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListAlbumListBinding albumListBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.list_album_list, parent, false);
        return new MyClassView(albumListBinding);
    }

    @Override
    public int getItemCount() {
        Log.e("LLL_Size: ", String.valueOf(files.size()));
        return files.size();
    }

    public void addAll(List<Directory<ImageFile>> imgMain1DownloadList) {
        this.files.addAll(imgMain1DownloadList);
        activity.runOnUiThread(() -> notifyDataSetChanged());
    }

    public void clearData() {
        this.files.clear();
        notifyDataSetChanged();
    }

    //Define your Interface method here
    public interface ItemClickListener {
        void onItemClick(int position);
    }

    public class MyClassView extends RecyclerView.ViewHolder {

        private final ListAlbumListBinding albumListBinding;

        public MyClassView(ListAlbumListBinding albumListBinding) {
            super(albumListBinding.getRoot());

            this.albumListBinding = albumListBinding;
        }
    }
}
