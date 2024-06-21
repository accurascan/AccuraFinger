# AccuraScan's Android SDK for KYC & ID Verification - FingerPrint Verification

Finger Verification Is Used for Matching Both The Source And The Target User's fingerprint

Below steps to setup AccuraScan's Finger SDK to your project.

## Install Finger SDK in to your App

#### Step 1: Add the JitPack repository to your build file:
    Add it in your root build.gradle at the end of repositories.

    allprojects {
        repositories {
            ...
            maven {
                url 'https://jitpack.io'
                credentials { username authToken }
            }
        }
    }

#### Step 2. Add the token to `gradle.properties`:

    authToken=jp_45kf9tvkijvd9c7cf34mehj1b6

#### Step 3: Add the dependency:
    Set Accura SDK as a dependency to our app/build.gradle file.

    android {
    
        defaultConfig {
            ...
            ndk {
                // Specify CPU architecture.
                // 'armeabi-v7a' & 'arm64-v8a' are respectively 32 bit and 64 bit device architecture 
                // 'x86' & 'x86_64' are respectively 32 bit and 64 bit emulator architecture
                abiFilters 'armeabi-v7a', 'arm64-v8a', 'x86', 'x86_64'
            }
        }
        compileOptions {
            coreLibraryDesugaringEnabled true
            sourceCompatibility JavaVersion.VERSION_17
            targetCompatibility JavaVersion.VERSION_17
        }
		
    }
    dependencies {
        ...

        coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:1.1.5'
        // Accura Finger
        implementation 'com.github.accurascan:AccuraFingerSDK:1.0.6'
    }

#### Step 4: Add files to project assets folder:

    Create "assets" folder under app/src/main and Add license file in to assets folder.
    - key.license
    Generate your Accura license from https://accurascan.com/developer/dashboard

## 1. Setup Accura OCR
* Require `key.license` to implement Accura OCR in to your app
#### Step 1 : To initialize sdk on app start:

    FingerEngine fingerEngine = new FingerEngine();
    FingerEngine.SDKModel sdkModel = fingerEngine.initEngine(your activity context);

    if (sdkModel.i > 0) { // if license is valid
         if (sdkModel.isFingerEnable) // RecogType.FINGER_PRINT
    }

