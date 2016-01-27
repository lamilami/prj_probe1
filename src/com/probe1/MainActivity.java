package com.probe1;

import java.io.IOException;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;



//public class MainActivity extends Activity {
public class MainActivity extends Activity implements OnLoadCompleteListener {
		
	
///////////////////////////////////////////////////////////////////////SoundPool
	  final String LOG_TAG = "myLogs";
	  final int MAX_STREAMS = 5;	
	  SoundPool sp;
	  int soundIdShot;
	  int soundIdExplosion;
	  int soundIDrestricted;
	      
	  int streamIDrestact;
	  int streamIDShot;
	  int streamIDExplosion;
    
		///////////////////////////////////////////////////////////////////COM PORT
	int baudRate; /* baud rate */
	byte stopBit; /* 1:1stop bits, 2:2 stop bits */
	byte dataBit; /* 8:8bit, 7: 7bit */
	byte parity; /* 0: none, 1: odd, 2: even, 3: mark, 4: space */
	byte flowControl; /* 0:none, 1: flow control(CTS,RTS) */  
    ///////////////////////////////////////////////////////	  
	byte status;
    byte[] writeBuffer =new byte[] {'C','O','R','E','X','\r'}; 
    byte[] readBuffer;
    char[] readBufferToChar;
    int[] actualNumBytes;
	public boolean bConfiged = false;
	public FT311UARTInterface uartInterface;
	public String act_string;
	public Context global_context;
	/* thread to read the data */
	public handler_thread handlerThread;
	////////////////////////////////////////////////////////////////////////////The GAME
	int lives_cnt;
	int magazine_cnt;
	int ammo_cnt;
	int damage_cnt;
	TextView ammo_txt;
	ProgressBar progress_ammo;
	
	
	////////////////////////////////////////////////////////////////////////////App Preferences 
	View mDecorView;
    int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
          | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
          | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
          | View.SYSTEM_UI_FLAG_FULLSCREEN  ;
    
	
  	public SharedPreferences sharePrefSettings;	
	////////////////////////////////// TIMER
	// protected int splashTime = 3000;	
	// int timer =0;

	//////////////////////////////////////////////////////////////////////////// LAYOUT
  	Button btnOk;
  	//TextView tvOut;
	//EditText readText;

	@Override ////////////////////////////////////////////////////////////////// onCreate
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
        //////////////////////////////////////////////////////////////////SOUND
	    sp = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
	    sp.setOnLoadCompleteListener(this);
	    soundIdShot = sp.load(this, R.raw.shot, 1);
	    Log.d(LOG_TAG, "soundIdShot = " + soundIdShot);
	    ////////////////////////////////////////////////////// LOAD FROM SD CARD
	    soundIDrestricted = sp.load("/sdcard/Download/OFCI.mp3",1); // R.raw.restact, 1);
	    Log.d(LOG_TAG, "soundIDrestact = " + soundIDrestricted);	    	 	    
	    try 
	    {
	      soundIdExplosion = sp.load(getAssets().openFd("explosion.ogg"), 1);
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	    Log.d(LOG_TAG, "soundIdExplosion = " + soundIdExplosion);
	    ///////////////////////////////////////////////////////////////
        global_context = this;         
 ////////////////////////////////////////////////////////////////////////////////////////////////SYSTEM BAR
	    //tvOut = (TextView) findViewById(R.id.tvView);
		//readText = (EditText) findViewById(R.id.ReadValues);
        //mDecorView = getWindow().getDecorView();
      mDecorView = getWindow().getDecorView();
     // Hide both the navigation bar and the status bar.
     // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
     // a general rule, you should design your app to hide the status bar whenever you
     // hide the navigation bar.
      mDecorView.setSystemUiVisibility(uiOptions);
     View decorView = getWindow().getDecorView();
     decorView.setOnSystemUiVisibilityChangeListener
             (new View.OnSystemUiVisibilityChangeListener() {
         @Override
         public void onSystemUiVisibilityChange(int visibility) {
             // Note that system bars will only be "visible" if none of the
             // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
             if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                 // TODO: The system bars are visible. Make any desired
                 // adjustments to your UI, such as showing the action bar or
                 // other navigational controls.
            	
             } else {
                 // TODO: The system bars are NOT visible. Make any desired
                 // adjustments to your UI, such as hiding the action bar or
                 // other navigational controls.
            	    mDecorView.setSystemUiVisibility(uiOptions);
            	 //   sp.play(soundIdShot, 1, 1, 0, 0, 1);
             }
         }
     });     
     
