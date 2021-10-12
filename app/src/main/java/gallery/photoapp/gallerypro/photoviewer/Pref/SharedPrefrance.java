package gallery.photoapp.gallerypro.photoviewer.Pref;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.collection.ArraySet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gallery.photoapp.gallerypro.photoviewer.model.RecycleModel;


public class SharedPrefrance {
    public static final String MyPREFERENCES = "Gallery Viewer";
    public static String PASSWORD = "PasswordProtect";
    public static String PROTECT_LIST = "Protect_file_list";
    public static String FAVOURITE_LIST = "Favourite_file_list";
    public static String RECYCLE_LIST = "Recycle_file_list";
    public static String PROTECT_ = "Protect_file_encoded";
    public static String PROTECT_FILE = "Protect_file";

    public static String getPasswordProtect(Context c1) {
        SharedPreferences sharedpreferences = c1.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String ans = sharedpreferences.getString(PASSWORD, "");
        return ans;
    }

    public static void setPasswordProtect(Context c1, String value) {
        SharedPreferences sharedpreferences = c1.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(PASSWORD, value);
        editor.apply();
    }

    public static ArrayList<String> getHideFileList(Context c1) {
        SharedPreferences sharedpreferences = c1.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        Set<String> set = sharedpreferences.getStringSet(PROTECT_LIST, new ArraySet<>());
        ArrayList<String> sample = new ArrayList<>(set);
        return sample;
    }

    public static void setHideFileList(Context c1, ArrayList<String> hideFileList) {
        SharedPreferences prefs = c1.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        Set<String> set = new HashSet<String>();
        set.addAll(hideFileList);
        edit.putStringSet(PROTECT_LIST, set);
        edit.commit();
    }

    public static ArrayList<String> getFavouriteFileList(Context c1) {
        SharedPreferences sharedpreferences = c1.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        Set<String> set = sharedpreferences.getStringSet(FAVOURITE_LIST, new ArraySet<>());
        ArrayList<String> sample = new ArrayList<>(set);
        return sample;
    }

    public static void setFavouriteFileList(Context c1, ArrayList<String> hideFileList) {
        SharedPreferences prefs = c1.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        Set<String> set = new HashSet<String>();
        set.addAll(hideFileList);
        edit.putStringSet(FAVOURITE_LIST, set);
        edit.commit();
    }

    public static String getSharedPreferenceUri(Context c1) {
        SharedPreferences sharedpreferences = c1.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String ans = sharedpreferences.getString(PROTECT_, "");
        return ans;
    }

    public static void setSharedPreferenceUri(Context c1, Uri hideFileList) {
        SharedPreferences sharedpreferences = c1.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(PROTECT_, String.valueOf(hideFileList));
        editor.apply();
    }

    public static String getSharedPreference(Context c1) {
        SharedPreferences sharedpreferences = c1.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String ans = sharedpreferences.getString(PROTECT_FILE, "");
        return ans;
    }

    public static void setSharedPreference(Context c1, String hideFileList) {
        SharedPreferences sharedpreferences = c1.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(PROTECT_FILE, String.valueOf(hideFileList));
        editor.apply();
    }

    public static ArrayList<RecycleModel> getRecycleBinData(Context c1) {
        SharedPreferences sharedpreferences = c1.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String response = sharedpreferences.getString(RECYCLE_LIST, "");
        ArrayList<RecycleModel> lstArrayList = gson.fromJson(response,
                new TypeToken<List<RecycleModel>>() {
                }.getType());
        if (lstArrayList == null)
            lstArrayList = new ArrayList<>();
        return lstArrayList;
    }

    public static void setRecycleBinData(Context c1, ArrayList<RecycleModel> hideFileList) {
        Gson gson = new Gson();
        String json = gson.toJson(hideFileList);

        SharedPreferences prefs = c1.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(RECYCLE_LIST, json);
        edit.commit();
    }

}
