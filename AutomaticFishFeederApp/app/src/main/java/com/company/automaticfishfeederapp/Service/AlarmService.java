package com.company.automaticfishfeederapp.Service;

import static com.company.automaticfishfeederapp.App.CHANNEL_ID;
import static com.company.automaticfishfeederapp.BroadcastReceiver.AlarmBroadcastReceiver.TITLE;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.company.automaticfishfeederapp.LoginSession;
import com.company.automaticfishfeederapp.R;
import com.company.automaticfishfeederapp.RingActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class AlarmService extends Service {
    private MediaPlayer mediaPlayer;
    //private Vibrator vibrator;
    private DatabaseReference databaseReference;
    private String deviceId;
    @Override
    public void onCreate() {
        super.onCreate();
        //Log.i("NumberGenerated", "Function has generated zero.");
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm);
        mediaPlayer.setLooping(true);

        //vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        LoginSession sessionManagement =new LoginSession(getApplicationContext());
        HashMap<String, String> user = sessionManagement.readLoginSession();
        deviceId = user.get(LoginSession.KEY_DEVICEID);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("FishFeeding");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, RingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        String alarmTitle = String.format("%s Alarm", intent.getStringExtra(TITLE));

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Fish Feeding")
                .setContentText("Fish Feeding Now Starting...")
                .setSmallIcon(R.drawable.notify_icon)
                .setContentIntent(pendingIntent)
                .build();

        mediaPlayer.start();

        //long[] pattern = { 0, 100, 1000 };
        //vibrator.vibrate(pattern, 0);

        startForeground(1, notification);
        feedingNoNow(deviceId);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mediaPlayer.stop();
        //vibrator.cancel();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void feedingNoNow(String deviceId)
    {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("deviceId", deviceId);
        hashMap.put("triggerValue", 1);

        databaseReference.child(deviceId).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Runnable runnable=new Runnable() {
                    @Override
                    public void run() {
                        feedingOffNow(deviceId);
                    }
                };

                Handler handler=new Handler(Looper.getMainLooper());
                handler.postDelayed(runnable,20000);
            }
        });
    }

    private void feedingOffNow(String deviceId)
    {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("deviceId", deviceId);
        hashMap.put("triggerValue", 0);

        databaseReference.child(deviceId).updateChildren(hashMap);
    }
}