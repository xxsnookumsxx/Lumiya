package com.lumiyaviewer.lumiya.slproto.modules.rlv;

import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdAcceptTeleport;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdAddOutfit;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdAttach;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdAvatar;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdClear;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdDetach;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdEditObjects;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdFindFolder;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdGetAttach;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdGetInv;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdGetOutfit;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdGetStatus;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdGroup;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdRecvChat;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdRecvIM;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdRedirChat;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdRemoveOutfit;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdRezObjects;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdSendChannel;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdSendChat;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdSendIM;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdShowInventory;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdSit;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdTeleportLandmark;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdTeleportLocation;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdTeleportLure;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdTeleportSit;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdTeleportTo;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdTouch;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdUnsit;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdVersion;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdViewNotecard;

/* loaded from: classes.dex */
public enum RLVCommands {
    version(RLVCmdVersion.class),
    versionnew(RLVCmdVersion.class),
    versionnum(RLVCmdVersion.class),
    clear(RLVCmdClear.class),
    detach(RLVCmdDetach.class),
    remattach(RLVCmdDetach.class),
    attach(RLVCmdAttach.class),
    attachover(RLVCmdAttach.class),
    attachoverorreplace(RLVCmdAttach.class),
    attachall(RLVCmdAttach.class),
    attachallover(RLVCmdAttach.class),
    detachall(RLVCmdAttach.class),
    attachthis(RLVCmdAttach.class),
    attachallthis(RLVCmdAttach.class),
    detachthis(RLVCmdAttach.class),
    detachallthis(RLVCmdAttach.class),
    getinv(RLVCmdGetInv.class),
    getinvworn(RLVCmdGetInv.class),
    findfolder(RLVCmdFindFolder.class),
    findfolders(RLVCmdFindFolder.class),
    getpath(RLVCmdFindFolder.class),
    getpathnew(RLVCmdFindFolder.class),
    sendchat(RLVCmdSendChat.class),
    recvchat(RLVCmdRecvChat.class),
    sendim(RLVCmdSendIM.class),
    recvim(RLVCmdRecvIM.class),
    tplm(RLVCmdTeleportLandmark.class),
    tploc(RLVCmdTeleportLocation.class),
    sittp(RLVCmdTeleportSit.class),
    tplure(RLVCmdTeleportLure.class),
    tpto(RLVCmdTeleportTo.class),
    accepttp(RLVCmdAcceptTeleport.class),
    showinv(RLVCmdShowInventory.class),
    viewnote(RLVCmdViewNotecard.class),
    edit(RLVCmdEditObjects.class),
    rez(RLVCmdRezObjects.class),
    unsit(RLVCmdUnsit.class),
    sit(RLVCmdSit.class),
    remoutfit(RLVCmdRemoveOutfit.class),
    getoutfit(RLVCmdGetOutfit.class),
    addoutfit(RLVCmdAddOutfit.class),
    getattach(RLVCmdGetAttach.class),
    getstatus(RLVCmdGetStatus.class),
    sendchannel(RLVCmdSendChannel.class),
    redirchat(RLVCmdRedirChat.class),
    rediremote(RLVCmdAvatar.class),
    touch(RLVCmdTouch.class),
    touchall(RLVCmdTouch.class),
    touchworld(RLVCmdTouch.class),
    touchthis(RLVCmdTouch.class),
    touchme(RLVCmdTouch.class),
    fartouch(RLVCmdTouch.class),
    touchfar(RLVCmdTouch.class),
    getgroup(RLVCmdGroup.class),
    setgroup(RLVCmdGroup.class),
    setrot(RLVCmdAvatar.class),
    adjustheight(RLVCmdAvatar.class),
    standtp(RLVCmdAvatar.class),
    shownames(RLVCmdAvatar.class),
    shownametags(RLVCmdAvatar.class);

    private Class<? extends RLVCommand> handler;

    RLVCommands(Class<? extends RLVCommand> cls) {
        this.handler = cls;
    }

    public static RLVCommands getCommand(String str) {
        if (str == null) {
            return null;
        }
        try {
            return valueOf(str.trim().toLowerCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /* renamed from: values, reason: to resolve conflict with enum method */
    public static RLVCommands[] valuesCustom() {
        return values();
    }

    public RLVCommand getHandler() {
        try {
            return this.handler.newInstance();
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException e) {
            return null;
        }
    }
}
