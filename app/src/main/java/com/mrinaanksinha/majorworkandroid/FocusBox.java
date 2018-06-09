package com.mrinaanksinha.majorworkandroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class FocusBox extends View
{
    private final int maskColor = Color.argb(88,0,0,0);
    private final int rectangleColor = Color.argb(255,255,255,255);
    private final float rectangleThickness = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,4, getResources().getDisplayMetrics());
    private final Paint paint;
    private final ImageView selectorView;


    public FocusBox(Context context, ImageView _selectorView)
    {
        super(context);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectorView = _selectorView;

        this.setOnTouchListener(TouchListener);


    }

    private OnTouchListener TouchListener = new OnTouchListener()
    {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            return false;
        }
    };



    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        int screenWidth = canvas.getWidth();
        int screenHeight = canvas.getHeight();

        paint.setColor(maskColor);
        canvas.drawRect(0,0,screenWidth,selectorView.getTop(),paint);
        canvas.drawRect(0,selectorView.getTop(),selectorView.getLeft(),selectorView.getBottom(),paint);
        canvas.drawRect(selectorView.getRight(),selectorView.getTop(),screenWidth,selectorView.getBottom(),paint);
        canvas.drawRect(0,selectorView.getBottom(),screenWidth,screenHeight,paint);

        paint.setColor(rectangleColor);
        canvas.drawRect(selectorView.getLeft(),selectorView.getTop(),selectorView.getLeft() + rectangleThickness,selectorView.getBottom(),paint);
        canvas.drawRect(selectorView.getLeft() + rectangleThickness,selectorView.getTop(),selectorView.getLeft() + 4 * rectangleThickness,selectorView.getTop() + rectangleThickness,paint);
        canvas.drawRect(selectorView.getLeft() + rectangleThickness,selectorView.getBottom() -rectangleThickness,selectorView.getLeft() + 4 * rectangleThickness,selectorView.getBottom(),paint);

        canvas.drawRect(selectorView.getRight() - rectangleThickness,selectorView.getTop(),selectorView.getRight(),selectorView.getBottom(),paint);
        canvas.drawRect(selectorView.getRight() - 4*rectangleThickness,selectorView.getTop(),selectorView.getRight(),selectorView.getTop() + rectangleThickness,paint);
        canvas.drawRect(selectorView.getRight() - 4* rectangleThickness,selectorView.getBottom() -rectangleThickness,selectorView.getRight(),selectorView.getBottom(),paint);


    }
}
