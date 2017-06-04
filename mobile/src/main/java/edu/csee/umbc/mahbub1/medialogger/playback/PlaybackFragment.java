package edu.csee.umbc.mahbub1.medialogger.playback;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;

import java.util.concurrent.TimeUnit;

import edu.csee.umbc.mahbub1.medialogger.R;
import edu.csee.umbc.mahbub1.medialogger.common.database.RecordingItemDb;
import edu.csee.umbc.mahbub1.medialogger.common.recording.SoundRecorder;

//import edu.csee.umbc.mahbub1.medialogger.common.recording.RecordingItem;

/**
 * Created by Daniel on 1/1/2015.
 */
public class PlaybackFragment extends DialogFragment implements SoundRecorder.OnVoicePlaybackStateChangedListener{

    private static final String TAG = "PlaybackFragment";

    private static final String ARG_ITEM = "recording_item";
    private RecordingItemDb item;

    private Handler mHandler = new Handler();

    private MediaPlayer mMediaPlayer = null;

    private SeekBar mSeekBar = null;
    private FloatingActionButton mPlayButton = null;
    private TextView mCurrentProgressTextView = null;
    private TextView mFileNameTextView = null;
    private TextView mFileLengthTextView = null;


    private SoundRecorder mSoundRecorder;
    private AppState mState = AppState.READY;
    private PlaybackThread mPlaybackThread;
    private static final int REQUEST_RECORD_AUDIO = 13;
    //stores whether or not the mediaplayer is currently playing audio
    private boolean isPlaying = false;
    String VOICE_FILE_NAME;

    //stores minutes and seconds of the length of the file.
    long minutes = 0;
    long seconds = 0;

