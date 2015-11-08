package com.freel.ledcolor;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.freel.ledcolor.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by freel on 31.10.2015.
 */
public class ColorRing extends View{

    /**
     * Цвета градиента по кольцу
     */
    private static final int[] COLORS = new int[] { 0xFFFF0000, 0xFFFF00FF,
            0xFF0000FF, 0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000 };

    /**
     * Толщина цветового кольца
     */
    private int mColorWheelThickness;

    /**
     * Радиус цветового кольца
     */
    private int mColorWheelRadius;
    private int mPreferredColorWheelRadius;

    /**
     * Радиус центрального круга
     */
    private int mColorCenterRadius;
    private int mPreferredColorCenterRadius;

    /**
     * Радиус
     */
    private int mColorCenterHaloRadius;
    private int mPreferredColorCenterHaloRadius;

    /**
     * The radius of the pointer.
     */
    private int mColorPointerRadius;

    /**
     * The radius of the halo of the pointer.
     */
    private int mColorPointerHaloRadius;

    /**
     * The rectangle enclosing the color wheel.
     */
    private RectF mColorWheelRectangle = new RectF();

    /**
     * The rectangle enclosing the center inside the color wheel.
     */
    private RectF mCenterRectangle = new RectF();

    /**
     * The pointer's position expressed as angle (in rad).
     */
    private float mAngle;

    /**
     * {@code Paint} instance used to draw the color wheel.
     */
    private Paint mColorWheelPaint;

    /**
     * {@code Paint} instance used to draw the pointer's "halo".
     */
    private Paint mPointerHaloPaint;

    /**
     * {@code Paint} instance used to draw the pointer (the selected color).
     */
    private Paint mPointerColor;

    /**
     * {@code Paint} instance used to draw the center with the new selected
     * color.
     */
    private Paint mCenterNewPaint;

    /**
     * {@code Paint} instance used to draw the center with the old selected
     * color.
     */
    private Paint mCenterOldPaint;

    /**
     * {@code Paint} instance used to draw the halo of the center selected
     * colors.
     */
    private Paint mCenterHaloPaint;

    /**
     * The ARGB value of the center with the new selected color.
     */
    public int mCenterNewColor;

    /**
     * The ARGB value of the center with the old selected color.
     */
    private int mCenterOldColor;

    /**
     * Whether to show the old color in the center or not.
     */
    private boolean mShowCenterOldColor;

    /**
     * The ARGB value of the currently selected color.
     */
    private int mColor;

    /**
     * Number of pixels the origin of this view is moved in X- and Y-direction.
     *
     * <p>
     * We use the center of this (quadratic) View as origin of our internal
     * coordinate system. Android uses the upper left corner as origin for the
     * View-specific coordinate system. So this is the value we use to translate
     * from one coordinate system to the other.
     * </p>
     *
     * <p>
     * Note: (Re)calculated in {@link #onMeasure(int, int)}.
     * </p>
     *
     * @see #onDraw(android.graphics.Canvas)
     */
    private float mTranslationOffset;

    /**
     * Distance between pointer and user touch in X-direction.
     */
    private float mSlopX;

    /**
     * Distance between pointer and user touch in Y-direction.
     */
    private float mSlopY;

    /**
     * {@code true} if the user clicked on the pointer to start the move mode. <br>
     * {@code false} once the user stops touching the screen.
     *
     * @see #onTouchEvent(android.view.MotionEvent)
     */
    private boolean mUserIsMovingPointer = false;

    /**
     * {@code TouchAnywhereOnColorWheelEnabled} instance used to control <br>
     * if the color wheel accepts input anywhere on the wheel or just <br>
     * on the halo.
     */
    private boolean mTouchAnywhereOnColorWheelEnabled = true;

    /**
     * {@code onColorSelectedListener} instance of the onColorSelectedListener
     */
    private OnColorSelectedListener onColorSelectedListener;

    /**
     * Color of the latest entry of the onColorSelectedListener.
     */
    private int oldSelectedListenerColor;

