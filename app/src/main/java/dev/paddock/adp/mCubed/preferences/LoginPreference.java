package dev.paddock.adp.mCubed.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.model.Credentials;
import dev.paddock.adp.mCubed.utilities.Delegate.Func;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class LoginPreference extends DialogPreference {
	private EditText username, password;
	private LinearLayout loggingInProgress;
	private TextView error;
	private Func<Credentials, String> loginAction;

	public LoginPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.login_preference);
	}

	@Override
	protected void onBindDialogView(View view) {
		// Setup
		super.onBindDialogView(view);

		// Find the elements
		username = (EditText) view.findViewById(R.id.lp_username);
		password = (EditText) view.findViewById(R.id.lp_password);
		loggingInProgress = (LinearLayout) view.findViewById(R.id.lp_logging_in_progress);
		error = (TextView) view.findViewById(R.id.lp_error_text);

		// Update the typeface of the hint text on the password field
		password.setTypeface(Typeface.DEFAULT);
		password.setTransformationMethod(new PasswordTransformationMethod());
	}

	@Override
	protected void showDialog(Bundle state) {
		super.showDialog(state);
		final AlertDialog dialog = Utilities.cast(AlertDialog.class, getDialog());
		if (dialog != null) {
			Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
			if (button != null) {
				button.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Utilities.pushContext(getContext());
						try {
							LoginPreference.this.login(dialog);
						} finally {
							Utilities.popContext();
						}
					}
				});
			}
		}
	}

	private void login(final AlertDialog dialog) {
		if (loggingInProgress.getVisibility() == View.VISIBLE) {
			return; // Already logging in
		}
		if (error.getVisibility() == View.VISIBLE) {
			showFields(dialog);
			return;
		}
		showProgress(dialog);
		Utilities.dispatchToBackgroundThread(Utilities.getContext(), new Runnable() {
			@Override
			public void run() {
				final String error;
				if (loginAction == null) {
					error = Utilities.getResourceString(R.string.login_no_action);
				} else {
					error = loginAction.act(new Credentials(username.getText().toString(), password.getText().toString()));
				}
				Utilities.dispatchToUIThread(Utilities.getContext(), new Runnable() {
					@Override
					public void run() {
						if (Utilities.isNullOrEmpty(error)) {
							dialog.dismiss();
						} else {
							showError(dialog, error);
						}
					}
				});
			}
		});
	}

	private void showProgress(AlertDialog dialog) {
		username.setVisibility(View.GONE);
		password.setVisibility(View.GONE);
		loggingInProgress.setVisibility(View.VISIBLE);
		error.setVisibility(View.GONE);
		dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
	}

	private void showFields(AlertDialog dialog) {
		username.setVisibility(View.VISIBLE);
		password.setVisibility(View.VISIBLE);
		loggingInProgress.setVisibility(View.GONE);
		error.setVisibility(View.GONE);
		dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
	}

	private void showError(AlertDialog dialog, String error) {
		username.setVisibility(View.GONE);
		password.setVisibility(View.GONE);
		loggingInProgress.setVisibility(View.GONE);
		this.error.setVisibility(View.VISIBLE);
		this.error.setText(error);
		dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
	}

	public void setLoginAction(Func<Credentials, String> loginAction) {
		this.loginAction = loginAction;
	}
}
