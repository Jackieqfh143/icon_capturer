package com.lmy.iconcapturer.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.elvishew.xlog.XLog;
import com.lmy.iconcapturer.R;
import com.lmy.iconcapturer.utils.ConfigUtil;
import com.lmy.iconcapturer.utils.ToastUtil;
import com.lmy.iconcapturer.utils.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SettingActivity extends AppCompatActivity{

    private final String[] sortTypes = new String[]{"时间倒序", "时间升序"};

    private final String[] captureTypes = new String[]{"智能模式","小图模式","大图模式","自定义模式"};

    private HashMap<String,String> sortTypeMap = new HashMap<String,String>();

    private HashMap<String,ConfigUtil.CaptureType> captureTypeHashMap = new HashMap<>();

    private  EditText imgQualityEditText;

    private  EditText frameRateEditText;

    private final String toolTips = "捕获模式说明: \n" +
            "智能模式：自动判断表情包大小，并调整捕获参数\n" +
            "小图模式：适合捕获评论区或聊天框内的常规大小表情包\n" +
            "大图模式：适用于查看大图时的表情包捕获\n"+
            "自定义模式：根据需要，调整参数，以应对特殊场景\n";


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.setting_toolBar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        makeSeekBar("MAX_IMAGE_HEIGHT_RATIO");
        makeSeekBar("MAX_IMAGE_WIDTH_RATIO");
        makeSeekBar("MIN_IMAGE_WIDTH_RATIO");
        makeSeekBar("MIN_IMAGE_WIDTH_RATIO");
        makeSeekBar("MAX_IMAGE_SCALE_RATIO");
        sortTypeMap.put("时间倒序", "save_time desc");
        sortTypeMap.put("时间升序", "save_time asc");

        captureTypeHashMap.put("智能模式", ConfigUtil.CaptureType.AUTO);
        captureTypeHashMap.put("小图模式", ConfigUtil.CaptureType.SMALL);
        captureTypeHashMap.put("大图模式", ConfigUtil.CaptureType.BIG);
        captureTypeHashMap.put("自定义模式", ConfigUtil.CaptureType.CUSTOM);

        makeSortSpinner(sortTypes);

        makeCaptureSpinner(captureTypes);

        ImageButton captureTypeHelpBtn = (ImageButton) findViewById(R.id.capture_type_help_btn);



        captureTypeHelpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtil.makeText(SettingActivity.this, toolTips, ToastUtil.LENGTH_LONG).show();
            }
        });

        frameRateEditText = (EditText) findViewById(R.id.gif_frame_rate_text);
        frameRateEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                XLog.d("edit text get focus: " + b);
                if (!b){
                    onFinishTextType(frameRateEditText, 8, 30, 24);
                }
            }
        });

        frameRateEditText.setText(String.valueOf(ConfigUtil.getGifFrameRate()));

        imgQualityEditText = (EditText) findViewById(R.id.image_quality_text);
        imgQualityEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                XLog.d("edit text get focus: " + b);
                if (!b){
                    onFinishTextType(imgQualityEditText, 10, 100, 80);
                }
            }
        });

        imgQualityEditText.setText(String.valueOf(ConfigUtil.getImageQuality()));

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.setting_layout);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Integer> seekBarIds = Arrays.asList(
                        R.id.img_max_height_seekBar,
                        R.id.img_max_width_seekBar,
                        R.id.img_scale_ratio_seekBar);
                if (view.getId() != R.id.gif_frame_rate_text){
                    frameRateEditText.clearFocus();
                }
                if (view.getId() != R.id.image_quality_text){
                    imgQualityEditText.clearFocus();
                }
            }
        });


        LinearLayout restoreLayout = (LinearLayout) findViewById(R.id.restore_btn_layout);
        restoreLayout.setOnTouchListener((view, motionEvent) -> {
            ImageButton imageButton = (ImageButton) restoreLayout.findViewById(R.id.reset_btn);
            switch(motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // PRESSED
                    imageButton.setImageDrawable(getDrawable(R.drawable.restore_filled));
                    break;
                case MotionEvent.ACTION_UP:
                    // RELEASED
                    imageButton.setImageDrawable(getDrawable(R.drawable.restore));
                    break;
            }
            return false;
        });

        restoreLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    ConfigUtil.restoreDefaultSetting();
                    Utils.showText(SettingActivity.this, "恢复默认设置成功!");
                }catch (Exception e){
                    XLog.e("恢复默认设置失败!", e);
                }
            }
        });

    }

    private void onFinishTextType(EditText editText, int min, int max, int default_value){
        int value = Integer.parseInt(editText.getText().toString());
        if (value < min || value > max) {
            Utils.showText(SettingActivity.this, String.format(Locale.CHINESE, "请输入%d~%d范围内的数值", min, max));
            editText.setText(String.valueOf(default_value));
        }
        else if (editText.equals(frameRateEditText) ){
            ConfigUtil.setGifFrameRate(value);
        }else if (editText.equals(imgQualityEditText)){
            ConfigUtil.setImageQuality(value);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                break;

            default:
                break;
        }
        return true;
    }

    private void makeSortSpinner(String[] data){
        ArrayAdapter arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,data);
        final Spinner sp1 = (Spinner)findViewById(R.id.sort_type_spinner);
        sp1.setAdapter(arrayAdapter);
        int pos = ConfigUtil.getSortType().equals("save_time desc") ? 0:1;
        XLog.d("sortType: " + ConfigUtil.getSortType());
        XLog.d("selected pos: " + pos);
        sp1.setSelection(pos);
        sp1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                String sortType = adapterView.getItemAtPosition(pos).toString();
                XLog.d("sortType: " + sortType);
                ConfigUtil.setSortType(sortTypeMap.get(sortType));
                sendBroadcast(new Intent("com.lmy.iconcapturer.REFRESH_ORDER"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void updateSeekbar(ConfigUtil.CaptureType captureType){
        SeekBar maxHeightSeekBar= (SeekBar) findViewById(R.id.img_max_height_seekBar);
        SeekBar maxWidthSeekBar= (SeekBar) findViewById(R.id.img_max_width_seekBar);
        SeekBar minHeightSeekBar= (SeekBar) findViewById(R.id.img_min_height_seekBar);
        SeekBar minWidthSeekBar= (SeekBar) findViewById(R.id.img_min_width_seekBar);
        SeekBar scaleSeekBar= (SeekBar) findViewById(R.id.img_scale_ratio_seekBar);

        TextView maxHeightTextView= findViewById(R.id.img_max_height_text);
        TextView maxWidthTextView= findViewById(R.id.img_max_width_text);
        TextView minHeightTextView= findViewById(R.id.img_min_height_text);
        TextView minWidthTextView= findViewById(R.id.img_min_width_text);
        TextView scaleTextView= findViewById(R.id.img_scale_ratio_text);

        int maxHeightProgress,maxWidthProgress,minHeightProgress,minWidthProgress,scaleProgress;

        if (captureType.equals(ConfigUtil.CaptureType.BIG)){
            maxHeightProgress = 80;
            maxWidthProgress = 100;
            minHeightProgress = 50;
            minWidthProgress = 50;
            scaleProgress = 30;
        }else if (captureType.equals(ConfigUtil.CaptureType.CUSTOM)){
            maxHeightProgress = (int) (100 * ConfigUtil.getMaxImageHeightRatio());
            maxWidthProgress = (int) (100 * ConfigUtil.getMaxImageWidthRatio());
            minHeightProgress = (int) (100 * ConfigUtil.getMinImageHeightRatio());
            minWidthProgress = (int) (100 * ConfigUtil.getMinImageWidthRatio());
            scaleProgress = (int) (100 * ConfigUtil.getMinImageScaleRatio());
        }else{
            maxHeightProgress = 50;
            maxWidthProgress = 50;
            minHeightProgress = 10;
            minWidthProgress = 10;
            scaleProgress = 50;
        }

        if (captureType.equals(ConfigUtil.CaptureType.CUSTOM)){
            maxHeightSeekBar.setEnabled(true);
            maxWidthSeekBar.setEnabled(true);
            minHeightSeekBar.setEnabled(true);
            minWidthSeekBar.setEnabled(true);
            scaleSeekBar.setEnabled(true);
        }else{
            maxHeightSeekBar.setEnabled(false);
            maxWidthSeekBar.setEnabled(false);
            minHeightSeekBar.setEnabled(false);
            minWidthSeekBar.setEnabled(false);
            scaleSeekBar.setEnabled(false);
        }
        maxHeightSeekBar.setProgress(maxHeightProgress);
        maxHeightTextView.setText(getSimpleFloat(maxHeightProgress));

        maxWidthSeekBar.setProgress(maxWidthProgress);
        maxWidthTextView.setText(getSimpleFloat(maxWidthProgress));

        minHeightSeekBar.setProgress(minHeightProgress);
        minHeightTextView.setText(getSimpleFloat(minHeightProgress));

        minWidthSeekBar.setProgress(minWidthProgress);
        minWidthTextView.setText(getSimpleFloat(minWidthProgress));

        scaleSeekBar.setProgress(scaleProgress);
        scaleTextView.setText(getSimpleFloat(scaleProgress));

        ConfigUtil.setMaxImageHeightRatio(Float.parseFloat(getSimpleFloat(maxHeightProgress)));
        ConfigUtil.setMaxImageWidthRatio(Float.parseFloat(getSimpleFloat(maxWidthProgress)));
        ConfigUtil.setMinImageHeightRatio(Float.parseFloat(getSimpleFloat(minHeightProgress)));
        ConfigUtil.setMinImageWidthRatio(Float.parseFloat(getSimpleFloat(minWidthProgress)));
        ConfigUtil.setMinImageScaleRatio(Float.parseFloat(getSimpleFloat(scaleProgress)));
    }

    private void makeCaptureSpinner(String[] data){
        ArrayAdapter arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,data);
        final Spinner sp1 = (Spinner)findViewById(R.id.capture_type_spinner);
        sp1.setAdapter(arrayAdapter);
        ConfigUtil.CaptureType captureType = ConfigUtil.getCaptureType();
        sp1.setSelection(captureType.ordinal());
        sp1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                String captureTypeStr = adapterView.getItemAtPosition(pos).toString();
                XLog.d("captureType: " + captureTypeStr);
                ConfigUtil.setCaptureType(captureTypeHashMap.get(captureTypeStr));
                updateSeekbar(Objects.requireNonNull(captureTypeHashMap.get(captureTypeStr)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void makeSeekBar(String type){
        TextView textView = null;
        SeekBar seekBar = null;
        int MAX_VALUE = 100;
        int progress = 50;
        switch (type){
            case "MAX_IMAGE_HEIGHT_RATIO":
                progress = (int) (ConfigUtil.getMaxImageHeightRatio() * MAX_VALUE);
                textView= findViewById(R.id.img_max_height_text);
                seekBar= findViewById(R.id.img_max_height_seekBar);
                break;
            case "MAX_IMAGE_WIDTH_RATIO":
                progress = (int) (ConfigUtil.getMaxImageWidthRatio() * MAX_VALUE);
                textView= findViewById(R.id.img_max_width_text);
                seekBar= findViewById(R.id.img_max_width_seekBar);
                break;

            case "MIN_IMAGE_HEIGHT_RATIO":
                progress = (int) (ConfigUtil.getMinImageHeightRatio() * MAX_VALUE);
                textView= findViewById(R.id.img_min_height_text);
                seekBar= findViewById(R.id.img_min_height_seekBar);
                break;

            case "MIN_IMAGE_WIDTH_RATIO":
                progress = (int) (ConfigUtil.getMinImageWidthRatio() * MAX_VALUE);
                textView= findViewById(R.id.img_min_width_text);
                seekBar= findViewById(R.id.img_min_width_seekBar);
                break;

            case "MAX_IMAGE_SCALE_RATIO":
                progress = (int) (ConfigUtil.getMinImageScaleRatio() * MAX_VALUE);
                textView= findViewById(R.id.img_scale_ratio_text);
                seekBar= findViewById(R.id.img_scale_ratio_seekBar);
                break;

            default:
                break;
        }
        seekBar.setMax(100);
        seekBar.setProgress(progress);
        textView.setText(getSimpleFloat(progress));
        final TextView finalTextView = textView;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //改变数值
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = Math.min(Math.max(10, progress), 110);
                finalTextView.setText(getSimpleFloat(progress));
            }

            //开始拖动
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            //停止拖动
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                progress = Math.min(Math.max(10, progress), 110);
                finalTextView.setText(getSimpleFloat(progress));
                switch (type){
                    case "MAX_IMAGE_HEIGHT_RATIO":
                        ConfigUtil.setMaxImageHeightRatio(Float.parseFloat(getSimpleFloat(progress)));
                        break;
                    case "MAX_IMAGE_WIDTH_RATIO":
                        ConfigUtil.setMaxImageWidthRatio(Float.parseFloat(getSimpleFloat(progress)));
                        break;
                    case "MIN_IMAGE_HEIGHT_RATIO":
                        ConfigUtil.setMinImageHeightRatio(Float.parseFloat(getSimpleFloat(progress)));
                        break;
                    case "MIN_IMAGE_WIDTH_RATIO":
                        ConfigUtil.setMinImageWidthRatio(Float.parseFloat(getSimpleFloat(progress)));
                        break;
                    case "MAX_IMAGE_SCALE_RATIO":
                        ConfigUtil.setMinImageScaleRatio(Float.parseFloat(getSimpleFloat(progress)));
                        break;

                    default:
                        break;
                }
            }
        });
    }

    private String getSimpleFloat(int progress){
        return String.format(Locale.ENGLISH, "%.1f", (float) progress / 100);
    }
}