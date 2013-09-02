package com.ledpixelart.pixelopenxc;
 
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import alt.android.os.CountDownTimer;

import com.openxc.VehicleManager;
import com.openxc.measurements.Measurement;

import com.openxc.measurements.IgnitionStatus;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleButtonEvent;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.measurements.BrakePedalStatus;
import com.openxc.measurements.SteeringWheelAngle;
import com.openxc.measurements.AcceleratorPedalPosition;
import com.openxc.measurements.FuelLevel;
import com.openxc.measurements.FuelConsumed;
import com.openxc.measurements.TurnSignalStatus;
import com.openxc.measurements.HeadlampStatus;
import com.openxc.measurements.HighBeamStatus;
import com.openxc.measurements.WindshieldWiperStatus;
import com.openxc.measurements.Odometer;
import com.openxc.measurements.VehicleDoorStatus;
import com.openxc.measurements.TransmissionGearPosition;
import com.openxc.remote.VehicleServiceException;
import com.openxc.units.Boolean;
import com.openxc.units.Percentage;
import com.openxc.units.State;
import com.openxc.util.Range;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalInput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.telephony.SmsManager;
 
public class MainActivity extends IOIOActivity implements TextToSpeech.OnInitListener  {
	
	private VehicleManager mVehicleManager;
	private TextView mVehicleSpeedView;
	private TextView mVehicleBrakeView;
	private TextView mVehicleSteeringWheelView;
	private TextView mVehiclePedalView;
	
	private TextView mVehicleFuelConsumedView;
	private TextView mVehicleFuelLevelView;
	private TextView mVehicleTurnSignalView;
	private TextView mVehicleLightsView;
	private TextView mVehicleHighBeamsView;
	private TextView mVehicleWipersView;
	private TextView mVehicleOdometerView;
	private TextView mVehicleDoorView;
	private TextView mVehicleGearView;
	private TextView mVehicleButtonView;
	private TextView proxSensorView;
	private TextView button1View;
	private TextView button2View;
	private TextView tripCostView;
	private TextView ignitionStatusView;
	//buttonOne

	
	private AcceleratorPedalPosition _pedal;
	private int pedalInt;
	private int pedalRange;
	
	///*** stuff for PIXEL ******

  	private ioio.lib.api.RgbLedMatrix.Matrix KIND;  //have to do it this way because there is a matrix library conflict
	private android.graphics.Matrix matrix2;
    private static final String LOG_TAG = "pixelopenxc";	
    private short[] frame_ = new short[512];
  	public static final Bitmap.Config FAST_BITMAP_CONFIG = Bitmap.Config.RGB_565;
  	private byte[] BitmapBytes;
  	private int [] gearArray2;
  	private InputStream BitmapInputStream;
  	private Bitmap canvasBitmap;
  	private int width_original;
  	private int height_original; 	  
  	private float scaleWidth; 
  	private float scaleHeight; 	  	
  	private Bitmap resizedBitmap; 
  	private SharedPreferences prefs;
	private String OKText;
	private Resources resources;
	private String app_ver;	
	private int matrix_model;
	
	private int proxPinNumber = 32;
	
	///********** Timers
	private ConnectTimer connectTimer; 
	private PedalTimer pedalTimer; 
	//private Button1Timer button1Timer;
	//private Button2Timer button2Timer;
	//****************
	
	private String setupInstructionsString; 
	private String setupInstructionsStringTitle;
	private boolean noSleep = false;
	protected Button _button;
	protected ImageView _image;
	protected TextView _field;
	protected String _path;
	protected boolean _taken;
	protected static final String PHOTO_TAKEN	= "photo_taken";
	private static final int PICTURE_RESULT = 0;
    private Display display;
    private String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
    private int proxSensorEnabled = 0;
    
    private int pixelFound = 0; //this is our flag telling the app if pixel was found, very important as we can't do anything with the timers and led display until this has been found
    
    //private String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
    private String basepath = extStorageDirectory;
    
    private Context context;
    private boolean debug_;
    private int appAlreadyStarted = 0;
    private ioio.lib.api.RgbLedMatrix matrix_;
    private Bitmap bitmap;
    
    private int pedalPriority = 1;
    private int wipersPriority = 2;
    private int brakePriority = 3;
    private int turnPriority = 4;
    private int voicePriority = 5;
    private int fuPriority = 6;
    private int tonguePriority = 7;
    private int thanksPriority = 8;
    private int proxPriority = 9;
    private int rapidBrakePriority = 10;
    private int currentPriority = 0;
    
    private int proxRunningFlag = 0;
	private int pedalTimerRunning = 0;
	private int button1TimerRunning = 0;
	private int button2TimerRunning = 0;
	private int rapidBrakeTimerRunning = 0;
	private int tongueTimerRunning = 0;
    
    final Handler _pedalHandler = new Handler();
    private int i = 0;
    private int z = 0;
    private int v = 0;
    private int g = 0; //for the gas consumed piece
    private int g2 = 0;
    private int p = 0; //used for gear
    private int lastGear = 0;
    private int currentGear = 0;
    
    private int[] gearArray = new int[1000];
    
    private float _gasGallonCost;
    
    private int rapidBrakeInterval = 1;
    private int rapidBrakeRate = 2;
    private int rapidBrakeDisplayTime = 3;
    
    private float _gasTickSoundInterval;
    private int _gasGallonConsumedSoundInterval;
    
    private Timer _pedalTimer;
    private Timer _birdTimer;
    private Timer _thxTimer;
    private Timer _rapidBrakeTimer;
    private Timer _tongueTimer;
    private Timer _highSpeedTimer;
    
    private float proxValue;
  
  //  private double[] speedArray;
    private double speed;
    private double currentSpeed;
    private double previousSpeed;
    private double speedDelta;
    private double TripBaselineGas;
    private double TripGasConsumed;
    
    private double TripBaselineGas2;
    private double TripGasConsumed2;
    
    private Button _thanksButton;
    private Button _fuButton;
    private Button _tongueButton;
    private Button _tripCostButton;
    
    private SoundPool mSoundPool;
    private AudioManager  mAudioManager;
    private HashMap<Integer, Integer> mSoundPoolMap;
    private int mStream1 = 0;
    private int mStream2 = 0;
    final static int LOOP_1_TIME = 0;
    final static int LOOP_3_TIMES = 2;
    
    final static int ACCEL1 = 1;
    final static int ACCEL2 = 2;
    final static int ACCEL3 = 3;
    final static int ACCEL4 = 4;
    final static int ACCEL5 = 5;
    final static int ACCEL6 = 6;
    final static int ACCEL7 = 7;
    final static int ACCEL8 = 8;
    final static int ACCEL9 = 9;
    final static int ACCEL10 = 10;
    final static int ACCEL11 = 11;
    final static int JETSONS_START = 12;
    final static int JETSONS_RUNNING = 13;
    final static int THANKS = 14;
    final static int BIRD = 15;
    final static int TONGUE = 16;
    final static int GAS_DROP = 17;
    final static int DANGER = 18;
    final static int SHIFT_DOWN = 19;
    final static int SHIFT_UP = 20;
    final static int LIGHTS_ON = 21;
    final static int LIGHTS_OFF = 22;
    final static int HIGHBEAM_ON = 23;
    final static int BUBBLES = 24;
    final static int POWERUP = 25;
    final static int POWERUP2 = 26;
    final static int RAYGUN = 27;
    final static int ALERT = 27;
    
    private float  streamVolume;
    
    private boolean buttonSounds = true;
    private boolean acceleratorSounds = false;
    
    private boolean _pedalSound;
    private boolean _msgSound;
    private boolean _gearSound;
    private boolean _gasConsumedSound;
    private boolean _HeadlightSound;
    private boolean _highBeamSound;
    private boolean _highSpeedAlarmSound;
    private boolean _ignitionSound;
    private boolean _wipersSound;
    private boolean _rapidDecelerationSound;
    
    private boolean _enablePedal;
    private boolean _enableIOIOButtons;
    private boolean _enablePIXEL;
    private boolean _highSpeedSMS;
    private int _highSpeedSMSThreshold;
    
   // private int _speedLimitThreshold;
    private int _rapidBrakeInterval;
    private int _rapidBrakeRate;
    private int _rapidBrakeDisplayTime;
    
    private TextToSpeech tts;
    private static final int MY_DATA_CHECK_CODE = 1234;
    
    private String gasCostString = null;
	private String gasConsumedString = null;
	private boolean highSpeedRunningFlag = false;
	
	private SmsManager sms = SmsManager.getDefault();
	private String _highSpeedSMSTextNumber;
	private String gearString;
	private String ignitionString;
	private double odometerValue;
    
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //force only portrait mode
		
		 this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
	        
        try
        {
            app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        }
        catch (NameNotFoundException e)
        {
            Log.v(LOG_TAG, e.getMessage());
        }
        
        //******** preferences code
        resources = this.getResources();
        setPreferences();
        //***************************
        
        if (noSleep == true) {        	      	
        	this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //disables sleep mode
        }	
        
        connectTimer = new ConnectTimer(30000,5000); //pop up a message if PIXEL is not found within 30 seconds
 		connectTimer.start(); 
 		
 	//	button1Timer = new Button1Timer(5000,100); //button timer
 	//	button2Timer = new Button2Timer(5000,100); //
 		
 		//pedalTimer = new PedalTimer(30000,1000); 
 		
 		setupInstructionsString = getResources().getString(R.string.setupInstructionsString);
        setupInstructionsStringTitle = getResources().getString(R.string.setupInstructionsStringTitle);
        
        context = getApplicationContext();
		
		mVehicleSpeedView = (TextView) findViewById(R.id.vehicle_speed);
		mVehicleBrakeView = (TextView) findViewById(R.id.brake_status);
		mVehicleSteeringWheelView = (TextView) findViewById(R.id.steering_wheel);
		mVehiclePedalView = (TextView) findViewById(R.id.pedal_position);
		mVehicleFuelConsumedView = (TextView) findViewById(R.id.fuel_consumed);
		mVehicleFuelLevelView = (TextView) findViewById(R.id.fuel_level);
		mVehicleTurnSignalView = (TextView) findViewById(R.id.turn_signal);
		mVehicleLightsView = (TextView) findViewById(R.id.lights);
		mVehicleHighBeamsView = (TextView) findViewById(R.id.highbeams);
		mVehicleWipersView = (TextView) findViewById(R.id.wipers);
		mVehicleOdometerView = (TextView) findViewById(R.id.odometer);
		mVehicleDoorView = (TextView) findViewById(R.id.door);
		mVehicleGearView = (TextView) findViewById(R.id.gear);
		mVehicleButtonView = (TextView) findViewById(R.id.button_status);
		proxSensorView = (TextView) findViewById(R.id.proxSensor);
		