#### Step 2 : Set CameraView
```
Must have to extend com.accurascan.ocr.mrz.motiondetection.SensorsActivity to your activity.
- Make sure your activity orientation locked from Manifest. Because auto rotate not support.

private CameraView cameraView;

@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.your layout);

    // Recog type selection base on your license data
    RecogType recogType = RecogType.FingerPrint;

    // initialized camera
    initCamera();
}

private void initCamera() {
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
        // fingerType = FingerEngine.FINGER_ENROLL or FingerEngine.FINGER_VERIFY
        // fingerSideType = FingerEngine.LEFT_HAND  or FingerEngine.RIGHT_HAND
        cameraView.setFingerType(fingerType, fingerSideType);
        cameraView.setUserName(userName);
    }
    cameraView.setRecogType(recogType)
            .setFrameView(ocr_frame) // make sure ocr_frame 4 child layout same as used in this demo app
            .setFlashMode(CameraView.FLASH_MODE_ON) // CameraView.FLASH_MODE_OFF
            .setView(linearLayout) // To add camera view
            .setCameraFacing(0) // // To set selfie(1) or rear(0) camera.
            .setFingerCallback(this)  // To get feedback and Success Call back
            .setStatusBarHeight(statusBarHeight)  // To remove Height from Camera View if status bar visible
//                Optional setup
//                .setEnableMediaPlayer(false) // false to disable default sound and true to enable sound and default it is true
//                .setCustomMediaPlayer(MediaPlayer.create(this, /*custom sound file*/)) // To add your custom sound and Must have to enable media player
            .init();  // initialized camera
}

/**
 * To handle camera on window focus update
 * @param hasFocus
 */
@Override
public void onWindowFocusChanged(boolean hasFocus) {
    if (cameraView != null) {
        cameraView.onWindowFocusUpdate(hasFocus);
    }
}

@Override
protected void onResume() {
    super.onResume();
    if (cameraView != null) cameraView.onResume();
}

@Override
protected void onPause() {
    cameraView.onPause();
    if (cameraView != null) super.onPause();
}

@Override
protected void onDestroy() {
    if (cameraView != null) cameraView.onDestroy();
    super.onDestroy();
}

/**
 * Call {@link CameraView#startOcrScan(boolean isReset)} To start Camera Preview
 */
@Override
public void onUpdateLayout(int width, int height) {
    if (cameraView != null) cameraView.startOcrScan(false);
}

/**
 * Override this method after scan complete to get data
 */
@Override
public void onScannedComplete(Object result) {
    // display data on ui thread
    Log.e("TAG", "onScannedComplete: ");
    if (result != null && recogType == RecogType.FINGER_PRINT) {
    	// make sure release camera view before open result screen
    	// if (cameraView != null) cameraView.release(true);
        // Do some code for display data
        FingerEngine fingerEngine = new FingerEngine();
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
                
                // Access data from database
                List<FingerModel> fingerModels = dbhelper.getUserByUniqueID(fingerModel);
                
                // Proccsing fingerprint images
                fingerEngine.processImage(fingerModels);
                
                // To manage local database
                for (FingerModel model : fingerModels){
                    dbhelper.updateFeatures(model);
                }
                
                
                boolean isValid = fingerEngine.fingerValidation(fingerModels, 0);
                if (isValid) {
                    Toast.makeText(this, "Enrollment Failed, Bad Prints Detected", Toast.LENGTH_SHORT).show()
                    
                    // delete user from local database
                    for (FingerModel model : fingerModels){
                        dbhelper.deleteUser(model);
                    }
                    return;
                }
                List<FingerModel> fingerAPIModels = dbhelper.getAllUser(fingerModels.get(0), false);

                JSONObject object = new JSONObject();
                boolean _isValid = fingerEngine.checkingEnrollment(fingerModels, fingerAPIModels, object);
                if (_isValid) {
                    Toast.makeText(this, "Member successfully added", Toast.LENGTH_SHORT).show()
                } else {
                    // delete user from local database
                    for (FingerModel model : fingerModels){
                        dbhelper.deleteUser(model);
                    }
                    Toast.makeText(this, "Enrollment failed, Found duplicate with " + object.getString("name"), Toast.LENGTH_SHORT).show()
                }
                
            } else if (result instanceof FingerModel) {
                ((FingerModel) result).setUserName(uniqueName); // set enrolled user name to match with
                ((FingerModel) result).setUniqueID(uniqueId); // set enrolled unique Id to match with
                FingerModel.setModel((FingerModel) result);
                
                fingerEngine.processImage(fingerModel);
                List<FingerModel> fingerModels = new DatabaseHelper(FingerActivity.this).getAllUser(fingerModel, true);
                float v2 = fingerEngine.fingerAuthentication(fingerModel, fingerModels, 0, new JSONObject(), "", null);
                
                Toast.makeText(this, "Hello " + fingerModel.getUserName() + ",\n" + fingerModel.getStrings()[0], Toast.LENGTH_SHORT).show();
                
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
 * @param isFlip  true to set your customize animation for scan back card alert after complete front scan
 *                and also used cameraView.flipImage(ImageView) for default animation
 */
@Override
public void onProcessUpdate(int titleCode, String errorMessage, boolean isFlip) {
// make sure update view on ui thread
    runOnUiThread(new Runnable() {
        @Override
        public void run() {
            // Review FingerActivity.java file for UI changes and messages 
   
            if (errorMessage != null) {
                Toast.makeText(context, getErrorMessage(errorMessage), Toast.LENGTH_SHORT).show(); // display message
            } else {
                // show/hide animation if finger are not in camera
            }
        }
    });
}

@Override
public void onProgress(float progress) {
    // To display scanning progress
    Toast.makeText(context, progress, Toast.LENGTH_SHORT).show();
}

@Override
public void onError(String errorMessage) {
    // display data on ui thread
    // stop ocr if failed
    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
}
```

## ProGuard

Depending on your ProGuard (DexGuard) config and usage, you may need to include the following lines in your proguards.

```
-keep public class com.machinezoo.sourceafis.**
```
