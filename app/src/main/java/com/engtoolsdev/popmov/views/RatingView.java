package com.engtoolsdev.popmov.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.engtoolsdev.popmov.R;

/**
 * Created by Jose on 6/6/15.
 */
public class RatingView extends View {

    private static final double MAX_VOTE_AVERAGE = 10.0;


    Double averageVote = 0.0;

    private float screenDensity = getResources().getDisplayMetrics().density;

    private Paint textPaint = new Paint();


    private float interpolatedTime = 0.f;

    private final Rect textBounds = new Rect();

    private Paint arcPaint = new Paint();
    private RectF arcRect = new RectF();

    private final ArchAnimation archAnimation = new ArchAnimation();

    private int textColor;
    private float archWidth;
    private int arcColor;
    private float textSizeDp = 16;
    private boolean animate = true;

    private float textSizePixels = (float) (textSizeDp * screenDensity + 0.5f);


    public RatingView(Context context) {
        super(context);

        init();
    }

    public RatingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setAttrs(context, attrs, 0);

        init();
    }



    public RatingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setAttrs(context, attrs, defStyleAttr);

        init();
    }

    private void setAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.RatingView, defStyleAttr, 0);

        textColor = a.getInt(R.styleable.RatingView_textColor, getResources().getColor(android.R.color.white));
        archWidth = a.getDimension(R.styleable.RatingView_arcWidth, getResources().getDimension(R.dimen.default_arc_width));
        arcColor = a.getColor(R.styleable.RatingView_arcColor, getResources().getColor(R.color.primary));
        textSizePixels = a.getDimension(R.styleable.RatingView_textSize, (textSizeDp * screenDensity + 0.5f));
        animate = a.getBoolean(R.styleable.RatingView_animate, true);




        a.recycle();
    }

    private void init(){
        arcPaint.setColor(arcColor);
        arcPaint.setAntiAlias(true);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(archWidth);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint.setColor(textColor);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(textSizePixels);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        float height = canvas.getHeight();
        float width = canvas.getWidth();

        if(!animate){
            interpolatedTime = 1.0f;
        }

        float sweepAngle = (float) ( 360 * (averageVote / MAX_VOTE_AVERAGE)) * interpolatedTime;

        arcRect.set(arcPaint.getStrokeWidth(), arcPaint.getStrokeWidth(), width - arcPaint.getStrokeWidth(), height - arcPaint.getStrokeWidth());

        canvas.drawArc(arcRect, 0, sweepAngle, false, arcPaint);

        String valueString = String.valueOf(averageVote);

        textPaint.getTextBounds(valueString, 0, valueString.length(), textBounds);
        canvas.drawText(valueString, (width/2), (height/2) - textBounds.exactCenterY(), textPaint);

        if(animate) {
            if (getAnimation() == null && averageVote != null) {
                startAnimation(archAnimation);
            }
        }


    }

    public void setRating(double averageVote){
        this.averageVote = averageVote;

        invalidate();
    }

    public void setArcColor(int color){
        arcColor = color;
        init();
        invalidate();
    }

    private class ArchAnimation extends Animation{

        public ArchAnimation(){

            setDuration(2000);
            setInterpolator(new AccelerateDecelerateInterpolator());
        }

        @Override
        protected void applyTransformation(float time, Transformation t) {
            super.applyTransformation(time, t);



            if(time < 1.0f){
                interpolatedTime = time;
                invalidate();
            }
        }
    }

}
