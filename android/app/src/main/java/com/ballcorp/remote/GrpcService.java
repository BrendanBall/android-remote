package com.ballcorp.remote;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class GrpcService extends Service {

    private String mHost = "192.168.1.103";
    private int mPort =  50051;
    private ManagedChannel mChannel;
    private TouchpadGrpc.TouchpadStub stub;

    private final IBinder binder = new GrpcBinder();

    public class GrpcBinder extends Binder {
        GrpcService getService() {
            return GrpcService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("grpc", "Connecting Grpc service");
        try {
            mChannel = ManagedChannelBuilder.forAddress(mHost, mPort)
                    .usePlaintext(true)
                    .build();
            stub = TouchpadGrpc.newStub(mChannel);
        } catch(Exception e) {
            Log.e("grpc", "failed connecting grpc server", e);
        }
        Log.i("grpc", "grpc channel state: "+ mChannel.getState(true));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChannel == null) {
            return;
        }
        try {
            mChannel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void move(MoveEvent... events) {
        try {
            MoveRequest message = MoveRequest.newBuilder().addAllMoveEvents(Arrays.asList(events)).build();
            stub.move(message, new StreamObserver<MoveReply>() {
                @Override
                public void onNext(MoveReply value) { }

                @Override
                public void onError(Throwable t) {
                   Log.e("grpc", "move error", t);
                }

                @Override
                public void onCompleted() { }
           });
        } catch(Exception e) {
            Log.e("grpc", "failed making grpc call", e);
        }
    }
}
