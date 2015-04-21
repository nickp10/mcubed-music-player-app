package dev.paddock.adp.mCubed.preferences;

import android.content.Context;
import android.graphics.Typeface;
import android.preference.DialogPreference;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.model.Credentials;
import dev.paddock.adp.mCubed.utilities.Delegate.Action;

public class LoginPreference extends DialogPreference {
	private EditText username, password;
	private Action<Credentials> loginAction;

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
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult && loginAction != null) {
			loginAction.act(new Credentials(username.getText().toString(), password.getText().toString()));
		}
	}

	public void setLoginAction(Action<Credentials> loginAction) {
		this.loginAction = loginAction;
	}
}
