package com.lumiyaviewer.lumiya.voice.model;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Immutable state snapshot for a voice chat session on a chatter.
 */
public class VoiceChatInfo {
    @Nullable
    public final UUID activeSpeakerID;
    public final boolean isConference;
    public final boolean localMicActive;
    public final int numActiveSpeakers;
    @Nonnull
    public final VoiceChatState previousState;
    @Nonnull
    public final VoiceChatState state;

    private static final Interner<VoiceChatInfo> interner = Interners.newWeakInterner();
    private static final VoiceChatInfo emptyChatState = interner.intern(
            new VoiceChatInfo(VoiceChatState.None, VoiceChatState.None, 0, null, false, false));

    public enum VoiceChatState {
        None,
        Ringing,
        Connecting,
        Active
    }

    private VoiceChatInfo(@Nonnull VoiceChatState state,
                          @Nonnull VoiceChatState previousState,
                          int numActiveSpeakers,
                          @Nullable UUID activeSpeakerID,
                          boolean isConference,
                          boolean localMicActive) {
        this.state = state;
        this.previousState = previousState;
        this.numActiveSpeakers = numActiveSpeakers;
        this.activeSpeakerID = activeSpeakerID;
        this.isConference = isConference;
        this.localMicActive = localMicActive;
    }

    @Nonnull
    public static VoiceChatInfo create(@Nonnull VoiceChatState state,
                                       @Nonnull VoiceChatState previousState,
                                       int numActiveSpeakers,
                                       @Nullable UUID activeSpeakerID,
                                       boolean isConference,
                                       boolean localMicActive) {
        return interner.intern(new VoiceChatInfo(state, previousState, numActiveSpeakers, activeSpeakerID, isConference, localMicActive));
    }

    @Nonnull
    public static VoiceChatInfo empty() {
        return emptyChatState;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        VoiceChatInfo other = (VoiceChatInfo) obj;
        if (this.numActiveSpeakers == other.numActiveSpeakers
                && this.isConference == other.isConference
                && this.localMicActive == other.localMicActive
                && this.state == other.state
                && this.previousState == other.previousState) {
            return this.activeSpeakerID == null
                    ? other.activeSpeakerID == null
                    : this.activeSpeakerID.equals(other.activeSpeakerID);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = this.state.hashCode();
        result = result * 31 + this.previousState.hashCode();
        result = result * 31 + this.numActiveSpeakers;
        result = result * 31 + (this.activeSpeakerID == null ? 0 : this.activeSpeakerID.hashCode());
        result = result * 31 + (this.isConference ? 1 : 0);
        result = result * 31 + (this.localMicActive ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "VoiceChatInfo{state=" + this.state
                + ", previousState=" + this.previousState
                + ", numActiveSpeakers=" + this.numActiveSpeakers
                + ", activeSpeakerID=" + this.activeSpeakerID
                + ", isConference=" + this.isConference
                + ", localMicActive=" + this.localMicActive + '}';
    }
}
