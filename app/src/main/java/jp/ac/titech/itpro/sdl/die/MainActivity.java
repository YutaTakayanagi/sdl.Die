package jp.ac.titech.itpro.sdl.die;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    private final static String TAG = MainActivity.class.getSimpleName();

    final int MSG_ROLL = 1234;
    final int MSG_X = 111;
    final int MSG_Y = 222;
    final int MSG_Z = 333;

    private GLSurfaceView glView;
    private SimpleRenderer renderer;

    private Cube cube;
    private Pyramid pyramid;

    private RollThread threadX;
    private RollThread threadY;
    private RollThread threadZ;

    private boolean isRollX;
    private boolean isRollY;
    private boolean isRollZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        glView = findViewById(R.id.gl_view);
        SeekBar seekBarX = findViewById(R.id.seekbar_x);
        SeekBar seekBarY = findViewById(R.id.seekbar_y);
        SeekBar seekBarZ = findViewById(R.id.seekbar_z);
        seekBarX.setMax(360);
        seekBarY.setMax(360);
        seekBarZ.setMax(360);
        seekBarX.setOnSeekBarChangeListener(this);
        seekBarY.setOnSeekBarChangeListener(this);
        seekBarZ.setOnSeekBarChangeListener(this);

        isRollX = false;
        isRollY = false;
        isRollZ = false;

        Button buttonX = findViewById(R.id.button_x);
        Button buttonY = findViewById(R.id.button_y);
        Button buttonZ = findViewById(R.id.button_z);

        buttonX.setOnClickListener(v -> {
            Log.d(TAG, "onClick - Click x");
            if(isRollX) {
                isRollX = false;
                buttonX.setText(R.string.button_roll);
                threadX.stopThread();
            } else {
                isRollX = true;
                buttonX.setText(R.string.button_stop);
                threadX = new RollThread(new RollHandler(this), MSG_X);
                threadX.start();
            }
        });

        buttonY.setOnClickListener(v -> {
            Log.d(TAG, "onClick - Click y");
            if(isRollY) {
                isRollY = false;
                buttonY.setText(R.string.button_roll);
                threadY.stopThread();
            } else {
                isRollY = true;
                buttonY.setText(R.string.button_stop);
                threadY = new RollThread(new RollHandler(this), MSG_Y);
                threadY.start();
            }
        });

        buttonZ.setOnClickListener(v -> {
            Log.d(TAG, "onClick - Click z");
            if(isRollZ) {
                isRollZ = false;
                buttonZ.setText(R.string.button_roll);
                threadZ.stopThread();
            } else {
                isRollZ = true;
                buttonZ.setText(R.string.button_stop);
                threadZ = new RollThread(new RollHandler(this), MSG_Z);
                threadZ.start();
            }
        });

        renderer = new SimpleRenderer();
        cube = new Cube();
        pyramid = new Pyramid();
        renderer.setObj(cube);
        glView.setRenderer(renderer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        glView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        glView.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
        public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
        case R.id.menu_cube:
            renderer.setObj(cube);
            break;
        case R.id.menu_pyramid:
            renderer.setObj(pyramid);
            break;
        }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
        case R.id.seekbar_x:
            renderer.rotateObjX(progress);
            break;
        case R.id.seekbar_y:
            renderer.rotateObjY(progress);
            break;
        case R.id.seekbar_z:
            renderer.rotateObjZ(progress);
            break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    public void changeSeekBar(int dimension, int degree) {
        switch (dimension){
            case MSG_X:
                SeekBar seekBar_x = findViewById(R.id.seekbar_x);
                seekBar_x.setProgress(degree);
                break;
            case MSG_Y:
                SeekBar seekBar_y = findViewById(R.id.seekbar_y);
                seekBar_y.setProgress(degree);
                break;
            case MSG_Z:
                SeekBar seekBar_z = findViewById(R.id.seekbar_z);
                seekBar_z.setProgress(degree);
            default:
                break;
        }
    }

    private class RollThread extends Thread {
        private final Handler handler;
        private final int dimension;
        private int degree;
        private boolean isRun;

        RollThread(Handler handler, int dimension) {
            this.handler = handler;
            this.dimension = dimension;
            degree = 0;
        }

        @Override
        public void run() {
            isRun = true;
            while(isRun) {
                Log.d(TAG, "roll");
                handler.sendMessage(handler.obtainMessage(MSG_ROLL, dimension, degree));
                degree = (degree + 10) % 360;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void stopThread(){
            isRun = false;
        }
    }

    @SuppressLint("HandlerLeak")
    private  class RollHandler extends  Handler {
        private final WeakReference<MainActivity> activityRef;

        RollHandler(MainActivity activity) {
            super(Looper.getMainLooper());
            this.activityRef = new WeakReference<MainActivity>(activity);
        }

        public void handleMessage(@NotNull Message msg) {
            if (msg.what == MSG_ROLL) {
                MainActivity activity = activityRef.get();
                activity.changeSeekBar(msg.arg1, msg.arg2);
            }
        }
    }
}