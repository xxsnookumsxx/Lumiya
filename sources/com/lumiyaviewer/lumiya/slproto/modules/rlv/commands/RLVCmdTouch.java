package com.lumiyaviewer.lumiya.slproto.modules.rlv.commands;

import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommand;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommands;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVController;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVRestrictionType;
import java.util.UUID;

/* loaded from: classes.dex */
public class RLVCmdTouch implements RLVCommand {
    @Override // com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommand
    public void Handle(RLVController rLVController, UUID uuid, RLVCommands rLVCommands, String str, String str2) {
        String option = str2 != null ? str2.trim() : "";
        RLVRestrictionType restType;
        String target = "";
        switch (rLVCommands) {
            case touch:
            case touchall:
                restType = RLVRestrictionType.touchall;
                break;
            case touchworld:
                restType = RLVRestrictionType.touchworld;
                target = option;
                break;
            case touchthis:
                restType = RLVRestrictionType.touchthis;
                target = option.isEmpty() ? uuid.toString() : option;
                break;
            case touchme:
                restType = RLVRestrictionType.touchme;
                break;
            case fartouch:
            case touchfar:
                restType = RLVRestrictionType.fartouch;
                break;
            default:
                return;
        }
        if ("n".equals(str) || "add".equals(str)) {
            rLVController.getRestrictions().addRestriction(restType, uuid, target);
        } else if ("y".equals(str) || "rem".equals(str)) {
            rLVController.getRestrictions().removeRestriction(restType, uuid, target);
        }
    }
}
