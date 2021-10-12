package gallery.photoapp.gallerypro.photoviewer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import gallery.photoapp.gallerypro.photoviewer.R;
import gallery.photoapp.gallerypro.photoviewer.databinding.CustomTabBinding;
import gallery.photoapp.gallerypro.photoviewer.fragment.AlbumFragment;
import gallery.photoapp.gallerypro.photoviewer.fragment.PhotosFragment;
import gallery.photoapp.gallerypro.photoviewer.fragment.PrivateFragment;

public class TabsPagerAdapter extends FragmentPagerAdapter {

    private final String[] tabTitles = new String[]{"Photos", "Albums", "Private"};
    CustomTabBinding customTabBinding;
    Context context;

    public TabsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    public View getTabView(int position) {
        customTabBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.custom_tab, null, false);

        customTabBinding.tvTab.setText(tabTitles[position]);

        if (position == 0) {
            customTabBinding.tvTab.setTextColor(context.getResources().getColor(R.color.select_text_color));
        } else {
            customTabBinding.tvTab.setTextColor(context.getResources().getColor(R.color.text_color));
        }

        return customTabBinding.getRoot();
    }


    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new PhotosFragment(); //ChildFragment1 at position 0
            case 1:
                return new AlbumFragment(); //ChildFragment2 at position 1
            case 2:
                return new PrivateFragment();
        }

        return null;
    }

    @Override
    public int getItemPosition(Object object) {
        // POSITION_NONE makes it possible to reload the PagerAdapter
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
