package co.edu.ue.mediturno.ui.citas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import co.edu.ue.mediturno.R;

public class CitasFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_placeholder, container, false);
        TextView titulo = vista.findViewById(R.id.tvPlaceholderTitulo);
        titulo.setText(R.string.nav_citas);
        return vista;
    }
}