package co.edu.ue.mediturno;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import co.edu.ue.mediturno.api.ApiClient;
import co.edu.ue.mediturno.model.PuntoAtencion;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedClient;

    private final ActivityResultLauncher<String> permisoLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) activarUbicacion();
                else Toast.makeText(this, "Sin permiso de ubicación", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment fragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (fragment != null) fragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnInfoWindowClickListener(this::abrirNavegacion);

        cargarPuntosAtencion();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            activarUbicacion();
        } else {
            permisoLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void cargarPuntosAtencion() {
        ApiClient.getApiService().obtenerPuntosAtencion().enqueue(new Callback<List<PuntoAtencion>>() {
            @Override
            public void onResponse(Call<List<PuntoAtencion>> call, Response<List<PuntoAtencion>> response) {
                if (!response.isSuccessful() || response.body() == null) return;
                for (PuntoAtencion p : response.body()) {
                    LatLng pos = new LatLng(p.getLatitud(), p.getLongitud());
                    mMap.addMarker(new MarkerOptions()
                            .position(pos)
                            .title(p.getNombre())
                            .snippet(p.getDireccion()));
                }
            }

            @Override
            public void onFailure(Call<List<PuntoAtencion>> call, Throwable t) {
                Toast.makeText(MapsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void activarUbicacion() {
        mMap.setMyLocationEnabled(true);
        fusedClient.getLastLocation().addOnSuccessListener(loc -> {
            if (loc != null) {
                LatLng yo = new LatLng(loc.getLatitude(), loc.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(yo, 14f));
            }
        });
    }

    private void abrirNavegacion(Marker marker) {
        LatLng d = marker.getPosition();
        Uri uri = Uri.parse("google.navigation:q=" + d.latitude + "," + d.longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Google Maps no está instalado", Toast.LENGTH_SHORT).show();
        }
    }
}