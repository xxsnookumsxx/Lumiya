package com.lumiyaviewer.lumiya.slproto.modules.rlv.commands;

import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommand;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommands;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVController;
import java.util.UUID;

/* loaded from: classes.dex */
public class RLVCmdFindFolder implements RLVCommand {
    @Override // com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommand
    public void Handle(RLVController rLVController, UUID uuid, RLVCommands rLVCommands, String str, String str2) {
        try {
            int channel = Integer.parseInt(str.trim());
            String option = str2 != null ? str2.trim() : "";
            String reply;
            switch (rLVCommands) {
                case findfolder:
                    reply = rLVController.findRLVFolders(option, false);
                    break;
                case findfolders:
                    reply = rLVController.findRLVFolders(option, true);
                    break;
                case getpath:
                    reply = rLVController.getRLVPathForTarget(uuid, option, false);
                    break;
                case getpathnew:
                    reply = rLVController.getRLVPathForTarget(uuid, option, true);
                    break;
                default:
                    return;
            }
            rLVController.sayOnChannel(channel, reply != null ? reply : "");
        } catch (NumberFormatException e) {
            Debug.Warning(e);
        }
    }
}
