package gallery.photoapp.gallerypro.photoviewer;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import gallery.photoapp.gallerypro.photoviewer.Pref.SharedPrefrance;
import gallery.photoapp.gallerypro.photoviewer.databinding.ActivityOtpVerificationBinding;

public class OtpVerificationActivity extends AppCompatActivity {

    ActivityOtpVerificationBinding verificationBinding;
    MyClickHandlers myClickHandlers;

    private String mVerificationId = "";
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private ProgressDialog dialog;
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.e("LLLL_Verify_Com: ", "onVerificationCompleted:" + credential.getSmsCode());
            dialog.setMessage("Verifying otp...");
            dialog.show();
            verificationBinding.etOtp.setText(credential.getSmsCode());
            signInWithPhoneAuthCredential(credential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.e("LLLL_Verify_Failed: ", "onVerificationFailed", e);

            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Log.e("LLLL_Verify_Failed1: ", "onVerificationFailed" + e.getLocalizedMessage());
            } else if (e instanceof FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Log.e("LLLL_Verify_Failed2: ", "onVerificationFailed" + e.getLocalizedMessage());
            }

            // Show a message and update the UI
        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
//            verifyVerificationCode(verificationId);
            Log.d("LLLL_Code: ", "onCodeSe+nt:" + verificationId);
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
            verificationBinding.llSendOtp.setVisibility(View.GONE);
            verificationBinding.llVerifyOtp.setVisibility(View.VISIBLE);

            // Save verification ID and resending token so we can use them later
            mVerificationId = verificationId;
            mResendToken = token;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verificationBinding = DataBindingUtil.setContentView(OtpVerificationActivity.this, R.layout.activity_otp_verification);

        dialog = new ProgressDialog(OtpVerificationActivity.this);

        mAuth = FirebaseAuth.getInstance();
        myClickHandlers = new MyClickHandlers(OtpVerificationActivity.this);
        verificationBinding.setOnClick(myClickHandlers);

    }

    private void sendOTP() {

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(verificationBinding.etContactNo.getText().toString())       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)// Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyVerificationCode(String otp) {
        //creating the credential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);

        //signing the user
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("LLLL_User: ", "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            Log.e("LLLL_Verify_User: ", user.getPhoneNumber());
                            if (dialog != null && dialog.isShowing())
                                dialog.dismiss();
                            SharedPrefrance.setPasswordProtect(OtpVerificationActivity.this, "");
                            onBackPressed();
                            // Update UI
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.e("LLLL_Faild: ", "signInWithCredential:failure", task.getException());
                            Toast.makeText(OtpVerificationActivity.this, "Please enter valid otp.", Toast.LENGTH_SHORT).show();
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    private boolean isValidPhoneNumber(CharSequence phoneNumber) {
        if (!TextUtils.isEmpty(phoneNumber)) {
            return Patterns.PHONE.matcher(phoneNumber).matches();
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public class MyClickHandlers {
        Context context;

        public MyClickHandlers(Context context) {
            this.context = context;
        }

        public void onSendOtp(View view) {
            if (verificationBinding.etContactNo.getText().length() > 0) {
                if (isValidPhoneNumber(verificationBinding.etContactNo.getText().toString())) {
                    dialog.setMessage("Sending otp...");
                    dialog.show();
                    sendOTP();
                } else {
                    Toast.makeText(OtpVerificationActivity.this, "Please enter a valid mobile number.", Toast.LENGTH_SHORT).show();
                }
            }
        }

        public void onOtpClick(View view) {
            if (verificationBinding.tvOTP.getText().toString().length() == 6) {
                dialog.setMessage("Verifying otp...");
                dialog.show();
                verifyVerificationCode(verificationBinding.tvOTP.getText().toString());
            } else {
                Toast.makeText(OtpVerificationActivity.this, "Please enter valid otp.", Toast.LENGTH_SHORT).show();
            }
        }

        public void onBackClick(View view) {
            onBackPressed();
        }

    }
}