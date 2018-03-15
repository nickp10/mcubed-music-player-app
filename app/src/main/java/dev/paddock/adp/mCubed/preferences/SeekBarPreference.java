package dev.paddock.adp.mCubed.preferences;

import dev.paddock.adp.mCubed.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SeekBarPreference extends DialogPreference implements OnSeekBarChangeListener {
	private static final int DEFAULT_DEFAULT_VALUE = 50;
	private SeekBar seekBar;
	private TextView valueText;
	private CheckBox enableCheckbox;
	private OnCheckedChangeListener checkboxChangedListener;
	private boolean onGetDefaultValueCalled;
	private String units;
	private int value, maximum = 100, defaultValue;
	
	public SeekBarPreference(Context context, AttributeSet attrs) {
		// Setup
		super(context, attrs);
		
		// Fix the default value
		if (!onGetDefaultValueCalled) {
			defaultValue = DEFAULT_DEFAULT_VALUE;
		}
		
		// Read attributes
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VolumePreference);
		units = a.getString(R.styleable.VolumePreference_units);
		maximum = a.getInt(R.styleable.VolumePreference_maximum, getMaximumValue());
		a.recycle();
	}
	
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		onGetDefaultValueCalled = true;
		defaultValue = a.getInt(index, DEFAULT_DEFAULT_VALUE);
		return getDefaultValue();
	}
	
	@Override
	protected View onCreateDialogView() {
		// Create the layout
		LinearLayout layout = new LinearLayout(getContext());
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(5, 5, 5, 5);
		
		// Create an enable checkbox
		enableCheckbox = new CheckBox(getContext());
		enableCheckbox.setText("Enable");
		enableCheckbox.setOnCheckedChangeListener(getCheckboxChangedListener());
		layout.addView(enableCheckbox, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		// Create the seek bar
		seekBar = new SeekBar(getContext());
		seekBar.setMax(getMaximumValue());
		seekBar.setOnSeekBarChangeListener(this);
		layout.addView(seekBar, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		// Create the value display
		valueText = new TextView(getContext());
		valueText.setGravity(Gravity.CENTER_HORIZONTAL);
		valueText.setTextSize(16);
		layout.addView(valueText, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		// Set the initial value
		setValue(getPersistedInt(getDefaultValue()));
		return layout;
	}
	
	private void updateDisplay() {
		updateEnableCheckbox();
		updateSeekBar();
		updateTextDisplay();
	}
	
	private void updateEnableCheckbox() {
		if (enableCheckbox != null) {
			boolean isChecked = isValueSet();
			enableCheckbox.setChecked(isChecked);
			seekBar.setEnabled(isChecked);
			valueText.setEnabled(isChecked);
		}
	}
	
	private void updateSeekBar() {
		if (seekBar != null) {
			int value = getDisplayValue();
			seekBar.setProgress(value);
		}
	}
	
	private void updateTextDisplay() {
		if (valueText != null) {
			int value = getDisplayValue();
			String valueStr = Integer.toString(value);
			String units = getUnits();
			if (units != null) {
				valueStr = valueStr.concat(units);
			}
			valueText.setText(valueStr);
		}
	}
	
	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		super.onSetInitialValue(restorePersistedValue, defaultValue);
		int defValue = 0;
		if (defaultValue != null && defaultValue instanceof Integer) {
			defValue = (Integer)defaultValue;
		}
		if (restorePersistedValue) {
			setValue(getPersistedInt(defValue));
		} else {
			setValue(defValue);
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
		if (isValueSet()) {
			setValue(value);
		}
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
			int value = getValue();
			if (shouldPersist()) {
				persistInt(value);
			}
			callChangeListener(value);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) { }

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) { }
	
	public OnCheckedChangeListener getCheckboxChangedListener() {
		if (checkboxChangedListener == null) {
			checkboxChangedListener = new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						setValue(getDisplayValue());
					} else {
						setValue(-1);
					}
				}
			};
		}
		return checkboxChangedListener;
	}
	
	public String getUnits() {
		return units;
	}
	
	public int getMaximumValue() {
		return maximum;
	}
	
	public int getDefaultValue() {
		return defaultValue;
	}
	
	public boolean isValueSet() {
		return getValue() >= 0;
	}
	
	public int getDisplayValue() {
		if (isValueSet()) {
			return getValue();
		}
		int defaultValue = getDefaultValue();
		return defaultValue >= 0 ? defaultValue : DEFAULT_DEFAULT_VALUE;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		if (this.value != value) {
			this.value = value;
		}
		updateDisplay();
	}
}