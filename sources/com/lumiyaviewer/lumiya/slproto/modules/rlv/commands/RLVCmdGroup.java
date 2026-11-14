package com.lumiyaviewer.lumiya.slproto.modules.rlv.commands;

import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommand;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommands;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVController;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVRestrictionType;
import java.util.UUID;

/* loaded from: classes.dex */
public class RLVCmdGroup implements RLVCommand {
    @Override // com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommand
    public void Handle(RLVController rLVController, UUID uuid, RLVCommands rLVCommands, String str, String str2) {
        if (rLVCommands == RLVCommands.getgroup) {
            try {
                int channel = Integer.parseInt(str.trim());
                rLVController.sayOnChannel(channel, rLVController.getActiveGroupName());
            } catch (NumberFormatException e) {
                Debug.Warning(e);
            }
            return;
        }
        if (rLVCommands == RLVCommands.setgroup) {
            if ("force".equals(str)) {
                if (rLVController.getRestrictions().isAllowed(RLVRestrictionType.setgroup, "", null, uuid)) {
                    rLVController.forceActivateGroup(str2 != null ? str2.trim() : "");
                }
            } else if ("n".equals(str) || "add".equals(str)) {
                rLVController.getRestrictions().addRestriction(RLVRestrictionType.setgroup, uuid, "");
            } else if ("y".equals(str) || "rem".equals(str)) {
                rLVController.getRestrictions().removeRestriction(RLVRestrictionType.setgroup, uuid, "");
            }
        }
    }
}
