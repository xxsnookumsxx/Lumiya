package com.lumiyaviewer.lumiya.voice.model;

import com.google.common.base.Objects;
import java.util.UUID;

/**
 * Voice login credentials obtained from a provisioning server.
 */
public class VoiceLoginCredentials {
    private final String sipUriHostname;
    private final String accountServerName;
    private final UUID agentUUID;
    private final String username;
    private final String password;

    public VoiceLoginCredentials(String sipUriHostname,
                                 String accountServerName,
                                 UUID agentUUID,
                                 String username,
                                 String password) {
        this.sipUriHostname = sipUriHostname;
        this.accountServerName = accountServerName;
        this.agentUUID = agentUUID;
        this.username = username;
        this.password = password;
    }

    public String getSipUriHostname() { return sipUriHostname; }
    public String getAccountServerName() { return accountServerName; }
    public UUID getAgentUUID() { return agentUUID; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VoiceLoginCredentials)) return false;
        VoiceLoginCredentials that = (VoiceLoginCredentials) o;
        return Objects.equal(agentUUID, that.agentUUID);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(agentUUID);
    }

    @Override
    public String toString() {
        return "VoiceLoginCredentials{" +
                "sipUriHostname='" + sipUriHostname + '\'' +
                ", agentUUID=" + agentUUID +
                '}';
    }
}
