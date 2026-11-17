package com.lumiyaviewer.lumiya.ui.voice;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.common.base.Objects;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.GridConnectionService;
import com.lumiyaviewer.lumiya.R;
import com.lumiyaviewer.lumiya.react.Subscription;
import com.lumiyaviewer.lumiya.react.SubscriptionData;
import com.lumiyaviewer.lumiya.react.SubscriptionSingleKey;
import com.lumiyaviewer.lumiya.react.UIThreadExecutor;
import com.lumiyaviewer.lumiya.slproto.users.ChatterID;
import com.lumiyaviewer.lumiya.slproto.users.ChatterNameRetriever;
import com.lumiyaviewer.lumiya.slproto.users.manager.UserManager;
import com.lumiyaviewer.lumiya.ui.render.OnHoverListenerCompat;
import com.lumiyaviewer.lumiya.voice.model.VoiceAudioDevice;
import com.lumiyaviewer.lumiya.voice.model.VoiceAudioProperties;
import com.lumiyaviewer.lumiya.voice.model.VoiceBluetoothState;
import com.lumiyaviewer.lumiya.voice.model.VoiceChatInfo;
import javax.annotation.Nullable;

/**
 * In-app voice status card showing call state, speaker indicators, mic toggle, and audio routing controls.
 */
public class VoiceStatusView extends FrameLayout {
    private static final int REQUEST_RECORD_AUDIO = 102;

    @Nullable
    private ChatterID activeChatterID;
    @Nullable
    private ChatterNameRetriever activeChatterNameRetriever;
    private boolean canConnect;
    @Nullable
    private ChatterID chatterID;
    private boolean hoverEnabled;
    private View.OnClickListener onCallButtonListener;
    private boolean showActiveChatterName;
    private boolean showWhenInactive;
    @Nullable
    private ChatterNameRetriever speakerNameRetriever;
    private boolean updatingAudioVolume;

    @BindView(R.id.voice_answer_button)
    ImageButton voiceAnswerButton;
    @BindView(R.id.voice_bluetooth_button)
    Button voiceBluetoothButton;
    @BindView(R.id.voice_loudspeaker_button)
    Button voiceLoudspeakerButton;
    @BindView(R.id.voice_mic_off_button)
    ImageButton voiceMicOffButton;
    @BindView(R.id.voice_mic_on_button)
    ImageButton voiceMicOnButton;
    @BindView(R.id.voice_speak_indicator_left)
    ImageView voiceSpeakIndicatorLeft;
    @BindView(R.id.voice_speak_indicator_right)
    ImageView voiceSpeakIndicatorRight;
    @BindView(R.id.voice_speaker_volume_control)
    SeekBar voiceSpeakerVolumeControl;
    @BindView(R.id.voice_status_card_view)
    CardView voiceStatusCardView;
    @BindView(R.id.voice_status_controls)
    ViewGroup voiceStatusControls;
    @BindView(R.id.voice_status_small_text)
    TextView voiceStatusSmallText;
    @BindView(R.id.voice_status_text)
    TextView voiceStatusText;
    @BindView(R.id.voice_terminate_button)
    ImageButton voiceTerminateButton;

    private final SubscriptionData<SubscriptionSingleKey, VoiceAudioProperties> voiceAudioProperties =
            new SubscriptionData<>(UIThreadExecutor.getInstance(), new Subscription.OnData<VoiceAudioProperties>() {
                @Override
                public void onData(VoiceAudioProperties props) {
                    onVoiceAudioProperties(props);
                }
            });

    private final SubscriptionData<ChatterID, VoiceChatInfo> voiceChatInfo =
            new SubscriptionData<>(UIThreadExecutor.getInstance(), new Subscription.OnData<VoiceChatInfo>() {
                @Override
                public void onData(VoiceChatInfo info) {
                    onVoiceChatInfo(info);
                }
            });