    /**
     * {@code onColorChangedListener} instance of the onColorChangedListener
     */
    private OnColorChangedListener onColorChangedListener;

    /**
     * Color of the latest entry of the onColorChangedListener.
     */
    private int oldChangedListenerColor;

    /**
     * Color calculated in calculateColor func.
     */
    private int calculatedColor;

    private TextView textView;

    /**
     * An interface that is called whenever a new color has been selected.
     * Currently it is always called when the color wheel has been released.
     *
     */
    public interface OnColorSelectedListener {
        public void onColorSelected(int color);
    }

    /**
     * An interface that is called whenever the color is changed. Currently it
     * is always called when the color is changes.
     *
     * @author lars
     *
     */
    public interface OnColorChangedListener {
        public void onColorChanged(int color);
    }

    public interface MyCallback{
        void callBackReturn();
    }

    MyCallback myCallback;

    void registerCallBack(MyCallback callback){
        this.myCallback = callback;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // All of our positions are using our internal coordinate system.
        // Instead of translating
        // them we let Canvas do the work for us.
        canvas.translate(mTranslationOffset, mTranslationOffset);

        // Draw the color wheel.
        canvas.drawOval(mColorWheelRectangle, mColorWheelPaint);

        float[] pointerPosition = calculatePointerPosition(mAngle);

        // Draw the pointer's "halo"
        canvas.drawCircle(pointerPosition[0], pointerPosition[1],
                mColorPointerHaloRadius, mPointerHaloPaint);

        // Draw the pointer (the currently selected color) slightly smaller on
        // top.
        canvas.drawCircle(pointerPosition[0], pointerPosition[1],
                mColorPointerRadius, mPointerColor);

        // Draw the halo of the center colors.
        canvas.drawCircle(0, 0, mColorCenterHaloRadius, mCenterHaloPaint);

        // Draw the new selected color in the center.
        canvas.drawArc(mCenterRectangle, 0, 360, true, mCenterNewPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int intrinsicSize = 2 * (mPreferredColorWheelRadius + mColorPointerHaloRadius);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(intrinsicSize, widthSize);
        } else {
            width = intrinsicSize;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(intrinsicSize, heightSize);
        } else {
            height = intrinsicSize;
        }

        int min = Math.min(width, height);
        setMeasuredDimension(min, min);
        mTranslationOffset = min * 0.5f;

        // fill the rectangle instances.
        mColorWheelRadius = min / 2 - mColorWheelThickness - mColorPointerHaloRadius;
        mColorWheelRectangle.set(-mColorWheelRadius, -mColorWheelRadius,
                mColorWheelRadius, mColorWheelRadius);

        mColorCenterRadius = (int) ((float) mPreferredColorCenterRadius * ((float) mColorWheelRadius / (float) mPreferredColorWheelRadius));
        mColorCenterHaloRadius = (int) ((float) mPreferredColorCenterHaloRadius * ((float) mColorWheelRadius / (float) mPreferredColorWheelRadius));
        mCenterRectangle.set(-mColorCenterRadius, -mColorCenterRadius,
                mColorCenterRadius, mColorCenterRadius);
    }

    public ColorRing(Context context) {
        super(context);
        init(null, 0);
    }

