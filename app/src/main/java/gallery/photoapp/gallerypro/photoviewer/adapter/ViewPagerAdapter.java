package gallery.photoapp.gallerypro.photoviewer.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import gallery.photoapp.gallerypro.photoviewer.R;
import gallery.photoapp.gallerypro.photoviewer.databinding.ListPagerBinding;
import gallery.photoapp.gallerypro.photoviewer.model.ImageFile;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class ViewPagerAdapter extends PagerAdapter {
    private final LayoutInflater inflater;
    private final Activity activity;
    List<ImageFile> files;
    private int directoryPosition = 0;

    public ViewPagerAdapter(Activity activity, List<ImageFile> images, int directoryPosition) {
        this.activity = activity;
        this.files = images;
        this.directoryPosition = directoryPosition;
        inflater = LayoutInflater.from(activity);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @Override
    public int getCount() {
        return files.size();
    }


    @Override
    public Object instantiateItem(ViewGroup view, int position) {

        ListPagerBinding listPagerBinding;

        listPagerBinding = DataBindingUtil.inflate(inflater, R.layout.list_pager, view, false);

        ImageFile file = files.get(position);

        Log.e("LLL_ActualPath: ", file.getPath());

        RequestOptions options = new RequestOptions();
        if (file.getPath().endsWith(".PNG") || file.getPath().endsWith(".png")) {
            Glide.with(activity)
                    .load(file.getPath())
                    .apply(options.skipMemoryCache(true)
                            .priority(Priority.LOW)
                            .format(DecodeFormat.PREFER_ARGB_8888))
                    .transition(withCrossFade())
                    .transition(new DrawableTransitionOptions().crossFade(500))
                    .into(listPagerBinding.imgImages);
        } else {
            Glide.with(activity)
                    .load(file.getPath())
                    .apply(options.skipMemoryCache(true)
                            .priority(Priority.LOW))
                    .transition(withCrossFade())
                    .transition(new DrawableTransitionOptions().crossFade(500))
                    .into(listPagerBinding.imgImages);
        }

        view.addView(listPagerBinding.getRoot(), 0);
        return listPagerBinding.getRoot();
    }

    public void addAll(List<ImageFile> imgMain1DownloadList) {
        this.files.addAll(imgMain1DownloadList);
        notifyDataSetChanged();
    }
}
