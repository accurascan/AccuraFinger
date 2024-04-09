package com.accura.finger.print.demo.adapter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.accura.finger.print.demo.R;
import com.accura.finger.print.sdk.model.FingerModel;
import com.bumptech.glide.Glide;

import java.util.List;

public class FingerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = FingerAdapter.class.getSimpleName();
    Context context;
    List<FingerModel> countryList;

    public FingerAdapter(Context context, List<FingerModel> countryList) {
        this.countryList = countryList;
        this.context = context;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.table_row_primery, parent, false));
    }

    private boolean isNetworkAvailable() {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FingerModel country = countryList.get(position);
        ((ViewHolder) holder).tv_username.setText(country.getUserName() + " -> " + country.getUniqueID());
//        ((ViewHolder) holder).tvType1.setText("Index Finger : \n \n SIFT : " + country.getSIFT_array()[1] + "\n Quality : " + country.getQuality_array()[1]);
//        ((ViewHolder) holder).tvType2.setText("Middle Finger : \n \n SIFT : " + country.getSIFT_array()[2] + "\n Quality : " + country.getQuality_array()[2]);
//        ((ViewHolder) holder).tvType3.setText("Ring Finger : \n \n SIFT : " + country.getSIFT_array()[3] + "\n Quality : " + country.getQuality_array()[3]);
//        ((ViewHolder) holder).tvType4.setText("Little Finger : \n \n SIFT : " + country.getSIFT_array()[4] + "\n Quality : " + country.getQuality_array()[4]);
        ((ViewHolder) holder).tvType1.setText("Index Finger");
        ((ViewHolder) holder).tvType2.setText("Middle Finger");
        ((ViewHolder) holder).tvType3.setText("Ring Finger");
        ((ViewHolder) holder).tvType4.setText("Little Finger");
//        Glide.with(context).load(country.getIndexFinger()).into(((ViewHolder) holder).img1);
//        Glide.with(context).load(country.getMiddleFinger()).into(((ViewHolder) holder).img2);
//        Glide.with(context).load(country.getRingFinger()).into(((ViewHolder) holder).img3);
//        Glide.with(context).load(country.getLittleFinger()).into(((ViewHolder) holder).img4);

//        ((ViewHolder) holder).tv1.setText(getColoredString(country.getFloatArrayOld()[1], country.getFloatArrayMinutiae()[1], country.getFloatArrayNewMinutiae()[1]));
//        ((ViewHolder) holder).tv2.setText(getColoredString(country.getFloatArrayOld()[2], country.getFloatArrayMinutiae()[2], country.getFloatArrayNewMinutiae()[2]));
//        ((ViewHolder) holder).tv3.setText(getColoredString(country.getFloatArrayOld()[3], country.getFloatArrayMinutiae()[3], country.getFloatArrayNewMinutiae()[3]));
//        ((ViewHolder) holder).tv4.setText(getColoredString(country.getFloatArrayOld()[4], country.getFloatArrayMinutiae()[4], country.getFloatArrayNewMinutiae()[4]));

        Glide.with(context).load(country.getIndexFingerInverted()).into(((ViewHolder) holder).img1);
        Glide.with(context).load(country.getMiddleFingerInverted()).into(((ViewHolder) holder).img2);
        Glide.with(context).load(country.getRingFingerInverted()).into(((ViewHolder) holder).img3);
        Glide.with(context).load(country.getLittleFingerInverted()).into(((ViewHolder) holder).img4);

        ((ViewHolder) holder).tv1.setText(getColoredString(country.getInvertedFloatArrayOld()[1], 0,0));//country.getInvertedFloatArrayMinutiae()[1], country.getInvertedFloatArrayNewMinutiae()[1]));
        ((ViewHolder) holder).tv2.setText(getColoredString(country.getInvertedFloatArrayOld()[2], 0,0));//country.getInvertedFloatArrayMinutiae()[2], country.getInvertedFloatArrayNewMinutiae()[2]));
        ((ViewHolder) holder).tv3.setText(getColoredString(country.getInvertedFloatArrayOld()[3], 0,0));//country.getInvertedFloatArrayMinutiae()[3], country.getInvertedFloatArrayNewMinutiae()[3]));
        ((ViewHolder) holder).tv4.setText(getColoredString(country.getInvertedFloatArrayOld()[4], 0,0));//country.getInvertedFloatArrayMinutiae()[4], country.getInvertedFloatArrayNewMinutiae()[4]));
    }
    public Spannable getColoredString(float SIFScore, float minutiaeScore, float newMinutiaeScore) {

        SpannableStringBuilder builder = new SpannableStringBuilder();

        SpannableString str1= new SpannableString(" Final Score: ");
        str1.setSpan(new ForegroundColorSpan(this.context.getResources().getColor(R.color.colorAccent)), 0, str1.length(), 0);
        builder.append(str1);
        SpannableString str2= new SpannableString("" + SIFScore);
        str2.setSpan(new ForegroundColorSpan(this.context.getResources().getColor(R.color.darkGray)), 0, str2.length(), 0);
        builder.append(str2);

//        SpannableString minutiaeStr1= new SpannableString("\n Try Score 1: ");
//        minutiaeStr1.setSpan(new ForegroundColorSpan(this.context.getResources().getColor(R.color.colorAccent)), 0, minutiaeStr1.length(), 0);
//        builder.append(minutiaeStr1);
//        SpannableString minutiaeStr2= new SpannableString("" + minutiaeScore);
//        minutiaeStr2.setSpan(new ForegroundColorSpan(this.context.getResources().getColor(R.color.darkGray)), 0, minutiaeStr2.length(), 0);
//        builder.append(minutiaeStr2);
//
//        SpannableString newMinutiaeStr1= new SpannableString("\n Try Score 2: ");
//        newMinutiaeStr1.setSpan(new ForegroundColorSpan(this.context.getResources().getColor(R.color.colorAccent)), 0, newMinutiaeStr1.length(), 0);
//        builder.append(newMinutiaeStr1);
//        SpannableString newMinutiaeStr2= new SpannableString("" + newMinutiaeScore);
//        newMinutiaeStr2.setSpan(new ForegroundColorSpan(this.context.getResources().getColor(R.color.darkGray)), 0, newMinutiaeStr2.length(), 0);
//        builder.append(newMinutiaeStr2);
        return builder;
    }

    @Override
    public int getItemCount() {
        return countryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_username,tv1,tv2,tv3,tv4;
        ImageView img1,img2,img3,img4;
//        TextView tv12,tv22,tv32,tv42;
        TextView tvType1,tvType2,tvType3,tvType4;
        ImageView img12,img22,img32,img42;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_username = itemView.findViewById(R.id.tv_username);
            tvType1 = itemView.findViewById(R.id.tv_type1);
            tvType2 = itemView.findViewById(R.id.tv_type2);
            tvType3 = itemView.findViewById(R.id.tv_type3);
            tvType4 = itemView.findViewById(R.id.tv_type4);
            tv1 = itemView.findViewById(R.id.tv_name1);
            img1 = itemView.findViewById(R.id.iv_image1);
            tv2 = itemView.findViewById(R.id.tv_name2);
            img2 = itemView.findViewById(R.id.iv_image2);
            tv3 = itemView.findViewById(R.id.tv_name3);
            img3 = itemView.findViewById(R.id.iv_image3);
            tv4 = itemView.findViewById(R.id.tv_name4);
            img4 = itemView.findViewById(R.id.iv_image4);
//            tv12 = itemView.findViewById(R.id.tv_name12);
            img12 = itemView.findViewById(R.id.iv_image12);
//            tv22 = itemView.findViewById(R.id.tv_name22);
            img22 = itemView.findViewById(R.id.iv_image22);
//            tv32 = itemView.findViewById(R.id.tv_name32);
            img32 = itemView.findViewById(R.id.iv_image32);
//            tv42 = itemView.findViewById(R.id.tv_name42);
            img42 = itemView.findViewById(R.id.iv_image42);

        }
    }
}
