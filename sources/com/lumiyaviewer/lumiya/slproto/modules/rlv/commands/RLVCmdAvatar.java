package com.lumiyaviewer.lumiya.slproto.modules.rlv.commands;

import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommand;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommands;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVController;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVRestrictionType;
import java.util.UUID;

/* loaded from: classes.dex */
public class RLVCmdAvatar implements RLVCommand {
    @Override // com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommand
    public void Handle(RLVController rLVController, UUID uuid, RLVCommands rLVCommands, String str, String str2) {
        String option = str2 != null ? str2.trim() : "";
        switch (rLVCommands) {
            case setrot:
                if ("force".equals(str) && !option.isEmpty()) {
                    try {
                        double angleRadians = Double.parseDouble(option);
                        float degrees = 90.0f - ((float) Math.toDegrees(angleRadians));
                        rLVController.getModules().avatarControl.setAgentHeading(degrees);
                    } catch (NumberFormatException e) {
                        Debug.Warning(e);
                    }
                }
                break;
            case adjustheight:
                break;
            case standtp:
                if ("n".equals(str) || "add".equals(str)) {
                    rLVController.recordStandingPosition();
                    rLVController.getRestrictions().addRestriction(RLVRestrictionType.standtp, uuid, "");
                } else if ("y".equals(str) || "rem".equals(str)) {
                    rLVController.getRestrictions().removeRestriction(RLVRestrictionType.standtp, uuid, "");
                }
                break;
            case shownames:
                if ("n".equals(str) || "add".equals(str)) {
                    rLVController.getRestrictions().addRestriction(RLVRestrictionType.shownames, uuid, option);
                } else if ("y".equals(str) || "rem".equals(str)) {
                    rLVController.getRestrictions().removeRestriction(RLVRestrictionType.shownames, uuid, option);
                }
                break;
            case shownametags:
                if ("n".equals(str) || "add".equals(str)) {
                    rLVController.getRestrictions().addRestriction(RLVRestrictionType.shownametags, uuid, option);
                } else if ("y".equals(str) || "rem".equals(str)) {
                    rLVController.getRestrictions().removeRestriction(RLVRestrictionType.shownametags, uuid, option);
                }
                break;
            case rediremote:
                if (!option.isEmpty()) {
                    try {
                        int channel = Integer.parseInt(option);
                        if ("n".equals(str) || "add".equals(str)) {
                            rLVController.getRestrictions().addRestriction(RLVRestrictionType.rediremote, uuid, Integer.toString(channel));
                        } else if ("y".equals(str) || "rem".equals(str)) {
                            rLVController.getRestrictions().removeRestriction(RLVRestrictionType.rediremote, uuid, Integer.toString(channel));
                        }
                    } catch (NumberFormatException e2) {
                        Debug.Warning(e2);
                    }
                }
                break;
            default:
                break;
        }
    }
}
