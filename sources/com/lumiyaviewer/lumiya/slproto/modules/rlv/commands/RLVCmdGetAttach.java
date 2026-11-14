package com.lumiyaviewer.lumiya.slproto.modules.rlv.commands;

import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.slproto.avatar.SLAttachmentPoint;
import com.lumiyaviewer.lumiya.slproto.modules.SLAvatarAppearance;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommand;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommands;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVController;
import java.util.UUID;

/* loaded from: classes.dex */
public class RLVCmdGetAttach implements RLVCommand {
    private static final int NUM_ATTACHMENT_POINTS_LSL = SLAttachmentPoint.NUM_ATTACHMENT_POINTS;

    @Override // com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommand
    public void Handle(RLVController rLVController, UUID uuid, RLVCommands rLVCommands, String str, String str2) {
        try {
            int i = Integer.parseInt(str);
            SLAvatarAppearance sLAvatarAppearance = rLVController.getModules().avatarAppearance;
            String normalizedTarget = str2 != null ? str2.trim().replace('_', ' ') : "";
            StringBuilder sb = new StringBuilder(NUM_ATTACHMENT_POINTS_LSL);
            for (int i2 = 0; i2 < NUM_ATTACHMENT_POINTS_LSL; i2++) {
                String lowerCase = SLAttachmentPoint.attachmentPoints[i2] != null ? SLAttachmentPoint.attachmentPoints[i2].name.toLowerCase() : "nonexistent";
                if (normalizedTarget.isEmpty() || lowerCase.equalsIgnoreCase(normalizedTarget)) {
                    sb.append(sLAvatarAppearance.getAttachmentUUID(i2) != null ? '1' : '0');
                }
            }
            rLVController.sayOnChannel(i, sb.toString());
        } catch (NumberFormatException e) {
            Debug.Warning(e);
        }
    }
}
