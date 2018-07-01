package com.mrinaanksinha.majorworkandroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class FocusBox extends View
{
    private final int MASK_COLOR = Color.argb(88, 0, 0, 0);
    private final int RECTANGLE_COLOR = Color.argb(255, 255, 255, 255);
    private final float RECTANGLE_THICKNESS = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
    private Paint paint;
    private ImageView selectorView;
    public Boolean enabled = true;

    public FocusBox(Context context, ImageView _selectorView)
    {
        super(context);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectorView = _selectorView;

        this.setOnTouchListener(TouchListener);


    }

    private OnTouchListener TouchListener = new OnTouchListener()
    {
        int startX = -1;
        int startY = -1;
        int lastX = -1;
        int lastY = -1;

        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            Log.d(">>>>>>>>>>>", Integer.toString(event.getAction()));
//            vvvvvvvvvvvvvvvv Repeats too many times for a single offense
//            if (event.getPointerCount()>1)
//            {
//                Toast.makeText(getContext(),"Please use a single finger to resize",Toast.LENGTH_SHORT).show();
//            }
            if(!enabled)
            {
                return true;
            }
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    startX = (int) event.getX();
                    startY = (int) event.getY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    int currentX = (int) event.getX();
                    int currentY = (int) event.getY();
                    try
                    {
//                        final int BUFFER = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
//                        final int BUFFER = 100;
//                        updateFocusBox(currentX - startX, currentY - startY);
                        if (lastX >= 0)
                        {
                            // Top left corner
                            if ((/*currentX <= v.getWidth() / 2
                                    || */startX <= v.getWidth() / 2)
                                    && (/*currentY <= v.getHeight() / 2
                                    || */startY <= v.getHeight() / 2))
                            {
                                updateFocusBox(lastX - currentX,
                                        lastY - currentY);
                            }
                            //Top right corner
                            else if ((/*currentX >= v.getWidth() / 2
                                    || */startX >= v.getWidth() / 2)
                                    && (/*currentY <= v.getHeight() / 2
                                    || */startY <= v.getHeight() / 2))
                            {
                                updateFocusBox(currentX - lastX, lastY - currentY);
                            }
                            //Bottom left corner
                            else if ((/*currentX <= v.getWidth() / 2
                                    || */startX <= v.getWidth() / 2)
                                    && (/*currentY >= v.getHeight() / 2
                                    || */startY >= v.getHeight() / 2))
                            {
                                updateFocusBox(lastX - currentX,
                                        currentY - lastY);
                            }
                            //Bottom right corner
                            else if ((/*currentX >= v.getWidth() / 2
                                    ||*/ startX >= v.getWidth() / 2)
                                    && (/*currentY >= v.getHeight() / 2
                                    || */startY >= v.getHeight() / 2))
                            {
                                updateFocusBox(currentX - lastX,
                                        currentY - lastY);
                            }
//                            //Left side
//                            else if ((currentX <= selectorView.getLeft()
//                                    || startX <= selectorView.getLeft())
//                                    && ((currentY <= selectorView.getBottom()
//                                    && currentY >= selectorView.getTop())
//                                    || (startY <= selectorView.getBottom()
//                                    && startY >= selectorView.getTop())))
//                            {
//                                updateFocusBox(startX - currentX, 0);
//                            }
//                            //Right side
//                            else if ((currentX >= selectorView.getRight()
//                                    || startX >= selectorView.getRight())
//                                    && ((currentY <= selectorView.getBottom()
//                                    && currentY >= selectorView.getTop())
//                                    || (startY <= selectorView.getBottom()
//                                    && startY >= selectorView.getTop())))
//                            {
//                                updateFocusBox(currentX - startX, 0);
//                            }
//                            //Top side
//                            else if ((currentY <= selectorView.getTop()
//                                    || startY <= selectorView.getTop() )
//                                    && ((currentX <= selectorView.getRight()
//                                    && currentX >= selectorView.getLeft())
//                                    || (startX <= selectorView.getRight()
//                                    && startX >= selectorView.getLeft())))
//                            {
//
//                                updateFocusBox(0, startY - currentY);
//                            }
//                            //Bottom side
//                            else if ((currentY >= selectorView.getBottom()
//                                    || startY >= selectorView.getBottom())
//                                    && ((currentX <= selectorView.getRight()
//                                    && currentX >= selectorView.getLeft())
//                                    || (startX <= selectorView.getRight()
//                                    && startX >= selectorView.getLeft())))
//                            {
//                                updateFocusBox(0, currentY - startY);
//                            }


                        }
                    }
                    catch (NullPointerException e)
                    {
                        e.printStackTrace();
                    }
                    v.invalidate();
                    lastX = currentX;
                    lastY = currentY;
                    return true;
                case MotionEvent.ACTION_UP:
                    startX = -1;
                    startY = -1;
                    lastX = -1;
                    lastY=-1;
                    return true;
            }
            return false;

        }
    };


    private void updateFocusBox(int dW, int dH)
    {
        final int MIN_FOCUS_BOX_WIDTH = 300;
        final int MIN_FOCUS_BOX_HEIGHT = 300;

        int newLeft = selectorView.getLeft() - dW;
        int newRight = selectorView.getRight() + dW;
        int newTop = selectorView.getTop() - dH;
        int newBottom = selectorView.getBottom() + dH;

        if (newRight - newLeft < MIN_FOCUS_BOX_WIDTH
                || newBottom - newTop < MIN_FOCUS_BOX_HEIGHT
                || newLeft <=6
                || newRight >= this.getWidth() - 6
                || newTop <= 6
                || newBottom >= this.getHeight() - 6)
        {
            return;
        }


        selectorView.setLeft(newLeft);
        selectorView.setRight(newRight);
        selectorView.setTop(newTop);
        selectorView.setBottom(newBottom);


//
//        Size ScrRes = new Size(1920,1080);
//        int newWidth = (selectorView.getWidth() + dW > ScrRes.getWidth() - 4 || selectorView.getWidth() + dW < MIN_FOCUS_BOX_WIDTH)
//                ? 0
//                : selectorView.getWidth() + dW;
//
//        int newHeight = (selectorView.getHeight() + dH > ScrRes.getHeight() - 4 || selectorView.getHeight() + dH < MIN_FOCUS_BOX_HEIGHT)
//                ? 0
//                : selectorView.getHeight() + dH;
//
//        int leftOffset = (ScrRes.getWidth() - newWidth) / 2;
//
//        int topOffset = (ScrRes.getWidth() - newHeight) / 2;
//
//
//
//        selectorView.setLeft(leftOffset);
//        selectorView.setTop(topOffset);
//        selectorView.setRight(leftOffset+newWidth);
//        selectorView.setBottom(topOffset+newHeight);

    }

    public Rect getSelectorViewBox()
    {
        return ImageProcessingTools.getBox(selectorView);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        int screenWidth = canvas.getWidth();
        int screenHeight = canvas.getHeight();

        paint.setColor(MASK_COLOR);
        canvas.drawRect(0, 0, screenWidth, selectorView.getTop(), paint);
        canvas.drawRect(0, selectorView.getTop(), selectorView.getLeft(), selectorView.getBottom(), paint);
        canvas.drawRect(selectorView.getRight(), selectorView.getTop(), screenWidth, selectorView.getBottom(), paint);
        canvas.drawRect(0, selectorView.getBottom(), screenWidth, screenHeight, paint);

        paint.setColor(RECTANGLE_COLOR);
        canvas.drawRect(selectorView.getLeft(), selectorView.getTop(), selectorView.getLeft() + RECTANGLE_THICKNESS, selectorView.getBottom(), paint);
        canvas.drawRect(selectorView.getLeft() + RECTANGLE_THICKNESS, selectorView.getTop(), selectorView.getLeft() + 4 * RECTANGLE_THICKNESS, selectorView.getTop() + RECTANGLE_THICKNESS, paint);
        canvas.drawRect(selectorView.getLeft() + RECTANGLE_THICKNESS, selectorView.getBottom() - RECTANGLE_THICKNESS, selectorView.getLeft() + 4 * RECTANGLE_THICKNESS, selectorView.getBottom(), paint);

        canvas.drawRect(selectorView.getRight() - RECTANGLE_THICKNESS, selectorView.getTop(), selectorView.getRight(), selectorView.getBottom(), paint);
        canvas.drawRect(selectorView.getRight() - 4 * RECTANGLE_THICKNESS, selectorView.getTop(), selectorView.getRight(), selectorView.getTop() + RECTANGLE_THICKNESS, paint);
        canvas.drawRect(selectorView.getRight() - 4 * RECTANGLE_THICKNESS, selectorView.getBottom() - RECTANGLE_THICKNESS, selectorView.getRight(), selectorView.getBottom(), paint);



    }
}