    enum AppState {
        READY, PLAYING_VOICE, PLAYING_MUSIC, RECORDING
    }
    public PlaybackFragment newInstance(RecordingItemDb item) {
        PlaybackFragment f = new PlaybackFragment();
        Bundle b = new Bundle();
        b.putParcelable(ARG_ITEM, item);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        item = getArguments().getParcelable(ARG_ITEM);

        long itemDuration = item.getLength();
        minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration)
                - TimeUnit.MINUTES.toSeconds(minutes);
        //this.clipDir = TransferUtils.setFileNameAndPath(getActivity())+File.separator;



    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_media_playback, null);

        mFileNameTextView = (TextView) view.findViewById(R.id.file_name_text_view);
        mFileLengthTextView = (TextView) view.findViewById(R.id.file_length_text_view);
        mCurrentProgressTextView = (TextView) view.findViewById(R.id.current_progress_text_view);

        mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
        ColorFilter filter = new LightingColorFilter
                (getResources().getColor(R.color.primary), getResources().getColor(R.color.primary));
        mSeekBar.getProgressDrawable().setColorFilter(filter);
        mSeekBar.getThumb().setColorFilter(filter);

        //VOICE_FILE_NAME = TransferUtils.getAlbumStorageDir("cliprecorder") + File.separator + item.getName();
        VOICE_FILE_NAME = item.getName();
        start();

        /*mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mMediaPlayer != null && fromUser) {
                    mMediaPlayer.seekTo(progress);
                    mHandler.removeCallbacks(mRunnable);

                    long minutes = TimeUnit.MILLISECONDS.toMinutes(mMediaPlayer.getCurrentPosition());
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(mMediaPlayer.getCurrentPosition())
                            - TimeUnit.MINUTES.toSeconds(minutes);
                    mCurrentProgressTextView.setText(String.format("%02d:%02d", minutes,seconds));

                    updateSeekBar();

                } else if (mMediaPlayer == null && fromUser) {
                    prepareMediaPlayerFromPoint(progress);
                    updateSeekBar();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(mMediaPlayer != null) {
                    // remove message Handler from updating progress bar
                    mHandler.removeCallbacks(mRunnable);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mMediaPlayer != null) {
                    mHandler.removeCallbacks(mRunnable);
                    mMediaPlayer.seekTo(seekBar.getProgress());

                    long minutes = TimeUnit.MILLISECONDS.toMinutes(mMediaPlayer.getCurrentPosition());
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(mMediaPlayer.getCurrentPosition())
                            - TimeUnit.MINUTES.toSeconds(minutes);
                    mCurrentProgressTextView.setText(String.format("%02d:%02d", minutes,seconds));
                    updateSeekBar();
                }
            }
        });*/
       /* String clipPath= TransferUtils.getAlbumStorageDir("cliprecorder") + File.separator + item.getName();
        short[] samples = null;
        try {
            samples = getAudioSample(clipPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (samples != null) {
            final FloatingActionButton playFab = (FloatingActionButton) view.findViewById(R.id.fab_play);

            mPlaybackThread = new PlaybackThread(samples, new PlaybackListener() {
                @Override
                public void onProgress(int progress) {
                    mPlaybackView.setMarkerPosition(progress);
                }
                @Override
                public void onCompletion() {
                    mPlaybackView.setMarkerPosition(mPlaybackView.getAudioLength());
                    playFab.setImageResource(android.R.drawable.ic_media_play);
                }
            });
            mPlaybackView.setChannels(1);
            mPlaybackView.setSampleRate(PlaybackThread.SAMPLE_RATE);
            mPlaybackView.setSamples(samples);

            playFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mPlaybackThread.playing()) {
                        mPlaybackThread.startPlayback();
                        playFab.setImageResource(android.R.drawable.ic_media_pause);
                    } else {
                        mPlaybackThread.stopPlayback();
                        playFab.setImageResource(android.R.drawable.ic_media_play);
                    }
                }
            });
        }*/
        mPlayButton = (FloatingActionButton) view.findViewById(R.id.fab_play);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mState==AppState.PLAYING_VOICE){
                    mPlayButton.setImageResource(R.drawable.ic_media_play);//.setImageResource(R.drawable.ic_media_pause);
                    mState = AppState.READY;
                    mSoundRecorder.stopPlaying();

                }else if(mState == AppState.READY) {
                    mPlayButton.setImageResource(R.drawable.ic_media_pause);
                    mState = AppState.PLAYING_VOICE;
                    //mUiState = state;
                    mSoundRecorder.startPlay();

                }
            }
        });

        mFileNameTextView.setText(item.getName());
        mFileLengthTextView.setText(String.format("%02d:%02d", minutes,seconds));

        builder.setView(view);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return builder.create();
}

    @Override
    public void onStart() {
        super.onStart();

        //set transparent background
        Window window = getDialog().getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);

        //disable buttons from dialog
        AlertDialog alertDialog = (AlertDialog) getDialog();
        alertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
        alertDialog.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(false);
        alertDialog.getButton(Dialog.BUTTON_NEUTRAL).setEnabled(false);
    }
    private void start() {
        mSoundRecorder = new SoundRecorder(getActivity(), VOICE_FILE_NAME, this);
        /*int[] thumbResources = new int[] {R.id.mic, R.id.play, R.id.music};
        ImageView[] thumbs = new ImageView[3];
        for(int i=0; i < 3; i++) {
            thumbs[i] = (ImageView) findViewById(thumbResources[i]);
        }
        View containerView = findViewById(R.id.container);
        ImageView expandedView = (ImageView) findViewById(R.id.expanded);
        int animationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        mUIAnimation = new UIAnimation(containerView, thumbs, expandedView, animationDuration,
                this);*/
    }
    @Override
    public void onPause() {
        super.onPause();

        if (mState != AppState.PLAYING_VOICE) {
            mSoundRecorder.stopPlaying();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mState != AppState.PLAYING_VOICE) {
            mSoundRecorder.stopPlaying();
        }
    }

   /* // Play start/stop
    private void onPlay(boolean isPlaying){
        if (!isPlaying) {
            //currently MediaPlayer is not playing audio
            if(mMediaPlayer == null) {
                startPlaying(); //start from beginning
            } else {
                resumePlaying(); //resume the currently paused MediaPlayer
            }

        } else {
            //pause the MediaPlayer
            pausePlaying();
        }
    }*/

    /*private void startPlaying() {
        mPlayButton.setImageResource(R.drawable.ic_media_pause);
        mMediaPlayer = new MediaPlayer();

        try {
            String clipPath= TransferUtils.getAlbumStorageDir("cliprecorder") + File.separator + item.getName();
            //mMediaPlayer.setDataSource(item.getFilePath());

            //Log.d(TAG, "clipspath"+clipPath);
            mMediaPlayer.setDataSource(clipPath);
            mMediaPlayer.prepare();
            mSeekBar.setMax(mMediaPlayer.getDuration());

            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed"+e.getLocalizedMessage());
        }

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopPlaying();
            }
        });

        updateSeekBar();

        //keep screen on while playing audio
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }*/



    /*
    * playback PCM
     */

    /*private short[] getAudioSample(String file) throws IOException{
        InputStream is =new FileInputStream(file);// getResources().openRawResource(R.raw.jinglebells);
        byte[] data;
        try {
            data = IOUtils.toByteArray(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }

        ShortBuffer sb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        short[] samples = new short[sb.limit()];
        sb.get(samples);
        return samples;
    }*/



    /*private void prepareMediaPlayerFromPoint(int progress) {
        //set mediaPlayer to start from middle of the audio file

        mMediaPlayer = new MediaPlayer();

        try {
            mMediaPlayer.setDataSource(item.getFilePath());
            mMediaPlayer.prepare();
            mSeekBar.setMax(mMediaPlayer.getDuration());
            mMediaPlayer.seekTo(progress);

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlaying();
                }
            });

        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        //keep screen on while playing audio
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void pausePlaying() {
        mPlayButton.setImageResource(R.drawable.ic_media_play);
        mHandler.removeCallbacks(mRunnable);
        mMediaPlayer.pause();
    }

    private void resumePlaying() {
        mPlayButton.setImageResource(R.drawable.ic_media_pause);
        mHandler.removeCallbacks(mRunnable);
        mMediaPlayer.start();
        updateSeekBar();
    }

    private void stopPlaying() {
        mPlayButton.setImageResource(R.drawable.ic_media_play);
        mHandler.removeCallbacks(mRunnable);
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;

        mSeekBar.setProgress(mSeekBar.getMax());
        isPlaying = !isPlaying;

        mCurrentProgressTextView.setText(mFileLengthTextView.getText());
        mSeekBar.setProgress(mSeekBar.getMax());

        //allow the screen to turn off again once audio is finished playing
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }*/
    @Override
    public void onPlaybackStopped() {
        // mUIAnimation.transitionToHome();
        //mUiState = UIAnimation.UIState.HOME;
        mState = AppState.READY;
    }/*
    //updating mSeekBar
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if(mMediaPlayer != null){

                int mCurrentPosition = mMediaPlayer.getCurrentPosition();
                mSeekBar.setProgress(mCurrentPosition);

                long minutes = TimeUnit.MILLISECONDS.toMinutes(mCurrentPosition);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(mCurrentPosition)
                        - TimeUnit.MINUTES.toSeconds(minutes);
                mCurrentProgressTextView.setText(String.format("%02d:%02d", minutes, seconds));

                updateSeekBar();
            }
        }
    };

    private void updateSeekBar() {
        mHandler.postDelayed(mRunnable, 1000);
    }*/



}
