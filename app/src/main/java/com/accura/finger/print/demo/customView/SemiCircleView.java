package com.accura.finger.print.demo.customView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;

import com.accura.finger.scan.FingerEngine;

public class SemiCircleView extends FrameLayout {
    private final static float CORNER_RADIUS = 50.0f; //card radius change accordingly
    private float cornerRadius;

    private Paint mTransparentPaint;
    private Paint mSemiBlackPaint;
    private Path mPath = new Path();
    private Paint mBorderPaint;
    private int fingerSideType = FingerEngine.LEFT_HAND;

    public SemiCircleView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public SemiCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public SemiCircleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        cornerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CORNER_RADIUS, metrics);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        mBorderPaint = new Paint();
        mBorderPaint.setColor(Color.WHITE);
        mBorderPaint.setStrokeWidth(10);
        mBorderPaint.setStyle(Paint.Style.STROKE);

        mTransparentPaint = new Paint();
        mTransparentPaint.setColor(Color.TRANSPARENT);
        mTransparentPaint.setStrokeWidth(10);

        mSemiBlackPaint = new Paint();
        mSemiBlackPaint.setColor(Color.TRANSPARENT);
        mSemiBlackPaint.setStrokeWidth(10);
    }
    public RectF myOval(float width, float height, float x, float y){
        float halfW = width / 2;
        float halfH = (height *0.11f);/// 2;
        if (fingerSideType == FingerEngine.RIGHT_HAND) {
            return new RectF(width*0.08f,  halfH, width+10, height-halfH);
        }
        return new RectF(x-10,  halfH, width-(width*0.08f), height-halfH);
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        int count = canvas.save();

        mPath.reset();

        mPath.addRect(myOval(canvas.getWidth(), canvas.getHeight(), 0, 0), Path.Direction.CW);
        mPath.setFillType(Path.FillType.INVERSE_WINDING);

        canvas.drawRect(myOval(canvas.getWidth(), canvas.getHeight(), 0, 0), mTransparentPaint);
        canvas.drawRect(myOval(canvas.getWidth(), canvas.getHeight(), 0, 0), mBorderPaint);
        canvas.drawPath(mPath, mSemiBlackPaint);
        canvas.clipPath(mPath);
        canvas.drawColor(Color.parseColor("#A6000000"));

        super.dispatchDraw(canvas);
        canvas.restoreToCount(count);
    }

    public void setType(int fingerSideType) {
        this.fingerSideType = fingerSideType;
    }
}