package com.lumiyaviewer.lumiya.slproto.modules.rlv.commands;

import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommand;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommands;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVController;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVRestrictionType;
import java.util.UUID;

/* loaded from: classes.dex */
public class RLVCmdAttach implements RLVCommand {
    @Override // com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommand
    public void Handle(RLVController rLVController, UUID uuid, RLVCommands rLVCommands, String str, String str2) {
        String option = str2 != null ? str2.trim() : "";
        if ("force".equals(str)) {
            switch (rLVCommands) {
                case attach:
                    rLVController.attachRLVFolder(option, false, true);
                    break;
                case attachover:
                case attachoverorreplace:
                    rLVController.attachRLVFolder(option, false, false);
                    break;
                case attachall:
                    rLVController.attachRLVFolder(option, true, true);
                    break;
                case attachallover:
                    rLVController.attachRLVFolder(option, true, false);
                    break;
                case detachall:
                    rLVController.detachRLVFolder(option, true);
                    break;
                default:
                    break;
            }
            return;
        }
        String folderPath = option.isEmpty() ? rLVController.getRLVPathForTarget(uuid, "", false) : rLVController.normalizeRLVPath(option);
        if (folderPath == null || folderPath.isEmpty()) {
            return;
        }
        RLVRestrictionType restType;
        switch (rLVCommands) {
            case attachthis:
                restType = RLVRestrictionType.attachthis;
                break;
            case attachallthis:
                restType = RLVRestrictionType.attachallthis;
                break;
            case detachthis:
                restType = RLVRestrictionType.detachthis;
                break;
            case detachallthis:
                restType = RLVRestrictionType.detachallthis;
                break;
            default:
                return;
        }
        if ("n".equals(str) || "add".equals(str)) {
            rLVController.getRestrictions().addRestriction(restType, uuid, folderPath);
        } else if ("y".equals(str) || "rem".equals(str)) {
            rLVController.getRestrictions().removeRestriction(restType, uuid, folderPath);
        }
    }
}
