package com.maximcedaroff.soundrec;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Daniel on 12/30/2014.
 */
public class RecordingItem implements Parcelable {
	private String name; // file name
	private String filePath; //file path
	private int id; //id in database
	private int length; // length of recording in seconds
	private long time, fileSize; // date/time of the recording
	
	public RecordingItem() {
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public int getLength() {
		return length;
	}
	
	public void setLength(int length) {
		this.length = length;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public long getTime() {
		return time;
	}
	
	public void setTime(long time) {
		this.time = time;
	}
	
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	
	public long getFileSize() {
	
		return fileSize;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.name);
		dest.writeString(this.filePath);
		dest.writeInt(this.id);
		dest.writeInt(this.length);
		dest.writeLong(this.time);
		dest.writeLong(this.fileSize);
	}
	
	protected RecordingItem(Parcel in) {
		this.name = in.readString();
		this.filePath = in.readString();
		this.id = in.readInt();
		this.length = in.readInt();
		this.time = in.readLong();
		this.fileSize = in.readLong();
	}
	
	public static final Creator<RecordingItem> CREATOR = new Creator<RecordingItem>() {
		@Override
		public RecordingItem createFromParcel(Parcel source) {
			return new RecordingItem(source);
		}
		
		@Override
		public RecordingItem[] newArray(int size) {
			return new RecordingItem[size];
		}
	};
}