package com.accura.finger.print.demo;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.accura.finger.print.demo.database.DatabaseHelper;
import com.accura.finger.print.sdk.CameraView;
import com.accura.finger.print.sdk.interfaces.FingerCallback;
import com.accura.finger.print.sdk.model.FingerModel;
import com.accura.finger.print.sdk.motiondetection.SensorsActivity;
import com.accura.finger.print.sdk.util.AccuraFingerLog;
import com.accura.finger.scan.FingerEngine;
import com.accura.finger.scan.RecogType;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class FingerActivity extends SensorsActivity implements FingerCallback {

    private static final String TAG = FingerActivity.class.getSimpleName();
    private CameraView cameraView;
    private TextView tvTitle, tvScanMessage;
    private HorizontalProgressView tvProgress;
    RecogType recogType;
    private ViewGroup ocr_frame;
    private String fingerType = FingerEngine.FINGER_ENROLL;
    private String userName;
    int progress = 0;
    private long uniqueId;
    private String uniqueName;
    private int fingerSideType;
    private ImageView imHintSmall;
    private boolean hideAnim = false;
    private DatabaseHelper dbHelper;

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
                        activity.tvTitle.setText(s);
                        break;
                    case 2:
                        if (msg.obj instanceof Float) {
                            float progress = ((Float) msg.obj);
                            activity.progress = (int) progress;
                        }
                        activity.tvProgress.setProgress(activity.progress);
                        break;
                    case 3:
                        if (msg.obj instanceof Boolean) activity.hideAnim = (Boolean) msg.obj;
                        if (activity.hideAnim) {
                            activity.imHintSmall.setVisibility(View.VISIBLE);
                        } else activity.imHintSmall.setVisibility(View.GONE);
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
        fingerType = getIntent().getStringExtra("finger_scan_type");
        userName = getIntent().getStringExtra("user_name");
        uniqueId = getIntent().getLongExtra("unique_id", -1); // for local database
        uniqueName = getIntent().getStringExtra("unique_name");
        fingerSideType = getIntent().getIntExtra("left_right_type", FingerEngine.LEFT_HAND);

        AccuraFingerLog.loge(TAG, "RecogType " + recogType);
        if (this.dbHelper == null) this.dbHelper = new DatabaseHelper(this);
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
            cameraView.setUserName(userName);
        }
        cameraView.setRecogType(recogType)
                .setFrameView(ocr_frame)
                .setFlashMode(CameraView.FLASH_MODE_ON)
                .setView(linearLayout) // To add camera view
                .setCameraFacing(0) // To set front or back camera.
                .setFingerCallback(this)  // To get Update and Success Call back
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

        // set Progress View Layout Params
        RelativeLayout.LayoutParams tvlp = (RelativeLayout.LayoutParams) tvProgress.getLayoutParams();
        if (fingerSideType == FingerEngine.RIGHT_HAND) {
            tvlp.leftMargin = (int) (width*0.08f);
        }
        tvlp.width = (int) (width-(width*0.08f));
        tvProgress.setLayoutParams(tvlp);

        // set Frame Layout Params
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) ocr_frame.getLayoutParams();
        if (fingerSideType == FingerEngine.RIGHT_HAND) {
            layoutParams.leftMargin = (int) (width*0.08f);
        }
        layoutParams.width = (int) (width-(width*0.08f));
        layoutParams.height = height;
        ocr_frame.setLayoutParams(layoutParams);

        // Access all 4 Sub Layout of layout Params
        ImageView imIndex = (ImageView) ocr_frame.getChildAt(0);

        ImageView imMiddle = (ImageView) ocr_frame.getChildAt(1);

        ImageView imRing = (ImageView) ocr_frame.getChildAt(2);

        ImageView imLittle = (ImageView) ocr_frame.getChildAt(3);

        imHintSmall = findViewById(R.id.im_hint_small);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imHintSmall.getLayoutParams();
        if (fingerSideType == FingerEngine.RIGHT_HAND) {
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
                if (fingerType.equals(FingerEngine.FINGER_ENROLL)) {
                    List<?> list = new ArrayList<>();
                    if (result.getClass().isArray() ) {
                        list = Arrays.asList((Object[])result);
                    } else if (result instanceof Collection) {
                        list = new ArrayList<>((Collection<?>) result);
                    }
                    long uniqueID = -1;
                    FingerModel fingerModel = null;
                    for (Object o : list) {
                        fingerModel = (FingerModel) o;
                        uniqueID = dbHelper.addUser(fingerModel, uniqueID);
                    }
                    FingerModel.setModel(fingerModel);
                    Intent intent = getIntent();
                    intent.putExtra("uniqueID", uniqueID);
                    setResult(RESULT_OK);
                    finish();
                } else if (result instanceof FingerModel){
                    ((FingerModel) result).setUserName(uniqueName); // set enrolled user name to match with
                    ((FingerModel) result).setUniqueID(uniqueId); // set enrolled user name to match with
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
     * @param titleCode
     *
     * @param errorMessage To display process message.
     *                null if message is not available
     * @param isShowAnim  To set your customize animation after for hint
     */
    @Override
    public void onProcessUpdate(int titleCode, String errorMessage, boolean isShowAnim) {
        AccuraFingerLog.loge(TAG, "onProcessUpdate :-> " + titleCode + "," + errorMessage + "," + isShowAnim);
        Message message;

        if (errorMessage != null) {
            message = new Message();
            message.what = 1;
            message.obj = getErrorMessage(errorMessage);
            handler.sendMessage(message);
        } else {
            message = new Message();
            message.what = 3;
            message.obj = isShowAnim;
            handler.sendMessage(message);
        }
    }

    private String getErrorMessage(String s) {
        switch (s) {
            case FingerEngine.ACCURA_ERROR_CODE_RIGHT_HAND:
                return getResources().getString(R.string.info_right_msg);
            case FingerEngine.ACCURA_ERROR_CODE_LEFT_HAND:
                return getResources().getString(R.string.info_msg);
            case FingerEngine.ACCURA_ERROR_CODE_KEEP_DISTANCE:
                return getResources().getString(R.string.keep_distance);
            case FingerEngine.ACCURA_ERROR_CODE_AWAY:
                return getResources().getString(R.string.away_message);
            case FingerEngine.ACCURA_ERROR_CODE_CLOSER:
                return getResources().getString(R.string.closer_msg);
            case FingerEngine.ACCURA_ERROR_CODE_MESSAGE:
                return getResources().getString(R.string.finger_error_msg);
            default:
                return s;
        }
    }

    @Override
    public void onProgress(float progress) {
        Message message = new Message();
        message.what = 2;
        message.obj = progress;
        handler.sendMessage(message);
    }

    @Override
    public void onError(final String errorMessage) {
        // stop ocr if failed
        tvScanMessage.setText(errorMessage);
        Runnable runnable = () -> Toast.makeText(FingerActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        runOnUiThread(runnable);
    }
}