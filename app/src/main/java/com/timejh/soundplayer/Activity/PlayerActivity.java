package com.timejh.soundplayer.Activity;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.timejh.soundplayer.Adapter.PlayerAdapter;
import com.timejh.soundplayer.Domain.Sound;
import com.timejh.soundplayer.Manager.DataLoader;
import com.timejh.soundplayer.R;

import java.util.List;

public class PlayerActivity extends AppCompatActivity {

    ViewPager viewPager;
    ImageButton btnRew, btnPlay, btnFf;

    List<Sound> datas;
    PlayerAdapter adapter;

    MediaPlayer player;
    SeekBar seekBar;
    TextView txtDuration, txtCurrent;

    // 플레이어 상태 플래그
    private static final int PLAY = 0;
    private static final int PAUSE = 1;
    private static final int STOP = 2;

    // 현재 플레이어 상태
    private static int playStatus = STOP;

    // 현재 음원 index
    int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        playStatus = STOP;

        // 볼륨 조절 버튼으로 미디어 음량만 조절하기 위한 설정
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        // seekBar 의 변경사항을 체크하는 리스너 등록
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        txtDuration = (TextView) findViewById(R.id.txtDuration);
        txtCurrent = (TextView) findViewById(R.id.txtCurrent);

        btnRew = (ImageButton) findViewById(R.id.btnRew);
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnFf = (ImageButton) findViewById(R.id.btnFf);

        btnRew.setOnClickListener(clickListener);
        btnPlay.setOnClickListener(clickListener);
        btnFf.setOnClickListener(clickListener);

        // 0. 데이터 가져오기
        datas = DataLoader.getSounds(this);

        // 1. 뷰페이저 가져오기
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        // 2. 뷰페이저용 아답터 생성
        adapter = new PlayerAdapter(datas, this);
        // 3. 뷰페이저 아답터 연결
        viewPager.setAdapter(adapter);
        // 4. 뷰페이지 리스너 연결
        viewPager.addOnPageChangeListener(viewPagerListener);
        // * 페이지 트랜스포머 연결
        viewPager.setPageTransformer(false, pageTransformer);
        // 5. 특정 페이지 호출
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            position = bundle.getInt("position");
            // 첫페이지일 경우만 init 호출
            // 이유 : 첫페이지가 아닐경우 위의 setCurrentItem 에 의해서 ViewPager의 onPageSelected가 호출된다.
            if (position == 0) {
                init();
            } else {
                // 0 페이지가 아닐경우 해당페이지로 이동한다. 이동후 listener 에서 init 이 자동으로 호출된다.
                viewPager.setCurrentItem(position);
            }
        }
    }

    // 컨트롤러 정보 초기화
    private void init() {
        // 뷰페이저로 이동할 경우 플레이어에 세팅된 값을 해제한후 로직을 실행한다.
        if (player != null) {
            // 플레어 상태를 STOP 으로 변경
            playStatus = STOP;
            // 아이콘을 플레이 버튼으로 변경
            btnPlay.setImageResource(android.R.drawable.ic_media_play);
            player.release();
        }
        playerInit();

        controllerInit();

        play();
    }

    private void playerInit() {
        Uri musicUri = datas.get(position).music_uri;
        // 플레이어에 음원 세팅
        player = MediaPlayer.create(this, musicUri);
        player.setLooping(false); // 반복여부
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                next();
            }
        });
    }

    private void controllerInit() {
        seekBar.setMax(player.getDuration()); // seekBar 길이설정
        seekBar.setProgress(0); // seekBar 현재값 0으로
        txtDuration.setText(covertMiliToTime(player.getDuration()) + ""); // 전체 플레이시간 설정
        txtCurrent.setText("0"); // 현재 플레이시간을 0으로 설정
    }

    private void play() {
        // 플레이중이 아니면 음악 실행
        switch (playStatus) {
            case STOP:
                playStop();
                break;
            // 플레이중이면 멈춤
            case PLAY:
                playPlay();
                break;
            // 멈춤상태이면 거기서 부터 재생
            case PAUSE:
                playPause();
                break;
        }
    }

    private void playStop() {
        player.start();

        playStatus = PLAY;
        btnPlay.setImageResource(android.R.drawable.ic_media_pause);
        // 새로운 쓰레드로 스타트
        Thread thread = new TimerThread();
        thread.start();
    }

    private void playPlay() {
        player.pause();
        playStatus = PAUSE;
        btnPlay.setImageResource(android.R.drawable.ic_media_play);
    }

    private void playPause() {
        player.start();

        playStatus = PLAY;
        btnPlay.setImageResource(android.R.drawable.ic_media_pause);
    }

    // 시간 포맷 변경 00:00
    private String covertMiliToTime(long mili) {
        long min = mili / 1000 / 60;
        long sec = mili / 1000 % 60;

        return String.format("%02d", min) + ":" + String.format("%02d", sec);
    }

    private void prev() {
        if (position > 0)
            viewPager.setCurrentItem(position - 1);
    }

    private void next() {
        if (position < datas.size())
            viewPager.setCurrentItem(position + 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release(); // 사용이 끝나면 해제해야만 한다.
        }
        playStatus = STOP;
    }

    // 버튼 클릭 리스너
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnPlay:
                    play();
                    break;
                case R.id.btnRew:
                    prev();
                    break;
                case R.id.btnFf:
                    next();
                    break;
            }
        }
    };

    // 뷰페이저 체인지 리스너
    ViewPager.OnPageChangeListener viewPagerListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            PlayerActivity.this.position = position;
            init();
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    // SeekBar 체인지 리스너
    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (player != null && fromUser)
                player.seekTo(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    // 페이지 트랜스포머
    ViewPager.PageTransformer pageTransformer = new ViewPager.PageTransformer() {
        @Override
        public void transformPage(View page, float position) {
            float normalizedposition = Math.abs(1 - Math.abs(position));

            page.setAlpha(normalizedposition);  //View의 투명도 조절
            page.setScaleX(normalizedposition / 2 + 0.5f); //View의 x축 크기조절
            page.setScaleY(normalizedposition / 2 + 0.5f); //View의 y축 크기조절
            page.setRotationY(position * 80); //View의 Y축(세로축) 회전 각도
        }
    };

    // sub thread 를 생성해서 mediaplayer 의 현재 포지션 값으로 seekbar 를 변경해준다. 매 1초마다
    // sub thread 에서 동작할 로직 정의
    class TimerThread extends Thread {
        @Override
        public void run() {
            while (playStatus < STOP) {
                if (player != null) {
                    // 이 부분은 메인쓰레드에서 동작하도록 Runnable instance를 메인쓰레드에 던져준다
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                seekBar.setProgress(player.getCurrentPosition());
                                txtCurrent.setText(covertMiliToTime(player.getCurrentPosition()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}