package com.lmy.iconcapturer.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.elvishew.xlog.XLog;
import com.lmy.iconcapturer.R;
import com.lmy.iconcapturer.utils.ConfigUtil;

public class UpdateDialogFragment extends DialogFragment {
    public CheckBox dontShowAgainCheckbox;

    public static UpdateDialogFragment newInstance(String latestVersion, String title, String content, String updateLog, boolean showCheckbox) {
        UpdateDialogFragment f = new UpdateDialogFragment();

        Bundle args = new Bundle();
        args.putString("latestVersion", latestVersion);
        args.putString("title", title);
        args.putString("content", content);
        args.putString("updateLog", updateLog);
        args.putBoolean("showCheckbox", showCheckbox);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.update_dialog_fragment, container, false);
        LinearLayout cancelBtnLayout = (LinearLayout) view.findViewById(R.id.cancel_btn_layout);
        LinearLayout confirmBtnLayout = (LinearLayout) view.findViewById(R.id.confirm_btn_layout);
        ImageButton closeBtn = (ImageButton) view.findViewById(R.id.close_fragment_btn);
        TextView titleTextView = (TextView) view.findViewById(R.id.fragment_title);
        TextView contentTextView = (TextView) view.findViewById(R.id.fragment_message);
        TextView updateLogTextView = (TextView) view.findViewById(R.id.app_update_log);

        TextView confirmBtnText = confirmBtnLayout.findViewById(R.id.confirm_btn_text);
        TextView cancelBtnText = cancelBtnLayout.findViewById(R.id.cancel_btn_text);

        titleTextView.setText(getArguments().getString("title"));
        contentTextView.setText(getArguments().getString("content"));
        updateLogTextView.setText(getArguments().getString("updateLog"));

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                XLog.e("closeLayout is clicked ");
                dismiss();
            }
        });

        dontShowAgainCheckbox = (CheckBox) view.findViewById(R.id.skip_checkbox);
        dontShowAgainCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                XLog.d("dontShowAgainCheckbox.isChecked(): " + dontShowAgainCheckbox.isChecked());
                ConfigUtil.setUpdateDontShowAgain(dontShowAgainCheckbox.isChecked());
                ConfigUtil.setVersionCode(getArguments().getString("latestVersion"));
            }
        });

        if (!getArguments().getBoolean("showCheckbox")){
            dontShowAgainCheckbox.setVisibility(View.INVISIBLE);
        }

        confirmBtnText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onConfirm();
            }
        });

        cancelBtnText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCancel();
            }
        });


        confirmBtnLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmBtnLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       onConfirm();
                    }
                });

            }
        });

        cancelBtnLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancel();
            }
        });

        return view;
    }

    private void onConfirm(){
        XLog.e("clicked confirmBtn");
        Intent intent = new Intent("com.lmy.iconcapturer.CONFIRM_UPDATE");
        intent.putExtra("confirmUpdate", true);
        getActivity().sendOrderedBroadcast(intent, null);
    }

    private void onCancel(){
        Intent intent = new Intent("com.lmy.iconcapturer.CONFIRM_UPDATE");
        intent.putExtra("confirmUpdate", false);
        getActivity().sendOrderedBroadcast(intent, null);
    }
}