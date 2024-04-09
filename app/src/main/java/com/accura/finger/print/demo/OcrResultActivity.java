package com.accura.finger.print.demo;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.accura.finger.print.demo.adapter.FingerAdapter;
import com.accura.finger.print.sdk.model.FingerModel;
import com.bumptech.glide.Glide;
import com.accura.finger.scan.RecogType;

import java.util.List;
import java.util.Locale;

public class OcrResultActivity extends AppCompatActivity {
    TableLayout  front_table_layout;
    View  ly_front_container;
    List<FingerModel> fingerModels ;
    private RecyclerView rvFinger;


    protected void onCreate(Bundle savedInstanceState) {
        if (getIntent().getIntExtra("app_orientation", 1) != 0) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_result);

        initUI();
        RecogType recogType = RecogType.detachFrom(getIntent());

        if (recogType == RecogType.FINGER_PRINT) {
            final FingerModel fingerModel = FingerModel.getModel();
            if (fingerModel != null) {
                fingerModels = FingerModel.getModels();
                setFingerPrintData(fingerModel);
            }
        }
    }

    private void initUI() {
        //initialize the UI

        front_table_layout = findViewById(R.id.front_table_layout);

        ly_front_container = findViewById(R.id.ly_front_container);

        ly_front_container.setVisibility(View.GONE);

        Button btn_list = findViewById(R.id.btn_list);
        btn_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rvFinger == null) {
                    setRVFingerAdapter();
                    btn_list.setText("Hide");
                } else {
                    if (rvFinger.getVisibility() == View.VISIBLE) {
                        rvFinger.setVisibility(View.GONE);
                        btn_list.setText("Show");
                    } else {
                        rvFinger.setVisibility(View.VISIBLE);
                        btn_list.setText("Hide");
                    }
                }
            }
        });

    }

    private void setRVFingerAdapter(){
        rvFinger = findViewById(R.id.rv_fingerList);
        rvFinger.setVisibility(View.VISIBLE);
        FingerAdapter fingerAdapter = new FingerAdapter(this, fingerModels);
        rvFinger.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        rvFinger.setAdapter(fingerAdapter);

    }

    public Spannable getColoredStringNew(String matchedString, String SIFTString, float e1, float e2, float e3) {

        SpannableStringBuilder builder = new SpannableStringBuilder();

        final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);
        SpannableString sstr0= new SpannableString(matchedString + " ");
        sstr0.setSpan(bss, 0, sstr0.length()-1, Spannable.SPAN_INCLUSIVE_INCLUSIVE); // make first 4 characters Bold
        builder.append(sstr0);

        SpannableString sstr1= new SpannableString("\nUserName: ");
        sstr1.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), 0, sstr1.length(), 0);
        builder.append(sstr1);
        SpannableString sstr2= new SpannableString(SIFTString.toUpperCase(Locale.ROOT) + ", \n");
        sstr2.setSpan(new RelativeSizeSpan(0.8f), SIFTString.length()+1,sstr2.length(), 0); // set size
        builder.append(sstr2);


        SpannableString str1= new SpannableString("1) ");
        str1.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), 0, str1.length(), 0);
        builder.append(str1);
        SpannableString str11= new SpannableString(e1+"\n");
        str11.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.darkGray)), 0, str11.length(), 0);
        builder.append(str11);
        
        SpannableString str2= new SpannableString("2) ");
        str2.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), 0, str2.length(), 0);
        builder.append(str2);
        SpannableString str22= new SpannableString(e2+"\n");
        str22.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.darkGray)), 0, str22.length(), 0);
        builder.append(str22);
        
        SpannableString str3= new SpannableString("3) ");
        str3.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), 0, str3.length(), 0);
        builder.append(str3);
        SpannableString str33= new SpannableString(e3+"");
        str33.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.darkGray)), 0, str33.length(), 0);
        builder.append(str33);


        return builder;
    }
    private void setFingerPrintData(FingerModel printData) {
        ly_front_container.setVisibility(View.VISIBLE);
        for (int i = 0; i < 5; i++) {
            String imagePath = null;
            String s = null;
            if (i == 0) {
                continue;
            } else if (i == 1) {
                s = "Index Finger";
                imagePath = printData.getIndexFinger();
            } else if (i == 2) {
                s = "Middle Finger";
                imagePath = printData.getMiddleFinger();
            } else if (i == 3) {
                s = "Ring Finger";
                imagePath = printData.getRingFinger();
            } else {
                s = "Little Finger";
                imagePath = printData.getLittleFinger();
            }
            final View layout;
            layout = LayoutInflater.from(OcrResultActivity.this).inflate(R.layout.item_finger_details, null);
            final TextView tv_key = layout.findViewById(R.id.tv_key);
            final TextView tv_value = layout.findViewById(R.id.tv_value);
            final ImageView imageView = layout.findViewById(R.id.iv_image);
            tv_key.setText(s);
            Glide.with(OcrResultActivity.this).load(imagePath).into(imageView);
            imageView.setVisibility(View.VISIBLE);

            Spannable builderInverted = getColoredStringNew(printData.getStringsOld()[0].toUpperCase(Locale.ROOT),
                    printData.getStringsOld()[i],fingerModels.get(0).getFloatArrayOld()[i],fingerModels.get(1).getFloatArrayOld()[i],fingerModels.get(2).getFloatArrayOld()[i]);

            tv_value.setVisibility(View.VISIBLE);
            tv_value.setText(builderInverted, TextView.BufferType.SPANNABLE);
            front_table_layout.addView(layout);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Runtime.getRuntime().gc();
    }

}
