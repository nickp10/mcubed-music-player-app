package dev.paddock.adp.mCubed.preferences;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SeekBarPreference extends DialogPreference implements OnSeekBarChangeListener {
	private static final String androidns = "http://schema.android.com/apk/res";
	
	private SeekBar seekBar;
	private TextView valueText;
	private CheckBox enableCheckbox;
	private OnClickListener checkboxClickListener;
	private Context context;
	private String units;
	private int defaultValue, maxValue, value;
	
	public SeekBarPreference(Context context, AttributeSet attrs) {
		// Setup
		super(context, attrs);
		this.context = context;
		
		// Read attributes
		units = attrs.getAttributeValue(androidns, "units");
		defaultValue = attrs.getAttributeIntValue(androidns, "defaultValue", 50);
		maxValue = attrs.getAttributeIntValue(androidns, "maxValue", 100);
	}
	
	@Override
	protected View onCreateDialogView() {
		// Create the layout
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(5, 5, 5, 5);
		
		// Create an enable checkbox
		enableCheckbox = new CheckBox(context);
		enableCheckbox.setText("Enable");
		enableCheckbox.setOnClickListener(getCheckboxClickListener());
		layout.addView(enableCheckbox, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		// Create the seek bar
		seekBar = new SeekBar(context);
		seekBar.setMax(maxValue);
		seekBar.setOnSeekBarChangeListener(this);
		layout.addView(seekBar, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		// Create the value display
		valueText = new TextView(context);
		valueText.setGravity(Gravity.CENTER_HORIZONTAL);
		valueText.setTextSize(16);
		layout.addView(valueText, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		// Set the initial value
		setValue(getPersistedInt(defaultValue));
		updateEnabled(true);
		return layout;
	}
	
	private void updateEnabled(boolean doCheckValue) {
		// Check the value
		if (doCheckValue) {
			enableCheckbox.setChecked(value >= 0);
		}
		
		// Update the state of the elements
		boolean isChecked = enableCheckbox.isChecked();
		seekBar.setEnabled(isChecked);
		valueText.setEnabled(isChecked);
		
		// Update the value
		if (value < 0) {
			setValue(defaultValue);
		}
		if (!isChecked) {
			value = -1;
		}
	}
	
	private void updateTextDisplay() {
		String valueStr = String.valueOf(value);
		if (units != null) {
			valueStr = valueStr.concat(units);
		}
		valueText.setText(valueStr);
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
		setValue(value);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		super.onClick(dialog, which);
		if (which == DialogInterface.BUTTON_POSITIVE) {
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
	
	public OnClickListener getCheckboxClickListener() {
		if (checkboxClickListener == null) {
			checkboxClickListener = new OnClickListener() {
				@Override
				public void onClick(View v) {
					SeekBarPreference.this.updateEnabled(false);
				}
			};
		}
		return checkboxClickListener;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = value;
		if (seekBar != null) {
			seekBar.setProgress(value);
		}
		if (valueText != null) {
			updateTextDisplay();
		}
	}
}