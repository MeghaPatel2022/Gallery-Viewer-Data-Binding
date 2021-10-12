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

import java.util.ArrayList;

import gallery.photoapp.gallerypro.photoviewer.R;
import gallery.photoapp.gallerypro.photoviewer.RecycleBinViewActivity;
import gallery.photoapp.gallerypro.photoviewer.constant.Util;
import gallery.photoapp.gallerypro.photoviewer.databinding.ListPrivateImageBinding;
import gallery.photoapp.gallerypro.photoviewer.model.RecycleModel;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class RecycleBinAdapter extends RecyclerView.Adapter<RecycleBinAdapter.MyClassView> {

    ArrayList<RecycleModel> pathList;
    Activity activity;

    public RecycleBinAdapter(ArrayList<RecycleModel> pathList, Activity activity) {
        this.pathList = pathList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyClassView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListPrivateImageBinding privateImageBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.list_private_image, parent, false);
        ViewGroup.LayoutParams params = privateImageBinding.getRoot().getLayoutParams();
        if (params != null) {
            WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
            int width = wm.getDefaultDisplay().getWidth();
            params.height = width / Util.COLUMN_TYPE;
        }

        return new MyClassView(privateImageBinding);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull MyClassView holder, int position) {
        RecycleModel recycleModel = pathList.get(position);

        holder.privateImageBinding.mImage.setClipToOutline(true);
        String filePath = recycleModel.getNewImagePath();

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
                    .into(holder.privateImageBinding.mImage);
        } else {
            Glide.with(activity)
                    .load(filePath)
                    .apply(options.centerCrop()
                            .skipMemoryCache(true)
                            .priority(Priority.LOW))
                    .transition(withCrossFade())
                    .transition(new DrawableTransitionOptions().crossFade(500))
                    .into(holder.privateImageBinding.mImage);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(activity, RecycleBinViewActivity.class);
            intent.putExtra("Position", position);
            activity.startActivity(intent);

        });
    }

    @Override
    public int getItemCount() {
        return pathList.size();
    }

    public class MyClassView extends RecyclerView.ViewHolder {

        private final ListPrivateImageBinding privateImageBinding;

        public MyClassView(ListPrivateImageBinding privateImageBinding) {
            super(privateImageBinding.getRoot());
            this.privateImageBinding = privateImageBinding;
        }
    }
}
