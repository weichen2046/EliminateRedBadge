package com.demo.eliminateredbadge;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by chenwei on 4/24/15.
 */
public class CustomView extends View {
    private static final String TAG = "CustomView";

    private int mRadius = 80;
    private int mDynamicRadius;

    private Paint mPaint;
    private Point mOrigin;

    private int mDownX;
    private int mDownY;

    private int mDeltaX;
    private int mDeltaY;

    private int mMovingX;
    private int mMovingY;
    private int mEmulateX;
    private int mEmulateY;

    private boolean mDownInBadge;

    private static final int MAX_STICKY_DISTANCE = 400;
    private int mStartDistance;
    private int mCurrDistance;

    private Paint mPaintGreen = new Paint();
    private Paint mPaintYellow = new Paint();
    private Paint mPaintBlue = new Paint();

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

        mPaintBlue.setColor(Color.argb(0x99, 0x00, 0x00, 0xFF));

        mPaintGreen.setColor(Color.GREEN);
        mPaintGreen.setAntiAlias(true);
        mPaintGreen.setStyle(Paint.Style.STROKE);
        mPaintGreen.setStrokeWidth(1f);

        mPaintYellow.setColor(Color.YELLOW);
        mPaintYellow.setAntiAlias(true);
        mPaintYellow.setStyle(Paint.Style.STROKE);
        mPaintYellow.setStrokeWidth(1f);

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

