package gallery.photoapp.gallerypro.photoviewer.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

import gallery.photoapp.gallerypro.photoviewer.PrivateViewActivity;
import gallery.photoapp.gallerypro.photoviewer.R;
import gallery.photoapp.gallerypro.photoviewer.constant.Util;
import gallery.photoapp.gallerypro.photoviewer.databinding.ListPrivateImageBinding;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class PrivateImageAdapter extends RecyclerView.Adapter<PrivateImageAdapter.MyClassView> {

    private final FirebaseAnalytics mFirebaseAnalytics;
    ArrayList<String> pathList;
    Activity activity;

    public PrivateImageAdapter(ArrayList<String> pathList, Activity activity) {
        this.pathList = pathList;
        this.activity = activity;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
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

        String filePath = pathList.get(position);

        holder.privateImageBinding.mImage.setClipToOutline(true);

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

        holder.privateImageBinding.getRoot().setOnClickListener(v -> {
            fireAnalytics(PrivateImageAdapter.class.getSimpleName(), "PrivateImage", "Open");
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

    public void addAll(ArrayList<String> pathList) {
        this.pathList = new ArrayList<>();
        this.pathList = pathList;
        notifyDataSetChanged();
    }

    public void clearData() {
        this.pathList.clear();
        notifyDataSetChanged();
    }

    private void fireAnalytics(String arg1, String arg2, String arg3) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, arg1);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, arg2);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, arg3);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    public class MyClassView extends RecyclerView.ViewHolder {

        private final ListPrivateImageBinding privateImageBinding;

        public MyClassView(ListPrivateImageBinding privateImageBinding) {
            super(privateImageBinding.getRoot());

            this.privateImageBinding = privateImageBinding;
        }
    }
}
