package gallery.photoapp.gallerypro.photoviewer;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Objects;

import gallery.photoapp.gallerypro.photoviewer.adapter.TabsPagerAdapter;
import gallery.photoapp.gallerypro.photoviewer.constant.ConnectionDetector;
import gallery.photoapp.gallerypro.photoviewer.constant.Util;
import gallery.photoapp.gallerypro.photoviewer.databinding.ActivityMainBinding;

public class MainActivity extends BaseActivity {

    TabsPagerAdapter tabsPagerAdapter;
    ActivityMainBinding mainBinding;

    private static final int RC_APP_UPDATE = 101;
    MyClickHandlers myClickHandlers;
    private AppUpdateManager mAppUpdateManager;

    private AdView adView;
    private boolean isInternetPresent;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void permissionGranted() {

    }

    private void fireAnalyticsAds(String arg1, String arg2) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, arg1);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, arg2);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
    }

    InstallStateUpdatedListener installStateUpdatedListener = new
            InstallStateUpdatedListener() {
                @Override
                public void onStateUpdate(InstallState state) {
                    if (state.installStatus() == InstallStatus.DOWNLOADED) {
                        //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                        popupSnackbarForCompleteUpdate();
                    } else if (state.installStatus() == InstallStatus.INSTALLED) {
                        if (mAppUpdateManager != null) {
                            mAppUpdateManager.unregisterListener(installStateUpdatedListener);
                        }

                    } else {
                        Log.i("LLL_Update_App: ", "InstallStateUpdatedListener: state: " + state.installStatus());
                    }
                }
            };

    @Override
    protected void onStart() {
        super.onStart();

        mAppUpdateManager = AppUpdateManagerFactory.create(MainActivity.this);

        mAppUpdateManager.registerListener(installStateUpdatedListener);

        mAppUpdateManager = AppUpdateManagerFactory.create(MainActivity.this);

        mAppUpdateManager.registerListener(installStateUpdatedListener);

        mAppUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE /*AppUpdateType.IMMEDIATE*/)) {

                try {
                    mAppUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo, AppUpdateType.FLEXIBLE /*AppUpdateType.IMMEDIATE*/, MainActivity.this, RC_APP_UPDATE);
                    Log.e("LLL_Update_App: ", "Update available");
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                    Log.e("LLL_Update_App: ", e.getMessage());
                }

            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                popupSnackbarForCompleteUpdate();
            } else {
                Log.e("LLL_Update_App: ", "checkForAppUpdateAvailability: something else");
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAppUpdateManager != null) {
            mAppUpdateManager.unregisterListener(installStateUpdatedListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAppUpdateManager != null) {
            mAppUpdateManager.unregisterListener(installStateUpdatedListener);
        }
    }

    private void popupSnackbarForCompleteUpdate() {

        Snackbar snackbar =
                Snackbar.make(
                        findViewById(R.id.coordinatorLayout_main),
                        "New app is ready!",
                        Snackbar.LENGTH_INDEFINITE);

        snackbar.setAction("Install", view -> {
            if (mAppUpdateManager != null) {
                mAppUpdateManager.completeUpdate();
            }
        });


        snackbar.setActionTextColor(getResources().getColor(R.color.teal_200));
        snackbar.show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(MainActivity.this, R.layout.activity_main);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(MainActivity.this);

        myClickHandlers = new MyClickHandlers(MainActivity.this);
        mainBinding.setMoreBtnClick(myClickHandlers);

        tabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager(), MainActivity.this);
        mainBinding.content.viewPager.setAdapter(tabsPagerAdapter);

        mainBinding.tabGallery.setupWithViewPager(mainBinding.content.viewPager);

        for (int i = 0; i < mainBinding.tabGallery.getTabCount(); i++) {
            TabLayout.Tab tab = mainBinding.tabGallery.getTabAt(i);
            Objects.requireNonNull(tab).setCustomView(tabsPagerAdapter.getTabView(i));
        }

        if (mainBinding.content.viewPager.getCurrentItem() == 0) {
            mainBinding.rlViewType.setVisibility(View.GONE);
        } else {
            mainBinding.rlViewType.setVisibility(View.VISIBLE);
        }

        mainBinding.content.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (mainBinding.cvMainMenu.getVisibility() == View.VISIBLE)
                    mainBinding.cvMainMenu.setVisibility(View.GONE);
                if (mainBinding.cvViewType.getVisibility() == View.VISIBLE)
                    mainBinding.cvViewType.setVisibility(View.GONE);
                if (mainBinding.cvColumn.getVisibility() == View.VISIBLE)
                    mainBinding.cvColumn.setVisibility(View.GONE);
                layGone();
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    mainBinding.rlSorting.setVisibility(View.VISIBLE);
                } else {
                    mainBinding.rlSorting.setVisibility(View.GONE);
                }
                if (position == 0) {
                    mainBinding.rlViewType.setVisibility(View.GONE);
                } else {
                    mainBinding.rlViewType.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mainBinding.tabGallery.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TextView tv = Objects.requireNonNull(tab.getCustomView()).findViewById(R.id.tv_tab);
                tv.setTextColor(getResources().getColor(R.color.select_text_color));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TextView tv = Objects.requireNonNull(tab.getCustomView()).findViewById(R.id.tv_tab);
                tv.setTextColor(getResources().getColor(R.color.text_color));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        ConnectionDetector cd = new ConnectionDetector(MainActivity.this);
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Step 1 - Create an AdView and set the ad unit ID on it.
                    adView = new AdView(MainActivity.this);
                    adView.setAdUnitId(getString(R.string.banner_ad));
                    mainBinding.content.bannerContainer.addView(adView);
                    adView.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            super.onAdLoaded();
                            fireAnalyticsAds("admob_banner", "loaded");
                        }

                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            super.onAdFailedToLoad(loadAdError);
                            if (loadAdError.getMessage() != null)
                                fireAnalyticsAds("admob_banner_Error", loadAdError.getMessage());
                        }
                    });
                    loadBanner();
                }
            }, 2000);
        }
    }

    private void loadBanner() {
        // Create an ad request. Check your logcat output for the hashed device ID
        // to get test ads on a physical device, e.g.,
        // "Use AdRequest.Builder.addTestDevice("ABCDE0123") to get test ads on this
        // device."
        AdRequest adRequest =
                new AdRequest.Builder().build();

        AdSize adSize = AdSize.BANNER;
        // Step 4 - Set the adaptive ad size on the ad view.
        adView.setAdSize(adSize);


        // Step 5 - Start loading the ad in the background.
        adView.loadAd(adRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_APP_UPDATE) {
            if (resultCode != RESULT_OK) {
                Log.e("LLL_Update_App: ", "onActivityResult: app download failed");
            }
        }
    }

    private void layVisible() {
        mainBinding.rlOne.setVisibility(View.VISIBLE);
        mainBinding.content.rlTwo.setVisibility(View.VISIBLE);
    }

    private void layGone() {
        mainBinding.rlOne.setVisibility(View.GONE);
        mainBinding.content.rlTwo.setVisibility(View.GONE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void sortingPopUp() {
        String[] radio1 = {getResources().getString(R.string.no_of_photos),
                getResources().getString(R.string.last_modified_date),
                getResources().getString(R.string.name)};

        String[] radio2 = {getResources().getString(R.string.ascending),
                getResources().getString(R.string.descending)};

        final Dialog dial = new Dialog(MainActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dial.requestWindowFeature(1);
        dial.setContentView(R.layout.dialog_sorting);
        dial.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dial.setCanceledOnTouchOutside(true);

        RadioGroup rg_sorting1 = dial.findViewById(R.id.rg_sorting1);
        RadioGroup rg_sorting2 = dial.findViewById(R.id.rg_sorting2);

        for (int i = 0; i < radio1.length; i++) {
            String sorting1 = radio1[i];
            RadioButton radioButton = new RadioButton(this);
            radioButton.setPadding(30, 30, 7, 30);
            radioButton.setText(sorting1);
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            radioButton.setTextColor(getResources().getColor(R.color.menu_text_color));
            radioButton.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_200)));
            radioButton.setId(i);

            if (Util.SORTING_TYPE2.equals("")) {
                if (i == 1) {
                    radioButton.setChecked(true);
                    Util.SORTING_TYPE = radioButton.getText().toString();
                }
            } else if (radioButton.getText().equals(Util.SORTING_TYPE)) {
                radioButton.setChecked(true);
            }

            rg_sorting1.addView(radioButton);

        }

        //set listener to radio button group
        rg_sorting1.setOnCheckedChangeListener((group, checkedId) -> {
            int checkedRadioButtonId = group.getCheckedRadioButtonId();
            RadioButton radioBtn = dial.findViewById(checkedRadioButtonId);
            Util.SORTING_TYPE = radioBtn.getText().toString();
            Log.e("LLLLL_Refill_time: ", Util.SORTING_TYPE);
        });

        for (int i = 0; i < radio2.length; i++) {
            String sorting2 = radio2[i];
            RadioButton radioButton = new RadioButton(this);
            radioButton.setPadding(30, 30, 7, 30);
            radioButton.setText(sorting2);
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            radioButton.setTextColor(getResources().getColor(R.color.menu_text_color));
            radioButton.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_200)));
            radioButton.setId(i + radio1.length);

            if (Util.SORTING_TYPE2.equals("")) {
                if (i == 0) {
                    radioButton.setChecked(true);
                    Util.SORTING_TYPE2 = radioButton.getText().toString();
                }
            } else if (radioButton.getText().equals(Util.SORTING_TYPE2)) {
                radioButton.setChecked(true);
            }

            rg_sorting2.addView(radioButton);

        }

        //set listener to radio button group
        rg_sorting2.setOnCheckedChangeListener((group, checkedId) -> {
            int checkedRadioButtonId = group.getCheckedRadioButtonId();
            RadioButton radioBtn = dial.findViewById(checkedRadioButtonId);
            Util.SORTING_TYPE2 = radioBtn.getText().toString();
            Log.e("LLLLL_Refill_time: ", Util.SORTING_TYPE2);
        });

        dial.findViewById(R.id.tv_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dial.dismiss();
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(MainActivity.this);
                Intent localIn = new Intent("TAG_REFRESH");
                lbm.sendBroadcast(localIn);
            }
        });

        dial.show();
    }

    @Override
    public void onBackPressed() {
        if (mainBinding.cvMainMenu.getVisibility() == View.VISIBLE)
            mainBinding.cvMainMenu.setVisibility(View.GONE);
        else if (mainBinding.cvViewType.getVisibility() == View.VISIBLE)
            mainBinding.cvViewType.setVisibility(View.GONE);
        else if (mainBinding.cvColumn.getVisibility() == View.VISIBLE)
            mainBinding.cvColumn.setVisibility(View.GONE);
        else
            super.onBackPressed();
        layGone();
    }

    public class MyClickHandlers {
        Context context;

        public MyClickHandlers(Context context) {
            this.context = context;
        }

        public void onMoreBtnClicked(View view) {
            if (mainBinding.cvMainMenu.getVisibility() == View.VISIBLE)
                mainBinding.cvMainMenu.setVisibility(View.GONE);
            else
                mainBinding.cvMainMenu.setVisibility(View.VISIBLE);
            layVisible();

        }

        public void onViewType(View view) {
            if (mainBinding.cvMainMenu.getVisibility() == View.VISIBLE)
                mainBinding.cvMainMenu.setVisibility(View.GONE);
            if (mainBinding.cvViewType.getVisibility() == View.GONE)
                mainBinding.cvViewType.setVisibility(View.VISIBLE);
            layVisible();
        }

        public void onColumnType(View view) {
            if (mainBinding.cvMainMenu.getVisibility() == View.VISIBLE)
                mainBinding.cvMainMenu.setVisibility(View.GONE);
            if (mainBinding.cvColumn.getVisibility() == View.GONE)
                mainBinding.cvColumn.setVisibility(View.VISIBLE);
            layVisible();
        }

        public void onColumn2Click(View view) {
            if (mainBinding.cvColumn.getVisibility() == View.VISIBLE)
                mainBinding.cvColumn.setVisibility(View.GONE);
            layGone();
            Util.COLUMN_TYPE = 2;
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(MainActivity.this);
            Intent localIn = new Intent("TAG_REFRESH");
            lbm.sendBroadcast(localIn);
        }

        public void onColumn3Click(View view) {
            if (mainBinding.cvColumn.getVisibility() == View.VISIBLE)
                mainBinding.cvColumn.setVisibility(View.GONE);
            layGone();
            Util.COLUMN_TYPE = 3;
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(MainActivity.this);
            Intent localIn = new Intent("TAG_REFRESH");
            lbm.sendBroadcast(localIn);
        }

        public void onColumn4Click(View view) {
            if (mainBinding.cvColumn.getVisibility() == View.VISIBLE)
                mainBinding.cvColumn.setVisibility(View.GONE);
            layGone();
            Util.COLUMN_TYPE = 4;
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(MainActivity.this);
            Intent localIn = new Intent("TAG_REFRESH");
            lbm.sendBroadcast(localIn);
        }

        public void listClick(View view) {
            if (mainBinding.cvViewType.getVisibility() == View.VISIBLE)
                mainBinding.cvViewType.setVisibility(View.GONE);
            layGone();
            Util.isList = true;
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(MainActivity.this);
            Intent localIn = new Intent("TAG_REFRESH");
            lbm.sendBroadcast(localIn);
        }

        public void gridClick(View view) {
            if (mainBinding.cvViewType.getVisibility() == View.VISIBLE)
                mainBinding.cvViewType.setVisibility(View.GONE);
            layGone();
            Util.isList = false;
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(MainActivity.this);
            Intent localIn = new Intent("TAG_REFRESH");
            lbm.sendBroadcast(localIn);
        }

        public void onFavouriteClick(View view) {
            if (mainBinding.cvMainMenu.getVisibility() == View.VISIBLE)
                mainBinding.cvMainMenu.setVisibility(View.GONE);
            layGone();
            Intent intent = new Intent(MainActivity.this, FavouriteActivity.class);
            startActivity(intent);
        }

        public void onRecycleBinClick(View view) {
            if (mainBinding.cvMainMenu.getVisibility() == View.VISIBLE)
                mainBinding.cvMainMenu.setVisibility(View.GONE);
            layGone();
            Intent intent = new Intent(MainActivity.this, RecycleBinActivity.class);
            startActivity(intent);
            finish();
        }

        public void onSortingClick(View view) {
            if (mainBinding.cvMainMenu.getVisibility() == View.VISIBLE)
                mainBinding.cvMainMenu.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                sortingPopUp();
                layGone();
            }
        }
    }

}