    public ColorRing(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ColorRing(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        final TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.ColorRing, defStyle, 0);
        final Resources b = getContext().getResources();

        mColorWheelThickness = a.getDimensionPixelSize(
                R.styleable.ColorRing_color_wheel_thickness,
                b.getDimensionPixelSize(R.dimen.color_wheel_thickness));
        mColorWheelRadius = a.getDimensionPixelSize(
                R.styleable.ColorRing_color_wheel_radius,
                b.getDimensionPixelSize(R.dimen.color_wheel_radius));
        mPreferredColorWheelRadius = mColorWheelRadius;
        mColorCenterRadius = a.getDimensionPixelSize(
                R.styleable.ColorRing_color_center_radius,
                b.getDimensionPixelSize(R.dimen.color_center_radius));
        mPreferredColorCenterRadius = mColorCenterRadius;
        mColorCenterHaloRadius = a.getDimensionPixelSize(
                R.styleable.ColorRing_color_center_halo_radius,
                b.getDimensionPixelSize(R.dimen.color_center_halo_radius));
        mPreferredColorCenterHaloRadius = mColorCenterHaloRadius;
        mColorPointerRadius = a.getDimensionPixelSize(
                R.styleable.ColorRing_color_pointer_radius,
                b.getDimensionPixelSize(R.dimen.color_pointer_radius));
        mColorPointerHaloRadius = a.getDimensionPixelSize(
                R.styleable.ColorRing_color_pointer_halo_radius,
                b.getDimensionPixelSize(R.dimen.color_pointer_halo_radius));

        a.recycle();

        mAngle = (float) (-Math.PI / 2);

        Shader s = new SweepGradient(0, 0, COLORS, null);

        mColorWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mColorWheelPaint.setShader(s);
        mColorWheelPaint.setStyle(Paint.Style.STROKE);
        mColorWheelPaint.setStrokeWidth(mColorWheelThickness);

        mPointerHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointerHaloPaint.setColor(Color.BLACK);
        mPointerHaloPaint.setAlpha(0x50);

        mPointerColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointerColor.setColor(calculateColor(mAngle));

        mCenterNewPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterNewPaint.setColor(calculateColor(mAngle));
        mCenterNewPaint.setStyle(Paint.Style.FILL);

        mCenterOldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterOldPaint.setColor(calculateColor(mAngle));
        mCenterOldPaint.setStyle(Paint.Style.FILL);

        mCenterHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterHaloPaint.setColor(Color.BLACK);
        mCenterHaloPaint.setAlpha(0x00);

        mCenterNewColor = calculateColor(mAngle);
        mCenterOldColor = calculateColor(mAngle);
    }

    private int calculateColor(float angle) {
        float unit = (float) (angle / (2 * Math.PI));
        if (unit < 0) {
            unit += 1;
        }

        if (unit <= 0) {
            mColor = COLORS[0];
            return COLORS[0];
        }
        if (unit >= 1) {
            mColor = COLORS[COLORS.length - 1];
            return COLORS[COLORS.length - 1];
        }

        float p = unit * (COLORS.length - 1);
        int i = (int) p;
        p -= i;

        int c0 = COLORS[i];
        int c1 = COLORS[i + 1];
        int a = ave(Color.alpha(c0), Color.alpha(c1), p);
        int r = ave(Color.red(c0), Color.red(c1), p);
        int g = ave(Color.green(c0), Color.green(c1), p);
        int b = ave(Color.blue(c0), Color.blue(c1), p);

        return Color.argb(a, r, g, b);
    }

    private int ave(int s, int d, float p) {
        return s + Math.round(p * (d - s));
    }


    /**
     * Calculate the pointer's coordinates on the color wheel using the supplied
     * angle.
     *
     * @param angle The position of the pointer expressed as angle (in rad).
     *
     * @return The coordinates of the pointer's center in our internal
     *         coordinate system.
     */
    private float[] calculatePointerPosition(float angle) {
        float x = (float) (mColorWheelRadius * Math.cos(angle));
        float y = (float) (mColorWheelRadius * Math.sin(angle));

        return new float[] { x, y };
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);

