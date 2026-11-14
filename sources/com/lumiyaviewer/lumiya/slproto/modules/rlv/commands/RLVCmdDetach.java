package com.lumiyaviewer.lumiya.slproto.modules.rlv.commands;

import com.lumiyaviewer.lumiya.slproto.avatar.SLAttachmentPoint;
import com.lumiyaviewer.lumiya.slproto.modules.SLAvatarAppearance;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVController;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVRestrictionType;
import java.util.UUID;

/* loaded from: classes.dex */
public class RLVCmdDetach extends RLVCmdGenericRestriction {
    public RLVCmdDetach() {
        super(RLVRestrictionType.detach, true);
    }

    @Override // com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdGenericRestriction
    protected void HandleForce(RLVController rLVController, UUID uuid, String str) {
        UUID attachmentUUID;
        SLAvatarAppearance sLAvatarAppearance = rLVController.getModules().avatarAppearance;
        String normalized = str != null ? str.trim().replace('_', ' ') : "";
        boolean matchedPoint = false;
        for (int i = 0; i < SLAttachmentPoint.NUM_ATTACHMENT_POINTS; i++) {
            if (SLAttachmentPoint.attachmentPoints[i] != null) {
                String lowerCase = SLAttachmentPoint.attachmentPoints[i].name.toLowerCase();
                if (normalized.isEmpty() || lowerCase.equalsIgnoreCase(normalized)) {
                    matchedPoint = true;
                    if ((attachmentUUID = sLAvatarAppearance.getAttachmentUUID(i)) != null && rLVController.getRestrictions().isAllowed(RLVRestrictionType.detach, lowerCase, attachmentUUID)) {
                        sLAvatarAppearance.DetachItemFromPoint(i);
                    }
                }
            }
        }
        if (!matchedPoint && !normalized.isEmpty()) {
            rLVController.detachRLVFolder(str.trim(), false);
        }
    }
}
