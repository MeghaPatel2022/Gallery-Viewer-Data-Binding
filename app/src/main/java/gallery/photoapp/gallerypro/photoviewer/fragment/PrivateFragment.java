package gallery.photoapp.gallerypro.photoviewer.fragment;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import gallery.photoapp.gallerypro.photoviewer.OtpVerificationActivity;
import gallery.photoapp.gallerypro.photoviewer.Pref.SharedPrefrance;
import gallery.photoapp.gallerypro.photoviewer.R;
import gallery.photoapp.gallerypro.photoviewer.adapter.PrivateImageAdapter;
import gallery.photoapp.gallerypro.photoviewer.adapter.PrivateListImageAdapter;
import gallery.photoapp.gallerypro.photoviewer.constant.Util;
import gallery.photoapp.gallerypro.photoviewer.databinding.FragmentPrivateBinding;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.KEYGUARD_SERVICE;

public class PrivateFragment extends BaseFragment {

    FragmentPrivateBinding privateBinding;
    private static final int INTENT_AUTHENTICATE = 101;
    public ArrayList<String> pathList = new ArrayList<>();
    MyClickHandlers myClickHandlers;
    String pass1 = "";
    String pass2 = "";
    String pass3 = "";
    String pass4 = "";
    String newPass1 = "";
    String newPass2 = "";
    String newPass3 = "";
    String newPass4 = "";
    Animation fadeout, fadein;
    PrivateImageAdapter privateImageAdapter;
    PrivateListImageAdapter privateListImageAdapter;
    private boolean isSet = false;
    // TODO: Rename and change types of parameters

    private MyReceiver myReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myReceiver = new MyReceiver();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(myReceiver,
                new IntentFilter("TAG_REFRESH"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_private, container, false);

        privateBinding.antiTheftT9KeyForgot.setOnClickListener(v -> {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                KeyguardManager km = (KeyguardManager) getActivity().getSystemService(KEYGUARD_SERVICE);

                if (km.isKeyguardSecure()) {
                    Intent authIntent = km.createConfirmDeviceCredentialIntent(getString(R.string.app_name), "Confirm your screen lock pattern, PIN or password.");
                    startActivityForResult(authIntent, INTENT_AUTHENTICATE);
                } else {
                    Intent intent = new Intent(getActivity(), OtpVerificationActivity.class);
                    startActivity(intent);
                }
            }
        });

        fadeout = AnimationUtils.loadAnimation(getContext(), R.anim.stay);
        fadein = AnimationUtils.loadAnimation(getContext(), R.anim.ripple_anim);

