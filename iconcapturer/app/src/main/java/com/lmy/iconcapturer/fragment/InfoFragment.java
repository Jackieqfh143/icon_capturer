package com.lmy.iconcapturer.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.lmy.iconcapturer.R;


public class InfoFragment extends DialogFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String APP_INFO_TAG = "appUpdateInfo";

    // TODO: Rename and change types of parameters
    private String APP_INFO;

    public InfoFragment() {
        // Required empty public constructor
    }

    public static InfoFragment newInstance(String appUpdateInfo) {
        InfoFragment fragment = new InfoFragment();
        Bundle args = new Bundle();
        args.putString(APP_INFO_TAG, appUpdateInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            APP_INFO = getArguments().getString(APP_INFO_TAG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);
        TextView textView = view.findViewById(R.id.app_update_log);
        textView.setText(APP_INFO);

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