package com.example.trabajopractico1;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TP1-AIRPLANE-DYNAMIC";
    private boolean airplaneReceiverRegistered = false;

    private final ActivityResultLauncher<String> notifPerm =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (!granted) {
                            Toast.makeText(this,
                                    "Para ver el aviso cuando se active Modo Avión, habilitá notificaciones.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

    // Receiver dinámico que mantiene la demo en foreground
    private final BroadcastReceiver airplaneReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive() dinámico llamado");
            if (intent == null) return;
            if (!Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())) return;

            boolean activado = intent.getBooleanExtra("state", false);
            Log.d(TAG, "Extra 'state' = " + activado);

            Toast.makeText(context,
                    activado ? "Modo Avión ACTIVADO (Dinámico)" : "Modo Avión DESACTIVADO (Dinámico)",
                    Toast.LENGTH_SHORT).show();

            if (activado) {
                // En foreground podés abrir directo el marcador
                Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:2664553747"));
                try {
                    startActivity(dial);
                    Log.d(TAG, "startActivity(ACTION_DIAL) ejecutado (dinámico)");
                } catch (Exception e) {
                    Log.e(TAG, "Error al abrir marcador en dinámico", e);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Canal + permiso de notificaciones (para el receiver del Manifest)
        NotificationUtils.ensureChannel(this);
        if (Build.VERSION.SDK_INT >= 33) {
            notifPerm.launch(Manifest.permission.POST_NOTIFICATIONS);
        }

        // Registrar el receiver dinámico UNA SOLA VEZ
        registerAirplaneReceiverIfNeeded();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

    }

    private void registerAirplaneReceiverIfNeeded() {
        if (airplaneReceiverRegistered) return;
        IntentFilter filter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(airplaneReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(airplaneReceiver, filter);
        }
        airplaneReceiverRegistered = true;
        Log.d(TAG, "Receiver dinámico REGISTRADO (onCreate)");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (airplaneReceiverRegistered) {
            try {
                unregisterReceiver(airplaneReceiver);
                Log.d(TAG, "Receiver dinámico DESREGISTRADO (onDestroy)");
            } catch (Exception ignored) {}
            airplaneReceiverRegistered = false;
        }
    }

    // Solo para diagnóstico local
    @SuppressWarnings("unused")
    private void logReceiversVisibles() {
        try {
            android.content.pm.PackageManager pm = getPackageManager();
            Intent q = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            java.util.List<android.content.pm.ResolveInfo> ris = pm.queryBroadcastReceivers(q, 0);
            for (android.content.pm.ResolveInfo ri : ris) {
                android.util.Log.d("TP1-PM", "Receiver visible: "
                        + ri.activityInfo.packageName + "/" + ri.activityInfo.name);
            }
        } catch (Exception e) {
            android.util.Log.e("TP1-PM", "Error queryBroadcastReceivers", e);
        }
    }
}
