package com.lumiyaviewer.lumiya.ui.voice;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.lumiyaviewer.lumiya.R;

public class VoiceStatusView_ViewBinding implements Unbinder {
    private VoiceStatusView target;
    private View viewAnswerButton;
    private View viewTerminateButton;
    private View viewBluetoothButton;
    private View viewMicOnButton;
    private View viewLoudspeakerButton;
    private View viewMicOffButton;
    private View viewStatusCardView;

    @UiThread
    public VoiceStatusView_ViewBinding(VoiceStatusView voiceStatusView) {
        this(voiceStatusView, voiceStatusView);
    }

    @UiThread
    public VoiceStatusView_ViewBinding(final VoiceStatusView voiceStatusView, View view) {
        this.target = voiceStatusView;
        View answerView = Utils.findRequiredView(view, R.id.voice_answer_button, "field 'voiceAnswerButton' and method 'onVoiceAnswerButton'");
        voiceStatusView.voiceAnswerButton = (ImageButton) Utils.castView(answerView, R.id.voice_answer_button, "field 'voiceAnswerButton'", ImageButton.class);
        this.viewAnswerButton = answerView;
        answerView.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View v) {
                voiceStatusView.onVoiceAnswerButton();
            }
        });

        View terminateView = Utils.findRequiredView(view, R.id.voice_terminate_button, "field 'voiceTerminateButton' and method 'onVoiceTerminateButton'");
        voiceStatusView.voiceTerminateButton = (ImageButton) Utils.castView(terminateView, R.id.voice_terminate_button, "field 'voiceTerminateButton'", ImageButton.class);
        this.viewTerminateButton = terminateView;
        terminateView.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View v) {
                voiceStatusView.onVoiceTerminateButton();
            }
        });

        voiceStatusView.voiceStatusSmallText = (TextView) Utils.findRequiredViewAsType(view, R.id.voice_status_small_text, "field 'voiceStatusSmallText'", TextView.class);

        View btView = Utils.findRequiredView(view, R.id.voice_bluetooth_button, "field 'voiceBluetoothButton' and method 'onVoiceBluetoothButton'");
        voiceStatusView.voiceBluetoothButton = (Button) Utils.castView(btView, R.id.voice_bluetooth_button, "field 'voiceBluetoothButton'", Button.class);
        this.viewBluetoothButton = btView;
        btView.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View v) {
                voiceStatusView.onVoiceBluetoothButton();
            }
        });

        voiceStatusView.voiceSpeakIndicatorLeft = (ImageView) Utils.findRequiredViewAsType(view, R.id.voice_speak_indicator_left, "field 'voiceSpeakIndicatorLeft'", ImageView.class);

        View micOnView = Utils.findRequiredView(view, R.id.voice_mic_on_button, "field 'voiceMicOnButton' and method 'onVoiceMicOnButton'");
        voiceStatusView.voiceMicOnButton = (ImageButton) Utils.castView(micOnView, R.id.voice_mic_on_button, "field 'voiceMicOnButton'", ImageButton.class);
        this.viewMicOnButton = micOnView;
        micOnView.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View v) {
                voiceStatusView.onVoiceMicOnButton();
            }
        });

        voiceStatusView.voiceStatusControls = (ViewGroup) Utils.findRequiredViewAsType(view, R.id.voice_status_controls, "field 'voiceStatusControls'", ViewGroup.class);

        View speakerView = Utils.findRequiredView(view, R.id.voice_loudspeaker_button, "field 'voiceLoudspeakerButton' and method 'onLoudspeakerButton'");
        voiceStatusView.voiceLoudspeakerButton = (Button) Utils.castView(speakerView, R.id.voice_loudspeaker_button, "field 'voiceLoudspeakerButton'", Button.class);
        this.viewLoudspeakerButton = speakerView;
        speakerView.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View v) {
                voiceStatusView.onLoudspeakerButton();
            }
        });

        voiceStatusView.voiceStatusText = (TextView) Utils.findRequiredViewAsType(view, R.id.voice_status_text, "field 'voiceStatusText'", TextView.class);
        voiceStatusView.voiceSpeakIndicatorRight = (ImageView) Utils.findRequiredViewAsType(view, R.id.voice_speak_indicator_right, "field 'voiceSpeakIndicatorRight'", ImageView.class);

        View micOffView = Utils.findRequiredView(view, R.id.voice_mic_off_button, "field 'voiceMicOffButton' and method 'onVoiceMicOffButton'");
        voiceStatusView.voiceMicOffButton = (ImageButton) Utils.castView(micOffView, R.id.voice_mic_off_button, "field 'voiceMicOffButton'", ImageButton.class);
        this.viewMicOffButton = micOffView;
        micOffView.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View v) {
                voiceStatusView.onVoiceMicOffButton();
            }
        });

        View cardView = Utils.findRequiredView(view, R.id.voice_status_card_view, "field 'voiceStatusCardView' and method 'onVoiceStatusCardClick'");
        voiceStatusView.voiceStatusCardView = (CardView) Utils.castView(cardView, R.id.voice_status_card_view, "field 'voiceStatusCardView'", CardView.class);
        this.viewStatusCardView = cardView;
        cardView.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View v) {
                voiceStatusView.onVoiceStatusCardClick();
            }
        });

        voiceStatusView.voiceSpeakerVolumeControl = (SeekBar) Utils.findRequiredViewAsType(view, R.id.voice_speaker_volume_control, "field 'voiceSpeakerVolumeControl'", SeekBar.class);
    }

    @Override
    @CallSuper
    public void unbind() {
        VoiceStatusView voiceStatusView = this.target;
        if (voiceStatusView == null) {
            throw new IllegalStateException("Bindings already cleared.");
        }
        this.target = null;
        voiceStatusView.voiceAnswerButton = null;
        voiceStatusView.voiceTerminateButton = null;
        voiceStatusView.voiceStatusSmallText = null;
        voiceStatusView.voiceBluetoothButton = null;
        voiceStatusView.voiceSpeakIndicatorLeft = null;
        voiceStatusView.voiceMicOnButton = null;
        voiceStatusView.voiceStatusControls = null;
        voiceStatusView.voiceLoudspeakerButton = null;
        voiceStatusView.voiceStatusText = null;
        voiceStatusView.voiceSpeakIndicatorRight = null;
        voiceStatusView.voiceMicOffButton = null;
        voiceStatusView.voiceStatusCardView = null;
        voiceStatusView.voiceSpeakerVolumeControl = null;

        this.viewAnswerButton.setOnClickListener(null);
        this.viewAnswerButton = null;
        this.viewTerminateButton.setOnClickListener(null);
        this.viewTerminateButton = null;
        this.viewBluetoothButton.setOnClickListener(null);
        this.viewBluetoothButton = null;
        this.viewMicOnButton.setOnClickListener(null);
        this.viewMicOnButton = null;
        this.viewLoudspeakerButton.setOnClickListener(null);
        this.viewLoudspeakerButton = null;
        this.viewMicOffButton.setOnClickListener(null);
        this.viewMicOffButton = null;
        this.viewStatusCardView.setOnClickListener(null);
        this.viewStatusCardView = null;
    }
}
