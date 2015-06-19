package dev.paddock.adp.mCubed.scrobble;

import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.utilities.Log;
import dev.paddock.adp.mCubed.utilities.PreferenceManager;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class ScrobbleService {
	private static final String API_URL = "https://ws.audioscrobbler.com/2.0/";

	public static boolean isLoggedIn() {
		return !Utilities.isNullOrEmpty(getSessionKey());
	}

	public static boolean isTurnedOn() {
		return PreferenceManager.getSettingBoolean(R.string.pref_scrobble_on);
	}

	public static String getSessionKey() {
		return PreferenceManager.getSettingString(R.string.pref_scrobble_key);
	}

	public static <TRequest extends ScrobbleRequest<TResponse>, TResponse extends ScrobbleResponse> TResponse sendRequest(TRequest scrobbleRequest) throws ScrobbleException {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, Schema.WS_TIMEOUT_MILLIS);
		HttpConnectionParams.setSoTimeout(params, Schema.WS_TIMEOUT_MILLIS);
		HttpClient httpclient = new DefaultHttpClient(params);
		try {
			HttpPost request = new HttpPost(API_URL);
			request.setEntity(new UrlEncodedFormEntity(scrobbleRequest.createParameters()));
			HttpResponse response = httpclient.execute(request);
			InputStream responseStream = response.getEntity().getContent();
			String responseBody = Utilities.loadStream(responseStream);
			return ScrobbleResponse.parse(scrobbleRequest.getResponseClass(), responseBody);
		} catch (ScrobbleException e) {
			throw e;
		} catch (Exception e) {
			Log.e(e);
			throw new ScrobbleException(e);
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
	}
}
