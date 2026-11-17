package com.lumiyaviewer.lumiya.voice.model;

import com.google.common.base.Objects;
import javax.annotation.Nullable;

/**
 * Metadata describing a Second Life spatial, group, or ad-hoc voice channel.
 */
public class VoiceChannelInfo {
    public final boolean isConference;
    public final boolean isSpatial;
    public final String voiceChannelURI;
    @Nullable
    public final String channelCredentials;

    public VoiceChannelInfo(String voiceChannelURI, boolean isSpatial, boolean isConference) {
        this(voiceChannelURI, null, isSpatial, isConference);
    }

    public VoiceChannelInfo(String voiceChannelURI, @Nullable String channelCredentials, boolean isSpatial, boolean isConference) {
        this.voiceChannelURI = voiceChannelURI;
        this.channelCredentials = channelCredentials;
        this.isSpatial = isSpatial;
        this.isConference = isConference;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        VoiceChannelInfo other = (VoiceChannelInfo) obj;
        return this.isSpatial == other.isSpatial
                && this.isConference == other.isConference
                && Objects.equal(this.voiceChannelURI, other.voiceChannelURI)
                && Objects.equal(this.channelCredentials, other.channelCredentials);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.voiceChannelURI, this.channelCredentials, this.isSpatial, this.isConference);
    }
}