    private final SeekBar.OnSeekBarChangeListener volumeChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser && !updatingAudioVolume) {
                float volume = ((float) progress) / ((float) seekBar.getMax());
                GridConnectionService serviceInstance = GridConnectionService.getServiceInstance();
                if (serviceInstance != null) {
                    serviceInstance.setVoiceSpeakerVolume(volume);
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    private final ChatterNameRetriever.OnChatterNameUpdated onActiveSpeakerNameUpdated = new ChatterNameRetriever.OnChatterNameUpdated() {
        @Override
        public void onChatterNameUpdated(ChatterNameRetriever chatterNameRetriever) {
            Debug.Printf("Voice: chatter name updated: %s", chatterNameRetriever.getResolvedName());
            updateVoiceState();
        }
    };

    public VoiceStatusView(Context context) {
        super(context);
        initView(context);
    }

    public VoiceStatusView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initView(context);
    }

    public VoiceStatusView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        initView(context);
    }

    @TargetApi(21)
    public VoiceStatusView(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        super(context, attributeSet, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.voice_status, (ViewGroup) this, true);
        ButterKnife.bind(this);
        updateVoiceState();
        this.voiceSpeakerVolumeControl.setOnSeekBarChangeListener(this.volumeChangeListener);
    }

    private void onVoiceAudioProperties(VoiceAudioProperties props) {
        Debug.Printf("Voice: voice audio properties updated, bluetooth state %s",
                props != null ? props.bluetoothState : null);
        updateVoiceState();
    }

    private void onVoiceChatInfo(VoiceChatInfo info) {
        updateVoiceState();
    }

    private void updateVoiceState() {
        ChatterID.ChatterIDUser nextActiveSpeaker = null;
        VoiceChatInfo data = this.voiceChatInfo.getData();
        Debug.Printf("VoiceStatusView: voice state %s", data);
        if (data == null || data.state == VoiceChatInfo.VoiceChatState.None) {
            this.voiceStatusControls.setVisibility(View.GONE);
            if (this.showWhenInactive) {
                setVisibility(View.VISIBLE);
                this.voiceStatusText.setText(R.string.voice_not_connected);
                if (this.canConnect) {
                    this.voiceStatusSmallText.setText(R.string.voice_tap_to_connect);
                    this.voiceStatusSmallText.setVisibility(View.VISIBLE);
                } else {
                    this.voiceStatusSmallText.setVisibility(View.GONE);
                }
                this.voiceSpeakIndicatorLeft.setVisibility(View.INVISIBLE);
                this.voiceSpeakIndicatorRight.setVisibility(View.INVISIBLE);
                this.voiceTerminateButton.setVisibility(View.INVISIBLE);
                this.voiceMicOnButton.setVisibility(View.INVISIBLE);
                this.voiceMicOffButton.setVisibility(View.INVISIBLE);
                this.voiceAnswerButton.setVisibility(this.canConnect ? View.VISIBLE : View.INVISIBLE);
            } else {
                setVisibility(View.GONE);
            }
        } else {
            setVisibility(View.VISIBLE);
            this.voiceTerminateButton.setVisibility(View.VISIBLE);
            boolean active = data.state == VoiceChatInfo.VoiceChatState.Active;
            this.voiceMicOnButton.setVisibility((active && data.localMicActive) ? View.VISIBLE : View.INVISIBLE);
            this.voiceMicOffButton.setVisibility((active && !data.localMicActive) ? View.VISIBLE : View.INVISIBLE);
            this.voiceAnswerButton.setVisibility(data.state == VoiceChatInfo.VoiceChatState.Ringing ? View.VISIBLE : View.INVISIBLE);

            String statusText = null;
            if (data.state == VoiceChatInfo.VoiceChatState.Active && data.numActiveSpeakers != 0) {
                if (this.voiceSpeakIndicatorLeft.getVisibility() != View.VISIBLE
                        || this.voiceSpeakIndicatorRight.getVisibility() != View.VISIBLE) {
                    this.voiceSpeakIndicatorLeft.setVisibility(View.VISIBLE);
                    this.voiceSpeakIndicatorRight.setVisibility(View.VISIBLE);
                    Drawable left = this.voiceSpeakIndicatorLeft.getDrawable();
                    if (left instanceof AnimationDrawable) {
                        ((AnimationDrawable) left).start();
                    }
                    Drawable right = this.voiceSpeakIndicatorRight.getDrawable();
                    if (right instanceof AnimationDrawable) {
                        ((AnimationDrawable) right).start();
                    }
                }
                if (data.numActiveSpeakers == 1 && data.activeSpeakerID != null && this.chatterID != null) {
                    nextActiveSpeaker = ChatterID.getUserChatterID(this.chatterID.agentUUID, data.activeSpeakerID);
                    if (this.speakerNameRetriever != null && Objects.equal(nextActiveSpeaker, this.speakerNameRetriever.chatterID)) {
                        statusText = this.speakerNameRetriever.getResolvedName();
                    }
                }
                if (statusText == null && data.numActiveSpeakers != 1) {
                    statusText = getContext().getString(R.string.speakers_speaking, data.numActiveSpeakers);
                }
            } else if (data.state == VoiceChatInfo.VoiceChatState.Ringing) {
                this.voiceSpeakIndicatorLeft.setVisibility(View.VISIBLE);
                this.voiceSpeakIndicatorRight.setVisibility(View.VISIBLE);
            } else {
                this.voiceSpeakIndicatorLeft.setVisibility(View.INVISIBLE);
                this.voiceSpeakIndicatorRight.setVisibility(View.INVISIBLE);
            }

            if (statusText == null) {
                switch (data.state) {
                    case Active:
                        statusText = data.localMicActive
                                ? getContext().getString(R.string.voice_status_speak_now)
                                : getContext().getString(R.string.voice_status_tap_mic);
                        break;
                    case Connecting:
                        statusText = getContext().getString(R.string.voice_status_connecting);
                        break;
                    case Ringing:
                        statusText = getContext().getString(R.string.voice_status_ringing);
                        break;
                    default:
                        break;
                }
            }

            String activeChatterName = (this.showActiveChatterName && this.activeChatterNameRetriever != null)
                    ? this.activeChatterNameRetriever.getResolvedName()
                    : null;
            if (activeChatterName != null) {
                this.voiceStatusSmallText.setVisibility(View.VISIBLE);
                this.voiceStatusText.setText(activeChatterName);
                this.voiceStatusSmallText.setText(statusText);
            } else {
                this.voiceStatusSmallText.setText((CharSequence) null);
                this.voiceStatusText.setText(statusText);
                this.voiceStatusSmallText.setVisibility(View.GONE);
                if (data.state == VoiceChatInfo.VoiceChatState.Active && data.localMicActive) {
                    this.voiceStatusSmallText.setText(R.string.tap_for_audio_controls);
                    this.voiceStatusSmallText.setVisibility(View.VISIBLE);
                }
            }
        }

        if (!Objects.equal(this.activeChatterID, nextActiveSpeaker)) {
            this.activeChatterID = nextActiveSpeaker;
            if (nextActiveSpeaker != null) {
                if (this.speakerNameRetriever == null || !Objects.equal(this.speakerNameRetriever.chatterID, nextActiveSpeaker)) {
                    if (this.speakerNameRetriever != null) {
                        this.speakerNameRetriever.dispose();
                    }
                    this.speakerNameRetriever = new ChatterNameRetriever(nextActiveSpeaker, this.onActiveSpeakerNameUpdated, UIThreadExecutor.getInstance(), false);
                    this.speakerNameRetriever.subscribe();
                }
            } else if (this.speakerNameRetriever != null) {
                this.speakerNameRetriever.dispose();
                this.speakerNameRetriever = null;
            }
        }

        VoiceAudioProperties audioProps = this.voiceAudioProperties.getData();
        if (audioProps != null) {
            Drawable[] btDrawables = this.voiceBluetoothButton.getCompoundDrawables();
            int underlineRes;
            switch (audioProps.bluetoothState) {
                case Active:
                    underlineRes = R.drawable.active_button_underline;
                    break;
                case Connecting:
                    underlineRes = R.drawable.yellow_button_underline;
                    break;
                default:
                    underlineRes = R.drawable.inactive_button_underline;
                    break;
            }
            this.voiceBluetoothButton.setCompoundDrawablesWithIntrinsicBounds(
                    btDrawables[0], null, null, ContextCompat.getDrawable(getContext(), underlineRes));
            this.voiceLoudspeakerButton.setCompoundDrawablesWithIntrinsicBounds(
                    this.voiceLoudspeakerButton.getCompoundDrawables()[0],
                    null,
                    null,
                    ContextCompat.getDrawable(getContext(),
                            audioProps.speakerphoneOn ? R.drawable.active_button_underline : R.drawable.inactive_button_underline));
            this.updatingAudioVolume = true;
            this.voiceSpeakerVolumeControl.setProgress(Math.round(audioProps.speakerVolume * this.voiceSpeakerVolumeControl.getMax()));
            this.updatingAudioVolume = false;
        }
    }

    public void disableMic() {
        VoiceChatInfo data;
        GridConnectionService serviceInstance;
        if (this.chatterID == null
                || (data = this.voiceChatInfo.getData()) == null
                || data.state != VoiceChatInfo.VoiceChatState.Active
                || (serviceInstance = GridConnectionService.getServiceInstance()) == null) {
            return;
        }
        serviceInstance.enableVoiceMic(false);
    }

    public void enableHover(final OnHoverListenerCompat onHoverListenerCompat) {
        if (Build.VERSION.SDK_INT >= 21) {
            this.hoverEnabled = true;
            View.OnHoverListener onHoverListener = new View.OnHoverListener() {
                @Override
                public boolean onHover(View view, MotionEvent motionEvent) {
                    if (!(view instanceof ImageButton)) {
                        return false;
                    }
                    switch (motionEvent.getActionMasked()) {
                        case MotionEvent.ACTION_HOVER_ENTER:
                            onHoverListenerCompat.onHoverEnter(view);
                            break;
                        case MotionEvent.ACTION_HOVER_EXIT:
                            onHoverListenerCompat.onHoverExit(view);
                            break;
                    }
                    return false;
                }
            };
            int paddingPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14.0f, getResources().getDisplayMetrics());
            TypedArray ta = getContext().obtainStyledAttributes(new int[]{R.attr.buttonShapeMoveControl});
            for (ImageButton imageButton : new ImageButton[]{this.voiceAnswerButton, this.voiceTerminateButton, this.voiceMicOnButton, this.voiceMicOffButton}) {
                imageButton.setOnHoverListener(onHoverListener);
                if (imageButton == this.voiceMicOnButton) {
                    imageButton.setBackground(getContext().getDrawable(R.drawable.fab_shape_move_control_green));
                } else {
                    imageButton.setBackground(ta.getDrawable(0));
                }
                imageButton.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                imageButton.setAlpha(0.5f);
            }
            ta.recycle();
        }
    }

    public void hideBackground() {
        this.voiceStatusCardView.setCardBackgroundColor(0);
        this.voiceStatusCardView.setCardElevation(0.0f);
    }

    @OnClick({R.id.voice_loudspeaker_button})
    public void onLoudspeakerButton() {
        VoiceAudioProperties data = this.voiceAudioProperties.getData();
        GridConnectionService serviceInstance = GridConnectionService.getServiceInstance();
        if (serviceInstance == null || data == null) {
            return;
        }
        serviceInstance.setVoiceAudioDevice(data.speakerphoneOn ? VoiceAudioDevice.Default : VoiceAudioDevice.Loudspeaker);
    }

    @OnClick({R.id.voice_answer_button})
    public void onVoiceAnswerButton() {
        requestRecordAudioIfNeeded();
        VoiceChatInfo data = this.voiceChatInfo.getData();
        if (this.onCallButtonListener != null && (this.chatterID == null || data == null || data.state == VoiceChatInfo.VoiceChatState.None)) {
            this.onCallButtonListener.onClick(this.voiceAnswerButton);
        }
        GridConnectionService serviceInstance = GridConnectionService.getServiceInstance();
        if (this.chatterID != null && serviceInstance != null) {
            serviceInstance.acceptVoiceCall(this.chatterID);
        }
    }

    @OnClick({R.id.voice_bluetooth_button})
    public void onVoiceBluetoothButton() {
        VoiceAudioProperties data = this.voiceAudioProperties.getData();
        GridConnectionService serviceInstance = GridConnectionService.getServiceInstance();
        if (serviceInstance == null || data == null) {
            return;
        }
        serviceInstance.setVoiceAudioDevice(
                data.bluetoothState == VoiceBluetoothState.Active ? VoiceAudioDevice.Default : VoiceAudioDevice.Bluetooth);
    }

    @OnClick({R.id.voice_mic_off_button})
    public void onVoiceMicOffButton() {
        requestRecordAudioIfNeeded();
        GridConnectionService serviceInstance = GridConnectionService.getServiceInstance();
        if (serviceInstance != null) {
            serviceInstance.enableVoiceMic(true);
        }
    }

    @OnClick({R.id.voice_mic_on_button})
    public void onVoiceMicOnButton() {
        GridConnectionService serviceInstance = GridConnectionService.getServiceInstance();
        if (serviceInstance != null) {
            serviceInstance.enableVoiceMic(false);
        }
    }

    @OnClick({R.id.voice_status_card_view})
    public void onVoiceStatusCardClick() {
        if (this.voiceStatusControls != null) {
            if (this.voiceStatusControls.getVisibility() == View.VISIBLE) {
                this.voiceStatusControls.setVisibility(View.GONE);
                return;
            }
            this.voiceStatusControls.setVisibility(View.VISIBLE);
            this.voiceStatusControls.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.expand_vertically));
        }
    }

    @OnClick({R.id.voice_terminate_button})
    public void onVoiceTerminateButton() {
        GridConnectionService serviceInstance = GridConnectionService.getServiceInstance();
        if (this.chatterID != null && serviceInstance != null) {
            serviceInstance.terminateVoiceCall(this.chatterID);
        }
    }

    private void requestRecordAudioIfNeeded() {
        Context ctx = getContext();
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        while (ctx instanceof ContextWrapper) {
            if (ctx instanceof Activity) {
                ActivityCompat.requestPermissions((Activity) ctx, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
                return;
            }
            ctx = ((ContextWrapper) ctx).getBaseContext();
        }
    }

    public void setCanConnect(boolean canConnect) {
        this.canConnect = canConnect;
        updateVoiceState();
    }

    public void setChatterID(@Nullable ChatterID chatterID) {
        this.chatterID = chatterID;
        if (chatterID != null) {
            UserManager userManager = chatterID.getUserManager();
            if (userManager != null) {
                this.voiceAudioProperties.subscribe(userManager.getVoiceAudioProperties(), SubscriptionSingleKey.Value);
                this.voiceChatInfo.subscribe(userManager.getVoiceChatInfo(), chatterID);
                if (this.activeChatterNameRetriever != null && !Objects.equal(this.activeChatterNameRetriever.chatterID, chatterID)) {
                    this.activeChatterNameRetriever.dispose();
                    this.activeChatterNameRetriever = null;
                }
                if (this.showActiveChatterName) {
                    this.activeChatterNameRetriever = new ChatterNameRetriever(chatterID, this.onActiveSpeakerNameUpdated, UIThreadExecutor.getInstance(), false);
                    this.activeChatterNameRetriever.subscribe();
                }
            }
        } else {
            this.voiceAudioProperties.unsubscribe();
            this.voiceChatInfo.unsubscribe();
            if (this.speakerNameRetriever != null) {
                this.speakerNameRetriever.dispose();
                this.speakerNameRetriever = null;
            }
            if (this.activeChatterNameRetriever != null) {
                this.activeChatterNameRetriever.dispose();
                this.activeChatterNameRetriever = null;
            }
            GridConnectionService serviceInstance = GridConnectionService.getServiceInstance();
            if (serviceInstance != null) {
                serviceInstance.enableVoiceMic(false);
            }
        }
        updateVoiceState();
    }

    public void setLightTheme() {
        this.voiceStatusText.setTextColor(-1);
        this.voiceStatusSmallText.setTextColor(-1);
        this.voiceAnswerButton.setImageResource(R.drawable.icon_material_voice_call);
        this.voiceMicOnButton.setImageResource(R.drawable.icon_material_mic);
        this.voiceMicOffButton.setImageResource(R.drawable.icon_material_mic_off);
        this.voiceTerminateButton.setImageResource(R.drawable.menu_close_light);
    }

    public void setOnCallButtonListener(View.OnClickListener onClickListener) {
        this.onCallButtonListener = onClickListener;
    }

    public void setShowActiveChatterName(boolean showActiveChatterName) {
        this.showActiveChatterName = showActiveChatterName;
        updateVoiceState();
    }

    public void setShowWhenInactive(boolean showWhenInactive) {
        this.showWhenInactive = showWhenInactive;
        updateVoiceState();
    }
}
