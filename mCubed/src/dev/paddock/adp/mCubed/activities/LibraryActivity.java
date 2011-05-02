package dev.paddock.adp.mCubed.activities;

import java.util.ArrayList;
import java.util.Date;

import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.BindingList;
import dev.paddock.adp.mCubed.model.BindingListAdapter;
import dev.paddock.adp.mCubed.model.MediaStatus;
import dev.paddock.adp.mCubed.receivers.ClientReceiver;
import dev.paddock.adp.mCubed.services.ClientCallback;
import dev.paddock.adp.mCubed.services.IClientCallback;
import dev.paddock.adp.mCubed.services.PlaybackClient;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Utilities;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class LibraryActivity extends Activity {
	private ClientReceiver clientReceiver;
	private IClientCallback clientCallback;
	private TextView headphoneView, bluetoothView, mountView;
	private ListView listView;
	private Button playButton, addButton, removeButton, clearButton;
	private boolean isInitialized;
	private final BindingList<String> items = new BindingList<String>(new ArrayList<String>());
	private final BindingListAdapter<String> itemsAdapter = new BindingListAdapter<String>(items) {
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView view = new TextView(getBaseContext());
			view.setText(items.get(position));
			return view;
		}
	};
	
	/**
	 * Click listener for the play/pause button
	 */
	private OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Utilities.pushContext(LibraryActivity.this);
			try {
				if (App.getPlayer().getStatus() == MediaStatus.Play) {
					PlaybackClient.pause();
				} else {
					PlaybackClient.play();
				}
			} finally {
				Utilities.popContext();
			}
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Initialize the creation
		Utilities.pushContext(this);
		try {
			super.onCreate(savedInstanceState);
			
			// Set the content view and retrieve the views
			setContentView(R.layout.main);
			headphoneView = (TextView)findViewById(R.id.headphone);
			bluetoothView = (TextView)findViewById(R.id.bluetooth);
			mountView = (TextView)findViewById(R.id.mount);
			playButton = (Button)findViewById(R.id.mainPlayButton);
			addButton = (Button)findViewById(R.id.mainAddButton);
			removeButton = (Button)findViewById(R.id.mainRemoveButton);
			clearButton = (Button)findViewById(R.id.mainClearButton);
			listView = (ListView)findViewById(R.id.mainListView);
			updateViews();
			
			// Register receiver and listeners
			if (!isInitialized) {
				// Listen for the click event
				isInitialized = true;
				playButton.setOnClickListener(clickListener);
				addButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						items.add(DateFormat.getTimeFormat(getBaseContext()).format(new Date()));
					}
				});
				removeButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (!items.isEmpty()) {
							items.remove(0);
						}
					}
				});
				clearButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						items.clear();
						itemsAdapter.setNotifyOnChange(!itemsAdapter.isNotifyOnChange());
					}
				});
				
				
				// Listen for service updates
				ClientReceiver receiver = getClientReceiver();
				IntentFilter filter = receiver.getIntentFilter();
				if (filter != null) {
					registerReceiver(receiver, filter);
				}
				
				// Notify the service of the new client
				PlaybackClient.startService();
			}
		} finally {
			Utilities.popContext();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Schema.MN_SETTINGS, 1, "Settings").setIcon(R.drawable.settings);
		menu.add(Menu.NONE, Schema.MN_EXIT, 2, "Exit").setIcon(R.drawable.exit);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Utilities.pushContext(this);
		try {
			switch(item.getItemId()) {
			case Schema.MN_SETTINGS:
				Intent intent = new Intent(this, PreferenceActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				return true;
			case Schema.MN_EXIT:
				PlaybackClient.stopService();
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		} finally {
			Utilities.popContext();
		}
	}
	
	@Override
	protected void onDestroy() {
		// Start the destroy process
		Utilities.pushContext(this);
		try {
			super.onDestroy();
			
			// Unregister receivers and listeners
			isInitialized = false;
			if (clientReceiver != null) {
				unregisterReceiver(clientReceiver);
			}
		} finally {
			Utilities.popContext();
		}
	}
	
	private void updateViews() {
		headphoneView.setText(Boolean.toString(App.getHeadset().isHeadphonesConnected()));
		bluetoothView.setText(Boolean.toString(App.getHeadset().isBluetoothConnected()));
		mountView.setText(Boolean.toString(App.getMount().isMounted()));
		playButton.setText(App.getPlayer().getStatus() == MediaStatus.Play ? "Pause" : "Play");
		listView.setAdapter(itemsAdapter);
	}
	
	private ClientReceiver getClientReceiver() {
		if (clientReceiver == null) {
			clientReceiver = new ClientReceiver(getClientCallback(), false);
			clientReceiver.addAction(Schema.I_MCUBED_PROGRESS);
		}
		return clientReceiver;
	}
	
	private IClientCallback getClientCallback() {
		if (clientCallback == null) {
			clientCallback = new ClientCallback() {
				public void propertyMountChanged(boolean isMounted) {
					updateViews();
				}
				
				public void propertyBlueoothChanged(boolean isBluetoothConnected) {
					updateViews();
				}
				
				public void propertyHeadphoneChanged(boolean isHeadphoneConnected) {
					updateViews();
				}
				
				public void propertyPlaybackStatusChanged(MediaStatus playbackStatus) {
					updateViews();
				}
			};
		}
		return clientCallback;
	}
}