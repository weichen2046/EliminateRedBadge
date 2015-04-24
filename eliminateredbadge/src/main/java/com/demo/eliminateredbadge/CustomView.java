package com.demo.eliminateredbadge;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by chenwei on 4/24/15.
 */
public class CustomView extends View {
    private static final String TAG = "CustomView";

    private int mRadius = 100;
    private int mDynamicRadius;

    private Paint mPaint;
    private Point mOrigin;

    private int mDownX;
    private int mDownY;

    private int mDeltaX;
    private int mDeltaY;

    private int mCurrX;
    private int mCurrY;

    private boolean mDownInBadge;

    private static final int MAX_STICKY_DISTANCE = 400;
    private int mStartDistance;
    private int mCurrDistance;

    private Paint mDebugPaint = new Paint();

    public CustomView(Context context) {
        super(context);
        init();
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CustomView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setColor(Color.RED);
            mPaint.setAntiAlias(true);
        }
        if (mOrigin == null) {
            mOrigin = new Point(0, 0);
        }

        mDebugPaint.setColor(Color.GREEN);
        mDebugPaint.setAntiAlias(true);
        mDebugPaint.setStyle(Paint.Style.STROKE);
        mDebugPaint.setStrokeWidth(4f);

        mDynamicRadius = mRadius;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int w = getMeasuredWidth();
        int h = getMeasuredHeight();

        int l = (w - 2 * mRadius) / 2;
        int t = 100;

        mOrigin.x = l + mRadius;
        mOrigin.y = t + mRadius;
    }

    private int distance(int x1, int y1, int x2, int y2) {
        return (int) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownX = mCurrX = (int) x;
                mDownY = mCurrY = (int) y;
                mDeltaX = mDownX - mOrigin.x;
                mDeltaY = mDownY - mOrigin.y;
                mStartDistance = distance(mDownX, mDownY, mOrigin.x, mOrigin.y);
                mDownInBadge = mStartDistance < mRadius;
                if (mDownInBadge) {
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                mCurrX = (int) x;
                mCurrY = (int) y;
                if (mDownInBadge) {
                    mCurrDistance = distance(mCurrX, mCurrY, mOrigin.x, mOrigin.y);
                    int deltaDist = mCurrDistance - mStartDistance;
                    float stickyProgress = 1.0f * deltaDist / MAX_STICKY_DISTANCE;
                    mDynamicRadius = (int) ((1 - stickyProgress) * mRadius);
                    mDynamicRadius = Math.max(20, mDynamicRadius);
                    mDynamicRadius = Math.min(mRadius, mDynamicRadius);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // TODO: need an animation to reset these attribute
                mDownInBadge = false;
                mDynamicRadius = mRadius;
                invalidate();
                break;
        }
        return true;
    }

    private void drawOriginalBadge(Canvas canvas) {
        RectF rect = new RectF();

        int left = mOrigin.x - mDynamicRadius;
        int top = mOrigin.y - mDynamicRadius;
        int right = mOrigin.x + mDynamicRadius;
        int bottom = mOrigin.y + mDynamicRadius;
        rect.set(left, top, right, bottom);

        canvas.drawOval(rect, mPaint);
    }

    private void drawCurrentBadge(Canvas canvas) {
        if (!mDownInBadge) {
            return;
        }

        RectF rect = new RectF();

        int emulateX = mCurrX - mDeltaX;
        int emulateY = mCurrY - mDeltaY;

        int left = emulateX - mRadius;
        int top = emulateY - mRadius;
        int right = emulateX + mRadius;
        int bottom = emulateY + mRadius;
        rect.set(left, top, right, bottom);

        canvas.drawOval(rect, mPaint);
        //canvas.drawCircle(emulateX, emulateY, mRadius, mDebugPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawOriginalBadge(canvas);

        drawCurrentBadge(canvas);
    }
}