        // Convert coordinates to our internal coordinate system
        float x = event.getX() - mTranslationOffset;
        float y = event.getY() - mTranslationOffset;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Check whether the user pressed on the pointer.
                float[] pointerPosition = calculatePointerPosition(mAngle);
                if (x >= (pointerPosition[0] - mColorPointerHaloRadius)
                        && x <= (pointerPosition[0] + mColorPointerHaloRadius)
                        && y >= (pointerPosition[1] - mColorPointerHaloRadius)
                        && y <= (pointerPosition[1] + mColorPointerHaloRadius)) {
                    mSlopX = x - pointerPosition[0];
                    mSlopY = y - pointerPosition[1];
                    mUserIsMovingPointer = true;
                    invalidate();
                }
                // Check whether the user pressed on the center.
                else if (x >= -mColorCenterRadius && x <= mColorCenterRadius
                        && y >= -mColorCenterRadius && y <= mColorCenterRadius) {
                    mCenterHaloPaint.setAlpha(0x50);
                    mAngle = colorToAngle(getOldCenterColor());
                    calculatedColor = calculateColor(mAngle);
                    setColor(calculatedColor);
                    myCallback.callBackReturn();
                    invalidate();
                }
                // Check whether the user pressed anywhere on the wheel.
                else if (Math.sqrt(x*x + y*y)  <= mColorWheelRadius + mColorPointerHaloRadius
                        && Math.sqrt(x*x + y*y) >= mColorWheelRadius - mColorPointerHaloRadius
                        && mTouchAnywhereOnColorWheelEnabled) {
                    mUserIsMovingPointer = true;
                    invalidate();
                }
                // If user did not press pointer or center, report event not handled
                else{
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mUserIsMovingPointer) {
                    mAngle = (float) Math.atan2(y - mSlopY, x - mSlopX);
                    calculatedColor = calculateColor(mAngle);
                    mPointerColor.setColor(calculatedColor);

                    setNewCenterColor(mCenterNewColor = calculatedColor);
                    myCallback.callBackReturn();
                    invalidate();
                }
                // If user did not press pointer or center, report event not handled
                else{
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                mUserIsMovingPointer = false;
                mCenterHaloPaint.setAlpha(0x00);

                if (onColorSelectedListener != null && mCenterNewColor != oldSelectedListenerColor) {
                    onColorSelectedListener.onColorSelected(mCenterNewColor);
                    oldSelectedListenerColor = mCenterNewColor;
                }

                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                if (onColorSelectedListener != null && mCenterNewColor != oldSelectedListenerColor) {
                    onColorSelectedListener.onColorSelected(mCenterNewColor);
                    oldSelectedListenerColor = mCenterNewColor;
                }
                break;
        }
        return true;
    }

    /**
     * Set the color to be highlighted by the pointer. If the
     * instances {@code SVBar} and the {@code OpacityBar} aren't null the color
     * will also be set to them
     *
     * @param color The RGB value of the color to highlight. If this is not a
     *            color displayed on the color wheel a very simple algorithm is
     *            used to map it to the color wheel. The resulting color often
     *            won't look close to the original color. This is especially
     *            true for shades of grey. You have been warned!
     */
    public void setColor(int color) {
        mPointerColor.setColor(color);
    }

    public static String sendColor(String colorUrl) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(colorUrl);

        try {
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity httpEntity =response.getEntity();
            String line = EntityUtils.toString(httpEntity, "UTF-8");
        }
        catch (IOException e) {
            return "i";
        }
        return "n";
    }

    public int getOldCenterColor() {
        return mCenterOldColor;
    }

    /**
     * Change the color of the center which indicates the new color.
     *
     * @param color int of the color.
     */
    public void setNewCenterColor(int color) {
        mCenterNewColor = color;
        mCenterNewPaint.setColor(color);
        if (mCenterOldColor == 0) {
            mCenterOldColor = color;
            mCenterOldPaint.setColor(color);
        }
        if (onColorChangedListener != null && color != oldChangedListenerColor ) {
            onColorChangedListener.onColorChanged(color);
            oldChangedListenerColor  = color;
        }
        invalidate();
    }

    /**
     * Convert a color to an angle.
     *
     * @param color The RGB value of the color to "find" on the color wheel.
     *
     * @return The angle (in rad) the "normalized" color is displayed on the
     *         color wheel.
     */
    private float colorToAngle(int color) {
        float[] colors = new float[3];
        Color.colorToHSV(color, colors);

        return (float) Math.toRadians(-colors[0]);
    }



}