package dev.paddock.adp.mCubed.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.receivers.ClientReceiver;
import dev.paddock.adp.mCubed.receivers.IProvideClientReceiver;
import dev.paddock.adp.mCubed.services.ClientCallback;
import dev.paddock.adp.mCubed.services.IClientCallback;
import dev.paddock.adp.mCubed.utilities.App;

public class NowPlayingView extends LinearLayout implements IProvideClientReceiver {
	private ClientReceiver clientReceiver;
	private ClientCallback clientCallback;
	private MediaFileDetailsView mediaFileView;
	
	public NowPlayingView(Context context) {
		super(context);
		initView(context);
	}
	
	public NowPlayingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	
	private void initView(Context context) {
		// Inflate the layout
		LayoutInflater inflater = App.getSystemService(LayoutInflater.class, context, Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.now_playing_view, this, true);
		
		// Find the views
		mediaFileView = (MediaFileDetailsView)findViewById(R.id.npv_media_file_details_view);
		
		// Update the views
		updateViews();
	}
	
	private void updateViews() {
		mediaFileView.setMediaFile(App.getPlayingMedia());
	}

	@Override
	public ClientReceiver getClientReceiver() {
		if (clientReceiver == null) {
			clientReceiver = new ClientReceiver(getClientCallback(), false);
		}
		return clientReceiver;
	}
	
	private IClientCallback getClientCallback() {
		if (clientCallback == null) {
			clientCallback = new ClientCallback() {
				@Override
				public void propertyPlaybackIDChanged(long playbackID) {
					updateViews();
				}
			};
		}
		return clientCallback;
	}
}
