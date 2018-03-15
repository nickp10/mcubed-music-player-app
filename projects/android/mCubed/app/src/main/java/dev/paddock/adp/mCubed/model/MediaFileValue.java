package dev.paddock.adp.mCubed.model;

import dev.paddock.adp.mCubed.lists.IGrouper;

public enum MediaFileValue implements IGrouper<MediaFile> {
	Artist(new IGrouper<MediaFile>() {
		@Override
		public String getGroup(MediaFile item) {
			return item.getArtist();
		}
	}), Album(new IGrouper<MediaFile>() {
		@Override
		public String getGroup(MediaFile item) {
			return item.getAlbum();
		}
	}), Genre(new IGrouper<MediaFile>() {
		@Override
		public String getGroup(MediaFile item) {
			return item.getGenre();
		}
	}), Title(new IGrouper<MediaFile>() {
		@Override
		public String getGroup(MediaFile item) {
			return item.getTitle();
		}
	}), Track(new IGrouper<MediaFile>() {
		@Override
		public String getGroup(MediaFile item) {
			return Integer.toString(item.getTrack());
		}
	}), Year(new IGrouper<MediaFile>() {
		@Override
		public String getGroup(MediaFile item) {
			return Integer.toString(item.getYear());
		}
	});
	
	private final IGrouper<MediaFile> valueProvider;
	
	private MediaFileValue(IGrouper<MediaFile> valueProvider) {
		this.valueProvider = valueProvider;
	}

	@Override
	public String getGroup(MediaFile item) {
		return getValue(item);
	}
	
	public String getValue(MediaFile item) {
		return valueProvider.getGroup(item);
	}
}