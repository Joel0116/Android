package com.scribblesinc.tams;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.os.Environment;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import java.io.IOException;
import android.Manifest;

/*
*INFORMATION: This class takes care of recording, updating or playing a memo,
* it has very basic functionality allowing an audio to play, record and stop a recording.
* When user selects to listen to an existing audio, the User needs to first Play
* the audio before it can choose to Stop it or update the audio with a new record.
*
 */

public class AudioCapture extends AppCompatActivity{
    //View buttons
    private Button play, stop, record;
    //Declaring the MediaRecording to be use by this activity
    private MediaRecorder appAudioRecorder;
    private MediaPlayer appPlayer;
    private String outputFile = null; //data storage as
    //Flag for to check if SD present
    Boolean isSDPresent;
    private static final int REQUEST_MIC = 200;
    private int REQUEST_Audio_RESULT = 1;
    //Variables Constant to be use for intent across activites
    static final String REC_AUDIO = "com.scribblesinc.tams";
    private static final String TAG = AudioCapture.class.getSimpleName();

    //Declaring intent to be use
    private Intent intent;
    private boolean ismic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //run parents method by extending the existing class or run this
        //class in addition to parent's class
        super.onCreate(savedInstanceState);
        //activity class creates window
        setContentView(R.layout.activity_audio);

        if(ContextCompat.checkSelfPermission(AudioCapture.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
            //if Permission is not granted
            ActivityCompat.requestPermissions(AudioCapture.this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_MIC);


        }else{
            ActivityCompat.requestPermissions(AudioCapture.this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_MIC);

        }

        //binding buttons to GUI component
        play = (Button)findViewById(R.id.play_sound_);
        stop = (Button) findViewById(R.id.stop_sound);
        record = (Button)findViewById(R.id.record_sound);

        //Get the information sent via intent
          intent = getIntent();
           outputFile = intent.getStringExtra(REC_AUDIO);


        //if outputFile has something
        if(outputFile != null) {
            //Initially stop button and play button are enabled
            stop.setEnabled(false);
            record.setEnabled(false);
            ismic = true;

        }else{
            //its a fresh recording with no previous audio
            //Initially stop button and play button are enabled
            stop.setEnabled(false);
            play.setEnabled(false);
            ismic = false;
        }

                /*Where shoud storing of recording go? SD or device
        if SD is present store there else store on device*/
        isSDPresent = isExternalStorageWritable();
        if(isSDPresent){
            //SD is present and declare a path there for storing voice rec
            outputFile = Environment.getExternalStorageDirectory().getAbsolutePath()+"/voice_recording.3gp";
        }else{
            //no SD-Card
            outputFile = getFilesDir().getAbsolutePath()+"/voice_recordig.3gp";
        }
        //Instantiating the toolbar of adding asset activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }



    }

