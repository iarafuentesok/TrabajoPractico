package com.example.trabajopractico1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class NotificationUtils {
    public static final String CHANNEL_ID = "airplane_events";

    public static void ensureChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Eventos del sistema", NotificationManager.IMPORTANCE_HIGH
            );
            ch.setDescription("Notifica cambios de Modo Avión");
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }


    public static boolean canPostNotifications(Context ctx) {
        if (Build.VERSION.SDK_INT < 33) {
            return NotificationManagerCompat.from(ctx).areNotificationsEnabled();
        }
        boolean granted = ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        return granted && NotificationManagerCompat.from(ctx).areNotificationsEnabled();
    }

    @SuppressWarnings("unused")
    public static void openNotificationSettings(Context ctx) {
        Intent i;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            i = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, ctx.getPackageName());
        } else {

            i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.fromParts("package", ctx.getPackageName(), null));
        }
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            ctx.startActivity(i);
        } catch (Exception e) {
            Toast.makeText(ctx, "No pude abrir Ajustes.", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("MissingPermission")
    public static void notifyOpenDialer(Context ctx, Intent dialIntent, int id) {
        if (canPostNotifications(ctx)) {
            PendingIntent pending = PendingIntent.getActivity(
                    ctx, 0, dialIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Modo avión activado")
                    .setContentText("Tocá para abrir el marcador con 2664553747")
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pending);

            try {
                NotificationManagerCompat.from(ctx).notify(id, b.build());
            } catch (SecurityException e) {
                Toast.makeText(ctx,
                        "Sin permiso para notificar. Revisá Ajustes de la app.",
                        Toast.LENGTH_LONG).show();
            }
        } else {

            Toast.makeText(ctx,
                    "Notificaciones deshabilitadas. Activalas para ver el aviso.",
                    Toast.LENGTH_LONG).show();
        }
    }
}