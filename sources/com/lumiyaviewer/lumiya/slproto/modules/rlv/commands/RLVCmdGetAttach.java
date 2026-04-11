package com.lumiyaviewer.lumiya.slproto.modules.rlv.commands;

import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.slproto.avatar.SLAttachmentPoint;
import com.lumiyaviewer.lumiya.slproto.modules.SLAvatarAppearance;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommand;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommands;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVController;
import java.util.UUID;

/* JADX INFO: loaded from: classes.dex */
public class RLVCmdGetAttach implements RLVCommand {
    private static final int NUM_ATTACHMENT_POINTS_LSL = 41;

    @Override // com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommand
    public void Handle(RLVController rLVController, UUID uuid, RLVCommands rLVCommands, String str, String str2) {
        try {
            int i = Integer.parseInt(str);
            SLAvatarAppearance sLAvatarAppearance = rLVController.getModules().avatarAppearance;
            String str3 = "";
            for (int i2 = 0; i2 < 41; i2++) {
                String lowerCase = SLAttachmentPoint.attachmentPoints[i2] != null ? SLAttachmentPoint.attachmentPoints[i2].name.toLowerCase() : "nonexistent";
                if (str2.equals("") || lowerCase.equalsIgnoreCase(str2)) {
                    str3 = sLAvatarAppearance.getAttachmentUUID(i2) != null ? str3 + "1" : str3 + "0";
                }
            }
            rLVController.sayOnChannel(i, str3);
        } catch (NumberFormatException e) {
            Debug.Warning(e);
        }
    }
}