        privateBinding.tvSetLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (privateBinding.llSetLock.getVisibility() == View.VISIBLE)
                    privateBinding.llSetLock.setVisibility(View.GONE);
                if (privateBinding.llPassword.getVisibility() == View.GONE)
                    privateBinding.llPassword.setVisibility(View.VISIBLE);
                if (privateBinding.rlPrivateData.getVisibility() == View.VISIBLE)
                    privateBinding.rlPrivateData.setVisibility(View.GONE);
            }
        });

        privateBinding.etPass4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!SharedPrefrance.getPasswordProtect(getContext()).equals("")) {
                    String password = pass1 +
                            pass2 +
                            pass3 +
                            pass4;
                    if (password.equals(SharedPrefrance.getPasswordProtect(getContext()))) {
                        if (!password.equals("****")) {
                            Log.e("LLLL_Password: ", password);
                            if (privateBinding.rlPrivateData.getVisibility() == View.GONE) {
                                if (pathList.size() == 0) {
                                    privateBinding.llNoData.setVisibility(View.VISIBLE);
                                } else {
                                    privateBinding.rlPrivateData.setVisibility(View.VISIBLE);
                                }
                            }
                            if (privateBinding.llPassword.getVisibility() == View.VISIBLE)
                                privateBinding.llPassword.setVisibility(View.GONE);
                        }
                    } else {

                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(() -> {
                            if (!pass4.equals("")) {
                                privateBinding.etPass1.setText("");
                                privateBinding.etPass2.setText("");
                                privateBinding.etPass3.setText("");
                                privateBinding.etPass4.setText("");
                                privateBinding.etPass1.setHint("*");
                                privateBinding.etPass2.setHint("*");
                                privateBinding.etPass3.setHint("*");
                                privateBinding.etPass4.setHint("*");
                                pass1 = "";
                                pass2 = "";
                                pass3 = "";
                                pass4 = "";
                                Toast.makeText(getContext(), "Password Incorrect...", Toast.LENGTH_SHORT).show();
                            }
                        }, 500);
                    }
                } else {
                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(() -> {
                        if (!isSet) {
                            if (!pass4.equals("")) {
                                isSet = true;
                                privateBinding.tvEnterPass.setText("Conform Password");
                                privateBinding.etPass1.setText("");
                                privateBinding.etPass2.setText("");
                                privateBinding.etPass3.setText("");
                                privateBinding.etPass4.setText("");
                                privateBinding.etPass1.setHint("");
                                privateBinding.etPass2.setHint("");
                                privateBinding.etPass3.setHint("");
                                privateBinding.etPass4.setHint("");
                            }
                        } else {
                            if (!newPass4.equals("")) {
                                String password = pass1 +
                                        pass2 +
                                        pass3 +
                                        pass4;

                                String password1 = newPass1 +
                                        newPass2 +
                                        newPass3 +
                                        newPass4;
                                if (password.equals(password1)) {
                                    if (!password.equals("****")) {
                                        Log.e("LLLL_Password: ", password);
                                        SharedPrefrance.setPasswordProtect(getContext(), password);
                                        Toast.makeText(getContext(), "Password set successfully...", Toast.LENGTH_SHORT).show();
                                        Util.isPrivate = false;
                                        if (privateBinding.rlPrivateData.getVisibility() == View.GONE) {
                                            if (pathList.size() == 0) {
                                                privateBinding.llNoData.setVisibility(View.VISIBLE);
                                            } else {
                                                privateBinding.rlPrivateData.setVisibility(View.VISIBLE);
                                            }
                                        }
                                        if (privateBinding.llPassword.getVisibility() == View.VISIBLE)
                                            privateBinding.llPassword.setVisibility(View.GONE);
                                    }
                                } else {
                                    Toast.makeText(getContext(), "Password Incorrect...", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }, 500);
                }
            }
        });

        myClickHandlers = new MyClickHandlers(getContext());
        privateBinding.setOnClick(myClickHandlers);

        return privateBinding.getRoot();
    }

    void startAnim() {
        privateBinding.rlLoading.setVisibility(View.VISIBLE);
    }

    void stopAnim() {
        privateBinding.rlLoading.setVisibility(View.GONE);
    }

    private void backSpaceCall() {
        if (!isSet) {
            String password = pass1 +
                    pass2 +
                    pass3 +
                    pass4;
            if (password.length() == 1) {
                privateBinding.etPass1.setText("");
                privateBinding.etPass1.setHint("*");
                pass1 = "";
            } else if (password.length() == 2) {
                privateBinding.etPass2.setText("");
                privateBinding.etPass2.setHint("*");
                pass2 = "";
            } else if (password.length() == 3) {
                privateBinding.etPass3.setText("");
                privateBinding.etPass3.setHint("*");
                pass3 = "";
            } else if (password.length() == 4) {
                privateBinding.etPass4.setText("");
                privateBinding.etPass4.setHint("*");
                pass4 = "";
            }
        } else {
            String password = newPass1 +
                    newPass2 +
                    newPass3 +
                    newPass4;
            if (password.length() == 1) {
                privateBinding.etPass1.setText("");
                privateBinding.etPass1.setHint("*");
                newPass1 = "";
            } else if (password.length() == 2) {
                privateBinding.etPass2.setText("");
                privateBinding.etPass2.setHint("*");
                newPass2 = "";
            } else if (password.length() == 3) {
                privateBinding.etPass3.setText("");
                privateBinding.etPass3.setHint("*");
                newPass3 = "";
            } else if (password.length() == 4) {
                privateBinding.etPass4.setText("");
                privateBinding.etPass4.setHint("*");
                newPass4 = "";
            }
        }
    }

    private void setText(TextView appCompatTextView) {
        if (privateBinding.etPass1.getText() == null || privateBinding.etPass1.getText().toString().trim().equals("")) {
            if (!isSet)
                pass1 = appCompatTextView.getText().toString().trim();
            else
                newPass1 = appCompatTextView.getText().toString().trim();

            privateBinding.etPass1.setText(appCompatTextView.getText().toString().trim());
            privateBinding.etPass1.setTextColor(getActivity().getResources().getColor(R.color.blue));
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> privateBinding.etPass1.setText("*"), 200);
        } else if (privateBinding.etPass2.getText() == null || privateBinding.etPass2.getText().toString().trim().equals("")) {
            if (!isSet)
                pass2 = appCompatTextView.getText().toString().trim();
            else
                newPass2 = appCompatTextView.getText().toString().trim();
            privateBinding.etPass2.setText(appCompatTextView.getText().toString().trim());
            privateBinding.etPass2.setTextColor(getActivity().getResources().getColor(R.color.blue));
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> privateBinding.etPass2.setText("*"), 200);
        } else if (privateBinding.etPass3.getText() == null || privateBinding.etPass3.getText().toString().trim().equals("")) {
            if (!isSet)
                pass3 = appCompatTextView.getText().toString().trim();
            else
                newPass3 = appCompatTextView.getText().toString().trim();
            privateBinding.etPass3.setText(appCompatTextView.getText().toString().trim());
            privateBinding.etPass3.setTextColor(getActivity().getResources().getColor(R.color.blue));
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> privateBinding.etPass3.setText("*"), 200);
        } else if (privateBinding.etPass4.getText() == null || privateBinding.etPass4.getText().toString().trim().equals("")) {
            if (!isSet)
                pass4 = appCompatTextView.getText().toString().trim();
            else
                newPass4 = appCompatTextView.getText().toString().trim();
            privateBinding.etPass4.setText(appCompatTextView.getText().toString().trim());
            privateBinding.etPass4.setTextColor(getActivity().getResources().getColor(R.color.blue));
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> privateBinding.etPass4.setText("*"), 200);
        }

        fadeout = AnimationUtils.loadAnimation(getContext(), R.anim.stay);
        fadein = AnimationUtils.loadAnimation(getContext(), R.anim.ripple_anim);

        appCompatTextView.setBackground(getActivity().getResources().getDrawable(R.drawable.txt_select));
        appCompatTextView.setTextColor(getActivity().getResources().getColor(R.color.white));

        appCompatTextView.setAnimation(fadein);

        fadein.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                appCompatTextView.setAnimation(fadeout);
                appCompatTextView.setBackground(getActivity().getResources().getDrawable(R.drawable.ic_back_txt));
                appCompatTextView.setTextColor(getActivity().getResources().getColor(R.color.black));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        if (SharedPrefrance.getPasswordProtect(getContext()).equals("")) {
            privateBinding.tvEnterPass.setText("Enter New Password");
        } else {
            privateBinding.tvEnterPass.setText("Enter Password");
            if (privateBinding.llSetLock.getVisibility() == View.VISIBLE)
                privateBinding.llSetLock.setVisibility(View.GONE);
            if (privateBinding.llPassword.getVisibility() == View.GONE)
                privateBinding.llPassword.setVisibility(View.VISIBLE);
            if (privateBinding.rlPrivateData.getVisibility() == View.VISIBLE)
                privateBinding.rlPrivateData.setVisibility(View.GONE);
        }

        new LongOperation().execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(myReceiver);
    }

    // call back when password is correct
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTENT_AUTHENTICATE) {
            if (resultCode == RESULT_OK) {
                SharedPrefrance.setPasswordProtect(getContext(), "");
                isSet = false;
                if (SharedPrefrance.getPasswordProtect(getContext()).equals("")) {
                    privateBinding.tvEnterPass.setText("Enter New Password");
                } else {
                    privateBinding.tvEnterPass.setText("Enter Password");
                }
            }
        }
    }

    @Override
    void permissionGranted() {

    }

    public class MyClickHandlers {
        Context context;

        public MyClickHandlers(Context context) {
            this.context = context;
        }

        public void onKey1Listener(View view) {
            setText(privateBinding.antiTheftT9Key1);
        }

        public void onKey2Listener(View view) {
            setText(privateBinding.antiTheftT9Key2);
        }

        public void onKey3Listener(View view) {
            setText(privateBinding.antiTheftT9Key3);
        }

        public void onKey4Listener(View view) {
            setText(privateBinding.antiTheftT9Key4);
        }

        public void onKey5Listener(View view) {
            setText(privateBinding.antiTheftT9Key5);
        }

        public void onKey6Listener(View view) {
            setText(privateBinding.antiTheftT9Key6);
        }

        public void onKey7Listener(View view) {
            setText(privateBinding.antiTheftT9Key7);
        }

        public void onKey8Listener(View view) {
            setText(privateBinding.antiTheftT9Key8);
        }

        public void onKey9Listener(View view) {
            setText(privateBinding.antiTheftT9Key9);
        }

        public void onKey0Listener(View view) {
            setText(privateBinding.antiTheftT9Key0);
        }

        public void onBackSpace(View view) {
            backSpaceCall();
        }

        public void onForgotListener(View view) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                KeyguardManager km = (KeyguardManager) getActivity().getSystemService(KEYGUARD_SERVICE);

                if (km.isKeyguardSecure()) {
                    Intent authIntent = km.createConfirmDeviceCredentialIntent(getString(R.string.app_name), "Confirm your screen lock pattern, PIN or password.");
                    startActivityForResult(authIntent, INTENT_AUTHENTICATE);
                } else {
                    Intent intent = new Intent(getActivity(), OtpVerificationActivity.class);
                    startActivity(intent);
                }
            }
        }

        public void onSetLock(View view) {
            if (privateBinding.llSetLock.getVisibility() == View.VISIBLE)
                privateBinding.llSetLock.setVisibility(View.GONE);
            if (privateBinding.llPassword.getVisibility() == View.GONE)
                privateBinding.llPassword.setVisibility(View.VISIBLE);
            if (privateBinding.rlPrivateData.getVisibility() == View.VISIBLE)
                privateBinding.rlPrivateData.setVisibility(View.GONE);
        }
    }

    private final class LongOperation extends AsyncTask<Void, Void, ArrayList<String>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startAnim();
        }

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            ArrayList<String> pathList;
            pathList = SharedPrefrance.getHideFileList(getContext());
            return pathList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            pathList = result;
            if (!Util.isList) {
                final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), Util.COLUMN_TYPE);
                privateBinding.rvImages.setLayoutManager(layoutManager);

                privateImageAdapter = new PrivateImageAdapter(pathList, getActivity());
                privateBinding.rvImages.setAdapter(privateImageAdapter);
            } else {
                final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
                privateBinding.rvImages.setLayoutManager(layoutManager);

                privateListImageAdapter = new PrivateListImageAdapter(pathList, getActivity());
                privateBinding.rvImages.setAdapter(privateListImageAdapter);
            }

            stopAnim();
        }
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            onResume();
        }
    }
}
