package com.lonelyship.line_toy;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Timer;


public class MainActivity extends ActionBarActivity implements ShakeWorker.OnShakeWorkerListener, View.OnClickListener,
        SensorEventListener {

    private ImageView ivBg;
    private ImageView ivBearHead;
    private ImageView ivBearBody;
    private ImageView ivMoonHead;
    private ImageView ivMoonBody;
    private ImageView ivRabbitHead;
    private ImageView ivRabbitBody;

    private final int HEAD = 1;
    private final int BODY = -1;
    private int degree = 10;
    private int shakePeriod = 300;
    private int shakePeriodTempProgress = 3;
    private int special_rabbit = 0;
    private int special_moon = 0;
    private int special_bear = 0;
    private int special_bearBody = 0;

    private ShakeWorker shakeWorker = null;
    Vibrator myVibrator = null;

    private Timer timer = null;

    private Boolean isShackMode = true;
    private Boolean isLightMode = false;
    private Boolean isSoundMode = false;

    private Boolean isMusicPlaying = false;
    private Boolean canVibrate = false;
    private Boolean isRunning = true;

    private MediaPlayer mp = null;
    private MediaPlayer mp_unlock = null;
    private Menu menu = null;

    private SensorManager mgr;
    private Sensor sensor;

    private final int MSG_SOUND_BIG = 1;
    private final int MSG_SOUND_MEDIUM = 2;
    private final int MSG_SOUND_LOW = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //防休眠
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        shakeWorker = new ShakeWorker(this, this);
        myVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);

        mRecorder = new MediaRecorder();

        mp = MediaPlayer.create(this, R.raw.music);
        mp.setLooping(true);

        this.mgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        this.sensor = this.mgr.getDefaultSensor(Sensor.TYPE_LIGHT);

        ivBg = (ImageView) findViewById(R.id.iv_bg);
        ivBearHead = (ImageView) findViewById(R.id.iv_bear_head);
        ivBearBody = (ImageView) findViewById(R.id.iv_bear_body);
        ivMoonHead = (ImageView) findViewById(R.id.iv_moon_head);
        ivMoonBody = (ImageView) findViewById(R.id.iv_moon_body);
        ivRabbitHead = (ImageView) findViewById(R.id.iv_rabbit_head);
        ivRabbitBody = (ImageView) findViewById(R.id.iv_rabbit_body);

        ivBg.setAlpha((float) 0.8);

        ivRabbitHead.setOnClickListener(this);
        ivMoonHead.setOnClickListener(this);
        ivBearHead.setOnClickListener(this);
        ivBearBody.setOnClickListener(this);
    }

    public void setAnimation(int duration, int degree, int repeat_count, ImageView iv, int part) {

        AnimationSet animationSet = new AnimationSet(true);

        animationSet.cancel();
        iv.setAnimation(animationSet);

        RotateAnimation rotateAnimation = new RotateAnimation(-degree * part, degree * part,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        rotateAnimation.setDuration(duration);
        rotateAnimation.setRepeatCount(repeat_count);
        rotateAnimation.setRepeatMode(Animation.REVERSE);
        rotateAnimation.setInterpolator(new AccelerateInterpolator());
        animationSet.addAnimation(rotateAnimation);

        iv.startAnimation(animationSet);
    }

    MenuItem musicMenuItem;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.line_actionbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getMenuInflater().inflate(R.menu.menu_main, menu);

        this.menu = menu;

        musicMenuItem = menu.findItem(R.id.menu_music);

        return true;
    }

    public void initialMode() {
        isShackMode = false;
        isLightMode = false;
        isSoundMode = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.menu_beach:

                initialMode();

                ivBg.setImageDrawable(getResources().getDrawable(
                        R.drawable.beach));
                degree = 40;
                gogogo(degree, -1, shakePeriod);
                break;

            case R.id.menu_sunrise:

                initialMode();

                ivBg.setImageDrawable(getResources().getDrawable(
                        R.drawable.sunrise));
                degree = 30;
                gogogo(30, -1, shakePeriod);
                break;
            case R.id.menu_cloudy:

                initialMode();

                ivBg.setImageDrawable(getResources().getDrawable(
                        R.drawable.cloudy));
                degree = 20;
                gogogo(degree, -1, shakePeriod);
                break;
            case R.id.menu_night:

                initialMode();

                ivBg.setImageDrawable(getResources().getDrawable(
                        R.drawable.night));
                degree = 10;
                gogogo(degree, -1, shakePeriod);
                break;
            case R.id.menu_shake:
                initialMode();
                isShackMode = true;

                Toast.makeText(this, "啟動搖動偵測模式", Toast.LENGTH_SHORT).show();
                gogogo(0, 0, shakePeriod);
                ivBg.setImageDrawable(getResources().getDrawable(
                        R.drawable.earth));
                break;
            case R.id.menu_music:
                mp.release();
                mp = MediaPlayer.create(this, R.raw.music);
                mp.setLooping(true);
                if (isMusicPlaying == false) {
                    isMusicPlaying = true;
                    musicMenuItem.setTitle("關閉音樂");
                    mp.start();
                } else {
                    isMusicPlaying = false;
                    musicMenuItem.setTitle("開啟音樂");
                    mp.pause();
                }
                break;
            case R.id.menu_shake_period:
                showShakePeriodDialog();
                break;
            case R.id.menu_vibrate:
                MenuItem vibrateMenuItem = menu.findItem(R.id.menu_vibrate);
                if (canVibrate == false) {
                    canVibrate = true;
                    vibrateMenuItem.setTitle("關閉震動");
                } else {
                    canVibrate = false;
                    vibrateMenuItem.setTitle("開啟震動");
                }
                break;
            case R.id.menu_light_mode:
                initialMode();
                if (this.sensor != null) {
                    isShackMode = false;
                    ivBg.setImageDrawable(getResources().getDrawable(
                            R.drawable.sun_power));
                    Toast.makeText(this, "啟動太陽能感光模式", Toast.LENGTH_SHORT).show();
                    isLightMode = true;
                    onSensorChanged(null);
                } else {
                    Toast.makeText(this, "很抱歉,您的裝置不支援感光模式", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_detect_sound:
                initialMode();
                isSoundMode = true;
                gogogo(0, 0, shakePeriod);
                ivBg.setImageDrawable(getResources().getDrawable(
                        R.drawable.bg_sound));
                detectSoundLevel();
                Toast.makeText(this, "啟動聲音偵測模式", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    public void showShakePeriodDialog() {

        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        final View seekView = inflater.inflate(R.layout.seek_bar_view, null);
        final SeekBar seek = (SeekBar) seekView
                .findViewById(R.id.seekBarPenSize);
        final TextView txtSeek = (TextView) seekView
                .findViewById(R.id.textViewSeekBar);
        seek.setMax(9);
        seek.setProgress(shakePeriodTempProgress);
        txtSeek.setText("設定擺動週期:"
                + (shakePeriodTempProgress + 1));
        popDialog.setView(seekView);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                txtSeek.setText("設定擺動週期:"
                        + (progress + 1));

                if(isLightMode) onSensorChanged(null);
                else if(isShackMode)gogogo(10, 5, (progress + 1) * 100);
                else if(isSoundMode)gogogo(10, 5, (progress + 1) * 100);
                else{
                    gogogo(degree, -1, (progress + 1) * 100);
                }

                shakePeriod = (progress + 1) * 100;
                shakePeriodTempProgress = progress;
            }

            public void onStartTrackingTouch(SeekBar arg0) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        popDialog.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                });
        popDialog.create();
        popDialog.show();
    }

    @Override
    public void onMShakeWork(int degree) {

        if (isShackMode) {
            gogogo(degree, 10, shakePeriod);
            if (canVibrate) {
                myVibrator.vibrate(1500);
            }
            Log.e("degree", degree + "");
        }
    }

    @Override
    public int getRequestedOrientation() {
        return super.getRequestedOrientation();
    }

    public void gogogo(int degree, int repeat_count, int shakePeriod) {
        int iRandomNum_1 = (int) (Math.random() * shakePeriod);
        int iRandomNum_2 = (int) (Math.random() * shakePeriod);
        int iRandomNum_3 = (int) (Math.random() * shakePeriod);

        this.degree = degree;
        setAnimation(shakePeriod + iRandomNum_1, this.degree, repeat_count, ivBearHead, HEAD);
        setAnimation(shakePeriod + iRandomNum_1, this.degree, repeat_count, ivBearBody, BODY);

        setAnimation(shakePeriod + iRandomNum_2, this.degree, repeat_count, ivMoonHead, HEAD);
        setAnimation(shakePeriod + iRandomNum_2, this.degree, repeat_count, ivMoonBody, BODY);

        setAnimation(shakePeriod + iRandomNum_3, this.degree, repeat_count, ivRabbitHead, HEAD);
        setAnimation(shakePeriod + iRandomNum_3, this.degree, repeat_count, ivRabbitBody, BODY);
    }


    // 感光偵測
    int type = 1;

    @Override
    public void onSensorChanged(SensorEvent event) {

        int light_num = 0;
        if (event == null) {
            light_num = 1;
        } else {
            light_num = (int) (event.values[0]);
        }


        String msg = "現在的亮度是 " + light_num + " Lux";
        Log.d("感光元件:", msg);

        if (isLightMode) {
            if (light_num > 1000 && type != 1) {
                type = 1;
                gogogo(180, -1, shakePeriod);
            } else if (light_num < 1000 && light_num > 500 && type != 2) {
                type = 2;
                gogogo(90, -1, shakePeriod);
            } else if (light_num < 500 && light_num > 200 && type != 3) {
                type = 3;
                gogogo(60, -1, shakePeriod);
            } else if (light_num < 200 && light_num > 100 && type != 4) {
                type = 4;
                gogogo(40, -1, shakePeriod);
            } else if (light_num < 100 && light_num > 50 && type != 5) {
                type = 5;
                gogogo(30, -1, shakePeriod);
            } else if (light_num < 50 && light_num > 20 && type != 6) {
                type = 6;
                gogogo(20, -1, shakePeriod);
            } else if (light_num < 20 && light_num > 10 && type != 7) {
                type = 7;
                gogogo(15, -1, shakePeriod);
            } else if (light_num < 10 && light_num > 1 && type != 8) {
                type = 8;
                gogogo(10, -1, shakePeriod);
            } else if (light_num == 1 && type != 9) {
                type = 9;
                gogogo(5, -1, shakePeriod);
            }
            Log.e("目前感光TYPE", type + "");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //聲音偵測

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_SOUND_BIG:
                gogogo(90, 10, shakePeriod);
                break;
                case MSG_SOUND_MEDIUM:
                    gogogo(45, 10, shakePeriod);
                    break;
                case MSG_SOUND_LOW:
                    gogogo(20, 10, shakePeriod);
                    break;
            }
        }
    };



    private MediaRecorder mRecorder = null;
    Boolean isFirstTime = true;
    int type_sound = 0;
    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (isRunning) {
                if (isSoundMode == true) {
                    try {
                        int temptMaxAmplitude = mRecorder.getMaxAmplitude();
                        if (temptMaxAmplitude > 20000) {
                            handler.sendEmptyMessage(MSG_SOUND_BIG);

                        } else if (temptMaxAmplitude > 10000) {
                            handler.sendEmptyMessage(MSG_SOUND_MEDIUM);

                        } else if (temptMaxAmplitude > 5000) {
                            handler.sendEmptyMessage(MSG_SOUND_LOW);
                        }

                        Log.e("聲音強度:", "" + temptMaxAmplitude);

                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });


    @Override
    protected void onDestroy() {
        isRunning = false;
        if(thread != null){
            thread.interrupt();
            thread = null;
        }
if(mRecorder != null) {

    mRecorder.release();

}
        super.onDestroy();
    }



    public void detectSoundLevel() {

        if (isFirstTime == true) {

            thread.start();
            isFirstTime = false;

            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            try {
                mRecorder.prepare();
                mRecorder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        shakeWorker.uiOnResume();
        if (this.sensor != null) {
            this.mgr.registerListener(this,
                    mgr.getDefaultSensor(Sensor.TYPE_LIGHT),
                    SensorManager.SENSOR_DELAY_UI);
        }
        if (isMusicPlaying == true) {
            mp.start();
        }
    }

    @Override
    protected void onPause() {
        shakeWorker.uiOnPause();
        if (this.sensor != null) {
            mgr.unregisterListener(this);
        }
        mp.pause();
        super.onPause();

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_rabbit_head:
                special_rabbit++;
                if (special_rabbit == 2 && special_bear == 5) {
                    special_bear = 0;

                    mp_unlock = MediaPlayer.create(this, R.raw.carol_unlock);
                    mp_unlock.start();
                    mp.release();
                    mp = MediaPlayer.create(this, R.raw.happy_birthday);
                    mp.setLooping(false);
                    musicMenuItem.setTitle("關閉音樂");
                    isMusicPlaying = true;
                    mp.start();
                    Toast.makeText(this, "恭喜你解鎖了Carol公仔!‧★,:*:‧\\(￣▽￣)/‧:*‧°★*", Toast.LENGTH_LONG).show();
                    ivRabbitHead.setImageDrawable(getResources().getDrawable(
                            R.drawable.carolhead));
                    ivRabbitBody.setImageDrawable(getResources().getDrawable(
                            R.drawable.carol_body));
                    ivBg.setImageDrawable(getResources().getDrawable(
                            R.drawable.birthday));
                    gogogo(degree, -1, shakePeriod);
                }
                break;

            case R.id.iv_moon_head:
                special_moon++;
                if (special_moon == 10 && special_bear == 11) {
                    special_bear = 0;

                    mp_unlock = MediaPlayer.create(this, R.raw.m_unlock);
                    mp_unlock.start();
                    Toast.makeText(this, "恭喜你解鎖了隱藏人物!!", Toast.LENGTH_SHORT).show();
                    ivMoonHead.setImageDrawable(getResources().getDrawable(
                            R.drawable.m_head));
                    ivMoonBody.setImageDrawable(getResources().getDrawable(
                            R.drawable.m_body));
                    ivBg.setImageDrawable(getResources().getDrawable(
                            R.drawable.baseball));
                }
                break;
            case R.id.iv_bear_head:
                special_bear++;
                break;
            case R.id.iv_bear_body:
                special_bearBody++;

                if(special_bearBody == 7){

                    mp_unlock = MediaPlayer.create(this, R.raw.ahh);
                    mp_unlock.start();
                    mp.release();
                    mp = MediaPlayer.create(this, R.raw.banana);
                    mp.setLooping(false);
                    musicMenuItem.setTitle("關閉音樂");
                    isMusicPlaying = true;
                    mp.start();
                    Toast.makeText(this, "恭喜你解鎖了小小兵!  (⊙ˍ⊙)", Toast.LENGTH_SHORT).show();
                    ivBearHead.setImageDrawable(getResources().getDrawable(
                            R.drawable.minions_head));
                    ivBearBody.setImageDrawable(getResources().getDrawable(
                            R.drawable.minions_body));
                }
            default:
                break;
        }
    }
}
