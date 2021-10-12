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

import gallery.photoapp.gallerypro.photoviewer.AlbumActivity;
import gallery.photoapp.gallerypro.photoviewer.R;
import gallery.photoapp.gallerypro.photoviewer.constant.Util;
import gallery.photoapp.gallerypro.photoviewer.databinding.ListAlbumBinding;
import gallery.photoapp.gallerypro.photoviewer.model.Directory;
import gallery.photoapp.gallerypro.photoviewer.model.ImageFile;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.MyClassView> {

    private static final String TAG = AlbumAdapter.class.getSimpleName();
    private final RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
    List<Directory<ImageFile>> files;
    Activity activity;

    public AlbumAdapter(List<Directory<ImageFile>> files, Activity activity) {
        this.files = files;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyClassView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListAlbumBinding listAlbumBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.list_album, parent, false);
        ViewGroup.LayoutParams params = listAlbumBinding.getRoot().getLayoutParams();
        if (params != null) {
            WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
            int width = wm.getDefaultDisplay().getWidth();
            params.height = width / Util.COLUMN_TYPE;
        }
        return new MyClassView(listAlbumBinding);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull MyClassView holder, int position) {
        Directory<ImageFile> DateName = files.get(position);
        holder.listAlbumBinding.setDirectory(DateName);

        ImageFile file = DateName.getFiles().get(0);

        holder.listAlbumBinding.mImage.setClipToOutline(true);
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
                    .into(holder.listAlbumBinding.mImage);
        } else {
            Glide.with(activity)
                    .load(file.getPath())
                    .apply(options.centerCrop()
                            .skipMemoryCache(true)
                            .priority(Priority.LOW))
                    .transition(withCrossFade())
                    .transition(new DrawableTransitionOptions().crossFade(500))
                    .into(holder.listAlbumBinding.mImage);
        }

        holder.listAlbumBinding.getRoot().setOnClickListener(v -> {
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

        private final ListAlbumBinding listAlbumBinding;

        public MyClassView(ListAlbumBinding listAlbumBinding) {
            super(listAlbumBinding.getRoot());
            this.listAlbumBinding = listAlbumBinding;
        }
    }
}
