package com.lumiyaviewer.lumiya.slproto.modules.rlv.commands;

import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommand;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommands;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVController;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVRestrictionType;
import java.util.HashSet;
import java.util.UUID;

/* loaded from: classes.dex */
public class RLVCmdClear implements RLVCommand {
    @Override // com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommand
    public void Handle(RLVController rLVController, UUID uuid, RLVCommands rLVCommands, String str, String str2) {
        String filter = (str != null && !str.isEmpty()) ? str.toLowerCase() : (str2 != null ? str2.toLowerCase() : "");
        HashSet<RLVRestrictionType> hashSet = new HashSet<>();
        for (RLVRestrictionType rLVRestrictionType : RLVRestrictionType.valuesCustom()) {
            if (filter.isEmpty() || rLVRestrictionType.toString().contains(filter)) {
                hashSet.add(rLVRestrictionType);
            }
        }
        rLVController.getRestrictions().removeRestrictions(uuid, hashSet);
    }
}
