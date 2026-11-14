package com.lumiyaviewer.lumiya.slproto.modules.rlv.commands;

import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommand;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommands;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVController;
import java.util.UUID;

/* loaded from: classes.dex */
public class RLVCmdVersion implements RLVCommand {
    private static final int RLV_VERSION_BUILD = 0;
    private static final int RLV_VERSION_MAJOR = 2;
    private static final int RLV_VERSION_MINOR = 9;
    private static final int RLV_VERSION_PATCH = 28;
    private static final int RLVa_VERSION_MAJOR = 2;
    private static final int RLVa_VERSION_MINOR = 4;
    private static final int RLVa_VERSION_PATCH = 2;

    public static String getManualVersionReply() {
        return String.format("RestrainedLove viewer v%d.%d.%d", Integer.valueOf(RLV_VERSION_MAJOR), Integer.valueOf(RLV_VERSION_MINOR), Integer.valueOf(RLV_VERSION_PATCH));
    }

    @Override // com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVCommand
    public void Handle(RLVController rLVController, UUID uuid, RLVCommands rLVCommands, String str, String str2) {
        try {
            int i = Integer.parseInt(str);
            switch (rLVCommands) {
                case version:
                case versionnew:
                    Object[] objArr = new Object[7];
                    objArr[0] = rLVCommands == RLVCommands.versionnew ? "RestrainedLove" : "RestrainedLife";
                    objArr[1] = Integer.valueOf(RLV_VERSION_MAJOR);
                    objArr[2] = Integer.valueOf(RLV_VERSION_MINOR);
                    objArr[3] = Integer.valueOf(RLV_VERSION_PATCH);
                    objArr[4] = Integer.valueOf(RLVa_VERSION_MAJOR);
                    objArr[5] = Integer.valueOf(RLVa_VERSION_MINOR);
                    objArr[6] = Integer.valueOf(RLVa_VERSION_PATCH);
                    rLVController.sayOnChannel(i, String.format("%s viewer v%d.%d.%d (RLVa %d.%d.%d)", objArr));
                    break;
                case versionnum:
                    rLVController.sayOnChannel(i, String.format("%d%02d%02d%02d", Integer.valueOf(RLV_VERSION_MAJOR), Integer.valueOf(RLV_VERSION_MINOR), Integer.valueOf(RLV_VERSION_PATCH), Integer.valueOf(RLV_VERSION_BUILD)));
                    break;
                default:
                    break;
            }
        } catch (NumberFormatException e) {
            Debug.Warning(e);
        }
    }
}
