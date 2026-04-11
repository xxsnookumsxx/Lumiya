package com.lumiyaviewer.lumiya.slproto.modules.rlv.commands;

import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommand;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommands;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVController;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVRestrictionType;
import java.util.UUID;

/* JADX INFO: loaded from: classes.dex */
public class RLVCmdGetStatus implements RLVCommand {
    @Override // com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommand
    public void Handle(RLVController rLVController, UUID uuid, RLVCommands rLVCommands, String str, String str2) {
        String str3;
        String str4;
        try {
            int i = Integer.parseInt(str);
            String str5 = str2 != null ? str2 : "";
            int iIndexOf = str5.indexOf(59);
            if (iIndexOf >= 0) {
                String strSubstring = str5.substring(iIndexOf + 1);
                String strSubstring2 = str5.substring(0, iIndexOf);
                str3 = strSubstring;
                str4 = strSubstring2;
            } else {
                str3 = "/";
                str4 = str5;
            }
            String lowerCase = str4.toLowerCase();
            String str6 = "";
            for (RLVRestrictionType rLVRestrictionType : rLVController.getRestrictions().getRestrictionsByObject(uuid)) {
                str6 = (lowerCase.equals("") || rLVRestrictionType.toString().indexOf(lowerCase) >= 0) ? str6 + str3 + rLVRestrictionType.toString() : str6;
            }
            rLVController.sayOnChannel(i, str6);
        } catch (NumberFormatException e) {
            Debug.Warning(e);
        }
    }
}