    private float distance(int x1, int y1, int x2, int y2) {
        return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownX = mMovingX = (int) x;
                mDownY = mMovingY = (int) y;
                mDeltaX = mDownX - mOrigin.x;
                mDeltaY = mDownY - mOrigin.y;

                mEmulateX = mMovingX - mDeltaX;
                mEmulateY = mMovingY - mDeltaY;

                mStartDistance = (int) distance(mDownX, mDownY, mOrigin.x, mOrigin.y);
                mDownInBadge = mStartDistance < mRadius;
                if (mDownInBadge) {
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                mMovingX = (int) x;
                mMovingY = (int) y;
                if (mMovingX == mDownX && mMovingY == mDownY) {
                    break;
                }
                if (mDownInBadge) {
                    mEmulateX = mMovingX - mDeltaX;
                    mEmulateY = mMovingY - mDeltaY;
                    mCurrDistance = (int) distance(mMovingX, mMovingY, mOrigin.x, mOrigin.y);
                    int deltaDist = mCurrDistance - mStartDistance;
                    float stickyProgress = 1.0f * deltaDist / MAX_STICKY_DISTANCE;
                    mDynamicRadius = (int) ((1 - stickyProgress) * mRadius);
                    mDynamicRadius = Math.max(20, mDynamicRadius);
                    mDynamicRadius = Math.min(mRadius, mDynamicRadius);
                    calcContactPoints2(mOrigin.x, mOrigin.y, mDynamicRadius, mEmulateX, mEmulateY, mRadius);
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

        // debug draw origin point
        canvas.drawCircle(mOrigin.x, mOrigin.y, 1, mPaintGreen);
        canvas.drawLine(left, mOrigin.y, right, mOrigin.y, mPaintGreen);
        canvas.drawLine(mOrigin.x, top, mOrigin.x, bottom, mPaintGreen);
        canvas.drawLine(mOriginContactPoint1[0], mOriginContactPoint1[1], mOrigin.x, mOrigin.y, mPaintYellow);
        canvas.drawLine(mOriginContactPoint2[0], mOriginContactPoint2[1], mOrigin.x, mOrigin.y, mPaintYellow);
    }

    private void drawDraggingBadge(Canvas canvas) {
        if (!mDownInBadge) {
            return;
        }

        RectF rect = new RectF();

        int left = mEmulateX - mRadius;
        int top = mEmulateY - mRadius;
        int right = mEmulateX + mRadius;
        int bottom = mEmulateY + mRadius;
        rect.set(left, top, right, bottom);

        canvas.drawOval(rect, mPaintBlue);
        //canvas.drawCircle(emulateX, emulateY, mRadius, mPaintGreen);
        rect.inset(mRadius - mDynamicRadius, mRadius - mDynamicRadius);
        canvas.drawOval(rect, mPaintGreen);

        // debug
        canvas.drawCircle(mEmulateX, mEmulateY, 1, mPaintGreen);
        canvas.drawLine(left, mEmulateY, right, mEmulateY, mPaintGreen);
        canvas.drawLine(mEmulateX, top, mEmulateX, bottom, mPaintGreen);
        canvas.drawLine(mDraggingContactPoint1[0], mDraggingContactPoint1[1], mEmulateX, mEmulateY, mPaintYellow);
        canvas.drawLine(mDraggingContactPoint2[0], mDraggingContactPoint2[1], mEmulateX, mEmulateY, mPaintYellow);
        canvas.drawLine(mOrigin.x, mOrigin.y, mEmulateX, mEmulateY, mPaintYellow);
    }

    private int[] mDraggingContactPoint1 = new int[2];
    private int[] mDraggingContactPoint2 = new int[2];
    private int[] mOriginContactPoint1 = new int[2];
    private int[] mOriginContactPoint2 = new int[2];

    private void calcContactPoints2(int x1, int y1, int r1, int x2, int y2, int r2) {
        float dX = Math.abs(x2 - x1);
        float dY = Math.abs(y2 - y1);
        float dR = r2 - r1;
        float dist = distance(x1, y1, x2, y2);

        float sin_a = dY / dist;
        float cos_a = dX / dist;
        float sin_r = dR / dist;
        float cos_r = (float) (Math.sqrt(dist * dist - dR * dR) / dist);

        // A = sin(a)*cos(r) + cos(a)*sin(r)
        // B = cos(a)*cos(r) - sin(a)*sin(r)
        float A = sin_a * cos_r + cos_a * sin_r;
        float B = cos_a * cos_r - sin_a * sin_r;

        float A2_B2 = A * A + B * B;

        float dY1 = (float) Math.sqrt(((A * A - 1) * r1 * r1 / A2_B2) + Math.pow(B * r1 / A2_B2, 2)) + B * r1 / A2_B2;
        float dX1 = (r1 - B * dY1) / A;
        float dY2 = dY1 * r2 / r1;
        float dX2 = dX1 * r2 / r1;

        mOriginContactPoint1[0] = (int) (x1 - dX1);
        mOriginContactPoint1[1] = (int) (y1 - dY1);
        mDraggingContactPoint1[0] = (int) (x2 - dX2);
        mDraggingContactPoint1[1] = (int) (y2 - dY2);

        float h1 = (sin_a * cos_r - cos_a * sin_r) * dX;
        float h2 = dY - h1;
        dY2 = r2 * (r2 - r1) / h2;
        dX2 = (float) Math.sqrt(r2 * r2 - dY2 * dY2);

        dY1 = dY2 * r1 / r2;
        dX1 = dX2 * r1 / r2;

        mDraggingContactPoint2[0] = (int) (x2 + dX2);
        mDraggingContactPoint2[1] = (int) (y2 + dY2);
        mOriginContactPoint2[0] = (int) (x1 + dX1);
        mOriginContactPoint2[1] = (int) (y1 + dY1);
    }

    private Path mFillSticky = new Path();

    private void fillSticky(Canvas canvas) {
        mFillSticky.reset();
        if (mDownInBadge) {
            canvas.drawLine(mDraggingContactPoint1[0], mDraggingContactPoint1[1], mOriginContactPoint1[0], mOriginContactPoint1[1], mPaintGreen);
            canvas.drawLine(mDraggingContactPoint2[0], mDraggingContactPoint2[1], mOriginContactPoint2[0], mOriginContactPoint2[1], mPaintGreen);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawOriginalBadge(canvas);

        drawDraggingBadge(canvas);

        fillSticky(canvas);
    }
}
