package com.trinhhoanglam.multithreadexample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends Activity {
    ProgressBar bar1;
    ProgressBar bar2;

    TextView msgWorking;
    TextView msgReturned;
    EditText txtDataBox;
    ScrollView myScrollView;

    Button btnDoSth;
    Button btnDoItAgain;

    int progressStep = 5;
    final int MAX_PROGRESS = 100;

    // this is a control var used by backg. threads
    protected boolean isRunning = false;

    // lifetime (in seconds) for background thread
    protected final int MAX_SEC = 30;

    // global value seen by all threads – add synchonized get/set
    protected int globalIntTest = 0;
    int accum = 0;

    long startingMills = System.currentTimeMillis();
    String PATIENCE = "Some important data is being collected now. " + "\nPlease be patient...wait...\n";

    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            String returnedValue = (String)msg.obj;

            // do sth with the value sent by the backg thread here
            msgReturned.append("\n returned value: " + returnedValue);
            myScrollView.fullScroll(View.FOCUS_DOWN);
            bar1.incrementProgressBy(1);

            // testing early termination
            if (bar1.getProgress() == MAX_SEC) {
                msgReturned.append(" \nDone \n back thread has been stopped");
                isRunning = false;
            }

            if (bar1.getProgress() == bar1.getMax()){
                msgWorking.setText("Done");
                bar1.setVisibility(View.INVISIBLE);
                bar2.setVisibility(View.INVISIBLE);
            }
            else {
                msgWorking.setText("Working..." + bar1.getProgress() );
            }
        } // handler
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bar1 = (ProgressBar) findViewById(R.id.progress1);
        bar2 =  (ProgressBar) findViewById(R.id.progress2);

        msgWorking = (TextView)findViewById(R.id.textWorkProgress);
        msgReturned = (TextView)findViewById(R.id.textReturnedValues);
        myScrollView = (ScrollView)findViewById(R.id.myScroller);

        txtDataBox = (EditText) findViewById(R.id.textBox1);
        txtDataBox.setHint(" Foreground distraction\n Enter some data here...");

        btnDoItAgain = (Button) findViewById(R.id.btnDoItAgain);
        btnDoItAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStart();
            }
        });

        btnDoSth = (Button) findViewById(R.id.btnDoSth);
        btnDoSth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = txtDataBox.getText().toString();
                Toast.makeText(MainActivity.this, "I'm quick - you said >> \n" + text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // prepare UI components
        txtDataBox.setText("");
        btnDoItAgain.setEnabled(false);
        // reset and show progress bars
        accum = 0;
        bar1.setMax(MAX_PROGRESS);
        bar1.setProgress(0);
        bar1.setVisibility(View.VISIBLE);
        bar2.setVisibility(View.VISIBLE);
        // this code creates the background activity where busy work is done
//        Thread background = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    for (int i = 0; i < MAX_SEC && isRunning; i++) {
//                        Thread.sleep(1000);
//                        Random rd = new Random();
//                        int localData = (int) rd.nextInt(101);
//                        String data = "Data-" + getGlobalIntTest() + "-" + localData;
//                        increaseGlobalIntTest(1);
//                        Message msg = handler.obtainMessage(1, (String) data);
//
//                        if (isRunning) {
//                            handler.sendMessage(msg);
//                        }
//                    }
//                } catch (Throwable t) {
//                    isRunning = false;
//                }
//            }
//        });
//
//        isRunning = true;
//        background.start();
        Thread myBackgroundThread = new Thread(backgroundTask, "backAlias");
        myBackgroundThread.start();
    }

    private Runnable foregroundRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                msgWorking.setText(PATIENCE
                            + "\npct progress: " + accum
                            + " globalVar: " + globalIntTest);
                bar1.incrementProgressBy(progressStep);
                Random rd = new Random();
                int localData = (int) rd.nextInt(101);
                String data = "Data-" + getGlobalIntTest() + "-" + localData;
                Message msg = handler.obtainMessage(1, (String) data);

                if (isRunning) {
                    handler.sendMessage(msg);
                }


                accum += progressStep;

                if (accum >= bar1.getMax()) {
                    msgWorking.setText("Slow background work is OVER!");
                    bar1.setVisibility(View.INVISIBLE);
                    bar2.setVisibility(View.INVISIBLE);
                    btnDoItAgain.setEnabled(true);
                }
            } catch (Exception e) {
                Log.e("<<foregroundTask>>", e.getMessage());
            }
        }
    };

    private Runnable backgroundTask = new Runnable() {
        @Override
        public void run() {
            try {
                for (int i = 0; i < 20; i++) {
                    Thread.sleep(1000);
                    globalIntTest++;
                    handler.post(foregroundRunnable);
                }
            } catch (InterruptedException e) {
                Log.e("<<foregroundTask>>", e.getMessage());
            }
        }
    };

//    @Override
//    protected void onStop() {
//        super.onStop();
//        isRunning = false;
//    }
//
    public synchronized int getGlobalIntTest() {
        return globalIntTest;
    }
//
//    public synchronized int increaseGlobalIntTest(int inc) {
//        return globalIntTest += inc;
//    }
}
