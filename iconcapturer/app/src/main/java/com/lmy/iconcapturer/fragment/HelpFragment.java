package com.lmy.iconcapturer.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lmy.iconcapturer.R;
import com.lmy.iconcapturer.utils.HelpAdapter;

import java.util.ArrayList;

public class HelpFragment extends DialogFragment {

    public HelpFragment() {
        // Required empty public constructor
    }

    public static HelpFragment newInstance() {
        HelpFragment fragment = new HelpFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_help, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.help_recycle_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        HelpAdapter helpAdapter = new HelpAdapter(getContext(), new ArrayList<>());
        recyclerView.setAdapter(helpAdapter);

        ImageButton closeBtn = (ImageButton) view.findViewById(R.id.close_fragment_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return view;
    }
}