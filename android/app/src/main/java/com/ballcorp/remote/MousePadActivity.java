package com.ballcorp.remote;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;

public class MousePadActivity extends Activity {

    private GrpcService grpcService;
    boolean grpcBound = false;

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, GrpcService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        setContentView(R.layout.activity_mouse_pad);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float x = event.getX(0);
                float y = event.getY(0);
                Log.i("touch", "onTouchEvent x: "+ x + " y: "+ y);
                grpcService.move(getMoveEvents(event));
        }
        return true;
    }

    private MoveEvent[] getMoveEvents(MotionEvent e) {
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

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            GrpcService.GrpcBinder binder = (GrpcService.GrpcBinder) service;
            grpcService = binder.getService();
            grpcBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            grpcBound = false;
        }
    };
}
