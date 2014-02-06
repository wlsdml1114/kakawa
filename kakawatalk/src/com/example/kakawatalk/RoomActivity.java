package com.example.kakawatalk;





import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.TimeUtils;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class RoomActivity extends Activity {
	private static final String MY_NAME = "engui";
	private static final String AUTO_NAME = "suckbakji";
	private HashMap<String, String> mKeywordMap;
	private ScrollView mScrollView;
	private View mCamera;
	private ImageView mImageView;
	private String mCurrentPhotoPath;
	private Bitmap mProfileImage;
	private View mEmoticon;
	private View mEmoticons;
	private int mRoomId;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "onCreate", Toast.LENGTH_LONG).show();
        setContentView(R.layout.activity_room);
        String name = getIntent().getExtras().getString("name");
        setTitle(name);
        mRoomId = getIntent().getExtras().getInt("room_id");
       
        
        initSendButton();
        
        mScrollView = (ScrollView) findViewById(R.id.scroll_view);
        mKeywordMap = new HashMap<String, String>();
        mKeywordMap.put("hi", "Hello");
        mKeywordMap.put("hello", "hello");
        mKeywordMap.put("nice", "Nice to meet you, too");
        mEmoticons = findViewById(R.id.emoticons);
        
        mCamera = findViewById(R.id.camera);
        mCamera.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(RoomActivity.this);
				CharSequence[] Items = new CharSequence[]{
						"Camera", "Gallery"
				};
				builder.setItems(Items, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						if(which == 0){
							dispatchTakePictureIntent();
						}else if(which == 1){
							pickPhotoFromGallery();
						}
						
					}
				});
				builder.show();
				
			}
		});
        mImageView = (ImageView) findViewById(R.id.image);
        
        String imageFileName = "profile";
        File storageDir = Environment
        		.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir.toString(), imageFileName + ".jpg");
        mCurrentPhotoPath = image.getAbsolutePath();
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inSampleSize = 8;
        mProfileImage = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        
        mEmoticon = findViewById(R.id.emoticon_bt);
        mEmoticon.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mEmoticons.getVisibility() == View.GONE){
					mEmoticons.setVisibility(View.VISIBLE);
					
				}else{
					mEmoticons.setVisibility(View.GONE);
				}
				
			}
		});
        
        setupEmoticons(R.id.emoticon1, R.drawable.emoticon);
        setupEmoticons(R.id.emoticon2, R.drawable.emoticon1);
        setupEmoticons(R.id.emoticon3, R.drawable.emoticon2);
        getMessage();
       
        }
	public void getMessage(){
		 new AsyncTask() {

				@Override
				protected Object doInBackground(Object... arg0) {
					final String result = RestClient.getMessages(mRoomId);
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							if (TextUtils.isEmpty(result)){
								return;
							}
							String[] tokens = result.split(",");
							LinearLayout messages = (LinearLayout) findViewById(R.id.messages);
							messages.removeAllViews();
							for (int i = tokens.length - 1; i>= 0; i--){
								String token = tokens[i];
								String[] details = token.split("\\\\");
								addMessageItem(details[0], details[1], null);
							}
							mScrollView.post(new Runnable() {
								
								@Override
								public void run() {
									mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
								}
							});
							
						}
					});
					return null;
				}
			}.execute();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		GcmIntentService.roomActivity = this;
	}
	
	@Override
	protected void onPause(){
		super.onResume();
		GcmIntentService.roomActivity = null;
	}
	
	
	@Override
	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		Toast.makeText(this, "onNewIntent", Toast.LENGTH_LONG).show();
	}
	
	private void setupEmoticons(int iconId, final int iconDrawableID){
		findViewById(iconId).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), iconDrawableID);
				addMessageItem(MY_NAME, null, imageBitmap);
				
			}
		});
	}
	private void initSendButton(){
		View send = findViewById(R.id.send);
		send.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				EditText text = (EditText) findViewById(R.id.edit);
				final String message = text.getEditableText().toString();
				addMessageItem(MY_NAME, message, null);
				analyzeMessage(message);
				new AsyncTask() {
					protected Object doInBackground(Object... arg0){
						RestClient.sendMessages(13, message, mRoomId);
						return null;
					}
				}.execute();
				text.setText("");
				
				
			}

			private void analyzeMessage(String message) {
				Set<String> keySet = mKeywordMap.keySet();
				for(String key : keySet){
					if (message.contains(key)) {
						addMessageItem(AUTO_NAME, mKeywordMap.get(key), null);
					}
				}
			}
		});
	}
	private void addMessageItem(String nameString, String messageString, Bitmap imageBitmap){
		
		View Item = View.inflate(this, R.layout.message_item, null);
		TextView message = (TextView) Item.findViewById(R.id.message);
		
		
		
		TextView time = (TextView) Item.findViewById(R.id.time);
		time.setText(DateFormat.format("hh:mm", new Date()));
		TextView Name = (TextView) Item.findViewById(R.id.name);
		Name.setText(nameString);
		ImageView image = (ImageView) Item.findViewById(R.id.image);
		if (imageBitmap == null){
			message.setText(messageString);
			message.setVisibility(View.VISIBLE);
			image.setVisibility(View.GONE);
		}else{
			image.setImageBitmap(imageBitmap);
			message.setVisibility(View.GONE);
			image.setVisibility(View.VISIBLE);
		}
		
		if(nameString == MY_NAME){
			ImageView pic = (ImageView) Item.findViewById(R.id.pic);
			pic.setImageBitmap(mProfileImage);
		}
		
		LinearLayout messages = (LinearLayout) findViewById(R.id.messages);
		messages.addView(Item);
		mScrollView.post(new Runnable() {
			
			@Override
			public void run() {
				mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
				
			}
		});
	}
	
	static final int REQUEST_IMAGE_CAPTURE = 1;
	static final int REQUEST_PICK_IMAGE = 2;
	
	private void dispatchTakePictureIntent() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		 if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			 startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
		 }
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		 if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
			 Bundle extras = data.getExtras();
			 Bitmap imageBitmap = (Bitmap) extras.get("data");
			 addMessageItem(MY_NAME, null, imageBitmap);
	
	}else if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK) {
		Uri uri = data.getData();
		
		 Cursor cursor = getContentResolver().query(uri, 
				 new String[] { 
				 android.provider.MediaStore.Images.ImageColumns.DATA}, null, null, null);
		 cursor.moveToFirst();
		 
		 final String imageFilePath = cursor.getString(0);
		 BitmapFactory.Options bmOptions = new BitmapFactory.Options(); 
		 bmOptions.inSampleSize = 8;
		 Bitmap imageBitmap = BitmapFactory.decodeFile(imageFilePath, bmOptions); 
		   addMessageItem(MY_NAME, null, imageBitmap);
		   cursor.close();
		 }
	}
	
private void pickPhotoFromGallery(){
	Intent intent = new Intent();
	intent.setType("image/*");
	intent.setAction(Intent.ACTION_GET_CONTENT);
	startActivityForResult(intent.createChooser(intent, "Select Picture"), REQUEST_PICK_IMAGE); 
}
}
