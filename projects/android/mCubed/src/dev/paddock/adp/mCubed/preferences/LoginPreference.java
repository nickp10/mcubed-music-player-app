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
import android.widget.Toast;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.model.Credentials;
import dev.paddock.adp.mCubed.utilities.Utilities;
import dev.paddock.adp.mCubed.utilities.Delegate.Func;

public class LoginPreference extends DialogPreference {
	private EditText username, password;
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
							Toast.makeText(getContext(), "Logged in", Toast.LENGTH_LONG).show();
							dialog.dismiss();
						} else {
							Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
						}
					}
				});
			}
		});
	}

	public void setLoginAction(Func<Credentials, String> loginAction) {
		this.loginAction = loginAction;
	}
}