     /////////////////////////////////////////////////////////////////////////////////////////////
     setContentView(R.layout.main);  
 	 //////////////////////////////////////////////////////////////////////////////////////////// TITLE    
	   	   
     setTitle("========= YOU HAVE 3 NEW MESSAGES ==========");     
     //////////////////////////////////////////////////////////////////////////////////////////////BUTTON MAP 
		Button mapsButton = (Button)findViewById(R.id.buttonMAP);
		mapsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*lastKnownLocation = getLastKnownLocation(locationManager);*/
			//	String lat = ""+lastKnownLocation.getLatitude();
			//	String lon = ""+lastKnownLocation.getLongitude();
				Intent webIntent = new Intent(v.getContext(),MAPviewActivity.class);
			//	webIntent.putExtra("lat", lat);
			//	webIntent.putExtra("lon", lon);
				startActivity(webIntent);
			//	startService(new Intent(v.getContext(),RouteTracking.class));
				
			}
		});

 
	    /////////////////////////////////////////////////////////////////////SERIAL Communication 
	    /* allocate buffer */
		//writeBuffer = new byte[4];
		readBuffer = new byte[8];
		readBufferToChar = new char[8]; 
		actualNumBytes = new int[1];

 	uartInterface = new FT311UARTInterface(this, sharePrefSettings);
		
		baudRate = 115200;
		stopBit = 1;
		dataBit = 8;
		parity = 0;
		flowControl = 0;
		
		/////////////////////////////////////////////////////???????????????????????????	                      
		act_string = getIntent().getAction();
		if( -1 != act_string.indexOf("android.intent.action.MAIN")){
			//restorePreference();			
		}			
		else if( -1 != act_string.indexOf("android.hardware.usb.action.USB_ACCESSORY_ATTACHED")){
			//cleanPreference();		
		}
		/////////////////////////////////////////////////////////
	
 
    /////////////////////////////////////////////////////////////////// THE GAME    
	btnOk = (Button) findViewById(R.id.btnOk);
	 progress_ammo=(ProgressBar) findViewById(R.id.ProgressBarGreen);
	 
	
	//////////////////////////////////////////////////
	ammo_txt = (TextView) findViewById(R.id.greentxt);
	ammo_cnt=100;
    ammo_txt.setText(String.valueOf("AMMO "+ammo_cnt));	
////////////////////////////////////////////////////////
///////////////////////onClick	
///////////////////////////////////////////////////////
	    OnClickListener oclBtnOk = new OnClickListener() 
	    {
	        @Override
	        public void onClick(View v) {	         
	        	
	        	progress_ammo.setProgress(ammo_cnt);
	        	ammo_txt.setText(String.valueOf("AMMO "+ammo_cnt--));
	        	sp.play(soundIdShot, 1, 1, 0, 0, 1);
	        	bConfiged = false;
	    		if(false == bConfiged)
	    		{
	    
	    			bConfiged = true;
	    		
	    			uartInterface.SendData(6, writeBuffer); 
	    		};	
	     
	       	        		  
	                                                          }
	      };	      
	      btnOk.setOnClickListener(oclBtnOk);

/////////////////////////////////////////////////////handlerThread
getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);///??????
handlerThread = new handler_thread(handler);
handlerThread.start();
///////////////////////////////////////////////////////////////////// POOL CORE 
/*		
Thread th=new Thread(){

				@Override
				public void run()
				{
					try
					{					     											
						while(true){
												 
								Thread.sleep(1000);
								runOnUiThread(new Runnable() { 
									@Override
									public void run() 
									{
										try {
											  //tv1.setText(name[timer]);
											  uartInterface.SendData(6, writeBuffer); 
										    }
										catch(Exception e) 
										{
											e.printStackTrace();
										} 
									}
								                               });
						            } 	
				
						
					}catch (InterruptedException e){}

				}
			};
			 th.start();
   		 
*/
	
	
}//OnCreate


	@Override
	  public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
	    Log.d(LOG_TAG, "onLoadComplete, sampleId = " + sampleId + ", status = " + status);		
	    uartInterface.SetConfig(baudRate, dataBit, stopBit, parity, flowControl);//////////TUK E DOBRE 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings)
		{   
			  sp.play(soundIdShot, 1, 1, 0, 0, 1);
			  // sp.play(soundIdExplosion, 1, 1, 0, 0, 1);
			  super.finish();
			  super.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/////////////////////////////////////////////////////////???????????????????????????????
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.main,container, false);
			return rootView;
		}
	}

