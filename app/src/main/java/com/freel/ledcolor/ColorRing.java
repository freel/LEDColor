package com.freel.ledcolor;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * LED Color
 * Created by freel on 31.10.2015.
 */
public class ColorRing extends View{

    /**
     * Цвета градиента по кольцу
     */
    private static final int[] COLORS = new int[] { 0xFFFF0000, 0xFFFF00FF,
            0xFF0000FF, 0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000 };

    /**
     * Действия для вызова
     */
    private enum callbackAction {
        LED_ON,
        LED_OFF,
        SET_RGB,
        SET_A;
    };

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
     * Радиус ореола центрального круга
     */
    private int mColorCenterHaloRadius;
    private int mPreferredColorCenterHaloRadius;

    /**
     * Радиус указателя
     */
    private int mColorPointerRadius;

    /**
     * Радиус ореола указателя
     */
    private int mColorPointerHaloRadius;

    /**
     * Прямоугольник в который вписано цветовое кольцо
     */
    private RectF mColorWheelRectangle = new RectF();

    /**
     * Прямоугольник в который вписан центральный круг
     */
    private RectF mCenterRectangle = new RectF();

    /**
     * Позиция указателя(угол).
     */
    private float mAngle;

    /**
     * {@code Paint} отрисовка цветового кольца.
     */
    private Paint mColorWheelPaint;

    /**
     * {@code Paint} отрисовка ореола указателя.
     */
    private Paint mPointerHaloPaint;

    /**
     * {@code Paint} отрисовка указателя выбранным цветом.
     */
    private Paint mPointerColor;

    /**
     * {@code Paint} отрисовка центрального круга выбраным цветом
     */
    private Paint mCenterPaint;

    /**
     * {@code Paint} отрисовка ореола центрального круга
     */
    private Paint mCenterHaloPaint;

    /**
     * Выбранный цвет ARGB
     */
    public int mColor;

    /**
     * Состояние лампы
     * {@code true} лампа включена
     * {@code false} лампа выключена
     */
    public boolean mLedOn = false;

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
     * Интерфейс обратного вызова
     */
    public interface MyCallback{
        void callBackReturn();
    }

    MyCallback myCallback;

    void registerCallBack(MyCallback callback){
        this.myCallback = callback;
    }

    /**
     * строка url команды
     * см. initCallback(callbackAction action)
     */
    public String url;

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
        canvas.drawArc(mCenterRectangle, 0, 360, true, mCenterPaint);
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

        mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterPaint.setColor(calculateColor(mAngle));
        mCenterPaint.setStyle(Paint.Style.FILL);
        mCenterHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterHaloPaint.setColor(Color.BLACK);
        mCenterHaloPaint.setAlpha(0x00);

        mColor = calculateColor(mAngle);
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

        /**
         * Color calculated in calculateColor func.
         */
        int calculatedColor;

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
                    calculatedColor = calculateColor(mAngle);
                    setColor(calculatedColor);
                    //Проверка включена ли лампа
                    if (mLedOn){
                        mLedOn = false;
                        initCallback(callbackAction.LED_OFF);
                    }
                    else{
                        mLedOn = true;
                        initCallback(callbackAction.LED_ON);
                    }
                    invalidate();
                }
                // Check whether the user pressed anywhere on the wheel.
                else if (Math.sqrt(x*x + y*y)  <= mColorWheelRadius + mColorPointerHaloRadius
                        && Math.sqrt(x*x + y*y) >= mColorWheelRadius - mColorPointerHaloRadius) {
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

                    setNewCenterColor(mColor = calculatedColor);
                    initCallback(callbackAction.SET_RGB);
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
                invalidate();
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

    /**
     * Change the color of the center which indicates the new color.
     *
     * @param color int of the color.
     */
    public void setNewCenterColor(int color) {
        mColor = color;
        mCenterPaint.setColor(color);
        invalidate();
    }

    private void initCallback(callbackAction action){
        switch (action){
            case LED_OFF:
                url = "LED=OFF";
                break;
            case LED_ON:
                /**
                 * Необходимо восстанавливать последний цвет
                 */
                url = Color.red(mColor) + "," + Color.green(mColor) + "," + Color.blue(mColor);
                break;
            case SET_RGB:
                if (mLedOn) {
                    url = Color.red(mColor) + "," + Color.green(mColor) + "," + Color.blue(mColor);
                }
                break;
            case SET_A:
                url = "A=" + Color.alpha(mColor);
                break;
        }


        myCallback.callBackReturn();
    }


}