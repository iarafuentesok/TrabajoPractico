package com.example.trabajopractico1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class AvisoModoAvion extends BroadcastReceiver {

    private static final String TAG = "TP1-AIRPLANE-MANIFEST";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive() llamado");
        if (intent == null) {
            Log.w(TAG, "Intent == null");
            return;
        }

        String action = intent.getAction();
        Log.d(TAG, "Action: " + action);
        if (!Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
            Log.w(TAG, "Acción ignorada: " + action);
            return;
        }

        boolean activado = intent.getBooleanExtra("state", false);
        Log.d(TAG, "Extra 'state' = " + activado);

        if (activado) {
            Toast.makeText(context, "Modo Avión ACTIVADO (Manifest)", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Preparando notificación + PendingIntent al marcador");

            Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:2664553747"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                context.startActivity(dial);
                Log.d(TAG, "Marcador abierto");
            } catch (Exception e) {
                Log.e(TAG, "No se pudo abrir el marcador", e);
            }
        } else {
            Toast.makeText(context, "Modo Avión DESACTIVADO (Manifest)", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Modo avión desactivado");
        }
    }
}