		button1View = (TextView) findViewById(R.id.buttonOne);
		button2View = (TextView) findViewById(R.id.buttonTwo);
		
		 _thanksButton = (Button) findViewById(R.id.thanks_button);
		 _fuButton = (Button) findViewById(R.id.fu_button);
		 _tongueButton = (Button) findViewById(R.id.tongue_button);
		 _tripCostButton = (Button) findViewById(R.id.tripCostButton);
		 
		 tripCostView = (TextView) findViewById(R.id.tripCost);
		 ignitionStatusView = (TextView) findViewById(R.id.ignitionStatus);
		
		Intent intent = new Intent(this, VehicleManager.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);  
        
        mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        mSoundPoolMap = new HashMap();
        //load fx
        mSoundPoolMap.put(JETSONS_START, mSoundPool.load(this, R.raw.jetsons_startup, 1));
        mSoundPoolMap.put(JETSONS_RUNNING, mSoundPool.load(this, R.raw.jetsons_running, 1));
        
        mSoundPoolMap.put(ACCEL1, mSoundPool.load(this, R.raw.accel1, 1));
        mSoundPoolMap.put(ACCEL2, mSoundPool.load(this, R.raw.accel2, 1));
        mSoundPoolMap.put(ACCEL3, mSoundPool.load(this, R.raw.accel3, 1));
        mSoundPoolMap.put(ACCEL4, mSoundPool.load(this, R.raw.accel4, 1));
        mSoundPoolMap.put(ACCEL5, mSoundPool.load(this, R.raw.accel5, 1));
        mSoundPoolMap.put(ACCEL6, mSoundPool.load(this, R.raw.accel6, 1));
        mSoundPoolMap.put(ACCEL7, mSoundPool.load(this, R.raw.accel7, 1));
        mSoundPoolMap.put(ACCEL8, mSoundPool.load(this, R.raw.accel8, 1));
        mSoundPoolMap.put(ACCEL9, mSoundPool.load(this, R.raw.accel9, 1));
        mSoundPoolMap.put(ACCEL10, mSoundPool.load(this, R.raw.accel10, 1));
        mSoundPoolMap.put(ACCEL11, mSoundPool.load(this, R.raw.accel11, 1));
        
        mSoundPoolMap.put(THANKS, mSoundPool.load(this, R.raw.uthanks, 1));
        mSoundPoolMap.put(BIRD, mSoundPool.load(this, R.raw.ubird, 1));
        mSoundPoolMap.put(TONGUE, mSoundPool.load(this, R.raw.utongue, 1));
        
        mSoundPoolMap.put(GAS_DROP, mSoundPool.load(this, R.raw.gas_drop, 1));
        mSoundPoolMap.put(DANGER, mSoundPool.load(this, R.raw.danger, 1));
        
        mSoundPoolMap.put(SHIFT_DOWN, mSoundPool.load(this, R.raw.shiftdown, 1));
        mSoundPoolMap.put(SHIFT_UP, mSoundPool.load(this, R.raw.shiftup, 1));
        
        mSoundPoolMap.put(LIGHTS_ON, mSoundPool.load(this, R.raw.lights, 1));
        mSoundPoolMap.put(LIGHTS_OFF, mSoundPool.load(this, R.raw.lights, 1));
        mSoundPoolMap.put(HIGHBEAM_ON, mSoundPool.load(this, R.raw.highbeam, 1));
        mSoundPoolMap.put(BUBBLES, mSoundPool.load(this, R.raw.bubbles, 1));
        
        mSoundPoolMap.put(POWERUP, mSoundPool.load(this, R.raw.powerup, 1));
        mSoundPoolMap.put(POWERUP2, mSoundPool.load(this, R.raw.powerup2, 1));
        mSoundPoolMap.put(RAYGUN, mSoundPool.load(this, R.raw.raygun, 1));
        mSoundPoolMap.put(ALERT, mSoundPool.load(this, R.raw.alert, 1));
        
        
    	streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
      
