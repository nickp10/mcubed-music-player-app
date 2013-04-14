package dev.paddock.adp.mCubed.controls;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.activities.ActivityUtils;
import dev.paddock.adp.mCubed.receivers.ClientReceiver;
import dev.paddock.adp.mCubed.receivers.IProvideClientReceiver;
import dev.paddock.adp.mCubed.services.ClientCallback;
import dev.paddock.adp.mCubed.services.IClientCallback;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class NowPlayingView extends Fragment implements IProvideClientReceiver {
	private ClientReceiver clientReceiver;
	private ClientCallback clientCallback;
	private MediaFileDetailsView mediaFileView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityUtils.registerClientReceivers(getActivity(), this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		ActivityUtils.unregisterClientReceivers(getActivity(), this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.now_playing_view, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Utilities.pushContext(view.getContext());
		try {
			// Find the views
			mediaFileView = (MediaFileDetailsView)view.findViewById(R.id.npv_media_file_details_view);
			
			// Update the views
			updateViews();
		} finally {
			Utilities.popContext();
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		// Clean-up the view since it has been destroyed
		mediaFileView.setMediaFile(null);
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
