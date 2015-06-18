package dev.paddock.adp.mCubed.controls;

import dev.paddock.adp.mCubed.utilities.Utilities;
import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * A simple layout group that provides a numeric text area with two buttons to
 * increment or decrement the value in the text area. Holding either button
 * will auto increment the value up or down appropriately. 
 */
public class NumberPicker extends LinearLayout {
	private static final long REPEAT_DELAY = 50;
	private static final int ELEMENT_HEIGHT = LayoutParams.WRAP_CONTENT;
	private static final int ELEMENT_WIDTH = 150;
	private final Handler repeatUpdateHandler = Utilities.getHandler();
	private final ValueUpdater incrementUpdater = new ValueUpdater(1);
	private final ValueUpdater decrementUpdater = new ValueUpdater(-1);
	private OnValueChangedListener onValueChangedListener;
	private Button decrementButton;
	private Button incrementButton;
	private EditText valueText;
	private int minimum = 0;
	private int maximum = 999;
	private int value;
	
	public static interface OnValueChangedListener {
		void onValueChanged(NumberPicker view, int value);
	}

	/**
	 * This little guy handles the auto part of the auto incrementing feature.
	 * In doing so it instantiates itself. There has to be a pattern name for
	 * that...
	 */
	private class ValueUpdater implements Runnable {
		private int change;
		
		public ValueUpdater(int change) {
			this.change = change;
		}
		
		public void run() {
			doChange();
			repeatUpdateHandler.postDelayed(this, REPEAT_DELAY);
		}
		
		public void doChange() {
			setValue(getValue() + change);
		}
		
		public void begin() {
			repeatUpdateHandler.post(this);
		}
		
		public void end() {
			repeatUpdateHandler.removeCallbacks(this);
		}
	}
	
	public NumberPicker(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		
		// Create the layout
		setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		LayoutParams elementParams = new LinearLayout.LayoutParams(ELEMENT_WIDTH, ELEMENT_HEIGHT);
		
		// Create the individual elements
		valueText = createEditText(context);
		incrementButton = createButton(context, "+", incrementUpdater);
		decrementButton = createButton(context, "-", decrementUpdater);
		
		// Set additional attributes
		setValue(0);
		
		// Can be configured to be vertical or horizontal
		if (getOrientation() == VERTICAL) {
			addView(incrementButton, elementParams);
			addView(valueText, elementParams);
			addView(decrementButton, elementParams);
		} else {
			addView(decrementButton, elementParams);
			addView(valueText, elementParams);
			addView(incrementButton, elementParams);
		}
	}
	
	private Button createButton(Context context, CharSequence text, final ValueUpdater updater) {
		// Create the increment button
		Button button = new Button(context);
		button.setTextSize(25);
		button.setText(text);
		
		// Change once for a click
		button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	updater.doChange();
            }
        });
		
		// Auto change for a long click
		button.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View v) {
				updater.begin();
				return false;
			}
		});
		
		// When the button is released, if we're auto changing then stop
		button.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					updater.end();
				}
				return false;
			}
		});
		return button;
	}
	
	private EditText createEditText(Context context) {
		// Create the edit text
		EditText editText = new EditText(context);
		editText.setTextSize(25);
		editText.setGravity(Gravity.CENTER);
		editText.setInputType(InputType.TYPE_CLASS_NUMBER);
		
		// Since we're a number that gets affected by the button, we need to be
		// ready to change the numeric value with a simple ++/--, so whenever
		// the value is changed with a keyboard, convert that text value to a
		// number. We can set the text area to only allow numeric input, but 
		// even so, a carriage return can get hacked through. To prevent this
		// little quirk from causing a crash, store the value of the internal
		// number before attempting to parse the changed value in the text area
		// so we can revert to that in case the text change causes an invalid
		// number
		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				int backupValue = getValue();
				try {
					int value = Integer.parseInt(s.toString());
					setValue(value);
				} catch(NumberFormatException e) {
					setValue(backupValue);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			
			@Override
			public void afterTextChanged(Editable s) { }
		});
		
		// Highlight the number when we get focus
		editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					((EditText)v).selectAll();
				}
			}
		});
		return editText;
	}
	
	public void setOnValueChangedListener(OnValueChangedListener onValueChangedListener) {
		this.onValueChangedListener = onValueChangedListener;
	}
	
	public int getMaximumValue() {
		return maximum;
	}
	
	public void setMaximumValue(int maximum) {
		this.maximum = maximum;
		setValue(getValue());
	}
	
	public int getMinimumValue() {
		return minimum;
	}
	
	public void setMinimumValue(int minimum) {
		this.minimum = minimum;
		setValue(getValue());
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		// Update the local value
		value = Math.min(value, getMaximumValue());
		value = Math.max(value, getMinimumValue());
		if (this.value != value) {
			this.value = value;
			
			// Notify the listener
			if (onValueChangedListener != null) {
				onValueChangedListener.onValueChanged(this, value);
			}
		}
		
		// Update the textbox
		String valueStr = Integer.toString(value);
		if (valueText != null && !valueText.getText().toString().equals(valueStr)) {
			valueText.setText(valueStr);
		}
	}	
}