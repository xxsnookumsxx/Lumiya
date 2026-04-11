package com.lumiyaviewer.lumiya.ui.render;

import android.annotation.SuppressLint;
import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.lumiyaviewer.lumiya.R;
import com.lumiyaviewer.lumiya.ui.voice.VoiceStatusView;

/* JADX INFO: loaded from: classes.dex */
public class CardboardActivity_ViewBinding implements Unbinder {
    private CardboardActivity target;
    private View view2131755255;
    private View view2131755256;
    private View view2131755257;
    private View view2131755260;
    private View view2131755261;
    private View view2131755262;
    private View view2131755263;
    private View view2131755264;
    private View view2131755265;
    private View view2131755266;
    private View view2131755268;
    private View view2131755269;
    private View view2131755270;
    private View view2131755272;
    private View view2131755274;
    private View view2131755275;
    private View view2131755277;
    private View view2131755281;
    private View view2131755282;

    @UiThread
    public CardboardActivity_ViewBinding(CardboardActivity cardboardActivity) {
        this(cardboardActivity, cardboardActivity.getWindow().getDecorView());
    }

    @UiThread
    @SuppressLint({"ClickableViewAccessibility"})
    public CardboardActivity_ViewBinding(final CardboardActivity cardboardActivity, View view) {
        this.target = cardboardActivity;
        View viewFindRequiredView = Utils.findRequiredView(view, R.id.button_speak, "field 'buttonSpeak' and method 'onSpeakButton'");
        cardboardActivity.buttonSpeak = (ImageButton) Utils.castView(viewFindRequiredView, R.id.button_speak, "field 'buttonSpeak'", ImageButton.class);
        this.view2131755256 = viewFindRequiredView;
        viewFindRequiredView.setOnClickListener(new DebouncingOnClickListener() { // from class: com.lumiyaviewer.lumiya.ui.render.CardboardActivity_ViewBinding.1
            @Override // butterknife.internal.DebouncingOnClickListener
            public void doClick(View view2) {
                cardboardActivity.onSpeakButton();
            }
        });
        View viewFindRequiredView2 = Utils.findRequiredView(view, R.id.button_chat, "field 'buttonChat' and method 'onChatButton'");
        cardboardActivity.buttonChat = (ImageButton) Utils.castView(viewFindRequiredView2, R.id.button_chat, "field 'buttonChat'", ImageButton.class);
        this.view2131755257 = viewFindRequiredView2;
        viewFindRequiredView2.setOnClickListener(new DebouncingOnClickListener() { // from class: com.lumiyaviewer.lumiya.ui.render.CardboardActivity_ViewBinding.2
            @Override // butterknife.internal.DebouncingOnClickListener
            public void doClick(View view2) {
                cardboardActivity.onChatButton();
            }
        });
        View viewFindRequiredView3 = Utils.findRequiredView(view, R.id.button_speech_send, "field 'buttonSpeechSend' and method 'onSpeechSendButton'");
        cardboardActivity.buttonSpeechSend = (ImageButton) Utils.castView(viewFindRequiredView3, R.id.button_speech_send, "field 'buttonSpeechSend'", ImageButton.class);
        this.view2131755281 = viewFindRequiredView3;
        viewFindRequiredView3.setOnClickListener(new DebouncingOnClickListener() { // from class: com.lumiyaviewer.lumiya.ui.render.CardboardActivity_ViewBinding.3
            @Override // butterknife.internal.DebouncingOnClickListener
            public void doClick(View view2) {
                cardboardActivity.onSpeechSendButton();
            }
        });
        cardboardActivity.speechRecognitionResults = (TextView) Utils.findRequiredViewAsType(view, R.id.speech_recognition_results, "field 'speechRecognitionResults'", TextView.class);
        cardboardActivity.chatsOverlayLayout = (LinearLayout) Utils.findRequiredViewAsType(view, R.id.cardboard_ims, "field 'chatsOverlayLayout'", LinearLayout.class);
        View viewFindRequiredView4 = Utils.findRequiredView(view, R.id.cardboard_aim_controls, "field 'cardboardAimControls' and method 'onAimControlsTouch'");
        cardboardActivity.cardboardAimControls = (ViewGroup) Utils.castView(viewFindRequiredView4, R.id.cardboard_aim_controls, "field 'cardboardAimControls'", ViewGroup.class);
        this.view2131755265 = viewFindRequiredView4;
        viewFindRequiredView4.setOnTouchListener(new View.OnTouchListener() { // from class: com.lumiyaviewer.lumiya.ui.render.CardboardActivity_ViewBinding.4
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view2, MotionEvent motionEvent) {
                return cardboardActivity.onAimControlsTouch(view2, motionEvent);
            }
        });
        View viewFindRequiredView5 = Utils.findRequiredView(view, R.id.cardboard_speak_controls, "field 'cardboardSpeakControls' and method 'onSpeakControlsTouch'");
        cardboardActivity.cardboardSpeakControls = (ViewGroup) Utils.castView(viewFindRequiredView5, R.id.cardboard_speak_controls, "field 'cardboardSpeakControls'", ViewGroup.class);
        this.view2131755277 = viewFindRequiredView5;
        viewFindRequiredView5.setOnTouchListener(new View.OnTouchListener() { // from class: com.lumiyaviewer.lumiya.ui.render.CardboardActivity_ViewBinding.5
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view2, MotionEvent motionEvent) {
                return cardboardActivity.m778com_lumiyaviewer_lumiya_ui_render_CardboardActivitymthref6(view2, motionEvent);
            }
        });
        View viewFindRequiredView6 = Utils.findRequiredView(view, R.id.cardboard_object_controls, "field 'cardboardObjectControls' and method 'onObjectControlsTouch'");
        cardboardActivity.cardboardObjectControls = (ViewGroup) Utils.castView(viewFindRequiredView6, R.id.cardboard_object_controls, "field 'cardboardObjectControls'", ViewGroup.class);
        this.view2131755266 = viewFindRequiredView6;
        viewFindRequiredView6.setOnTouchListener(new View.OnTouchListener() { // from class: com.lumiyaviewer.lumiya.ui.render.CardboardActivity_ViewBinding.6
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view2, MotionEvent motionEvent) {
                return cardboardActivity.m779com_lumiyaviewer_lumiya_ui_render_CardboardActivitymthref7(view2, motionEvent);
            }
        });
        View viewFindRequiredView7 = Utils.findRequiredView(view, R.id.object_touch_button, "field 'buttonTouchObject' and method 'onObjectTouch'");
        cardboardActivity.buttonTouchObject = (ImageButton) Utils.castView(viewFindRequiredView7, R.id.object_touch_button, "field 'buttonTouchObject'", ImageButton.class);
        this.view2131755268 = viewFindRequiredView7;
        viewFindRequiredView7.setOnClickListener(new DebouncingOnClickListener() { // from class: com.lumiyaviewer.lumiya.ui.render.CardboardActivity_ViewBinding.7
            @Override // butterknife.internal.DebouncingOnClickListener
            public void doClick(View view2) {
                cardboardActivity.onObjectTouch();
            }
        });
        View viewFindRequiredView8 = Utils.findRequiredView(view, R.id.cardboard_script_dialog, "field 'cardboardScriptDialog' and method 'onScriptDialogOutsideTouch'");
        cardboardActivity.cardboardScriptDialog = (ViewGroup) Utils.castView(viewFindRequiredView8, R.id.cardboard_script_dialog, "field 'cardboardScriptDialog'", ViewGroup.class);
        this.view2131755282 = viewFindRequiredView8;
        viewFindRequiredView8.setOnTouchListener(new View.OnTouchListener() { // from class: com.lumiyaviewer.lumiya.ui.render.CardboardActivity_ViewBinding.8
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view2, MotionEvent motionEvent) {
                return cardboardActivity.m780com_lumiyaviewer_lumiya_ui_render_CardboardActivitymthref8(view2, motionEvent);
            }
        });
        cardboardActivity.speakLevelIndicator = (ProgressBar) Utils.findRequiredViewAsType(view, R.id.speak_level_indicator, "field 'speakLevelIndicator'", ProgressBar.class);
        View viewFindRequiredView9 = Utils.findRequiredView(view, R.id.cardboard_yes_button, "field 'yesButton' and method 'onYesButton'");
        cardboardActivity.yesButton = (ImageButton) Utils.castView(viewFindRequiredView9, R.id.cardboard_yes_button, "field 'yesButton'", ImageButton.class);
        this.view2131755274 = viewFindRequiredView9;
        viewFindRequiredView9.setOnClickListener(new DebouncingOnClickListener() { // from class: com.lumiyaviewer.lumiya.ui.render.CardboardActivity_ViewBinding.9
            @Override // butterknife.internal.DebouncingOnClickListener
            public void doClick(View view2) {
                cardboardActivity.onYesButton();
            }
        });
        View viewFindRequiredView10 = Utils.findRequiredView(view, R.id.object_chat_button, "field 'buttonObjectChat' and method 'onObjectChat'");
        cardboardActivity.buttonObjectChat = (ImageButton) Utils.castView(viewFindRequiredView10, R.id.object_chat_button, "field 'buttonObjectChat'", ImageButton.class);
        this.view2131755270 = viewFindRequiredView10;
        viewFindRequiredView10.setOnClickListener(new DebouncingOnClickListener() { // from class: com.lumiyaviewer.lumiya.ui.render.CardboardActivity_ViewBinding.10
            @Override // butterknife.internal.DebouncingOnClickListener
            public void doClick(View view2) {
                cardboardActivity.onObjectChat();
            }
        });
        cardboardActivity.yesNoText = (TextView) Utils.findRequiredViewAsType(view, R.id.cardboard_yesno_text, "field 'yesNoText'", TextView.class);
        cardboardActivity.cardboardDetailsPage = (ViewGroup) Utils.findRequiredViewAsType(view, R.id.cardboard_details_page, "field 'cardboardDetailsPage'", ViewGroup.class);
        cardboardActivity.moveButtonsLayout = (ViewGroup) Utils.findRequiredViewAsType(view, R.id.move_buttons_layout, "field 'moveButtonsLayout'", ViewGroup.class);
        View viewFindRequiredView11 = Utils.findRequiredView(view, R.id.button_stand_up, "field 'buttonStandUp' and method 'onStandUpButton'");
        cardboardActivity.buttonStandUp = (ImageButton) Utils.castView(viewFindRequiredView11, R.id.button_stand_up, "field 'buttonStandUp'", ImageButton.class);
        this.view2131755264 = viewFindRequiredView11;
        viewFindRequiredView11.setOnClickListener(new DebouncingOnClickListener() { // from class: com.lumiyaviewer.lumiya.ui.render.CardboardActivity_ViewBinding.11
            @Override // butterknife.internal.DebouncingOnClickListener
            public void doClick(View view2) {
                cardboardActivity.onStandUpButton();
            }
        });
        cardboardActivity.speakNowText = (TextView) Utils.findRequiredViewAsType(view, R.id.speak_now_text, "field 'speakNowText'", TextView.class);
        View viewFindRequiredView12 = Utils.findRequiredView(view, R.id.button_move_forward, "field 'buttonMoveForward' and method 'onCamButtonTouch'");
        cardboardActivity.buttonMoveForward = (ImageButton) Utils.castView(viewFindRequiredView12, R.id.button_move_forward, "field 'buttonMoveForward'", ImageButton.class);
        this.view2131755260 = viewFindRequiredView12;
        viewFindRequiredView12.setOnTouchListener(new View.OnTouchListener() { // from class: com.lumiyaviewer.lumiya.ui.render.CardboardActivity_ViewBinding.12
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view2, MotionEvent motionEvent) {
                return cardboardActivity.onCamButtonTouch(view2, motionEvent);
            }
        });
        cardboardActivity.dialogQuestionText = (TextView) Utils.findRequiredViewAsType(view, R.id.dialogQuestionText, "field 'dialogQuestionText'", TextView.class);
        cardboardActivity.objectNameView = (TextView) Utils.findRequiredViewAsType(view, R.id.cardboard_object_name, "field 'objectNameView'", TextView.class);
        View viewFindRequiredView13 = Utils.findRequiredView(view, R.id.button_move_backward, "field 'buttonMoveBackward' and method 'onCamButtonTouch'");
        cardboardActivity.buttonMoveBackward = (ImageButton) Utils.castView(viewFindRequiredView13, R.id.button_move_backward, "field 'buttonMoveBackward'", ImageButton.class);
        this.view2131755262 = viewFindRequiredView13;
        viewFindRequiredView13.setOnTouchListener(new View.OnTouchListener() { // from class: com.lumiyaviewer.lumiya.ui.render.CardboardActivity_ViewBinding.13
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view2, MotionEvent motionEvent) {
                return cardboardActivity.onCamButtonTouch(view2, motionEvent);
            }
        });
        cardboardActivity.voiceStatusView = (VoiceStatusView) Utils.findRequiredViewAsType(view, R.id.cardboard_voice_status_view, "field 'voiceStatusView'", VoiceStatusView.class);
        View viewFindRequiredView14 = Utils.findRequiredView(view, R.id.cardboard_no_button, "field 'noButton' and method 'onNoButton'");
        cardboardActivity.noButton = (ImageButton) Utils.castView(viewFindRequiredView14, R.id.cardboard_no_button, "field 'noButton'", ImageButton.class);
        this.view2131755275 = viewFindRequiredView14;
        viewFindRequiredView14.setOnClickListener(new DebouncingOnClickListener() { // from class: com.lumiyaviewer.lumiya.ui.render.CardboardActivity_ViewBinding.14
            @Override // butterknife.internal.DebouncingOnClickListener
            public void doClick(View view2) {
                cardboardActivity.onNoButton();
            }
        });
        View viewFindRequiredView15 = Utils.findRequiredView(view, R.id.button_touch, "field 'buttonTouch' and method 'onTouchButton'");
        cardboardActivity.buttonTouch = (ImageButton) Utils.castView(viewFindRequiredView15, R.id.button_touch, "field 'buttonTouch'", ImageButton.class);
        this.view2131755255 = viewFindRequiredView15;
        viewFindRequiredView15.setOnClickListener(new DebouncingOnClickListener() { // from class: com.lumiyaviewer.lumiya.ui.render.CardboardActivity_ViewBinding.15
            @Override // butterknife.internal.DebouncingOnClickListener
            public void doClick(View view2) {
                cardboardActivity.onTouchButton();
            }
        });
        View viewFindRequiredView16 = Utils.findRequiredView(view, R.id.button_turn_left, "field 'buttonTurnLeft' and method 'onCamButtonTouch'");
        cardboardActivity.buttonTurnLeft = (ImageButton) Utils.castView(viewFindRequiredView16, R.id.button_turn_left, "field 'buttonTurnLeft'", ImageButton.class);
        this.view2131755261 = viewFindRequiredView16;
        viewFindRequiredView16.setOnTouchListener(new View.OnTouchListener() { // from class: com.lumiyaviewer.lumiya.ui.render.CardboardActivity_ViewBinding.16
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view2, MotionEvent motionEvent) {
                return cardboardActivity.onCamButtonTouch(view2, motionEvent);
            }
        });
        View viewFindRequiredView17 = Utils.findRequiredView(view, R.id.button_turn_right, "field 'buttonTurnRight' and method 'onCamButtonTouch'");
        cardboardActivity.buttonTurnRight = (ImageButton) Utils.castView(viewFindRequiredView17, R.id.button_turn_right, "field 'buttonTurnRight'", ImageButton.class);
        this.view2131755263 = viewFindRequiredView17;
        viewFindRequiredView17.setOnTouchListener(new View.OnTouchListener() { // from class: com.lumiyaviewer.lumiya.ui.render.CardboardActivity_ViewBinding.17
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view2, MotionEvent motionEvent) {
                return cardboardActivity.onCamButtonTouch(view2, motionEvent);
            }
        });
        View viewFindRequiredView18 = Utils.findRequiredView(view, R.id.object_sit_button, "field 'buttonSit' and method 'onObjectSit'");
        cardboardActivity.buttonSit = (ImageButton) Utils.castView(viewFindRequiredView18, R.id.object_sit_button, "field 'buttonSit'", ImageButton.class);
        this.view2131755269 = viewFindRequiredView18;
        viewFindRequiredView18.setOnClickListener(new DebouncingOnClickListener() { // from class: com.lumiyaviewer.lumiya.ui.render.CardboardActivity_ViewBinding.18
            @Override // butterknife.internal.DebouncingOnClickListener
            public void doClick(View view2) {
                cardboardActivity.onObjectSit();
            }
        });
        cardboardActivity.cardboardPrimaryControls = (ViewGroup) Utils.findRequiredViewAsType(view, R.id.cardboard_primary_controls, "field 'cardboardPrimaryControls'", ViewGroup.class);
        View viewFindRequiredView19 = Utils.findRequiredView(view, R.id.cardboard_yesno_dialog, "method 'onYesNoOutsideTouch'");
        this.view2131755272 = viewFindRequiredView19;
        viewFindRequiredView19.setOnTouchListener(new View.OnTouchListener() { // from class: com.lumiyaviewer.lumiya.ui.render.CardboardActivity_ViewBinding.19
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view2, MotionEvent motionEvent) {
                return cardboardActivity.m781com_lumiyaviewer_lumiya_ui_render_CardboardActivitymthref9(view2, motionEvent);
            }
        });
    }

    @Override // butterknife.Unbinder
    @CallSuper
    public void unbind() {
        CardboardActivity cardboardActivity = this.target;
        if (cardboardActivity == null) {
            throw new IllegalStateException("Bindings already cleared.");
        }
        this.target = null;
        cardboardActivity.buttonSpeak = null;
        cardboardActivity.buttonChat = null;
        cardboardActivity.buttonSpeechSend = null;
        cardboardActivity.speechRecognitionResults = null;
        cardboardActivity.chatsOverlayLayout = null;
        cardboardActivity.cardboardAimControls = null;
        cardboardActivity.cardboardSpeakControls = null;
        cardboardActivity.cardboardObjectControls = null;
        cardboardActivity.buttonTouchObject = null;
        cardboardActivity.cardboardScriptDialog = null;
        cardboardActivity.speakLevelIndicator = null;
        cardboardActivity.yesButton = null;
        cardboardActivity.buttonObjectChat = null;
        cardboardActivity.yesNoText = null;
        cardboardActivity.cardboardDetailsPage = null;
        cardboardActivity.moveButtonsLayout = null;
        cardboardActivity.buttonStandUp = null;
        cardboardActivity.speakNowText = null;
        cardboardActivity.buttonMoveForward = null;
        cardboardActivity.dialogQuestionText = null;
        cardboardActivity.objectNameView = null;
        cardboardActivity.buttonMoveBackward = null;
        cardboardActivity.voiceStatusView = null;
        cardboardActivity.noButton = null;
        cardboardActivity.buttonTouch = null;
        cardboardActivity.buttonTurnLeft = null;
        cardboardActivity.buttonTurnRight = null;
        cardboardActivity.buttonSit = null;
        cardboardActivity.cardboardPrimaryControls = null;
        this.view2131755256.setOnClickListener(null);
        this.view2131755256 = null;
        this.view2131755257.setOnClickListener(null);
        this.view2131755257 = null;
        this.view2131755281.setOnClickListener(null);
        this.view2131755281 = null;
        this.view2131755265.setOnTouchListener(null);
        this.view2131755265 = null;
        this.view2131755277.setOnTouchListener(null);
        this.view2131755277 = null;
        this.view2131755266.setOnTouchListener(null);
        this.view2131755266 = null;
        this.view2131755268.setOnClickListener(null);
        this.view2131755268 = null;
        this.view2131755282.setOnTouchListener(null);
        this.view2131755282 = null;
        this.view2131755274.setOnClickListener(null);
        this.view2131755274 = null;
        this.view2131755270.setOnClickListener(null);
        this.view2131755270 = null;
        this.view2131755264.setOnClickListener(null);
        this.view2131755264 = null;
        this.view2131755260.setOnTouchListener(null);
        this.view2131755260 = null;
        this.view2131755262.setOnTouchListener(null);
        this.view2131755262 = null;
        this.view2131755275.setOnClickListener(null);
        this.view2131755275 = null;
        this.view2131755255.setOnClickListener(null);
        this.view2131755255 = null;
        this.view2131755261.setOnTouchListener(null);
        this.view2131755261 = null;
        this.view2131755263.setOnTouchListener(null);
        this.view2131755263 = null;
        this.view2131755269.setOnClickListener(null);
        this.view2131755269 = null;
        this.view2131755272.setOnTouchListener(null);
        this.view2131755272 = null;
    }
}
