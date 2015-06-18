package dev.paddock.adp.mCubed.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.controls.NumberPicker;
import dev.paddock.adp.mCubed.controls.NumberPicker.OnValueChangedListener;
import dev.paddock.adp.mCubed.lists.BindingList;
import dev.paddock.adp.mCubed.lists.BindingListAdapter;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class DimensionPreference extends DialogPreference {
	private final BindingList<Dimension> dimensions = new BindingList<Dimension>();
	private NumberPicker numberPicker;
	private Spinner unitSpinner;
	private String dimensionsAttr;
	private char unit;
	private int value;
	
	private static class Dimension {
		private int minimum, maximium;
		private char unit;
		private String display;
		@Override
		public String toString() {
			return display;
		}
	}
	
	public DimensionPreference(Context context, AttributeSet attrs) {
		// Setup
		super(context, attrs);
		setDialogLayoutResource(R.layout.dimension_preference);
		
		// Read attributes
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DimensionPreference);
		dimensionsAttr = a.getString(R.styleable.DimensionPreference_dimensions);
		a.recycle();
		
		// Create the possible dimensions
		if (!Utilities.isNullOrEmpty(dimensionsAttr)) {
			String[] dimensions = dimensionsAttr.split(";");
			for (String dimension : dimensions) {
				String[] parts = dimension.split(",");
				if (parts.length == 4) {
					Dimension dim = new Dimension();
					dim.unit = parts[0].charAt(0);
					dim.display = parts[1];
					dim.minimum = Utilities.parseInt(parts[2]);
					dim.maximium = Utilities.parseInt(parts[3]);
					this.dimensions.add(dim);
				}
			}
		}
	}
	
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getString(index);
	}
	
	@Override
	protected void onBindDialogView(View view) {
		// Setup
		super.onBindDialogView(view);
		
		// Find the elements
		numberPicker = (NumberPicker)view.findViewById(R.id.dp_numberPicker);
		unitSpinner = (Spinner)view.findViewById(R.id.dp_spinner);
		
		// Set the value changed listener for the number picker
		numberPicker.setOnValueChangedListener(new OnValueChangedListener() {
			@Override
			public void onValueChanged(NumberPicker view, int value) {
				setValue(value);
			}
		});
		
		// Set the available items for the unit spinner
		BindingListAdapter<Dimension> unitAdapter = new BindingListAdapter<Dimension>(getContext(), dimensions);
		unitAdapter.registerWithSpinnerAdapterView(unitSpinner);
		
		// Register to the spinner selection changed
		unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Dimension dimension = (Dimension)parent.getItemAtPosition(position);
				if (numberPicker != null) {
					numberPicker.setMinimumValue(dimension.minimum);
					numberPicker.setMaximumValue(dimension.maximium);
				}
				setUnit(dimension.unit);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});
		
		// Set the initial value
		setPreferenceValue(getPersistedString(null));
	}
	
	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		super.onSetInitialValue(restorePersistedValue, defaultValue);
		String defValue = null;
		if (defaultValue != null && defaultValue instanceof String) {
			defValue = (String)defaultValue;
		}
		if (restorePersistedValue) {
			setPreferenceValue(getPersistedString(defValue));
		} else {
			setPreferenceValue(defValue);
		}
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
			String value = getPreferenceValue();
			if (shouldPersist()) {
				persistString(value);
			}
			callChangeListener(value);
		}
	}
	
	public String getPreferenceValue() {
		return Integer.toString(getValue()) + Character.toString(getUnit());
	}
	
	public void setPreferenceValue(String preferenceValue) {
		if (!Utilities.isNullOrEmpty(preferenceValue)) {
			setUnit(preferenceValue.charAt(preferenceValue.length() - 1));
			setValue(Utilities.parseInt(preferenceValue.substring(0, preferenceValue.length() - 1)));
		}
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		if (this.value != value) {
			this.value = value;
		}
		if (numberPicker != null) {
			numberPicker.setValue(this.value);
		}
	}
	
	public char getUnit() {
		return unit;
	}
	
	public void setUnit(char unit) {
		if (this.unit != unit) {
			this.unit = unit;
		}
		if (unitSpinner != null) {
			int sel = -1;
			for (int i = 0; i < dimensions.size(); i++) {
				Dimension dimension = dimensions.get(i);
				if (dimension.unit == unit) {
					sel = i;
					break;
				}
			}
			if (sel != -1) {
				unitSpinner.setSelection(sel);
			}
		}
	}
}