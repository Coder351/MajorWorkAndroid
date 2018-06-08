package com.mrinaanksinha.majorworkandroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class FocusBox extends View
{
    private final int maskColor;
    private final Paint paint;
    private final ImageView selectorView;


    public FocusBox(Context context, ImageView _selectorView)
    {
        super(context);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectorView = _selectorView;
        maskColor = R.color.maskColor;

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



    }
}
