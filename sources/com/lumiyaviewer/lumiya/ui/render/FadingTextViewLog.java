package com.lumiyaviewer.lumiya.ui.render;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.common.base.Strings;
import com.lumiyaviewer.lumiya.slproto.chat.generic.SLChatEvent;
import com.lumiyaviewer.lumiya.slproto.users.manager.ActiveChattersManager;
import com.lumiyaviewer.lumiya.slproto.users.manager.UserManager;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class FadingTextViewLog {
    private static final long STALE_CHAT_TIMEOUT = 5000;
    private final LinearLayout chatsOverlayLayout;
    private final Context context;
    private final int logBackgroundColor;
    private final int logTextColor;
    private final UserManager userManager;
    private final Handler mHandler = new Handler();
    private final Map<Long, ChatEventOverlay> chatEventOverlays = new LinkedHashMap();
    private boolean removeStaleChatsPosted = false;
    private final Runnable RemoveStaleChatsTask = new Runnable() { // from class: com.lumiyaviewer.lumiya.ui.render.FadingTextViewLog.1
        @Override // java.lang.Runnable
        public void run() {
            FadingTextViewLog.this.removeStaleChatsPosted = false;
            if (FadingTextViewLog.this.chatsOverlayLayout != null) {
                long jUptimeMillis = SystemClock.uptimeMillis();
                Iterator it = FadingTextViewLog.this.chatEventOverlays.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    if (entry != null) {
                        if (jUptimeMillis < ((ChatEventOverlay) entry.getValue()).timestamp + FadingTextViewLog.STALE_CHAT_TIMEOUT) {
                            break;
                        }
                        final TextView textView = ((ChatEventOverlay) entry.getValue()).textView;
                        if (Build.VERSION.SDK_INT >= 14) {
                            textView.animate().alpha(0.0f).setDuration(1000L).setListener(new AnimatorListenerAdapter() { // from class: com.lumiyaviewer.lumiya.ui.render.FadingTextViewLog.1.1
                                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                                public void onAnimationEnd(Animator animator) {
                                    FadingTextViewLog.this.chatsOverlayLayout.removeView(textView);
                                }
                            }).start();
                        } else {
                            FadingTextViewLog.this.chatsOverlayLayout.removeView(textView);
                        }
                        it.remove();
                    }
                }
            }
            FadingTextViewLog.this.postRemovingStaleChats();
        }
    };

    FadingTextViewLog(UserManager userManager, Context context, LinearLayout linearLayout, int i, int i2) {
        this.userManager = userManager;
        this.context = context;
        this.chatsOverlayLayout = linearLayout;
        this.logTextColor = i;
        this.logBackgroundColor = i2;
    }

    void clearChatEvents() {
        if (this.chatsOverlayLayout != null) {
            this.chatsOverlayLayout.removeAllViews();
        }
        this.chatEventOverlays.clear();
    }

    void handleChatEvent(ActiveChattersManager.ChatMessageEvent chatMessageEvent) {
        TextView textView = null;
        SLChatEvent sLChatEventLoadFromDatabaseObject = SLChatEvent.loadFromDatabaseObject(chatMessageEvent.chatMessage, this.userManager.getUserID());
        if (sLChatEventLoadFromDatabaseObject != null) {
            CharSequence plainTextMessage = sLChatEventLoadFromDatabaseObject.getPlainTextMessage(this.context, this.userManager, false);
            String string = plainTextMessage != null ? plainTextMessage.toString() : null;
            if (Strings.isNullOrEmpty(string)) {
                return;
            }
            String str = chatMessageEvent.isPrivate ? "[IM] " + string : string;
            if (chatMessageEvent.isNewMessage) {
                DisplayMetrics displayMetrics = this.context.getResources().getDisplayMetrics();
                int iApplyDimension = (int) TypedValue.applyDimension(1, 10.0f, displayMetrics);
                int iApplyDimension2 = (int) TypedValue.applyDimension(1, 5.0f, displayMetrics);
                int iApplyDimension3 = (int) TypedValue.applyDimension(1, 10.0f, displayMetrics);
                int iApplyDimension4 = (int) TypedValue.applyDimension(1, 5.0f, displayMetrics);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
                layoutParams.setMargins(iApplyDimension, iApplyDimension2, iApplyDimension, iApplyDimension2);
                textView = new TextView(this.context);
                textView.setBackgroundColor(this.logBackgroundColor);
                textView.setTextColor(this.logTextColor);
                textView.setLayoutParams(layoutParams);
                textView.setPadding(iApplyDimension3, iApplyDimension4, iApplyDimension3, iApplyDimension4);
                this.chatsOverlayLayout.addView(textView);
                this.chatEventOverlays.put(chatMessageEvent.chatMessage.getId(), new ChatEventOverlay(SystemClock.uptimeMillis(), textView));
                postRemovingStaleChats();
            } else {
                ChatEventOverlay chatEventOverlay = this.chatEventOverlays.get(chatMessageEvent.chatMessage.getId());
                if (chatEventOverlay != null) {
                    textView = chatEventOverlay.textView;
                }
            }
            if (textView != null) {
                textView.setText(str);
            }
        }
    }

    void postRemovingStaleChats() {
        Map.Entry<Long, ChatEventOverlay> next;
        if (this.removeStaleChatsPosted) {
            return;
        }
        Iterator<Map.Entry<Long, ChatEventOverlay>> it = this.chatEventOverlays.entrySet().iterator();
        if (!it.hasNext() || (next = it.next()) == null) {
            return;
        }
        this.removeStaleChatsPosted = true;
        this.mHandler.postAtTime(this.RemoveStaleChatsTask, next.getValue().timestamp + STALE_CHAT_TIMEOUT);
    }
}
