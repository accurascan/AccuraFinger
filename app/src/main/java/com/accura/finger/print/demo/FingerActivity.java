package com.accura.finger.print.demo;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.accura.finger.print.demo.customView.SemiCircleView;
import com.accura.finger.print.sdk.CameraView;
import com.accura.finger.print.sdk.interfaces.FingerCallback;
import com.accura.finger.print.sdk.model.FingerModel;
import com.accura.finger.print.sdk.motiondetection.SensorsActivity;
import com.accura.finger.print.sdk.util.AccuraFingerLog;
import com.accura.finger.scan.FingerEngine;
import com.accura.finger.scan.RecogType;

import java.lang.ref.WeakReference;

public class FingerActivity extends SensorsActivity implements FingerCallback {

    private static final String TAG = FingerActivity.class.getSimpleName();
    private CameraView cameraView;
    private TextView tvTitle, tvScanMessage;
    private HorizontalProgressView tvProgress;
    RecogType recogType;
    private ViewGroup ocr_frame;
    private String fingerType = "enroll";
    private String userName;
    int progress = 0;
    private long uniqueId;
    private int companyId = -1;
    private String uniqueName;
    private int fingerSideType = 0;
    private ImageView imHintSmall;
    private boolean hideAnim = false;

    private static class MyHandler extends Handler {
        private final WeakReference<FingerActivity> mActivity;

        public MyHandler(FingerActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            FingerActivity activity = mActivity.get();
            if (activity != null) {
                String s = "";
                if (msg.obj instanceof String) s = (String) msg.obj;
                switch (msg.what) {
                    case 1:
                        if (s.contains("%")) {
                            s = s.substring(0, s.indexOf("."));
                            activity.progress = Integer.parseInt(s);
                            activity.tvProgress.setProgress(activity.progress);
                        }
                        else {
                            if (!TextUtils.isEmpty(s) && (!s.contains("011") && !s.contains("111")) && !activity.hideAnim) {
                                activity.hideAnim = true;
                                activity.imHintSmall.setVisibility(View.GONE);
                            }
                            if (s.isEmpty()) {
                                activity.tvTitle.setText(s);
                            } else if (s.contains("011")) {
                                if (activity.fingerSideType == 1) {
                                    activity.tvTitle.setText(R.string.info_right_msg);
                                    break;
                                }
                                activity.tvTitle.setText(R.string.info_msg);
                            } else if (s.contains("111")) {
                                if (s.equalsIgnoreCase("1111")) {
                                    activity.hideAnim = false;
                                    activity.imHintSmall.setVisibility(View.VISIBLE);
                                }
                                activity.tvTitle.setText(R.string.keep_distance);
                            } else if (s.equalsIgnoreCase("211")) {
                                activity.tvTitle.setText(R.string.away_message);
                            } else if (s.equalsIgnoreCase("311")) {
                                activity.tvTitle.setText(R.string.closer_msg);
                            } else activity.tvTitle.setText(R.string.finger_error_msg);
                        }
                        break;
                    default: break;
                }
            }
            super.handleMessage(msg);
        }
    }

    private Handler handler = new MyHandler(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // Hide the window title.
        setContentView(R.layout.ocr_activity);

        recogType = RecogType.detachFrom(getIntent());
        fingerType = getIntent().getStringExtra("finger_type");
        userName = getIntent().getStringExtra("user_name");
        uniqueId = getIntent().getLongExtra("unique_id", -1); // for local database
        uniqueName = getIntent().getStringExtra("unique_name");
        fingerSideType = getIntent().getIntExtra("left_right_type", 0);

        AccuraFingerLog.loge(TAG, "RecogType " + recogType);

        init();
        initCamera();
    }

    private void initCamera() {
        AccuraFingerLog.loge(TAG, "Initialized camera");
        //<editor-fold desc="To get status bar height">
        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarTop = rectangle.top;
        int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int statusBarHeight = contentViewTop - statusBarTop;
        //</editor-fold>

        RelativeLayout linearLayout = findViewById(R.id.ocr_root); // layout width and height is match_parent

        cameraView = new CameraView(this);
        if (recogType == RecogType.FINGER_PRINT) {
            cameraView.setFingerType(fingerType, fingerSideType);
            cameraView.setName(userName);
        }
        cameraView.setRecogType(recogType)
                .setFrameView(ocr_frame)
                .setFlashMode(CameraView.FLASH_MODE_ON)
                .setView(linearLayout) // To add camera view
                .setCameraFacing(0) // To set front or back camera.
                .setOcrCallback(this)  // To get Update and Success Call back
                .setStatusBarHeight(statusBarHeight)  // To remove Height from Camera View if status bar visible
//                optional field
//                .setEnableMediaPlayer(false) // false to disable sound and true to enable sound and default it is true
                .init();  // initialized camera
    }