    @Override
    protected void onStart(){
        super.onStart();
        //works but crashes when after play and stop
        //working on it.

        record.setOnClickListener((new OnClickListener() {
            @Override
            public void onClick(View view) {
                record(view);
            }
        }));
        //OnClick listeners to call functions to take care of buttons
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play(view);
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop(view);
            }
        });


    }//endofonStart()


    public void play(View view){
        appPlayer= new MediaPlayer();
        try{
            appPlayer.setDataSource(outputFile);
        } catch(Exception e){
            //play is called before record
            e.printStackTrace();
        }
        try{
            appPlayer.prepare();
        } catch (IOException e){
            e.printStackTrace();
        }
        appPlayer.start();
        play.setEnabled(false);
        record.setEnabled(false);
        stop.setEnabled(true);
        Toast.makeText(getApplicationContext(),"Playing the recording...",Toast.LENGTH_LONG).show();
    }
    public void stop(View view){
        try{
            appAudioRecorder.stop();
            appAudioRecorder.reset();
            appAudioRecorder.release();

            //the need to start a new MediaRecorder, in case user decides to record a new recording
            appAudioRecorder = new MediaRecorder();
            appAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            appAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            appAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
            appAudioRecorder.setOutputFile(outputFile);



        }catch(IllegalStateException e){
            e.printStackTrace();
        }
        stop.setEnabled(false);//disabled stop button
        play.setEnabled(true);//enabled play button
        record.setEnabled(true);
        Toast.makeText(getApplicationContext(),"Audio stopped recording",Toast.LENGTH_LONG).show();
    }
    public void record(View view){
        try{
            appAudioRecorder.prepare();
            appAudioRecorder.start();//start recording
        }catch (Exception e){
            e.printStackTrace();
        }
        record.setEnabled(false);//disable record button
        stop.setEnabled(true);//enabled stop button
        Toast.makeText(getApplicationContext(),"Recording Started",Toast.LENGTH_LONG).show();
    }



    /*check if external storage is available for read and write */
    public boolean isExternalStorageWritable(){
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return true; //SD Present
        }
        return false;
    }
    //handle the permissions request response
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull  int[] grantResults){

           //start audio recording
            if(requestCode == REQUEST_MIC) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "App has microphone capturing permission", Toast.LENGTH_LONG).show();

                    //instantiate the MediaRecording to be use
                    appAudioRecorder = new MediaRecorder();
                     /*Define output format*/
                    appAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    appAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    appAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                    appAudioRecorder.setOutputFile(outputFile);
                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(AudioCapture.this, Manifest.permission.RECORD_AUDIO)) {
                        //explain user need of permission
                        finish();
                        Toast.makeText(getApplicationContext(),"Audio Capturing Permission Required", Toast.LENGTH_LONG).show();
                    } else {
                        //Don't ask again for permission, handle rest of app without this permisson
                        finish();
                        Toast.makeText(getApplicationContext(), "App has no audio capturing permission", Toast.LENGTH_LONG).show();
                    }
                }
            }

    }//end of onRequestPermissoinResult

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(!ismic) {
            getMenuInflater().inflate(R.menu.menu_audio, menu);
        }else{
            getMenuInflater().inflate(R.menu.menu_audio_update, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //if the back button is pressed
        if(item.getItemId() == android.R.id.home){
            finish(); //goes back to the previous activity
        }

        //if done button is pressed
        if(id == R.id.action_done||id==R.id.action_update){
            //get recording
            if(outputFile.isEmpty()|| !ismic){
                Toast.makeText(this,"No recording has been made",Toast.LENGTH_LONG).show();
            }else{
                if(id==R.id.action_update) {
                    //Instantiate an AlertDialog.Builder with its constructor
                    AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ConfirmationAlertDialogStyle);
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int choice) {
                            Intent sendToPreviousActivity = new Intent();
                            sendToPreviousActivity.putExtra(REC_AUDIO,outputFile);
                            setResult(RESULT_OK,sendToPreviousActivity);
                            Toast.makeText(getApplicationContext(),"Recording has been captured ",Toast.LENGTH_LONG).show();
                            finish();;
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int choice) {
                            Toast.makeText(getApplicationContext(),"Canceling update ",Toast.LENGTH_LONG).show();
                        }
                    });
                    builder.setMessage("Selecting 'Ok' will override current data. ");
                    builder.setTitle("Attemping to Update Data");

                    //Get the AlertDialog from create();
                    AlertDialog d = builder.create();
                    d.show();
                }else{//user is not updating data
                    Intent sendToPreviousActivity = new Intent();
                    sendToPreviousActivity.putExtra(REC_AUDIO,outputFile);
                    setResult(RESULT_OK,sendToPreviousActivity);
                    Toast.makeText(this,"Recording has been captured ",Toast.LENGTH_LONG).show();
                    finish();
                }

            }
        }


        return super.onOptionsItemSelected(item);
    }

    public void currentAudioAction(MenuItem item){


        if(Build.VERSION.SDK_INT >= 23){ //6.0+
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                //calculate current location

                Toast.makeText(getApplicationContext(), "Audio Permission Granted", Toast.LENGTH_SHORT).show();
            }else {
                //rejected permission request
                if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){
                    Toast.makeText(getApplicationContext(), "Audio Permission Required", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Audio Permission Required");
                }
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_Audio_RESULT);
            }
        }else{ // < 6.0
            Toast.makeText(getApplicationContext(), "Audio Permission Granted", Toast.LENGTH_SHORT).show();

        }
    }

}
