package dev.paddock.adp.mCubed.utilities;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import dev.paddock.adp.mCubed.Schema;

public class WebService {
	public static String submitFeedback(String email, String message) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(Schema.WS_SUBMIT_FEEDBACK_EMAIL, email));
		params.add(new BasicNameValuePair(Schema.WS_SUBMIT_FEEDBACK_MESSAGE, message));
		params.add(new BasicNameValuePair(Schema.WS_SUBMIT_FEEDBACK_LOGS, Log.readLogFile()));
		return sendHTTPPost(Schema.WS_METHOD_SUBMIT_FEEDBACK, params);
	}
	
	private static String sendHTTPPost(String method, List<? extends NameValuePair> params) {
		HttpClient httpclient = new DefaultHttpClient();
		try {
			HttpPost request = new HttpPost(String.format("%s%s", Schema.WEB_SERVICE_URL, method));
			request.setEntity(new UrlEncodedFormEntity(params));
			return httpclient.execute(request, new BasicResponseHandler());
		} catch (Exception e) {
			Log.e(e);
			return null;
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
	}
}