       // mStream2= mSoundPool.play(mSoundPoolMap.get(JETSONS_RUNNING), streamVolume, streamVolume, 1, LOOP_3_TIMES, 1f);
        
        
        _thanksButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				playThanksAnimation();
			}
        });
        
        _fuButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				playFUAnimation();
			}
        });
        
        _tongueButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				playTongueAnimation();
			}
        });
        
        _tripCostButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				speakTripCost();
			}
        });
        
        
        
        
        
	}
    
    private void playThanksAnimation() {
    	 if (thanksPriority >= currentPriority && pixelFound == 1 && button1TimerRunning == 0 ) {						
			 currentPriority = thanksPriority; 
			 button1TimerRunning = 1; 
			// showButton1("on");	
		   	  
		   	if (pedalTimerRunning == 1) { //now let's check if the timer is running and start it if not
    			_pedalTimer.cancel();
    			pedalTimerRunning = 0;
    			Log.w("openxc", "button 1 thx animation killed the pedal timer"); 
    		}
		   	
			if (button2TimerRunning == 1) { //check if the button2 timer is running and kill if yes
    			_birdTimer.cancel();
    			button2TimerRunning = 0;
    			Log.w("openxc", "button 1 thx animation killed the button 2 bird animation"); 
    		}
			
			if (tongueTimerRunning == 1) { 
	   			_tongueTimer.cancel();
	   			tongueTimerRunning = 0;
			}
			
			if (buttonSounds == true) {
		 		//mSoundPool.stop(mStream1);
		 		//mStream1 = mSoundPool.play(mSoundPoolMap.get(THANKS), streamVolume, streamVolume, 1, LOOP_1_TIME, 1f);
		 		tts.setLanguage(Locale.getDefault()); //let's set the language before talking, we do this dynamically as it can change mid stream
		    	tts.speak("You just said thanks", TextToSpeech.QUEUE_FLUSH, null);  
		 	}
		   
		   	i = 0;
		   	z = 0;
		    _thxTimer = new Timer();
	        _thxTimer.schedule(new TimerTask() {
	                @Override
	                public void run() {UpdateThx();}
	      }, 0, 100);
		}
    }
    
    private void playFUAnimation() {
    	 if (fuPriority >= currentPriority && pixelFound == 1 && button2TimerRunning == 0 ) {	
			  
    		// showButton2("on");
			  currentPriority = fuPriority; 
			  button2TimerRunning = 1;
		   	  
		   	if (pedalTimerRunning == 1) { //now let's check if the timer is running and start it if not
	   			_pedalTimer.cancel();
	   			pedalTimerRunning = 0;
	   			Log.w("openxc", "button 2 bird animation killed the pedal timer"); 
		   	}
		   	
			if (button1TimerRunning == 1) { //check if the button2 timer is running and kill if yes
	   			_thxTimer.cancel();
	   			button1TimerRunning = 0;
	   			Log.w("openxc", "button 2 bird animation killed the button 1 thx animation"); 
			}
			
			if (tongueTimerRunning == 1) { 
	   			_tongueTimer.cancel();
	   			tongueTimerRunning = 0;
			}
			
		 	if (buttonSounds == true) {
		 		//mSoundPool.stop(mStream1);
		 		//mStream1 = mSoundPool.play(mSoundPoolMap.get(BIRD), streamVolume, streamVolume, 1, LOOP_1_TIME, 1f);
		 		tts.setLanguage(Locale.getDefault()); //let's set the language before talking, we do this dynamically as it can change mid stream
		    	tts.speak("You just gave that asshole the bird", TextToSpeech.QUEUE_FLUSH, null);  
		 	}
			
			 i = 0; 
			  _birdTimer = new Timer();
		          _birdTimer.schedule(new TimerTask() {
		                @Override
		                public void run() {UpdateBird();}
		     }, 0, 100);
		 }
    }
    
    private void playTongueAnimation() {
   	 if (tonguePriority >= currentPriority && pixelFound == 1 && tongueTimerRunning == 0 ) {						
			 currentPriority = tonguePriority; 
			 tongueTimerRunning = 1; 
			// showButton1("on");	 
		   	  
		   	if (pedalTimerRunning == 1) { //now let's check if the timer is running and start it if not
	   			_pedalTimer.cancel();
	   			pedalTimerRunning = 0;
		   	}
		   	
			if (button2TimerRunning == 1) { //check if the button2 timer is running and kill if yes
	   			_birdTimer.cancel();
	   			button2TimerRunning = 0;
			}
			
			if (button1TimerRunning == 1) { //check if the button2 timer is running and kill if yes
	   			_thxTimer.cancel();
	   			button1TimerRunning = 0;
			}
			
			if (buttonSounds == true) {
		 		//mSoundPool.stop(mStream1);
		 		//mStream1 = mSoundPool.play(mSoundPoolMap.get(TONGUE), streamVolume, streamVolume, 1, LOOP_1_TIME, 1f);
		 		tts.setLanguage(Locale.getDefault()); //let's set the language before talking, we do this dynamically as it can change mid stream
		    	tts.speak("You're sticking out your tongue", TextToSpeech.QUEUE_FLUSH, null);  
		 	}
			
		   	i = 0;
		   	z = 0;
		    _tongueTimer = new Timer();
	        _tongueTimer.schedule(new TimerTask() {
	                @Override
	                public void run() {UpdateTongue();}
	      }, 0, 100);
		   	  
	   	  
		 }
   }
    
    private void WriteImagetoMatrix(Bitmap originalPhoto) throws ConnectionLostException {  //here we'll take the photo and resize it to 32x32 and then write to PIXEL
	     
        
   	 //the camera photo is going to be larger than PIXEL's 32x32 resolution so we'll need to scale the photo to 32x32
		 width_original = originalPhoto.getWidth();
		 height_original = originalPhoto.getHeight();
		 scaleWidth = ((float) KIND.width) / width_original;
	 	 scaleHeight = ((float) KIND.height) / height_original;
	 	 		
		 // create matrix for the manipulation
		 matrix2 = new Matrix();
		 // resize the bitmap
		 matrix2.postScale(scaleWidth, scaleHeight);
		 resizedBitmap = Bitmap.createBitmap(originalPhoto, 0, 0, width_original, height_original, matrix2, true);
		 canvasBitmap = Bitmap.createBitmap(KIND.width, KIND.height, Config.RGB_565); 
		 Canvas canvas = new Canvas(canvasBitmap);
		 canvas.drawRGB(0,0,0); //a black background
	   	 canvas.drawBitmap(resizedBitmap, 0, 0, null);
		 ByteBuffer buffer = ByteBuffer.allocate(KIND.width * KIND.height *2); //Create a new buffer
		 canvasBitmap.copyPixelsToBuffer(buffer); //copy the bitmap 565 to the buffer		
		 BitmapBytes = buffer.array(); //copy the buffer into the type array
		 
		 loadImage();  
		 matrix_.frame(frame_);  //write to the matrix   
} 
   
   protected void onDestroy() {
       super.onDestroy();
       connectTimer.cancel();  //if user closes the program, need to kill this timer or we'll get a crash
     //  _pedalTimer.cancel();
     //  _birdTimer.cancel();
     //  _thxTimer.cancel();
     //  _rapidBrakeTimer.cancel();
   }
   
   private void UpdateRapidBrake()  {
       i++;
			
		if (i == _rapidBrakeDisplayTime) {  //how long to display the rapidBrake image
				_rapidBrakeTimer.cancel();
	       		i = 0;
	       		rapidBrakeTimerRunning = 0;
				currentPriority = 0;
				try {
					clearMatrixImage(); //don't forget to clear as if we're at 0 speed and the brake was on, this image will stay there
				} catch (ConnectionLostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
       	
    }

//final Runnable rapidBrakeRunnable = new Runnable() {
  // public void run() {
	   
	//   BitmapInputStream = getResources().openRawResource(birdAnimation[i]);
		 
		//   Log.w("openxc", "loading bird animation frame: " + String.valueOf(i)); 
           
       //loadRGB565(); //this function loads a raw RGB565 image to the matrix
	 	// try {
		//	matrix_.frame(frame_);
		//} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}  //write to the matrix  
  // }
//};
   
   private void UpdateTongue() {
       i++;
		if (i > 48) {  //hold the image for 3 seconds and then set back to normal
		 		_tongueTimer.cancel();
		 		try {
					clearMatrixImage();
				} catch (ConnectionLostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	       		i = 0;
	       		tongueTimerRunning = 0;
				currentPriority = 0;
		}
      
       else {
    	   _pedalHandler.post(TongueRunnable);
       }
       	
    }

final Runnable TongueRunnable = new Runnable() {
   public void run() {
	  
	   int[] tongueAnimation = {R.raw.tongue0, R.raw.tongue1,R.raw.tongue2, R.raw.tongue3,R.raw.tongue4, R.raw.tongue5,R.raw.tongue6, R.raw.tongue7,R.raw.tongue8, R.raw.tongue9,R.raw.tongue10, R.raw.tongue11,R.raw.tongue12, R.raw.tongue13,R.raw.tongue14,R.raw.tongue15,R.raw.tongue16, R.raw.tongue17,R.raw.tongue18, R.raw.tongue19,R.raw.tongue20, R.raw.tongue21,R.raw.tongue22, R.raw.tongue23,R.raw.tongue24, R.raw.tongue25,R.raw.tongue26, R.raw.tongue27,R.raw.tongue28, R.raw.tongue29,R.raw.tongue30, R.raw.tongue31,R.raw.tongue32, R.raw.tongue33,R.raw.tongue34,R.raw.tongue35,R.raw.tongue36, R.raw.tongue37,R.raw.tongue38, R.raw.tongue39,R.raw.tongue40, R.raw.tongue41,R.raw.tongue42, R.raw.tongue43,R.raw.tongue44, R.raw.tongue45,R.raw.tongue46, R.raw.tongue47,R.raw.tongue48};
	   BitmapInputStream = getResources().openRawResource(tongueAnimation[i]);
		 
		   Log.w("openxc", "loading tongue animation frame: " + String.valueOf(i)); 
           
       loadRGB565(); //this function loads a raw RGB565 image to the matrix
	 	 try {
			matrix_.frame(frame_);
		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  //write to the matrix  
   }
};

   
   private void UpdateBird() {
	       i++;
	       if (i > 20) {  //then the animation is done so let's not keep playing
				
				if (i == 60) {  //hold the image for 3 seconds and then set back to normal
				 		_birdTimer.cancel();
				 		try {
							clearMatrixImage();
						} catch (ConnectionLostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			       		i = 0;
			       		button2TimerRunning = 0;
						currentPriority = 0;
				}
	       }	
	       else {
	    	   _pedalHandler.post(BirdRunnable);
	       }
	       	
	    }

    final Runnable BirdRunnable = new Runnable() {
       public void run() {
    	  
    	   int[] birdAnimation = {R.raw.bird0, R.raw.bird1,R.raw.bird2, R.raw.bird3,R.raw.bird4, R.raw.bird5,R.raw.bird6, R.raw.bird7,R.raw.bird8, R.raw.bird9,R.raw.bird10, R.raw.bird11,R.raw.bird12, R.raw.bird13,R.raw.bird14,R.raw.bird15,R.raw.bird16, R.raw.bird17,R.raw.bird18, R.raw.bird19,R.raw.bird20};
    	   BitmapInputStream = getResources().openRawResource(birdAnimation[i]);
  		 
   		   Log.w("openxc", "loading bird animation frame: " + String.valueOf(i)); 
               
           loadRGB565(); //this function loads a raw RGB565 image to the matrix
		 	 try {
				matrix_.frame(frame_);
			} catch (ConnectionLostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  //write to the matrix  
       }
    };
    
    
    
	    private void UpdateThx() {  //this is an 9 frame animation we will loop 6 times
		       i++;
		       z++;
		       
		       if (z == 50) {  //we've play it long enough so let's end
			 		_thxTimer.cancel();
			 		try {
						clearMatrixImage();
					} catch (ConnectionLostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		       		i = 0;
		       		z = 0;
		       		button1TimerRunning = 0;
					currentPriority = 0;
		       }
		       
		       else {
		    	   if (i > 8) {  //must reset the frame counter as it's only 8 frames
					i = 0;
			       }	
		    	   _pedalHandler.post(ThxRunable);
		       }
		       	
		    }

		    final Runnable ThxRunable = new Runnable() {
		       public void run() {
		    	  
		    	   int[] thxAnimation = {R.raw.hand_thx0, R.raw.hand_thx1,R.raw.hand_thx2, R.raw.hand_thx3,R.raw.hand_thx4, R.raw.hand_thx5,R.raw.hand_thx6, R.raw.hand_thx7,R.raw.hand_thx8};
		    	   BitmapInputStream = getResources().openRawResource(thxAnimation[i]);
		  		 
		   		   Log.w("openxc", "loading thx animation frame: " + String.valueOf(i)); 
		               
		           loadRGB565(); //this function loads a raw RGB565 image to the matrix
				 	 try {
						matrix_.frame(frame_);
					} catch (ConnectionLostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}  //write to the matrix  
		       }
		    };
   
   
   private void UpdatePedalBars() {
      // i++;
       
       
       _pedalHandler.post(myRunnable);
      
       //if (i == 10) {
       //	_pedalTimer.cancel();
       //	i = 0;
       	
       	//  _pedalTimer = new Timer();
         // _pedalTimer.schedule(new TimerTask() {
          //      @Override
           //     public void run() {UpdatePedalBars();}
           //  }, 0, 1000);
       //}
     
    }

    final Runnable myRunnable = new Runnable() {
       public void run() {
               
    	   BitmapInputStream = getResources().openRawResource(R.raw.m1); //just in case no match below
   		
  		 
   		//   Log.w("openxc", String.valueOf(pedalRange)); 
             
             switch (pedalRange) {
   			
   		 	case 1:					
   		 		BitmapInputStream = getResources().openRawResource(R.raw.m1);
   		 		break;					
   		 	case 2:		
   		 		BitmapInputStream = getResources().openRawResource(R.raw.m2);
   		 		break;		
   		 	case 3:				
   		 		BitmapInputStream = getResources().openRawResource(R.raw.m3);
   		 		break;		
   		 	case 4:				
   		 		BitmapInputStream = getResources().openRawResource(R.raw.m4);
   		 		break;		
   		 	case 5:					
   		 		BitmapInputStream = getResources().openRawResource(R.raw.m5);
   		 		break;	
   		 	case 6:					
   		 		BitmapInputStream = getResources().openRawResource(R.raw.m6);
   		 		break;					
   		 	case 7:		
   		 		BitmapInputStream = getResources().openRawResource(R.raw.m7);
   		 		break;		
   		 	case 8:				
   		 		BitmapInputStream = getResources().openRawResource(R.raw.m8);
   		 		break;		
   		 	case 9:				
   		 		BitmapInputStream = getResources().openRawResource(R.raw.m9);
   		 		break;		
   		 	case 10:					
   		 		BitmapInputStream = getResources().openRawResource(R.raw.m10);
   		 		break;	
   		 	case 11:					
   		 		BitmapInputStream = getResources().openRawResource(R.raw.m11);
   		 		break;	
   		 	default:
   		 		BitmapInputStream = getResources().openRawResource(R.raw.m1);
   	   }
               
              loadRGB565(); //this function loads a raw RGB565 image to the matrix
			 	 try {
					matrix_.frame(frame_);
				} catch (ConnectionLostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  //write to the matrix   
       }
    };
   
   
	public class PedalTimer extends CountDownTimer
	{
	public PedalTimer(long startTime, long interval)					
	
			{
				super(startTime, interval);
			}

		@Override
		public void onFinish()
			{
				
			pedalTimer.start();
			
			
			}

		@Override
		public void onTick(long millisUntilFinished)				{						
			
				
			
			BitmapInputStream = getResources().openRawResource(R.raw.m1); //just in case no match below
			
				 
			//   Log.w("openxc", String.valueOf(pedalRange)); 
                
                switch (pedalRange) {
				
			 	case 1:					
			 		BitmapInputStream = getResources().openRawResource(R.raw.m1);
			 		break;					
			 	case 2:		
			 		BitmapInputStream = getResources().openRawResource(R.raw.m2);
			 		break;		
			 	case 3:				
			 		BitmapInputStream = getResources().openRawResource(R.raw.m3);
			 		break;		
			 	case 4:				
			 		BitmapInputStream = getResources().openRawResource(R.raw.m4);
			 		break;		
			 	case 5:					
			 		BitmapInputStream = getResources().openRawResource(R.raw.m5);
			 		break;	
			 	case 6:					
			 		BitmapInputStream = getResources().openRawResource(R.raw.m6);
			 		break;					
			 	case 7:		
			 		BitmapInputStream = getResources().openRawResource(R.raw.m7);
			 		break;		
			 	case 8:				
			 		BitmapInputStream = getResources().openRawResource(R.raw.m8);
			 		break;		
			 	case 9:				
			 		BitmapInputStream = getResources().openRawResource(R.raw.m9);
			 		break;		
			 	case 10:					
			 		BitmapInputStream = getResources().openRawResource(R.raw.m10);
			 		break;	
			 	case 11:					
			 		BitmapInputStream = getResources().openRawResource(R.raw.m11);
			 		break;	
			 	default:
			 		BitmapInputStream = getResources().openRawResource(R.raw.m1);
		   }
                
                
               loadRGB565(); //this function loads a raw RGB565 image to the matrix
			 	 try {
					matrix_.frame(frame_);
				} catch (ConnectionLostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  //write to the matrix   
			 	 
			 	//meterMatch = 0; //reset this one
						 
						//if (BitmapInputStream == null) {
						//	BitmapInputStream = getResources().openRawResource(R.raw.m1); //just in case no match below
						//}
						 
			    }
		}
  
   
   public void loadImage() {

 		int y = 0;
 		for (int i = 0; i < frame_.length; i++) {
 			frame_[i] = (short) (((short) BitmapBytes[y] & 0xFF) | (((short) BitmapBytes[y + 1] & 0xFF) << 8));
 			y = y + 2;
 		}
 		
 		//we're done with the images so let's recycle them to save memory, not sure if this is really needed but hey what the heck
	    canvasBitmap.recycle();
	    resizedBitmap.recycle(); //only there if we had to resize an image
 	}
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu) 
   {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.mainmenu, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected (MenuItem item)
   {
      
		
     if (item.getItemId() == R.id.menu_instructions) {
	    	AlertDialog.Builder alert=new AlertDialog.Builder(this);
	      	alert.setTitle(setupInstructionsStringTitle).setIcon(R.drawable.icon).setMessage(setupInstructionsString).setNeutralButton(OKText, null).show();
	   }
   	
	  if (item.getItemId() == R.id.menu_about) {
		  
		    AlertDialog.Builder alert=new AlertDialog.Builder(this);
	      	alert.setTitle(getString(R.string.menu_about_title)).setIcon(R.drawable.icon).setMessage(getString(R.string.menu_about_summary) + "\n\n" + getString(R.string.versionString) + " " + app_ver).setNeutralButton(OKText, null).show();	
	   }
   	
   	if (item.getItemId() == R.id.menu_prefs)
      {
   		    		
   		Intent intent = new Intent()
      				.setClass(this,
      						com.ledpixelart.pixelopenxc.preferences.class);   
   				this.startActivityForResult(intent, 5);
      }
   	
      return true;
   }
   
	//now let's get data back from the preferences activity below
   @Override
   public void onActivityResult(int reqCode, int resCode, Intent data) //we'll go into a reset after this
   {
   	super.onActivityResult(reqCode, resCode, data);
   	
   	// if (debug == true) {
   	//	 Toast.makeText(getBaseContext(), "On Activity Result Code: " + reqCode, Toast.LENGTH_LONG).show();
       // }      	
   	
   	setPreferences(); //very important to have this here, after the menu comes back this is called, we'll want to apply the new prefs without having to re-start the app
   	
   	
   	if (reqCode == MY_DATA_CHECK_CODE)
       {
           if (resCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
           {
               // TTS installed, create instance
               tts = new TextToSpeech(this, (OnInitListener) this);
               Context context = getApplicationContext();
               
               if (debug_ == true) {
	                CharSequence text = "Text to Speech Engine is installed";
	                int duration = Toast.LENGTH_SHORT;
	                Toast toast = Toast.makeText(context, text, duration);
	                toast.show();
               }                
           }
           else
           {
               // TTS not installed, install it

               Context context = getApplicationContext();
              
               CharSequence text = "The Android Text to Speech Engine is NOT installed, you will now be prompted to install it";
               int duration = Toast.LENGTH_LONG;
               Toast toast = Toast.makeText(context, text, duration);
               toast.show();

               Intent installIntent = new Intent();
               installIntent.setAction(
                       TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
               startActivity(installIntent);
           }
       }
   	
       	
   	//String extraData=data.getStringExtra("ComingFrom");
   	//debug_.setText(extraData);    	
   	
   	
   }
   	
   
   private void setPreferences() //here is where we read the shared preferences into variables
   {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);     

    noSleep = prefs.getBoolean("pref_noSleep", true);     
    debug_ = prefs.getBoolean("pref_debugMode", false);
    
    _pedalSound = prefs.getBoolean("pref_pedalSound", false);
    _msgSound = prefs.getBoolean("pref_msgSound", false);
    _gearSound = prefs.getBoolean("pref_gearSound", false);
    _gasConsumedSound = prefs.getBoolean("pref_gasConsumedSound", true);
    _HeadlightSound = prefs.getBoolean("pref_HeadlightSound", false);
    _highBeamSound = prefs.getBoolean("pref_highBeamSound", false);
    _highSpeedAlarmSound = prefs.getBoolean("pref_highSpeedAlarmSound", false);
    _ignitionSound = prefs.getBoolean("pref_ignitionSound", false);
    _wipersSound = prefs.getBoolean("pref_wipersSound", false);
    _rapidDecelerationSound = prefs.getBoolean("pref_rapidDecelerationSound", true);
    
    _enablePedal = prefs.getBoolean("pref_enablePedal", true);
    _enableIOIOButtons = prefs.getBoolean("pref_enableIOIOButtons", false);
    _enablePIXEL = prefs.getBoolean("pref_enablePIXEL", true);
    
    _highSpeedSMS = prefs.getBoolean("pref_highSpeedSMS", false);
    
    _highSpeedSMSThreshold = Integer.valueOf(prefs.getString(   
   	        resources.getString(R.string.pref_highSpeedSMSThreshold),
   	        resources.getString(R.string.highSpeedSMSThresholdDefault))); 
    
    _highSpeedSMSTextNumber = prefs.getString(   
   	        resources.getString(R.string.pref_highSpeedSMSTextNumber),
   	        resources.getString(R.string.highSpeedSMSTextNumberDefault));
    
    _rapidBrakeInterval = Integer.valueOf(prefs.getString(   
   	        resources.getString(R.string.pref_rapidBrakeInterval),
   	        resources.getString(R.string.rapidBrakeIntervalDefault))); 
    
    _rapidBrakeRate = Integer.valueOf(prefs.getString(   
   	        resources.getString(R.string.pref_rapidBrakeRate),
   	        resources.getString(R.string.rapidBrakeRateDefault))); 
    
    _rapidBrakeDisplayTime = Integer.valueOf(prefs.getString(   
   	        resources.getString(R.string.pref_rapidBrakeDisplayTime),
   	        resources.getString(R.string.rapidBrakeDisplayTimeDefault))); 
    
     matrix_model = Integer.valueOf(prefs.getString(   //the selected RGB LED Matrix Type
   	        resources.getString(R.string.selected_matrix),
   	        resources.getString(R.string.matrix_default_value))); 
     
     _gasTickSoundInterval = Float.valueOf(prefs.getString(  
    	        resources.getString(R.string.pref_gasTickSoundInterval),
    	        resources.getString(R.string.gasTickSoundIntervalDefault))); 
     
     _gasGallonConsumedSoundInterval = Integer.valueOf(prefs.getString(   
    	        resources.getString(R.string.pref_gasGallonConsumedSoundInterval),
    	        resources.getString(R.string.gasGallonConsumedSoundIntervalDefault))); 
     
     _gasGallonCost = Float.valueOf(prefs.getString(   
 	        resources.getString(R.string.pref_gasGallonCost),
 	        resources.getString(R.string.gasGallonCostDefault))); 
     
     
    
    
    switch (matrix_model) {  //the user can use other LED displays other than PIXEL's by choosing from preferences
    case 0:
   	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x16;
   	 BitmapInputStream = getResources().openRawResource(R.raw.openxcgrey);
   	 break;
    case 1:
   	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.ADAFRUIT_32x16;
   	 BitmapInputStream = getResources().openRawResource(R.raw.openxcgrey);
   	 break;
    case 2:
   	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32_NEW; //v1 , this matrix has 4 IDC connectors
   	 BitmapInputStream = getResources().openRawResource(R.raw.openxcgrey);
   	 break;
    case 3:
   	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32; //v2
   	 BitmapInputStream = getResources().openRawResource(R.raw.openxcgrey);
   	 break;
    default:	    		 
   	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32; //v2 as the default, it has 2 IDC connectors
   	 BitmapInputStream = getResources().openRawResource(R.raw.openxcgrey);
    }
        
    frame_ = new short [KIND.width * KIND.height];
	 BitmapBytes = new byte[KIND.width * KIND.height *2]; //512 * 2 = 1024 or 1024 * 2 = 2048
	 
	 loadRGB565(); //this function loads a raw RGB565 image to the matrix
    
}
     
   
  private void loadRGB565() {
	   
		try {
  			int n = BitmapInputStream.read(BitmapBytes, 0, BitmapBytes.length); // reads
  																				// the
  																				// input
  																				// stream
  																				// into
  																				// a
  																				// byte
  																				// array
  			Arrays.fill(BitmapBytes, n, BitmapBytes.length, (byte) 0);
  		} catch (IOException e) {
  			e.printStackTrace();
  		}

  		int y = 0;
  		for (int i = 0; i < frame_.length; i++) {
  			frame_[i] = (short) (((short) BitmapBytes[y] & 0xFF) | (((short) BitmapBytes[y + 1] & 0xFF) << 8));
  			y = y + 2;
  		}
	   
  }
	
   
   public class ConnectTimer extends CountDownTimer
	{

		public ConnectTimer(long startTime, long interval)
			{
				super(startTime, interval);
			}

		@Override
		public void onFinish()
			{
				if (pixelFound == 0) {
					showNotFound (); 					
				}
				
			}

		@Override
		public void onTick(long millisUntilFinished)				{
			//not used
		}
	}
   
   public class Button1Timer extends CountDownTimer
	{

		public Button1Timer(long startTime, long interval)
			{
				super(startTime, interval);
			}

		@Override
		public void onFinish()
			{
				button1TimerRunning = 0;
				currentPriority = 0;
			}

		@Override
		public void onTick(long millisUntilFinished)				{
			 BitmapInputStream = getResources().openRawResource(R.raw.thanks); //load a blank image to clear it
		   	  loadRGB565();    	
		   	  try {
				matrix_.frame(frame_);
			} catch (ConnectionLostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
   
   public class Button2Timer extends CountDownTimer
	{

		public Button2Timer(long startTime, long interval)
			{
				super(startTime, interval);
			}

		@Override
		public void onFinish()
			{
				button2TimerRunning = 0;
				currentPriority = 0;	
			}

		@Override
		public void onTick(long millisUntilFinished)				{
			 BitmapInputStream = getResources().openRawResource(R.raw.hi); //load a blank image to clear it
		   	  loadRGB565();    	
		   	  try {
				matrix_.frame(frame_);
			} catch (ConnectionLostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void showNotFound() {	
		AlertDialog.Builder alert=new AlertDialog.Builder(this);
		alert.setTitle(getResources().getString(R.string.notFoundString)).setIcon(R.drawable.icon).setMessage(getResources().getString(R.string.bluetoothPairingString)).setNeutralButton(getResources().getString(R.string.OKText), null).show();	
   }
   
   class IOIOThread extends BaseIOIOLooper {

	   
	   private AnalogInput prox_;	
	   private DigitalInput button1_;
	   private DigitalInput button2_;
	   
 		@Override
 		protected void setup() throws ConnectionLostException {
 			matrix_ = ioio_.openRgbLedMatrix(KIND);
 			
 			connectTimer.cancel(); //we can stop this since it was found
 			
 			matrix_.frame(frame_);  //write select pic to the matrix
 			
 			if (debug_ == true) {  			
	  			showToast("Bluetooth Connected");
 			}
 			
 			pixelFound = 1; //if we went here, then we are connected over bluetooth or USB
 			
 			//prox_ = ioio_.openAnalogInput(proxPinNumber);
 			
 			if (_enableIOIOButtons == true) {
	 			button1_ = ioio_.openDigitalInput(3);
	 			button2_ = ioio_.openDigitalInput(5);
 			}
 			
 		//	try {
				//button1_.waitForValue(true);
				//if (button1_ == true ) {
					
				//}
			//} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				//e1.printStackTrace();
			//}
 			
 		
 			
 		//	DigitalInput in = ioio_.openDigitalInput(pinNum);
 			
 			//pedalTimer.start();
 			//pedalTimerRunning = 1;
 		}

 		@Override
 		public void loop() throws ConnectionLostException {
				
				//float proxValue;
			//	if (proxSensorEnabled == 1) {
					try {
						Thread.sleep(200);
					//	proxValue = prox_.read();
					//	 String proxString=Float.toString(proxValue);
					//	 float proxTrigger = proxValue * 1000;
					//	 showProxValue(convertReadingText(proxValue)); 
						 
					//	 if (proxTrigger < 100 && proxRunningFlag == 1) { //this means the object is out of range so let's put things back
					//		 proxRunningFlag = 0;
					//		 currentPriority = 0;
					//	 }
						 
					//	 if (proxTrigger > 100) {
						//	 if (proxPriority >= currentPriority && pixelFound == 1) { //but don't do anything unless it has display priority AND PIXEL HAS BEEN FOUND
							//	    currentPriority = proxPriority;
							//	    proxRunningFlag = 1;
								    
				            	//	if (pedalTimerRunning == 1) { //now let's check if the timer is running and start it if not
				            	//		pedalTimer.cancel();
				            	//		pedalTimerRunning = 0;
				           // 		}
				           // }
						//	 BitmapInputStream = getResources().openRawResource(R.raw.x32); //load a blank image to clear it
						 //  	 loadRGB565();    	
						  // 	 matrix_.frame(frame_);  
					//	 }
						
		 				
						if (_enableIOIOButtons == true) {
						
							boolean button1State = button1_.read(); //let's read the buttons
			 				boolean button2State = button2_.read();
			 				
							if (button1State == true) { //the button was pressed so let's show an image for a set amount of time
								  
								playThanksAnimation();
								
							}
							 else {
								 showButton1("off");	 
							 }
							
							if (button2State == true) { //the button was pressed so let's show an image for a set amount of time
								
								playFUAnimation();
								
								}
							 else {
								 showButton2("off");	 
							 }
						}
						 
						//	button1View = (TextView) findViewById(R.id.buttonOne);
						//	button2View = (TextView) findViewById(R.id.buttonTwo);
						 
						// showProxValue(proxString); 
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
			//	if (show_alcohol_value == true ) {
			  //setBreath writes the alcohol value & convertReading converts the raw decimal value to a whole number from 0 to 1000   
		      //  }
 		
 			//matrix_.frame(frame_); //writes whatever is in bitmap raw 565 file buffer to the RGB LCD
 		//as the loop runs ~30 times a second, it's better to do the writes to the LED matrix outside of this loop for performance reasons
 	 }	
 		
 		@Override
		public void disconnected() {
			Log.i(LOG_TAG, "IOIO disconnected");
			
			if (debug_ == true) {  			
	  			showToast("Bluetooth Disconnected");
 			}
		}

		@Override
		public void incompatible() {  //if the wrong firmware is there
			//AlertDialog.Builder alert=new AlertDialog.Builder(context); //causing a crash
			//alert.setTitle(getResources().getString(R.string.notFoundString)).setIcon(R.drawable.icon).setMessage(getResources().getString(R.string.bluetoothPairingString)).setNeutralButton(getResources().getString(R.string.OKText), null).show();	
			showToast("Incompatbile firmware!");
			showToast("This app won't work until you flash the IOIO with the correct firmware!");
			showToast("You can use the IOIO Manager Android app to flash the correct firmware");
			Log.e(LOG_TAG, "Incompatbile firmware!");
		}
 	}
 		
	public String convertReadingText(float num ) {
		num = num * 1000;
		String numtoString = new DecimalFormat("0").format(num);		
		return(numtoString);
	}
 	
	private void showProxValue(final String str) {
		runOnUiThread(new Runnable() {
			public void run() {
				proxSensorView.setText(str);
			}
		});
	}
	
	private void showButton1(final String str) {
		runOnUiThread(new Runnable() {
			public void run() {
				button1View.setText(str);
			}
		});
	}
	
	private void showButton2(final String str) {
		runOnUiThread(new Runnable() {
			public void run() {
				button2View.setText(str);
			}
		});
	}

 	@Override
 	protected IOIOLooper createIOIOLooper() {
 		return new IOIOThread();
 	}
   
   private void showToast(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast toast = Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG);
               toast.show();
			}
		});
	}  
   
   private void showToastShort(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast toast = Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT);
               toast.show();
			}
		});
	}  
   
     
   private void screenOn() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				WindowManager.LayoutParams lp = getWindow().getAttributes();  //turn the screen back on
				lp.screenBrightness = 10 / 100.0f;  
				//lp.screenBrightness = 100 / 100.0f;  
				getWindow().setAttributes(lp);
			}
		});
	}
   
	public void onPause() {
	    super.onPause();
	  //  Log.i("openxc", "Unbinding from vehicle service");
	  //  unbindService(mConnection);
	}
	
	    
   private void clearMatrixImage() throws ConnectionLostException {
   	//let's claear the image
   	 BitmapInputStream = getResources().openRawResource(R.raw.blank); //load a blank image to clear it
   	 loadRGB565();    	
   	 matrix_.frame(frame_); 
   }
   
   private void writeBrakeImage() throws ConnectionLostException {
	   	//let's claear the image
	   	 BitmapInputStream = getResources().openRawResource(R.raw.footbrake); //load a blank image to clear it
	   	 loadRGB565();    	
	   	 matrix_.frame(frame_); 
	   }
   
   private void writeSuddenBrakeImage() throws ConnectionLostException {
	   	//let's claear the image
	   	 BitmapInputStream = getResources().openRawResource(R.raw.rapid_brake); //load a blank image to clear it
	   	 loadRGB565();    	
	   	 matrix_.frame(frame_); 
	   }
   
	
 
	private ServiceConnection mConnection = new ServiceConnection() {
	    // Called when the connection with the service is established
	    public void onServiceConnected(ComponentName className,
	            IBinder service) {
	        Log.i("openxc", "Bound to VehicleManager");
	        mVehicleManager = ((VehicleManager.VehicleBinder)service).getService();
	        
	        // setting up all the listeners to capture the data we want
	        
	        try {
				mVehicleManager.addListener(VehicleSpeed.class, mSpeedListener);
			} catch (VehicleServiceException e) {
				e.printStackTrace();
			} catch (UnrecognizedMeasurementTypeException e) {
				e.printStackTrace();
			}
	        
	        //add the button listeners !!!!!
	        
	        
	       try {
				mVehicleManager.addListener(BrakePedalStatus.class, mBrakeListener);
			} catch (VehicleServiceException e) {
				e.printStackTrace();
			} catch (UnrecognizedMeasurementTypeException e) {
				e.printStackTrace();
			}
	       
	       try {
				mVehicleManager.addListener(IgnitionStatus.class, mIgnitionListener);
			} catch (VehicleServiceException e) {
				e.printStackTrace();
			} catch (UnrecognizedMeasurementTypeException e) {
				e.printStackTrace();
			}
	        
	 //       try {
	//			mVehicleManager.addListener(SteeringWheelAngle.class, mSteeringWheelListener);
	//		} catch (VehicleServiceException e) {
	//			e.printStackTrace();
	//		} catch (UnrecognizedMeasurementTypeException e) {
	//			e.printStackTrace();
	//		}
	        
	       if (_enablePedal == true) { 
		       try {
					mVehicleManager.addListener(AcceleratorPedalPosition.class, mPedalListener);
				} catch (VehicleServiceException e) {
					e.printStackTrace();
				} catch (UnrecognizedMeasurementTypeException e) {
					e.printStackTrace();
				}
	       }
	       
	       
	       if (_wipersSound == true) {
		       try {
					mVehicleManager.addListener(VehicleButtonEvent.class, mButtonEventWiperListener);
				} catch (VehicleServiceException e) {
					e.printStackTrace();
				} catch (UnrecognizedMeasurementTypeException e) {
					e.printStackTrace();
				}
	       }
	        
	        
	        
	 //       try {
	//			mVehicleManager.addListener(FuelLevel.class, mFuelLevelListener);
	//		} catch (VehicleServiceException e) {
	//			e.printStackTrace();
	//		} catch (UnrecognizedMeasurementTypeException e) {
	//			e.printStackTrace();
	//		}
	       
	   	//  private boolean ;
		 	  //  private boolean _gasConsumedSound;
		 	 // ;
		 	  //  private boolean ;
		 	  //  private boolean _highSpeedAlarmSound;
		 	  //  private boolean _ignitionSound;
		 	  //  private boolean _wipersSound;
		 	
		 	//if (_HeadlightSound == true)  {
	        
	        try {
				mVehicleManager.addListener(FuelConsumed.class, mFuelConsumedListener);
			} catch (VehicleServiceException e) {
				e.printStackTrace();
			} catch (UnrecognizedMeasurementTypeException e) {
				e.printStackTrace();
			}
	        
	        //add some sound effect for fuel consumed, that would be cool!
	        
	        try {
				mVehicleManager.addListener(TurnSignalStatus.class, mTurnSignalListener);
			} catch (VehicleServiceException e) {
				e.printStackTrace();
			} catch (UnrecognizedMeasurementTypeException e) {
				e.printStackTrace();
			}
	        
	        
	       // private boolean _pedalSound;
	       // private boolean _msgSound;
	       // private boolean _gearSound;
	       // private boolean _gasConsumedSound;
	       // private boolean _HeadlightSound;
	       // private boolean _highBeamSound;
	       // private boolean _highSpeedAlarmSound;
	       // private boolean _ignitionSound;
	       // private boolean ;
	        
	       // private boolean _enablePedal;
	       // private boolean _enableIOIOButtons;
	       // private boolean _enablePIXEL;
	       // private boolean _highSpeedSMS;
	       // private int _highSpeedSMSThreshold;
	        
	        if (_HeadlightSound == true) {
		        try {
					mVehicleManager.addListener(HeadlampStatus.class, mHeadLampListener);
				} catch (VehicleServiceException e) {
					e.printStackTrace();
				} catch (UnrecognizedMeasurementTypeException e) {
					e.printStackTrace();
				}
	        }
	        
	        if (_highBeamSound == true) {
		        try {
					mVehicleManager.addListener(HighBeamStatus.class, mHighBeamListener);
				} catch (VehicleServiceException e) {
					e.printStackTrace();
				} catch (UnrecognizedMeasurementTypeException e) {
					e.printStackTrace();
				}
	        }
	        
	        if (_wipersSound == true) {
		        try {
					mVehicleManager.addListener(WindshieldWiperStatus.class, mWindshieldWiperListener);
				} catch (VehicleServiceException e) {
					e.printStackTrace();
				} catch (UnrecognizedMeasurementTypeException e) {
					e.printStackTrace();
				}
	        }
	        
	        try {
				mVehicleManager.addListener(Odometer.class, mOdometerListener);
			} catch (VehicleServiceException e) {
				e.printStackTrace();
			} catch (UnrecognizedMeasurementTypeException e) {
				e.printStackTrace();
			}
	        
	//        try {
		//		mVehicleManager.addListener(VehicleDoorStatus.class, mVehicleDoorListener);
		//	} catch (VehicleServiceException e) {
		//		e.printStackTrace();
	//		} catch (UnrecognizedMeasurementTypeException e) {
	//			e.printStackTrace();
	//		}
	        
	  
	        try {
				mVehicleManager.addListener(TransmissionGearPosition.class, mGearListener);
			} catch (VehicleServiceException e) {
				e.printStackTrace();
			} catch (UnrecognizedMeasurementTypeException e) {
				e.printStackTrace();
			}
        }
	   
 
	    // Called when the connection with the service disconnects unexpectedly
	    public void onServiceDisconnected(ComponentName className) {
	        Log.w("openxc", "VehicleService disconnected unexpectedly");
	        mVehicleManager = null;
	    }
	};
 

	
	VehicleSpeed.Listener mSpeedListener = new VehicleSpeed.Listener() {
	    public void receive(Measurement measurement) {
	    	final VehicleSpeed _speed = (VehicleSpeed) measurement;
	        MainActivity.this.runOnUiThread(new Runnable() {
	            public void run() {
	            	speed = _speed.getValue().doubleValue() * 0.621371; //we need to convert km/h to mp/h
	            	String speedString = String.format("%.1f", speed);	
	               //mVehicleSpeedView.setText(
	                   // "Vehicle speed (mp/h): " + _speed.getValue().doubleValue());
	            	 mVehicleSpeedView.setText(
	                    "Speed (mp/h): " + speedString);
	            	 
	            	 
	            	if (_highSpeedAlarmSound == true) { //play a sound if user going too fast
		            	 
	            		if (speed > _highSpeedSMSThreshold) {
		            		 
		            		 if (highSpeedRunningFlag == false) {
		            			 i = 0;
		            			 highSpeedRunningFlag = true;
		            			 	
		            			 
		            			 	_highSpeedTimer = new Timer();
		            		 		_highSpeedTimer.schedule(new TimerTask() {
		       		                @Override
		       		                public void run() {UpdateHighSpeed();}
		       		          		}, 0, 2000); 
		            		 }
		            		
		            	 }
	            	}
	                
	                if (v == 0) { //what we'll do here is take two snapshots reading, one reading a second ago and then other the current time, then compare these to see if we have a rapid deceleration event
	                	previousSpeed = _speed.getValue().doubleValue() * 0.621371;
	                	 v++;
	                	// Log.w("openxc", "last speed " + v + ": " + previousSpeed);
	                }
	                else {
	                	
	                	if (v == _rapidBrakeInterval * 4) {  //if we are checking the speed for a rapid deceleation event every second for example, then this will be 1 * 4 , 4 because the ford rate api for speed is 4 Hz or 4 measurements per second
	                		currentSpeed = _speed.getValue().doubleValue() * 0.621371;
	                		v = 0;
	                	}
	                	else {
	                		v++;
	                	}
	                	//Log.w("openxc", "v: " + v);
	                	// Log.w("openxc", "current speed " + v + ": " + currentSpeed);
	                }
	                //the speed is 4 Hz meaning 4 measurements per second
	                //so if we reduce our speed by X in 2 seconds, then let's show the exclamation
	                speedDelta = previousSpeed - currentSpeed; 
	               // Log.w("openxc", "Speed Delta: " + speedDelta);
	                
	                if ((speedDelta > _rapidBrakeRate) && (rapidBrakePriority >= currentPriority) && (pixelFound == 1)) { //default rapidBrakeRate is 2 km/h deacceleration in 1 second
	                	    currentPriority = rapidBrakePriority;
	                		//************************************************
	                		//now kill other timers that may have been running
		            		if (pedalTimerRunning == 1) { //now let's check if the timer is running and start it if not
		            			_pedalTimer.cancel();
		            			pedalTimerRunning = 0;
		            		}
		            		
		            	    if (button2TimerRunning == 1) { //check if the button2 timer is running and kill if yes
		            			_birdTimer.cancel();
		            			button2TimerRunning = 0;
		            		}
		            		
		            		if (button1TimerRunning == 1) { //check if the button2 timer is running and kill if yes
		            			_thxTimer.cancel();
		            			button1TimerRunning = 0;
		            		}

	            		//	Log.w("openxc", "rapid brake killed all the timers"); 
		            		//**************************************************
		            		
		            		if (rapidBrakeTimerRunning == 0) { //now let's check if the timer is running and start it if not
		            			
		            			try {
		        					writeSuddenBrakeImage();  
								} catch (ConnectionLostException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
		            			i = 0;
		            			
		            			
		            			if (_rapidDecelerationSound == true) {
		            				mSoundPool.stop(mStream1);
	            	    	   		mStream1 = mSoundPool.play(mSoundPoolMap.get(DANGER), streamVolume, streamVolume, 1, LOOP_1_TIME, 1f);
		            			}
		            			
		            			//now let's start the timer as we'll want to display this image for x seconds and then clear the flags so other stuff can play again
		            			_rapidBrakeTimer = new Timer();
		            			_rapidBrakeTimer.schedule(new TimerTask() {
	            	                @Override
	            	                public void run() {UpdateRapidBrake();}
	            	             }, 0, 1000);
		            			
		            			rapidBrakeTimerRunning = 1;
		            		}
	                }
	            }
	        });
	    }
	};
	
	private void UpdateHighSpeed() {
		
		i++;
		if (i == 2) {
			mSoundPool.stop(mStream1);
	   		mStream1 = mSoundPool.play(mSoundPoolMap.get(ALERT), streamVolume, streamVolume, 1, LOOP_1_TIME, 1f);
	   	//	sms.sendTextMessage(_highSpeedSMSTextNumber, null, "speeding alert", null, null);
	   		_highSpeedTimer.cancel();
			highSpeedRunningFlag = false;
			i = 0;
		}
		
		//if (i == 3) {
			//highSpeedRunningFlag = false;
			//i = 0;
		//}	
	}
	
	private void sendSMS(String phoneNumber, String message)
	   {
	      // SmsManager sms = SmsManager.getDefault();
	       sms.sendTextMessage(phoneNumber, null, message, null, null);
	  }
	
	VehicleButtonEvent.Listener mButtonEventWiperListener = new VehicleButtonEvent.Listener() {  //if user pressed a button on the steering wheel
	    public void receive(Measurement measurement) {
	    	final VehicleButtonEvent _button = (VehicleButtonEvent) measurement;
	        MainActivity.this.runOnUiThread(new Runnable() {
	            public void run() {
	            	mVehicleButtonView.setText(
	            	 	"Button Event: " + _button.getValue());
	            }
	        });
	    }
	};
	
	WindshieldWiperStatus.Listener mWindshieldWiperListener = new WindshieldWiperStatus.Listener() {  //play rain animation
	    public void receive(Measurement measurement) {
	    	final WindshieldWiperStatus _wipers = (WindshieldWiperStatus) measurement;
	        MainActivity.this.runOnUiThread(new Runnable() {
	            public void run() {
	            	mVehicleWipersView.setText(
	            	 	"Wipers: " + _wipers.getValue().booleanValue());
	            	//boolean breakValue = _brakeStatus.getValue().booleanValue();
	            }
	        });
	    }
	};
	
	IgnitionStatus.Listener mIgnitionListener = new IgnitionStatus.Listener() {  //play rain animation
	    public void receive(Measurement measurement) {
	    	final IgnitionStatus _ignition = (IgnitionStatus) measurement;
	        MainActivity.this.runOnUiThread(new Runnable() {
	            public void run() {
	            	
	            	ignitionString = _ignition.getValue().toString();
	            	ignitionString = getValueString(ignitionString);
	            	
	            	ignitionStatusView.setText(
	            	 	"Ignition: " + ignitionString);
	            }
	        });
	    }
	};
	
	
	
	
	BrakePedalStatus.Listener mBrakeListener = new BrakePedalStatus.Listener() {
	    public void receive(Measurement measurement) {
	    	final BrakePedalStatus _brakeStatus = (BrakePedalStatus) measurement;
	        MainActivity.this.runOnUiThread(new Runnable() {
	            public void run() {
	            	
	            	mVehicleBrakeView.setText(
	                    //"Brake Status: " + _brakeStatus.getValue().doubleValue());
	            	 	"Brake: " + _brakeStatus.getValue().booleanValue());
	            	
	            	Log.w("openxc", "current priority " + currentPriority); 
	            	
	            	if (brakePriority >= currentPriority && pixelFound == 1) { 
	            		
	            		if (pedalTimerRunning == 1) { //now let's check if the timer is running and start it if not
	            			//pedalTimer.cancel();
	            			_pedalTimer.cancel();
	            			pedalTimerRunning = 0;
	            			Log.w("openxc", "brake killed the pedal timer"); 
	            		}
	            		
	            		//pedalTimer.cancel(); //stop the timer
	            		//pedalTimerRunning = 0;
	            		
	            		boolean breakValue = _brakeStatus.getValue().booleanValue();
	            		
	            		if (breakValue == true ) {
	            			
	            			currentPriority = brakePriority;
	            			Log.w("openxc", "brake was true"); 
	            			Log.w("openxc", "Speed Delta: " + speedDelta);
	            			if (speedDelta > 2) {
	            				try {
	            					writeSuddenBrakeImage();  //we'll need to add some code here to hold this image as well for the sudden brake acceleration
								} catch (ConnectionLostException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
	            			}
	            			else {
	            				try {
		            				writeBrakeImage();
								} catch (ConnectionLostException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
	            			}
			            		
	            			
	            		}
	            		else {
	            			 
	            			Log.w("openxc", "brake was false"); 
	            			 currentPriority = 0;
	            			// try {
								//	clearMatrixImage();
								//} catch (ConnectionLostException e) {
									// TODO Auto-generated catch block
								//	e.printStackTrace();
								//}
		            			
	            		}
	            		 
	            		// Log.w("openxc", breakValue); 
	            		
	            	}
	            }
	        });
	    }
	};
	
	
	AcceleratorPedalPosition.Listener mPedalListener = new AcceleratorPedalPosition.Listener() {  //only supposed to go here when we have a measurement
	    public void receive(Measurement measurement) {
	    	_pedal = (AcceleratorPedalPosition) measurement;
	        MainActivity.this.runOnUiThread(new Runnable() {
	            public void run() {
	            	
	            	double pedealValue = _pedal.getValue().doubleValue(); 
	            	String pedalString = String.format("%.1f", pedealValue);	
	            	mVehiclePedalView.setText("Gas Pedal: " + pedalString);
	            	
	            	if (pedalPriority >= currentPriority && pixelFound == 1) { //but don't do anything unless it has display priority AND PIXEL HAS BEEN FOUND
		            	
		            	
	            		if (pedalTimerRunning == 0) { //now let's check if the timer is running and start it if not
	            			//pedalTimer.start();
	            			
            		  	  _pedalTimer = new Timer();
            	          _pedalTimer.schedule(new TimerTask() {
            	                @Override
            	                //public void run() {UpdatePedalBars();}
            	                public void run() { _pedalHandler.post(myRunnable);}
            	             }, 0, 500);
	            			
	            			pedalTimerRunning = 1;
	            		}
	            		
	            		double pedalValue = _pedal.getValue().doubleValue();
		            	
		            	  if (pedalValue < 1) {
		            	    	 pedalRange = 1;
		            	    	 
		            	    //	mSoundPool.stop(mStream1);
		            	    //	float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		            	    //    streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		            	    //    mStream1 = mSoundPool.play(mSoundPoolMap.get(JETSONS_START), streamVolume, streamVolume, 1, LOOP_1_TIME, 1f);
		            	         
		            	     }
		            	     
		            	     if (pedalValue > 1 && pedalValue < 3)  {
		            	    	 pedalRange = 2;
		            	    	 
		            	    	// mSoundPool.stop(mStream1);
		            	    	// float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			            	    // streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			            	    // mStream1 = mSoundPool.play(mSoundPoolMap.get(ACCEL1), streamVolume, streamVolume, 1, LOOP_1_TIME, 1f);
		            	     }
		            	     
		            	     if (pedalValue > 3 && pedalValue < 7)  {
		            	    	 pedalRange = 3;
		            	    	 
		            	    	   	if (acceleratorSounds == true) {
		            	    	   		mSoundPool.stop(mStream1);
		            	    	   		mStream1 = mSoundPool.play(mSoundPoolMap.get(JETSONS_RUNNING), streamVolume, streamVolume, 1, LOOP_1_TIME, 1f);
		            	    	   	}
		            	     }
		            	     
		            	     if (pedalValue > 7 && pedalValue < 12)  { 
		            	    	 pedalRange = 4;
		            	    	 
		            	    	// mSoundPool.stop(mStream1);
		            	    	// float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			            	    // streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			            	    // mStream1 = mSoundPool.play(mSoundPoolMap.get(ACCEL3), streamVolume, streamVolume, 1, LOOP_1_TIME, 1f);
		            	     }
		            	     
		            	     if (pedalValue > 12 && pedalValue < 15)  { 
		            	    	 pedalRange = 5;
		            	    	 
		            	    	// mSoundPool.stop(mStream1);
		            	    	// float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			            	    // streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			            	    // mStream1 = mSoundPool.play(mSoundPoolMap.get(ACCEL4), streamVolume, streamVolume, 1, LOOP_1_TIME, 1f);
		            	     }
		            	     
		            	     if (pedalValue > 15 && pedalValue < 25)  { 
		            	    	 pedalRange = 6;
		            	    	 
		            	    	// mSoundPool.stop(mStream1);
		            	    	// float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			            	    // streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			            	    // mStream1 = mSoundPool.play(mSoundPoolMap.get(ACCEL5), streamVolume, streamVolume, 1, LOOP_1_TIME, 1f);
		            	     }
		            	     
		            	     if (pedalValue > 25 && pedalValue < 35)  { 
		            	    	 pedalRange = 7;
		            	    	 
		            	    	// mSoundPool.stop(mStream1);
		            	    	// float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			            	    // streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			            	     //mStream1 = mSoundPool.play(mSoundPoolMap.get(ACCEL6), streamVolume, streamVolume, 1, LOOP_1_TIME, 1f);
		            	     }
		            	     
		            	     if (pedalValue > 35 && pedalValue < 50)  { 
		            	    	 pedalRange = 8;
		            	    	 
		            	    //	 mSoundPool.stop(mStream1);
		            	    //	 float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			            	 //    streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			            	 //    mStream1 = mSoundPool.play(mSoundPoolMap.get(ACCEL7), streamVolume, streamVolume, 1, LOOP_1_TIME, 1f);
		            	     }
		            	     
		            	     if (pedalValue > 60 && pedalValue < 70)  { 
		            	    	 pedalRange = 9;
		            	    	 
		            	    //	 mSoundPool.stop(mStream1);
		            	    //	 float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			            	 //    streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			            	 //    mStream1 = mSoundPool.play(mSoundPoolMap.get(ACCEL8), streamVolume, streamVolume, 1, LOOP_1_TIME, 1f);
		            	     }
		            	     
		            	     if (pedalValue > 70 && pedalValue < 90)  { 
		            	    	 pedalRange = 10;
		            	    	 
		            	    //	 mSoundPool.stop(mStream1);
		            	   // 	 float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			            	//     streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			            	//     mStream1 = mSoundPool.play(mSoundPoolMap.get(ACCEL9), streamVolume, streamVolume, 1, LOOP_1_TIME, 1f);
		            	     }
		            	     
		            	     if (pedalValue > 90)  { 
		            	    	 pedalRange = 11;
		            	    	 
		            	    //	 mSoundPool.stop(mStream1);
		            	    //	 float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			            	//     streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			            	//     mStream1 = mSoundPool.play(mSoundPoolMap.get(ACCEL10), streamVolume, streamVolume, 1, LOOP_1_TIME, 1f);
		            	     }
		            	     
		            	     currentPriority = pedalPriority; //we've set the display priority to the pedal
	            	}   
	            	 //    Log.w("openxc", String.valueOf(pedalRange)); 
	            }
	        });
	    }
	};
	
	TurnSignalStatus.Listener mTurnSignalListener = new TurnSignalStatus.Listener() {
	    public void receive(Measurement measurement) {
	    	final TurnSignalStatus _turnSignal = (TurnSignalStatus) measurement;
	        MainActivity.this.runOnUiThread(new Runnable() {
	            public void run() {
	            	mVehicleTurnSignalView.setText(
	            	 	"Turn Signal: " + _turnSignal.getValue());
	            	
	            	Log.w("openxc", String.valueOf(_turnSignal)); 
	            	
	            	if (turnPriority >= currentPriority && pixelFound == 1) { 
	            		
	            		if (pedalTimerRunning == 1) { //now let's check if the timer is running and start it if not
	            			_pedalTimer.cancel();
	            			pedalTimerRunning = 0;
	            			Log.w("openxc", "turn signal killed the pedal timer"); 
	            		}
	            		
	            	}
	            	
	            	
	            }
	        });
	    }
	};

	
	//SteeringWheelAngle.Listener mSteeringWheelListener = new SteeringWheelAngle.Listener() {
		 //   public void receive(Measurement measurement) {
		   // 	final SteeringWheelAngle _steeringWheelAngle = (SteeringWheelAngle) measurement;
		    //    MainActivity.this.runOnUiThread(new Runnable() {
		     //       public void run() {
		      //      	mVehicleSteeringWheelView.setText(
		       //     	 	"Steering Wheel Angle: " + _steeringWheelAngle.getValue().doubleValue());
		        //    }
		       // });
		   // }
//		};
		
		//public int convertInt(String str ) {  //format is like this "Percentage{value=1.23}" so our job here is to extract the 1.23 and convert that to an int
		//	String[] pedalPair = str.split("=");
	 	 //   String pedalValue = pedalPair[1]; //1.23}
	 	 //   pedalValue = pedalValue.replace("}", ""); //take out the last bracket
	 	 //   pedalValue = pedalValue.trim(); //trim
	 	 //   int pInt = Integer.parseInt(pedalValue); //now convert to int
//			return(pInt);
//		}
		
	
//	FuelLevel.Listener mFuelLevelListener = new FuelLevel.Listener() {
//	    public void receive(Measurement measurement) {
	//    	final FuelLevel _fullLevel = (FuelLevel) measurement;
	 //       MainActivity.this.runOnUiThread(new Runnable() {
	  //          public void run() {
	   //         	mVehicleFuelLevelView.setText(
	    //        	 	"Fuel Level: " + _fullLevel.getValue().doubleValue());
	     //       }
	     //   });
	   // }
//	};
	
	FuelConsumed.Listener mFuelConsumedListener = new FuelConsumed.Listener() {
	    public void receive(Measurement measurement) {
	    	final FuelConsumed _fullConsumed = (FuelConsumed) measurement;
	        MainActivity.this.runOnUiThread(new Runnable() {
	            public void run() {
	            	
	            	double gasConsumed = _fullConsumed.getValue().doubleValue();
	            	double gasCost = gasConsumed * _gasGallonCost;
	                gasCostString = String.format("%.2f", gasCost);
	                gasConsumedString = String.format("%.2f", gasConsumed);
	            	
	            	mVehicleFuelConsumedView.setText(
		            	 	"Trip Fuel: " + gasConsumedString);
	           
	            	tripCostView.setText("Trip Cost: $" + gasCostString);
	            	
	            	if (_gasConsumedSound == true) {	
	            	
			            	if (g == 0) {
			            			TripBaselineGas = _fullConsumed.getValue().doubleValue();
			            		}
			            		
			            		if (g2 == 0) {
			            			TripBaselineGas2 = _fullConsumed.getValue().doubleValue();  //for the one gallon mark
			            		}
			            		
			            		TripGasConsumed = _fullConsumed.getValue().doubleValue();
			            		TripGasConsumed2 = _fullConsumed.getValue().doubleValue();
			            		
			            		g++;
			            		g2++;
			            		
			            		if (TripGasConsumed -TripBaselineGas > _gasTickSoundInterval ) {  //then we've used 1/10 a gallon of gas
			            			mSoundPool.stop(mStream1);
		        	    	   		mStream1 = mSoundPool.play(mSoundPoolMap.get(GAS_DROP), streamVolume, streamVolume, 1, LOOP_1_TIME, 1f);
		        	    	   		g = 0;
		        	    	   		
		        	    	   		//tts.setLanguage(Locale.getDefault()); //let's set the language before talking, we do this dynamically as it can change mid stream
		        	    	    	//tts.speak("you've just used some gas", TextToSpeech.QUEUE_FLUSH, null);    	
			            		}
			            		
			            		if (TripGasConsumed2 -TripBaselineGas2 > _gasGallonConsumedSoundInterval ) {  //then we've used 1/10 a gallon of gas
			            			mSoundPool.stop(mStream1);
		        	    	   		mStream1 = mSoundPool.play(mSoundPoolMap.get(BUBBLES), streamVolume, streamVolume, 1, LOOP_1_TIME, 1f);
		        	    	   		tts.setLanguage(Locale.getDefault()); //let's set the language before talking, we do this dynamically as it can change mid stream
		        	    	    	tts.speak("The cost of this trip so far is $" + gasCostString, TextToSpeech.QUEUE_FLUSH, null);    	
		        	    	   		g2 = 0;
			            		}
	            	}
	            		
	            	
	            	// private double TripBaselineGas;
	            	 //   private double TripGasConsumed;
	            	
	            }
	        });
	    }
	};
	
    private void speakTripCost () {
	   		
	   		if (gasCostString != null) {
	    		tts.setLanguage(Locale.getDefault()); //let's set the language before talking, we do this dynamically as it can change mid stream
		    	tts.speak("The cost of this trip was $" + gasCostString, TextToSpeech.QUEUE_FLUSH, null);   
	   		}
	   		else {
	   			tts.setLanguage(Locale.getDefault()); //let's set the language before talking, we do this dynamically as it can change mid stream
		    	tts.speak("Gas consumed data is not available", TextToSpeech.QUEUE_FLUSH, null);   
	   		}
		
    }
	
	
	 	HeadlampStatus.Listener mHeadLampListener = new HeadlampStatus.Listener() {
		    public void receive(Measurement measurement) {
		    	final HeadlampStatus _headLamp = (HeadlampStatus) measurement;
		        MainActivity.this.runOnUiThread(new Runnable() {
		            public void run() {
		            	mVehicleLightsView.setText("Lights: " + _headLamp.getValue().booleanValue());
		            }
		        });
		    }
		};

	
	
		HighBeamStatus.Listener mHighBeamListener = new HighBeamStatus.Listener() {
		    public void receive(Measurement measurement) {
		    	final HighBeamStatus _headBeam = (HighBeamStatus) measurement;
		        MainActivity.this.runOnUiThread(new Runnable() {
		            public void run() {
		            	mVehicleHighBeamsView.setText("High Beams: " + _headBeam.getValue().booleanValue());
		            	//"Wipers: " + _wipers.getValue().booleanValue());
		            }
		        });
		    }
		};
	
	
	
	
	Odometer.Listener mOdometerListener = new Odometer.Listener() {  //play rain animation
	    public void receive(Measurement measurement) {
	    	final Odometer _odometer = (Odometer) measurement;
	        MainActivity.this.runOnUiThread(new Runnable() {
	         	
	        	double odometerValue2 = _odometer.getValue().doubleValue();
	        	String odometerString = String.format("%.0f", odometerValue2);
	        	
	            public void run() {
	            	mVehicleOdometerView.setText(
	            	 	"Odometer: " + odometerString);
	            }
	        });
	    }
	};
	
//	VehicleDoorStatus.Listener mVehicleDoorListener = new VehicleDoorStatus.Listener() {  //play rain animation
//	    public void receive(Measurement measurement) {
	//    	final VehicleDoorStatus _doorStatus = (VehicleDoorStatus) measurement;
	 //       MainActivity.this.runOnUiThread(new Runnable() {
	  //          public void run() {
	   //         	mVehicleDoorView.setText(
	    //        	 	"Door Status: " + _doorStatus.getValue());
	     //       }
	     //   });
	   // }
//	};
	
	TransmissionGearPosition.Listener mGearListener = new TransmissionGearPosition.Listener() {  //play rain animation
	    public void receive(Measurement measurement) {
	    	final TransmissionGearPosition _gear = (TransmissionGearPosition) measurement;
	        //TransmissionGearPosition.GearPosition _gear = new TransmissionGearPosition.GearPosition();
	        MainActivity.this.runOnUiThread(new Runnable() {
	            public void run() {
	         
	            	gearString = _gear.getValue().toString();
	            	gearString = getValueString(gearString);
	            	mVehicleGearView.setText(
	            	"Gear: " + gearString);
	            	
	            	int gearNumber = 0;
	            	//int[] gearArray = new int[120];
	            	
	            	//if (string != null && string.equals(string2)) return true;
	            	
	            	if (gearString != null && gearString.equals("REVERSE")) {
	            		gearNumber = -1;
	            	}
	            	else if (gearString != null && gearString.equals("NEUTRAL")) {
	            		gearNumber = 0;
	            	}
	            	else if (gearString != null && gearString.equals("FIRST")) {
	            		gearNumber = 1;
	            	}
	            	else if (gearString != null && gearString.equals("SECOND")) {
	            		gearNumber = 2;
	            	}
	            	else if (gearString != null && gearString.equals("THIRD")) {
	            		gearNumber = 3;
	            	}
	            	else if (gearString != null && gearString.equals("FOURTH")) {
	            		gearNumber = 4;
	            	}
	            	else if (gearString != null && gearString.equals("FIFTH")) {
	            		gearNumber = 5;
	            	}
	            	else if (gearString != null && gearString.equals("SIXTH")) {
	            		gearNumber = 6;
	            	}
	            	else if (gearString != null && gearString.equals("SEVENTH")) {
	            		gearNumber = 7;
	            	}
	            	else if (gearString != null && gearString.equals("EIGHTH")) {
	            		gearNumber = 8;
	            	}
	            	else gearNumber = 0;
	            	
	                if (p > 900) {
	                	p = 0;
	                }
	                p++;
	            	gearArray[p] = gearNumber;  //this must be a global array!
	               
	            
	            	//Log.w("openxc", "gearString: " + gearString);
	            	Log.w("openxc", "gearNumber: " + gearNumber);
	            	Log.w("openxc", "current gear: " + gearArray[p]);
	                Log.w("openxc", "last gear: " + gearArray[p-1]);
	            	
	            	
	            	if (_gearSound == true) {
	            		
	            		if (gearArray[p] != gearArray[p-1]) {  //there was a change
	            		
		            		if (gearArray[p] > gearArray[p-1]) {  //we went up
		            			mSoundPool.stop(mStream1);
				    	   		mStream1 = mSoundPool.play(mSoundPoolMap.get(SHIFT_UP), streamVolume, streamVolume, 1, LOOP_1_TIME, 1f);
		            		}
		            		else {  //otherwise we shifted down
		            			mSoundPool.stop(mStream1);
				    	   		mStream1 = mSoundPool.play(mSoundPoolMap.get(SHIFT_DOWN), streamVolume, streamVolume, 1, LOOP_1_TIME, 1f);
		            		}
	            		}	
	            	}
	            	
	           }
	        });
	    }
	};
	
	public String getValueString(String str ) {  //format is like this "Percentage{value=1.23}" so our job here is to extract the 1.23 and convert that to an int
		String[] valuePair = str.split("=");
 	    String Value = valuePair[1]; //1.23}
 	    Value = Value.replace("}", ""); //take out the last bracket
 	    Value = Value.trim(); //trim
 	   // int pInt = Integer.parseInt(pedalValue); //now convert to int
		return(Value);
	}
	
	//public int convertInt(String str ) {  //format is like this "Percentage{value=1.23}" so our job here is to extract the 1.23 and convert that to an int
	//	String[] pedalPair = str.split("=");
 	 //   String pedalValue = pedalPair[1]; //1.23}
 	 //   pedalValue = pedalValue.replace("}", ""); //take out the last bracket
 	 //   pedalValue = pedalValue.trim(); //trim
 	 //   int pInt = Integer.parseInt(pedalValue); //now convert to int
//		return(pInt);
//	}


	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		
	}
	
}
