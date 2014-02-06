package com.example.kakawa.db;

import android.provider.BaseColumns;


public class RoomContract {
	 public RoomContract() {}
	 public static abstract class RoomEntry implements BaseColumns {
		 public static final String TABLE_NAME = "RoomTable";
		  public static final String COLUMN_NAME_PROFILE_ID = "ProfileID";
		   public static final String COLUMN_NAME_ROOM_NAME = "RoomName";
		   public static final String COLUMN_NAME_ROOM_ID = "RoomID";
	 }
}