    private void init() {
        ImageView imFlashMode = findViewById(R.id.im_flash_mode);
        imFlashMode.setOnClickListener(view -> {
            if (imFlashMode.getContentDescription().equals("on")) {
                imFlashMode.setImageDrawable(ContextCompat.getDrawable(FingerActivity.this, R.drawable.ic_flash_off));
                imFlashMode.setContentDescription("off");
                cameraView.setFlashMode(CameraView.FLASH_MODE_OFF);
            } else {
                imFlashMode.setImageDrawable(ContextCompat.getDrawable(FingerActivity.this, R.drawable.ic_flash_on));
                imFlashMode.setContentDescription("on");
                cameraView.setFlashMode(CameraView.FLASH_MODE_ON);
            }
        });
        SemiCircleView borderView = findViewById(R.id.border_view);
        borderView.setType(fingerSideType);
        tvTitle = findViewById(R.id.tv_title);
        tvScanMessage = findViewById(R.id.tv_scan_msg1);
        tvProgress = findViewById(R.id.tv_progress);
        ocr_frame = findViewById(R.id.ocr_frame);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        float halfH = (height *0.11f);
        height -= (2*halfH);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) ocr_frame.getLayoutParams();
        if (fingerSideType == 1) {
            layoutParams.leftMargin = (int) (width*0.08f);
        }
        layoutParams.width = (int) (width-(width*0.08f));
        layoutParams.height = height;

        ocr_frame.setLayoutParams(layoutParams);
        RelativeLayout.LayoutParams tvlp = (RelativeLayout.LayoutParams) tvProgress.getLayoutParams();
        if (fingerSideType == 1) {
            tvlp.leftMargin = (int) (width*0.08f);
        }
        tvlp.width = (int) (width-(width*0.08f));
        tvProgress.setLayoutParams(tvlp);

        ImageView imIndex = (ImageView) ocr_frame.getChildAt(0);

        ImageView imMiddle = (ImageView) ocr_frame.getChildAt(1);

        ImageView imRing = (ImageView) ocr_frame.getChildAt(2);

        ImageView imLittle = (ImageView) ocr_frame.getChildAt(3);

        imHintSmall = findViewById(R.id.im_hint_small);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imHintSmall.getLayoutParams();
        if (fingerSideType == 1) {
            params.leftMargin = dp2px(10);
            imHintSmall.setRotationY(180);
            tvTitle.setText(R.string.info_right_msg);
            imIndex.setRotation(180);
            imMiddle.setRotation(180);
            imRing.setRotation(180);
            imLittle.setRotation(180);
        } else
            params.rightMargin = dp2px(10);
    }

    public int dp2px(int px){
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                px, metrics);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (cameraView != null) cameraView.onWindowFocusUpdate(hasFocus);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraView != null) cameraView.onResume();
    }

    @Override
    protected void onPause() {
        if (cameraView != null) cameraView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        AccuraFingerLog.loge(TAG, "onDestroy");
        if (cameraView != null) cameraView.onDestroy();
        super.onDestroy();
        Runtime.getRuntime().gc(); // to clear garbage
    }

    /**
     * Override method call after camera initialized successfully
     *
     * And update your border frame according to width and height
     * it's different for different card
     *
     * Call {@link CameraView#startOcrScan(boolean isReset)} To start Camera Preview
     *
     * @param width    border layout width
     * @param height   border layout height
     */
    @Override
    public void onUpdateLayout(int width, int height) {
        AccuraFingerLog.loge(TAG, "Frame Size (wxh) : " + width + "x" +  height);
        if (cameraView != null) cameraView.startOcrScan(false);
    }
    @Override
    public void onScannedComplete(Object result) {
        Runtime.getRuntime().gc(); // To clear garbage
        AccuraFingerLog.loge(TAG, "onScannedComplete: ");
        if (result != null && recogType == RecogType.FINGER_PRINT) {
            Runnable runnable = () -> {
                if (fingerType.equals("enroll")) {
                    FingerModel.setModel((FingerModel) result);
                    Intent intent = getIntent();
                    intent.putExtra("uniqueID", ((FingerModel) result).getUniqueID());
                    setResult(RESULT_OK);
                    finish();
                } else {
                    ((FingerModel) result).setUserName(uniqueName); // set enrolled user name to match with
                    ((FingerModel) result).setUniqueID(uniqueId); // set enrolled user name to match with
                    ((FingerModel) result).setCompanyID(companyId); // set enrolled user name to match with
                    FingerModel.setModel((FingerModel) result);
                    Intent intent = getIntent();
                    intent.putExtra("uniqueID", ((FingerModel) result).getUniqueID());
                    setResult(RESULT_OK);
                    finish();
                }
            };
            runOnUiThread(runnable);
        } else Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
    }

    /**
     * @param titleCode to display scan card message on top of border Frame
     *
     * @param errorMessage To display process message.
     *                null if message is not available
     * @param isFlip  To set your customize animation after complete front scan
     */
    @Override
    public void onProcessUpdate(int titleCode, String errorMessage, boolean isFlip) {
        AccuraFingerLog.loge(TAG, "onProcessUpdate :-> " + titleCode + "," + errorMessage + "," + isFlip);
        Message message;

        if (errorMessage != null) {
            message = new Message();
            message.what = 1;
            message.obj = getErrorMessage(errorMessage);
            handler.sendMessage(message);
//            tvScanMessage.setText(message);
        }

    }

    private String getErrorMessage(String s) {
        switch (s) {
            case FingerEngine.ACCURA_ERROR_CODE_MOTION:
                return "Keep Document Steady";
            case FingerEngine.ACCURA_ERROR_CODE_PROCESSING:
                return "Processing...";
            case FingerEngine.ACCURA_ERROR_CODE_BLUR_DOCUMENT:
                return "Blur detect in document";
            default:
                return s;
        }
    }

    @Override
    public void onError(final String errorMessage) {
        // stop ocr if failed
        tvScanMessage.setText(errorMessage);
        Runnable runnable = () -> Toast.makeText(FingerActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        runOnUiThread(runnable);
    }
}