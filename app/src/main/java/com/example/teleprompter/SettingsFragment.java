package com.example.teleprompter;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.skydoves.colorpickerpreference.ColorEnvelope;
import com.skydoves.colorpickerpreference.ColorListener;
import com.skydoves.colorpickerpreference.ColorPickerDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * The color picker library is used to pick text and background color
 * https://github.com/QuadFlask/colorpicker
 */


public class SettingsFragment extends Fragment {

    public static final String PREF_KEY_TEXT_SPEED = "scroll_speed";
    public static final String PREF_KEY_TEXT_SIZE = "text_size";
    public static final String PREF_KEY_TEXT_COLOR = "text_color";
    public static final String PREF_KEY_DIALOG_TEXT_COLOR = "dialog_pref_key";

    SharedPreferences.Editor mPreferenceEditor;

    @BindView(R.id.settings_speed_value)
    TextView mTextSpeedValue;

    @BindView(R.id.settings_btn_speed_increase)
    ImageButton mTextSpeedIncrease;

    @BindView(R.id.settings_btn_speed_decrease)
    ImageButton mTextSpeedDecrease;

    @BindView(R.id.settings_text_size_value)
    TextView mTextSizeValue;

    @BindView(R.id.settings_btn_text_size_increase)
    ImageButton mTextSizeIncrease;

    @BindView(R.id.settings_btn_text_size_decrease)
    ImageButton mTextSizeDecrease;

    @BindView(R.id.settings_text_color_value)
    TextView mTextColorValue;

    @BindView(R.id.settings_text_color_preview)
    View mTextColorPreview;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferenceEditor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);
        //Populate the views from preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //Scroll speed
        int scrollSpeed = preferences.getInt(PREF_KEY_TEXT_SPEED, 3);
        mTextSpeedValue.setText(String.valueOf(scrollSpeed));

        //Text size
        int textSize = preferences.getInt(PREF_KEY_TEXT_SIZE, 20);
        mTextSizeValue.setText(String.valueOf(textSize));

        //Text color
        String textColor = preferences.getString(PREF_KEY_TEXT_COLOR, getString(R.string.settings_text_color_default));
        mTextColorValue.setText(textColor);
        mTextColorPreview.setBackgroundColor(Color.parseColor(textColor));

        return view;
    }

    @OnClick(R.id.settings_btn_speed_increase)
    void increaseScrollSpeed() {
        int scrollSpeed = Integer.valueOf(mTextSpeedValue.getText().toString());
        if (scrollSpeed < 50) { //Max speed = 50
            scrollSpeed++;
            mTextSpeedValue.setText(String.valueOf(scrollSpeed));
            mPreferenceEditor.putInt(PREF_KEY_TEXT_SPEED, scrollSpeed);
            mPreferenceEditor.apply();
        } else {
            Toast.makeText(getActivity(), R.string.setting_text_speed_max_toast, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.settings_btn_speed_decrease)
    void decreaseScrollSpeed() {
        int scrollSpeed = Integer.valueOf(mTextSpeedValue.getText().toString());
        if (scrollSpeed > 3) { //Min speed = 3
            scrollSpeed--;
            mTextSpeedValue.setText(String.valueOf(scrollSpeed));
            mPreferenceEditor.putInt(PREF_KEY_TEXT_SPEED, scrollSpeed);
            mPreferenceEditor.apply();
        } else {
            Toast.makeText(getActivity(), R.string.settings_toast_min_speed, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.settings_btn_text_size_increase)
    void increaseTextSize() {
        int textSize = Integer.valueOf(mTextSizeValue.getText().toString());
        if (textSize < 50) { //Max sie = 50
            textSize++;
            mTextSizeValue.setText(String.valueOf(textSize));
            mPreferenceEditor.putInt(PREF_KEY_TEXT_SIZE, textSize);
            mPreferenceEditor.apply();
        } else {
            Toast.makeText(getActivity(), R.string.settings_toast_max_text_size, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.settings_btn_text_size_decrease)
    void decreaseTextSize() {
        int textSize = Integer.valueOf(mTextSizeValue.getText().toString());
        if (textSize > 12) { //Min text size = 12
            textSize--;
            mTextSizeValue.setText(String.valueOf(textSize));
            mPreferenceEditor.putInt(PREF_KEY_TEXT_SIZE, textSize);
            mPreferenceEditor.apply();
        } else {
            Toast.makeText(getActivity(), R.string.settings_toast_min_text_size, Toast.LENGTH_SHORT).show();
        }
    }


    @OnClick(R.id.settings_layout_text_color)
    public void chooseTextColor() {
        ColorPickerDialog.Builder builder = new ColorPickerDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        builder.setTitle(getString(R.string.settings_text_color_label));
        builder.setPreferenceName(PREF_KEY_DIALOG_TEXT_COLOR); //Save the color to the view to be restored later
        builder.setPositiveButton(getString(R.string.settings_color_picker_confirm_label), new ColorListener() {
            @Override
            public void onColorSelected(ColorEnvelope colorEnvelope) {
                mTextColorValue.setText(getString(R.string.settings_text_color_value, colorEnvelope.getColorHtml()));
                mTextColorPreview.setBackgroundColor(colorEnvelope.getColor());
                mPreferenceEditor.putString(PREF_KEY_TEXT_COLOR, getString(R.string.settings_text_color_value, colorEnvelope.getColorHtml())); //Save the color to preferences
                mPreferenceEditor.apply();
            }
        });
        builder.setNegativeButton(R.string.settings_color_picker_cancel_label, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();

    }

}
