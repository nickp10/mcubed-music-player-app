package dev.paddock.adp.mCubed.controls;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.model.MediaFile;
import dev.paddock.adp.mCubed.utilities.App;

public class MediaFileDetailsView extends LinearLayout {
	private TextView artistTextView, albumTextView, titleTextView, genreTextView, trackTextView, yearTextView, durationTextView;
	private ImageView coverImageView;
	private MediaFile mediaFile;
	
	public MediaFileDetailsView(Context context) {
		super(context);
		initView(context);
	}
	
	public MediaFileDetailsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	
	private void initView(Context context) {
		// Inflate the layout
		LayoutInflater inflater = App.getSystemService(LayoutInflater.class, context, Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.media_file_details_view, this, true);
		
		// Find the views
		coverImageView = (ImageView)findViewById(R.id.mfdv_cover_image);
		artistTextView = (TextView)findViewById(R.id.mfdv_artist_textView);
		albumTextView = (TextView)findViewById(R.id.mfdv_album_textView);
		titleTextView = (TextView)findViewById(R.id.mfdv_title_textView);
		genreTextView = (TextView)findViewById(R.id.mfdv_genre_textView);
		trackTextView = (TextView)findViewById(R.id.mfdv_track_textView);
		yearTextView = (TextView)findViewById(R.id.mfdv_year_textView);
		durationTextView = (TextView)findViewById(R.id.mfdv_duration_textView);
		
		// Initialize the views
		updateViews();
	}

	private void updateViews() {
		// Update the media file information display
		Uri art = null;
		if (mediaFile == null) {
			for (TextView textView : new TextView[] { artistTextView, albumTextView, titleTextView, genreTextView, trackTextView, yearTextView, durationTextView }) {
				textView.setText("Unknown");
			}
		} else {
			art = mediaFile.getAlbumArt();
			artistTextView.setText(mediaFile.getArtist());
			albumTextView.setText(mediaFile.getAlbum());
			titleTextView.setText(mediaFile.getTitle());
			genreTextView.setText(mediaFile.getGenre());
			trackTextView.setText(Integer.toString(mediaFile.getTrack()));
			yearTextView.setText(Integer.toString(mediaFile.getYear()));
			durationTextView.setText(mediaFile.getDurationString());
		}
		
		// Update the album art
		if (art == null) {
			coverImageView.setImageResource(R.drawable.img_cover_missing);
		} else {
			coverImageView.setImageURI(art);
		}
	}

	public MediaFile getMediaFile() {
		return mediaFile;
	}

	public void setMediaFile(MediaFile mediaFile) {
		if (this.mediaFile != mediaFile) {
			this.mediaFile = mediaFile;
			updateViews();
		}
	}
}