////////////////////////////////////////////////////////
///////////////////////	onHomePressed
///////////////////////////////////////////////////////
	//@Override
	public void onHomePressed() {
	
		onBackPressed();		
	}	
////////////////////////////////////////////////////////
///////////////////////	onBackPressed
///////////////////////////////////////////////////////	
	public void onBackPressed() {
	//  super.onBackPressed();
		sp.play(soundIDrestricted, 1, 1, 0, 0, 1);
	 
	    	    
	}	
////////////////////////////////////////////////////////
///////////////////////	onResume
///////////////////////////////////////////////////////			
	@Override
	protected void onResume() {
		// Ideally should implement onResume() and onPause()
		// to take appropriate action when the activity looses focus
		super.onResume();		
		
		if( 2 == uartInterface.ResumeAccessory() )
		{
			//cleanPreference();
			//restorePreference();
		}
	}
    void msgToast(String str, int showTime)
    {
    	Toast.makeText(global_context, str, showTime).show();
    }	
////////////////////////////////////////////////////////
/////////////////////// onPause
///////////////////////////////////////////////////////	
	@Override
	protected void onPause() {
		// Ideally should implement onResume() and onPause()
		// to take appropriate action when the activity looses focus
	 	super.onPause();				
	}
////////////////////////////////////////////////////////
/////////////////////// onStop
///////////////////////////////////////////////////////	
	@Override
	protected void onStop() {
		// Ideally should implement onResume() and onPause()
		// to take appropriate action when the activity looses focus	
	 super.onStop();
		
	}
////////////////////////////////////////////////////////
///////////////////////	onDestroy
///////////////////////////////////////////////////////	
	@Override
	protected void onDestroy() {
		uartInterface.DestroyAccessory(bConfiged);
		super.onDestroy();
	}
////////////////////////////////////////////////////////
///////////////////////	Handler
///////////////////////////////////////////////////////	
	final Handler handler = new Handler() 
	{
		@Override
		public void handleMessage(Message msg) 
		{
			
			for(int i=0; i<actualNumBytes[0]; i++)
			{
				readBufferToChar[i] = (char)readBuffer[i];
			}
		

		        switch (readBuffer[0]) {
		            case 1:   sp.play(soundIdShot, 1, 1, 0, 0, 1);      break;		            
		            case 2:   sp.play(soundIdExplosion, 1, 1, 0, 0, 1); break;
		           
		            default:                   break;
		        }

		}
	};

	/* usb input data handler */
	private class handler_thread extends Thread {
		Handler mHandler;

		/* constructor */
		handler_thread(Handler h) {
			mHandler = h;
		}

		public void run() 
		{
			Message msg;

			while (true) 
			{
				
				try {
					Thread.sleep(100);
				    }catch (InterruptedException e) 
				    {
				    	
				    }
				  
				status = uartInterface.ReadData(8, readBuffer,actualNumBytes);
				  
				if (status == 0x00 && actualNumBytes[0] > 0) 
				{
					
					msg = mHandler.obtainMessage();
					mHandler.sendMessage(msg);
				};

			}

			
	       }//Rum
	}//Thread

 
	
	
	
	
	
	///////////////////////////////////////////bye
}

/*
 * 
 * 		  
          new AlertDialog.Builder(this)
	      .setIcon(android.R.drawable.ic_dialog_alert)
	      .setTitle("Closing Activity")
	      .setMessage("Are you sure you want to close this activity?")      
	      .setPositiveButton("Yes", new DialogInterface.OnClickListener()
	  {
	      @Override
	      public void onClick(DialogInterface dialog, int which) {
	          finish();    
	      }

	  })
	  .setNegativeButton("No", null)
	  .show();	
  
 * */



/*
 * AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Title");
		alert.setMessage("Message");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		
	public void onClick(DialogInterface dialog, int whichButton) {
		  String value = input.getText().toString();
		  // Do something with value!
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});

		alert.show();
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * */			
       