package com.accura.finger.print.demo;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.accura.finger.print.demo.database.DatabaseHelper;
import com.accura.finger.print.sdk.model.FingerModel;
import com.accura.finger.print.sdk.util.AccuraFingerLog;
import com.accura.finger.print.sdk.util.Util;
import com.accura.finger.scan.FingerEngine;
import com.accura.finger.scan.RecogType;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int USER_ENROLL = 101;
    private static final int USER_VERIFY = 102;
    private ProgressDialog progressBar;
    private DatabaseHelper dbhelper;
    private int companyId = -1;
    private CheckBox cbLeft, cbRight;

    public void selectHand(View view) {
        if (view.getId() == R.id.right_check) {
            fingerSideType = FingerEngine.RIGHT_HAND;
            cbRight.setChecked(true);
            cbLeft.setChecked(false);
        } else if (view.getId() == R.id.left_check) {
            fingerSideType = FingerEngine.LEFT_HAND;
            cbLeft.setChecked(true);
            cbRight.setChecked(false);
        }
    }

    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                if (activity.progressBar != null && activity.progressBar.isShowing()) {
                    activity.progressBar.dismiss();
                }
                if (msg.what == 1) {
                    if (activity.sdkModel.isFingerEnable) {
                        activity.btnEnroll.setVisibility(View.VISIBLE);
                        activity.btnVerify.setVisibility(View.VISIBLE);
                    }
                } else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
                    builder1.setMessage(activity.responseMessage);
                    builder1.setCancelable(true);
                    builder1.setPositiveButton(
                            "OK",
                            (dialog, id) -> dialog.cancel());
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
            }
        }
    }

    private static class NativeThread extends Thread {
        private final WeakReference<MainActivity> mActivity;

        public NativeThread(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void run() {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                try {
                    // doWorkNative();
                    FingerEngine fingerEngine = new FingerEngine();
                    AccuraFingerLog.enableLogs(true); // make sure to disable logs in release mode
                    AccuraFingerLog.refreshLogfile(activity);
                    AccuraFingerLog.loge(TAG,fingerEngine.getVersion());
                    fingerEngine.setDialog(false); // setDialog(false) To set your custom dialog for license validation
                    activity.sdkModel = fingerEngine.initEngine(activity);
                    if (activity.sdkModel == null){
                        return;
                    }
                    AccuraFingerLog.loge(TAG, "SDK version" + fingerEngine.getSDKVersion() + "\nInitialized Engine : " + activity.sdkModel.i + " -> " + activity.sdkModel.message);
                    activity.responseMessage = activity.sdkModel.message;

                    if (activity.sdkModel.i >= 0) {
                        fingerEngine.setBlurPercentage(activity, 100);
                        activity.handler.sendEmptyMessage(1);
                    } else
                        activity.handler.sendEmptyMessage(0);

                } catch (Exception e) {
                }
            }
            super.run();
        }
    }

    private Handler handler = new MyHandler(this);
    private Thread nativeThread = new NativeThread(this);
    private View btnEnroll, btnVerify;
    private FingerEngine.SDKModel sdkModel;
    private String responseMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String companyName = "Demo";
        TextView tv_company = findViewById(R.id.tv_company);
        tv_company.setText("Company : " + companyName);

