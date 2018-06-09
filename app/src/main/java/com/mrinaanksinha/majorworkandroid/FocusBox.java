package com.mrinaanksinha.majorworkandroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class FocusBox extends View
{
    private final int MASK_COLOR = Color.argb(88,0,0,0);
    private final int RECTANGLE_COLOR = Color.argb(255,255,255,255);
    private final float RECTANGLE_THICKNESS = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,4, getResources().getDisplayMetrics());
    private Paint paint;
    private ImageView selectorView;


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

        paint.setColor(MASK_COLOR);
        canvas.drawRect(0,0,screenWidth,selectorView.getTop(),paint);
        canvas.drawRect(0,selectorView.getTop(),selectorView.getLeft(),selectorView.getBottom(),paint);
        canvas.drawRect(selectorView.getRight(),selectorView.getTop(),screenWidth,selectorView.getBottom(),paint);
        canvas.drawRect(0,selectorView.getBottom(),screenWidth,screenHeight,paint);

        paint.setColor(RECTANGLE_COLOR);
        canvas.drawRect(selectorView.getLeft(),selectorView.getTop(),selectorView.getLeft() + RECTANGLE_THICKNESS,selectorView.getBottom(),paint);
        canvas.drawRect(selectorView.getLeft() + RECTANGLE_THICKNESS,selectorView.getTop(),selectorView.getLeft() + 4 * RECTANGLE_THICKNESS,selectorView.getTop() + RECTANGLE_THICKNESS,paint);
        canvas.drawRect(selectorView.getLeft() + RECTANGLE_THICKNESS,selectorView.getBottom() - RECTANGLE_THICKNESS,selectorView.getLeft() + 4 * RECTANGLE_THICKNESS,selectorView.getBottom(),paint);

        canvas.drawRect(selectorView.getRight() - RECTANGLE_THICKNESS,selectorView.getTop(),selectorView.getRight(),selectorView.getBottom(),paint);
        canvas.drawRect(selectorView.getRight() - 4* RECTANGLE_THICKNESS,selectorView.getTop(),selectorView.getRight(),selectorView.getTop() + RECTANGLE_THICKNESS,paint);
        canvas.drawRect(selectorView.getRight() - 4* RECTANGLE_THICKNESS,selectorView.getBottom() - RECTANGLE_THICKNESS,selectorView.getRight(),selectorView.getBottom(),paint);


    }
}
