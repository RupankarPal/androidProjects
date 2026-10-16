package com.example.bottonnavigation_and_recyclerview_implement_homepage;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DrawingOverlayView extends View {

    public enum Mode { CURSOR, TREND_LINE, HORIZONTAL_LINE, BRUSH, PRICE_RANGE }

    private Mode currentMode = Mode.CURSOR;
    private Paint paint;
    private List<Drawing> drawings = new ArrayList<>();
    private Drawing activeDrawing;

    public DrawingOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(5f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
    }

    public void setMode(Mode mode) {
        this.currentMode = mode;
        activeDrawing = null;
    }

    public void clearAll() {
        drawings.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Drawing d : drawings) {
            d.draw(canvas, paint);
        }
        if (activeDrawing != null) {
            activeDrawing.draw(canvas, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (currentMode == Mode.CURSOR) return false;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (currentMode == Mode.BRUSH) {
                    activeDrawing = new BrushDrawing(x, y);
                } else if (currentMode == Mode.TREND_LINE) {
                    activeDrawing = new LineDrawing(x, y);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (activeDrawing != null) {
                    activeDrawing.update(x, y);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (activeDrawing != null) {
                    drawings.add(activeDrawing);
                    activeDrawing = null;
                    invalidate();
                }
                break;
        }
        return true;
    }

    abstract static class Drawing {
        abstract void draw(Canvas canvas, Paint paint);
        abstract void update(float x, float y);
    }

    static class LineDrawing extends Drawing {
        float x1, y1, x2, y2;
        LineDrawing(float x, float y) { x1 = x2 = x; y1 = y2 = y; }
        @Override void update(float x, float y) { x2 = x; y2 = y; }
        @Override void draw(Canvas canvas, Paint paint) { canvas.drawLine(x1, y1, x2, y2, paint); }
    }

    static class BrushDrawing extends Drawing {
        Path path = new Path();
        BrushDrawing(float x, float y) { path.moveTo(x, y); }
        @Override void update(float x, float y) { path.lineTo(x, y); }
        @Override void draw(Canvas canvas, Paint paint) { canvas.drawPath(path, paint); }
    }
}
