package gallery.photoapp.gallerypro.photoviewer.adapter;

import android.app.Activity;
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

import java.util.ArrayList;
import java.util.List;

import gallery.photoapp.gallerypro.photoviewer.R;
import gallery.photoapp.gallerypro.photoviewer.databinding.ListPagerBinding;
import gallery.photoapp.gallerypro.photoviewer.model.ImageFile;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class FavImageViewAdapter extends PagerAdapter {
    private final LayoutInflater inflater;
    private final Activity activity;
    ArrayList<String> pathList;
    private String from = "";

    public FavImageViewAdapter(Activity activity, ArrayList<String> pathList, String from) {
        this.activity = activity;
        this.pathList = pathList;
        this.from = from;
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
        return pathList.size();
    }


    @Override
    public Object instantiateItem(ViewGroup view, int position) {


        ListPagerBinding listPagerBinding = DataBindingUtil.inflate(inflater, R.layout.list_pager, view, false);

        String filePath = pathList.get(position);

        RequestOptions options = new RequestOptions();
        if (filePath.endsWith(".PNG") || filePath.endsWith(".png")) {
            Glide.with(activity)
                    .load(filePath)
                    .apply(options.skipMemoryCache(true)
                            .priority(Priority.LOW)
                            .format(DecodeFormat.PREFER_ARGB_8888))
                    .transition(withCrossFade())
                    .transition(new DrawableTransitionOptions().crossFade(500))
                    .into(listPagerBinding.imgImages);
        } else {
            Glide.with(activity)
                    .load(filePath)
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
        notifyDataSetChanged();
    }
}