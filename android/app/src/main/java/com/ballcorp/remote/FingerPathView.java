package com.ballcorp.remote;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class FingerPathView extends View {

    public FingerPathView(Context context, GrpcService grpcService) {
        super(context);
        this.grpcService = grpcService;
    }

    private GrpcService grpcService;
    private Point p = new Point();
    private Paint paint = new Paint();

    {
        paint.setColor(Color.RED);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float radius=40;
        canvas.drawCircle(p.x, p.y, radius, paint);
    }

    public void updatePath(float x, float y) {
        p.x = x;
        p.y = y;
    }

    public void redraw() {
        this.invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float x = event.getX(0);
                float y = event.getY(0);
                Log.i("touch", "onTouchEvent x: "+ x + " y: "+ y);
                grpcService.move(getMoveEvents(event));
                updatePath(x, y);
        }
        redraw();
        return true;
    }

    private MoveEvent[] getMoveEvents(MotionEvent e) {
        Log.i("fingerpath", "motionevent: "+ e);
        final int historySize = e.getHistorySize();
        final int pointerCount = e.getPointerCount();
        MoveEvent[] moveEvents = new MoveEvent[historySize*pointerCount+pointerCount];

        int i = 0;
        for (int h = 0; h < historySize; h++) {
            for (int p = 0; p < pointerCount; p++) {
                moveEvents[i] = MoveEvent.newBuilder()
                        .setPointerId(e.getPointerId(p))
                        .setPositionX((int) e.getHistoricalX(p, h))
                        .setPositionY((int) e.getHistoricalY(p, h))
                        .setPressure( (int) e.getHistoricalPressure(p, h))
                        .build();
                i++;
            }
        }
        for (int p = 0; p < pointerCount; p++) {
            moveEvents[i] = MoveEvent.newBuilder()
                    .setPointerId(e.getPointerId(p))
                    .setPositionX((int) e.getX(p))
                    .setPositionY((int) e.getY(p))
                    .setPressure( (int) e.getPressure(p))
                    .build();
            i++;
        }
        return moveEvents;
    }

    private class Point {
        float x, y;
    }
}
