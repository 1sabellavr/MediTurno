package co.edu.ue.mediturno.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import co.edu.ue.mediturno.model.PuntoAtencion;

import java.util.List;
import java.util.Locale;

public final class UbicacionHelper {

    // Tiempo máximo de espera del GPS en Android 10 o inferior.
    private static final long ESPERA_MAXIMA_MS = 15000;

    public interface Callback {
        void onUbicacion(Location ubicacion);

        void onError();
    }

    private UbicacionHelper() {
    }

    public static boolean tienePermiso(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    public static void obtenerUbicacion(Context context, Callback callback) {
        if (!tienePermiso(context)) {
            callback.onError();
            return;
        }

        LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            callback.onError();
            return;
        }

        String proveedor = elegirProveedor(locationManager);
        if (proveedor == null) {
            entregar(callback, ultimaConocida(locationManager));
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            locationManager.getCurrentLocation(proveedor, null,
                    ContextCompat.getMainExecutor(context), location -> {
                        if (location != null) {
                            callback.onUbicacion(location);
                        } else {
                            entregar(callback, ultimaConocida(locationManager));
                        }
                    });
        } else {
            pedirActualizacionUnica(locationManager, proveedor, callback);
        }
    }

    private static String elegirProveedor(LocationManager locationManager) {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER;
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return LocationManager.NETWORK_PROVIDER;
        }
        return null;
    }

    @SuppressLint("MissingPermission")
    private static Location ultimaConocida(LocationManager locationManager) {
        Location mejor = null;
        String[] proveedores = {LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER};

        for (String proveedor : proveedores) {
            try {
                if (locationManager.getAllProviders().contains(proveedor)) {
                    Location candidata = locationManager.getLastKnownLocation(proveedor);
                    if (candidata != null
                            && (mejor == null || candidata.getTime() > mejor.getTime())) {
                        mejor = candidata;
                    }
                }
            } catch (SecurityException | IllegalArgumentException e) {
                // Este proveedor no está disponible con los permisos actuales; se ignora.
            }
        }
        return mejor;
    }

    private static void entregar(Callback callback, Location ubicacion) {
        if (ubicacion != null) {
            callback.onUbicacion(ubicacion);
        } else {
            callback.onError();
        }
    }

    @SuppressLint("MissingPermission")
    @SuppressWarnings("deprecation")
    private static void pedirActualizacionUnica(LocationManager locationManager,
                                                String proveedor, Callback callback) {
        Handler handler = new Handler(Looper.getMainLooper());
        boolean[] terminado = {false};

        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if (terminado[0]) {
                    return;
                }
                terminado[0] = true;
                handler.removeCallbacksAndMessages(null);
                callback.onUbicacion(location);
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };

        try {
            locationManager.requestSingleUpdate(proveedor, listener, Looper.getMainLooper());
        } catch (SecurityException | IllegalArgumentException e) {
            entregar(callback, ultimaConocida(locationManager));
            return;
        }

        handler.postDelayed(() -> {
            if (terminado[0]) {
                return;
            }
            terminado[0] = true;
            locationManager.removeUpdates(listener);
            entregar(callback, ultimaConocida(locationManager));
        }, ESPERA_MAXIMA_MS);
    }

    public static float distanciaMetros(Location ubicacion, PuntoAtencion punto) {
        float[] resultado = new float[1];
        Location.distanceBetween(ubicacion.getLatitude(), ubicacion.getLongitude(),
                punto.getLatitud(), punto.getLongitud(), resultado);
        return resultado[0];
    }

    public static PuntoAtencion masCercano(Location ubicacion, List<PuntoAtencion> puntos) {
        PuntoAtencion cercano = null;
        float menor = Float.MAX_VALUE;

        for (PuntoAtencion punto : puntos) {
            float distancia = distanciaMetros(ubicacion, punto);
            if (distancia < menor) {
                menor = distancia;
                cercano = punto;
            }
        }
        return cercano;
    }

    public static String formatearDistancia(float metros) {
        if (metros < 1000) {
            return String.format(Locale.getDefault(), "%d m", Math.round(metros));
        }
        return String.format(Locale.getDefault(), "%.1f km", metros / 1000f);
    }
}