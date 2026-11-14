package com.lumiyaviewer.lumiya.slproto.modules.rlv.commands;

import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommand;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommands;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVController;
import java.util.UUID;

/* loaded from: classes.dex */
public class RLVCmdGetInv implements RLVCommand {
    @Override // com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommand
    public void Handle(RLVController rLVController, UUID uuid, RLVCommands rLVCommands, String str, String str2) {
        try {
            int channel = Integer.parseInt(str.trim());
            String path = str2 != null ? str2.trim() : "";
            String reply;
            if (rLVCommands == RLVCommands.getinvworn) {
                reply = rLVController.getRLVInvWorn(path);
            } else {
                reply = rLVController.getRLVInv(path);
            }
            rLVController.sayOnChannel(channel, reply);
        } catch (NumberFormatException e) {
            Debug.Warning(e);
        }
    }
}