//        tvTime = findViewById(R.id.tv_time);
        cbLeft = findViewById(R.id.left_check);
        cbRight = findViewById(R.id.right_check);
        btnEnroll = findViewById(R.id.lout_enroll);
        btnEnroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        btnVerify = findViewById(R.id.lout_verify);
        dbhelper = new DatabaseHelper(this);
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSelectCandidateDialog();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Util.isPermissionsGranted(this)) {
            requestCameraPermission();
        } else {
            doWork();
        }


    }

    private void openSelectCandidateDialog() {

        List<FingerModel> arrayList = dbhelper.getUserList();
        if (arrayList.size() <= 0) {
            Toast.makeText(MainActivity.this, "Finger Print Not enrolled", Toast.LENGTH_LONG).show();
            return;
        }
        Dialog dialog = new Dialog(this, R.style.ThemeWithCorners);
        dialog.setContentView(R.layout.layout_form);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        TextView tv =  dialog.findViewById(R.id.tv_enroll_title);
        tv.setText("Enter username for Verification");
        EditText editText = (EditText) dialog.findViewById(R.id.et_user_name);
        Button btn_add = (Button) dialog.findViewById(R.id.btn_add);
        btn_add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (TextUtils.isEmpty(editText.getText())) {
                    editText.setError("Enter UserName");
                    return;
                }
                String s = editText.getText().toString();

                progressBar.setMessage("Fetching...");
                if (!isFinishing()) {
                    progressBar.show();
                }
                int which = -1;
                for (int j = 0; j < arrayList.size(); j++) {
                    FingerModel model = arrayList.get(j);
                    if (model.getUserName().equalsIgnoreCase(s)) {
                        which = j;
                        break;
                    }
                }
                if (which == -1) {
                    if (!isFinishing()) {
                        progressBar.dismiss();
                    }
                    editText.setText("");
                    editText.setError("User is not enrolled");
                } else {
                    try {
                        dialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!isFinishing()) {
                        progressBar.dismiss();
                    }
                    long uniqueID = arrayList.get(which).getUniqueID();
                    Intent intent = new Intent(MainActivity.this, FingerActivity.class);
                    RecogType.FINGER_PRINT.attachTo(intent);
                    intent.putExtra("card_name", "Finger Print");
                    intent.putExtra("finger_scan_type", FingerEngine.FINGER_VERIFY);
                    intent.putExtra("unique_id", uniqueID);
                    intent.putExtra("unique_name", arrayList.get(which).getUserName());
                    intent.putExtra("left_right_type", fingerSideType);
                    startActivityForResult(intent, USER_VERIFY);
                    overridePendingTransition(0, 0);
                }
            }
        });
        if (!isFinishing()) {
            dialog.show();
        }
    }

    private void openDialog() {
        Dialog dialog = new Dialog(this, R.style.ThemeWithCorners);
        dialog.setContentView(R.layout.layout_form);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        EditText editText = (EditText) dialog.findViewById(R.id.et_user_name);
        Button btn_add = (Button) dialog.findViewById(R.id.btn_add);
        btn_add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (TextUtils.isEmpty(editText.getText())) {
                    editText.setError("Enter UserName");
                    return;
                }
                String s = editText.getText().toString();

                progressBar.setMessage("Checking User...");
                if (!isFinishing()) {
                    progressBar.show();
                }
                if (dbhelper.getUserByName(s)) {
                    if (!isFinishing()) {
                        progressBar.dismiss();
                    }
                    editText.setText("");
                    editText.setError("The name has already been taken");
                    return;
                }

                try {
                    dialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!isFinishing()) {
                    progressBar.dismiss();
                }
                Intent intent = new Intent(MainActivity.this, FingerActivity.class);
                RecogType.FINGER_PRINT.attachTo(intent);
                intent.putExtra("card_name", "Finger Print");
                intent.putExtra("finger_scan_type", FingerEngine.FINGER_ENROLL);
                intent.putExtra("user_name", s);
                intent.putExtra("left_right_type", fingerSideType);
                startActivityForResult(intent, USER_ENROLL);
                overridePendingTransition(0, 0);

            }
        });
        if (!isFinishing()) {
            dialog.show();
        }
    }

    private void reEnrollDialog(String userName, String message) {
        Dialog dialog = new Dialog(this, R.style.ThemeWithCorners);
        dialog.setContentView(R.layout.layout_form_renenroll);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        TextView tv =  dialog.findViewById(R.id.tv_enroll_title);
        tv.setText("Hello " + userName + ",");
        TextView tv2 = dialog.findViewById(R.id.tv_message);
        tv2.setText(message);
        Button btn_add = (Button) dialog.findViewById(R.id.btn_add);
        btn_add.setText("Enroll Again");
        btn_add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    dialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!isFinishing()) {
                    progressBar.dismiss();
                }
                Intent intent = new Intent(MainActivity.this, FingerActivity.class);
                RecogType.FINGER_PRINT.attachTo(intent);
                intent.putExtra("card_name", "Finger Print");
                intent.putExtra("finger_scan_type", FingerEngine.FINGER_ENROLL);
                intent.putExtra("user_name", userName);
                intent.putExtra("left_right_type", fingerSideType);
                startActivityForResult(intent, USER_ENROLL);
                overridePendingTransition(0, 0);

            }
        });
        if (!isFinishing()) {
            dialog.show();
        }
    }

    private int fingerSideType = FingerEngine.LEFT_HAND;
    //requesting the camera permission
    public void requestCameraPermission() {
        int currentapiVersion = Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2296) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    doWork();
                }
            }
        } else if (requestCode == USER_ENROLL && resultCode == RESULT_OK) {
            ProgressDialog progressBar = new ProgressDialog(this);
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setMessage("Processing Fingerprints...");
            progressBar.setCancelable(false);
            if (!isFinishing()) {
                progressBar.show();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FingerEngine fingerEngine = new FingerEngine();
                    List<FingerModel> fingerModels = dbhelper.getUserByUniqueID(FingerModel.getModel());
                    fingerEngine.processImage(fingerModels);
                    for (FingerModel model : fingerModels){
                        dbhelper.updateFeatures(model);
                    }

                    runOnUiThread(() -> progressBar.setMessage("Checking the enrollment..."));
                    boolean isValid = fingerEngine.fingerValidation(fingerModels, 0);
                    if (!isValid) {
                        addMember(0, fingerModels, null);
                        return;
                    }
                    List<FingerModel> fingerAPIModels = dbhelper.getAllUser(fingerModels.get(0), false);

                    JSONObject object = new JSONObject();
                    boolean _isValid = fingerEngine.checkingEnrollment(fingerModels, fingerAPIModels, object);
                    if (_isValid) {
                        object = null;
                    }
                    addMember(1, fingerModels, object);
                }

                private void addMember(int isGood, List<FingerModel> fingerModels, JSONObject object) {
                    runOnUiThread(() -> {
                        if (progressBar.isShowing()) {
                            progressBar.dismiss();
                        }
                        if (object == null && isGood == 1)
                            Toast.makeText(MainActivity.this, "Member successfully added", Toast.LENGTH_LONG).show();
                        else {
                            if (isGood == 0) {
                                reEnrollDialog(fingerModels.get(0).getUserName(), "Enrollment Failed, Bad Prints Detected");
                            } else if (object != null) {
                                try {
                                    String name = object.getString("name");
                                    reEnrollDialog(fingerModels.get(0).getUserName(), "Enrollment failed, Found duplicate with " + name);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            for (FingerModel model : fingerModels){
                                dbhelper.deleteUser(model); // remove invalid user which is already added in database
                            }

                        }
                    });

                }
            }).start();
        } else if (requestCode == USER_VERIFY && resultCode == RESULT_OK) {
            final ProgressDialog progressBar = new ProgressDialog(this);
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setMessage("Processing Fingerprints...");
            progressBar.setCancelable(false);
            if (!isFinishing()) {
                progressBar.show();
            }
            final FingerModel fingerModel = FingerModel.getModel();
            final FingerEngine fingerEngine = new FingerEngine();
            Runnable runnable = new Runnable() {
                public void run() {
                    List<FingerModel> fingerModels = new DatabaseHelper(MainActivity.this).getAllUser(FingerModel.getModel(), true);
                    fingerEngine.processImage(fingerModel);

                    runOnUiThread(() -> progressBar.setMessage("Matching Data..."));

                    float v2 = fingerEngine.fingerAuthentication(fingerModel, fingerModels, new JSONObject());

                    runOnUiThread(() -> {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                        builder1.setTitle("Hello " + fingerModel.getUserName() + ",");
                        builder1.setMessage(fingerModel.getStrings()[0]);
                        builder1.setPositiveButton(
                                "OK",
                                (dialog, id) -> {
                                    dialog.cancel();
                                });
                        AlertDialog alert11 = builder1.create();
                        alert11.setCancelable(false);
                        alert11.setCanceledOnTouchOutside(false);
                        alert11.show();
                    });
                    if (progressBar.isShowing()) {
                        progressBar.dismiss();
                    }
                }
            };
            new Handler().postDelayed(runnable, 10);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        }
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        doWork();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(this, "You declined to allow the app to access your camera", Toast.LENGTH_LONG).show();
                }
        }
    }

    public void doWork() {
        progressBar = new ProgressDialog(this);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setMessage("Please wait...");
        progressBar.setCanceledOnTouchOutside(false);
        progressBar.setCancelable(false);
        if (!isFinishing()) {
            progressBar.show();
            nativeThread.start();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        if (state != null) {
        }
    }

}
