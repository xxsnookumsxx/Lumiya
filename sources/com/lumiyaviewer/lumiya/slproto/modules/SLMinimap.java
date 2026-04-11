package com.lumiyaviewer.lumiya.slproto.modules;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.google.common.base.Objects;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.react.RequestHandler;
import com.lumiyaviewer.lumiya.react.ResultHandler;
import com.lumiyaviewer.lumiya.react.SimpleRequestHandler;
import com.lumiyaviewer.lumiya.react.SubscriptionSingleKey;
import com.lumiyaviewer.lumiya.slproto.SLAgentCircuit;
import com.lumiyaviewer.lumiya.slproto.caps.SLCapEventQueue;
import com.lumiyaviewer.lumiya.slproto.handler.SLEventQueueMessageHandler;
import com.lumiyaviewer.lumiya.slproto.handler.SLMessageHandler;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDException;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDNode;
import com.lumiyaviewer.lumiya.slproto.messages.ParcelOverlay;
import com.lumiyaviewer.lumiya.slproto.modules.voice.SLVoice;
import com.lumiyaviewer.lumiya.slproto.objects.SLObjectInfo;
import com.lumiyaviewer.lumiya.slproto.types.ImmutableVector;
import com.lumiyaviewer.lumiya.slproto.types.LLVector3;
import com.lumiyaviewer.lumiya.slproto.users.ChatterID;
import com.lumiyaviewer.lumiya.slproto.users.ParcelData;
import com.lumiyaviewer.lumiya.slproto.users.manager.CurrentLocationInfo;
import com.lumiyaviewer.lumiya.slproto.users.manager.UserManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
public class SLMinimap extends SLModule {
    public static final float CHAT_RANGE = 20.0f;
    private static final int parcelBitmapSize = 256;
    public static final int parcelDataSize = 64;
    private static final byte parcelOverlayFlagBorderSouth = -128;
    private static final byte parcelOverlayFlagBorderWest = 64;
    private static final byte parcelOverlayFlagPrivate = 32;
    private static final byte parcelOverlayTypeAuction = 5;
    private static final byte parcelOverlayTypeForSale = 4;
    private static final byte parcelOverlayTypeMask = 15;
    private static final byte parcelOverlayTypeOwnedByGroup = 2;
    private static final byte parcelOverlayTypeOwnedByOther = 1;
    private static final byte parcelOverlayTypeOwnedBySelf = 3;
    private static final byte parcelOverlayTypePublic = 0;
    private static final int parcelUpsampleFactor = 4;
    private boolean afterTeleport;
    private int chatRangeUsersCount;

    @Nonnull
    private volatile MinimapBitmap minimapBitmap;
    private int myAvatarParcelDataIndex;

    @Nullable
    private ImmutableVector myAvatarPosition;
    private int nearbyUsersCount;
    private final int[] parcelIDs;
    private final Map<Integer, ParcelData> parcels;
    private final RequestHandler<SubscriptionSingleKey> userLocationRequestHandler;
    private final ResultHandler<SubscriptionSingleKey, UserLocations> userLocationsResultHandler;
    private final UserManager userManager;
    private final Map<UUID, UserLocation> userPositions;

    public static class MinimapBitmap {
        private final int bitmapHeight;
        private final int bitmapWidth;
        final int[] colors;

        MinimapBitmap(int i, int i2) {
            this.bitmapWidth = i;
            this.bitmapHeight = i2;
            this.colors = new int[i * i2];
        }

        MinimapBitmap(MinimapBitmap minimapBitmap, int i, int i2, int[] iArr) {
            this.bitmapWidth = minimapBitmap.bitmapWidth;
            this.bitmapHeight = minimapBitmap.bitmapHeight;
            this.colors = Arrays.copyOf(minimapBitmap.colors, minimapBitmap.colors.length);
            System.arraycopy(iArr, 0, this.colors, (this.bitmapHeight * i2) + i, iArr.length);
        }

        public Bitmap makeBitmap() {
            return Bitmap.createBitmap(this.colors, this.bitmapWidth, this.bitmapHeight, Bitmap.Config.ARGB_8888);
        }

        public void updateBitmap(Bitmap bitmap) {
            bitmap.setPixels(this.colors, 0, this.bitmapWidth, 0, 0, this.bitmapWidth, this.bitmapHeight);
        }
    }

    public static class UserLocation {

        @Nonnull
        public final ChatterID chatterID;
        public volatile float distance = Float.NaN;

        @Nonnull
        public volatile ImmutableVector location;

        UserLocation(@Nonnull ChatterID chatterID, @Nonnull ImmutableVector immutableVector) {
            this.chatterID = chatterID;
            this.location = immutableVector;
        }
    }

    public static class UserLocations {
        public final float myAvatarHeading;

        @Nullable
        public final ImmutableVector myAvatarPosition;
        public final Map<UUID, UserLocation> userPositions;

        UserLocations(@Nullable ImmutableVector immutableVector, float f, Map<UUID, UserLocation> map) {
            this.myAvatarPosition = immutableVector;
            this.myAvatarHeading = f;
            this.userPositions = map;
        }
    }

    SLMinimap(SLAgentCircuit sLAgentCircuit) {
        super(sLAgentCircuit);
        this.minimapBitmap = new MinimapBitmap(256, 256);
        this.parcelIDs = new int[4096];
        this.parcels = new ConcurrentHashMap();
        this.nearbyUsersCount = 0;
        this.chatRangeUsersCount = 0;
        this.userPositions = new ConcurrentHashMap(1, 0.75f, 2);
        this.myAvatarPosition = null;
        this.afterTeleport = false;
        this.myAvatarParcelDataIndex = -1;
        this.userLocationRequestHandler = new SimpleRequestHandler<SubscriptionSingleKey>() { // from class: com.lumiyaviewer.lumiya.slproto.modules.SLMinimap.1
            @Override // com.lumiyaviewer.lumiya.react.RequestHandler
            public void onRequest(@Nonnull SubscriptionSingleKey subscriptionSingleKey) {
                if (SLMinimap.this.userLocationsResultHandler != null) {
                    SLMinimap.this.userLocationsResultHandler.onResultData(subscriptionSingleKey, new UserLocations(SLMinimap.this.myAvatarPosition, SLMinimap.this.getMyAvatarHeading(), SLMinimap.this.userPositions));
                }
            }
        };
        this.userManager = UserManager.getUserManager(this.agentCircuit.circuitInfo.agentID);
        if (this.userManager != null) {
            this.userLocationsResultHandler = this.userManager.getUserLocationsPool().attachRequestHandler(this.userLocationRequestHandler);
        } else {
            this.userLocationsResultHandler = null;
        }
        this.afterTeleport = sLAgentCircuit.getAuthReply().fromTeleport ? !sLAgentCircuit.getAuthReply().isTemporary : false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public float getMyAvatarHeading() {
        return (this.agentCircuit.getModules().avatarControl.getAgentHeading() * 3.1415927f) / 180.0f;
    }

    private int getParcelDataIndex(ImmutableVector immutableVector) {
        int iFloor = (int) Math.floor((immutableVector.getX() * 64.0f) / 256.0f);
        int iFloor2 = (int) Math.floor((immutableVector.getY() * 64.0f) / 256.0f);
        if (iFloor < 0) {
            iFloor = 0;
        } else if (iFloor >= 64) {
            iFloor = 63;
        }
        return ((iFloor2 >= 0 ? iFloor2 >= 64 ? 63 : iFloor2 : 0) * 64) + iFloor;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: updateAvatarParcelData, reason: merged with bridge method [inline-methods] */
    public void m222com_lumiyaviewer_lumiya_slproto_modules_SLMinimapmthref0() {
        ParcelData parcelData = this.myAvatarParcelDataIndex >= 0 ? this.parcels.get(Integer.valueOf(this.parcelIDs[this.myAvatarParcelDataIndex])) : null;
        if (parcelData != null && this.afterTeleport) {
            this.afterTeleport = false;
            this.userManager.getChatterList().getActiveChattersManager().notifyTeleportComplete(parcelData.getName());
        }
        SLVoice sLVoice = this.agentCircuit.getModules().voice;
        if (parcelData != null) {
            sLVoice.setCurrentParcel(parcelData.getParcelID());
        }
        this.userManager.setCurrentLocationInfo(CurrentLocationInfo.create(parcelData, this.nearbyUsersCount, this.chatRangeUsersCount, sLVoice.getCurrentParcelVoiceChannel()));
    }

    @Override // com.lumiyaviewer.lumiya.slproto.modules.SLModule
    public void HandleCloseCircuit() {
        if (this.userManager != null) {
            this.userManager.getUserLocationsPool().detachRequestHandler(this.userLocationRequestHandler);
        }
        super.HandleCloseCircuit();
    }

    /* JADX WARN: Removed duplicated region for block: B:88:0x01df  */
    @com.lumiyaviewer.lumiya.slproto.handler.SLMessageHandler
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void HandleCoarseLocationUpdate(com.lumiyaviewer.lumiya.slproto.messages.CoarseLocationUpdate r14) {
        /*
            Method dump skipped, instruction units count: 491
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lumiyaviewer.lumiya.slproto.modules.SLMinimap.HandleCoarseLocationUpdate(com.lumiyaviewer.lumiya.slproto.messages.CoarseLocationUpdate):void");
    }

    @SLMessageHandler
    public void HandleParcelOverlay(ParcelOverlay parcelOverlay) {
        int i;
        Debug.Log("ParcelOverlay: SequenceID = " + parcelOverlay.ParcelData_Field.SequenceID);
        byte[] bArr = parcelOverlay.ParcelData_Field.Data;
        int length = bArr.length / 64;
        int[] iArr = new int[length * 4 * 64 * 4];
        int i2 = 0;
        int i3 = 0;
        while (i3 < length) {
            int i4 = i3 + (parcelOverlay.ParcelData_Field.SequenceID * 16);
            int i5 = 0;
            while (true) {
                int i6 = i5;
                i = i2;
                if (i6 < 64) {
                    int iRgb = 0;
                    switch ((byte) (bArr[i] & 15)) {
                        case 0:
                            iRgb = Color.rgb(0, 192, 0);
                            break;
                        case 1:
                            iRgb = Color.rgb(32, 128, 32);
                            break;
                        case 2:
                            iRgb = Color.rgb(0, 128, 128);
                            break;
                        case 3:
                            iRgb = Color.rgb(0, 255, 255);
                            break;
                        case 4:
                            iRgb = Color.rgb(128, 128, 0);
                            break;
                        case 5:
                            iRgb = Color.rgb(255, 255, 0);
                            break;
                    }
                    if ((bArr[i] & 32) != 0) {
                        int iRed = Color.red(iRgb);
                        int iGreen = Color.green(iRgb);
                        int iBlue = Color.blue(iRgb);
                        int i7 = iRed + 64;
                        if (i7 >= 255) {
                            int i8 = i7 - 255;
                            i7 -= i8;
                            iGreen -= i8;
                            iBlue -= i8;
                            if (iGreen < 0) {
                                iGreen = 0;
                            }
                            if (iBlue < 0) {
                                iBlue = 0;
                            }
                        }
                        iRgb = Color.rgb(i7, iGreen, iBlue);
                    }
                    int i9 = 0;
                    while (true) {
                        int i10 = i9;
                        if (i10 < 4) {
                            int i11 = ((((length * 4) - 1) - ((i3 * 4) + i10)) * 256) + (i6 * 4);
                            int i12 = 0;
                            while (true) {
                                int i13 = i12;
                                if (i13 < 4) {
                                    iArr[i11 + i13] = ((i10 != 0 || i4 == 0 || (bArr[i] & (-128)) == 0) && (i13 != 0 || i6 == 0 || (bArr[i] & 64) == 0)) ? iRgb : -1;
                                    i12 = i13 + 1;
                                }
                            }
                            i9 = i10 + 1;
                        }
                    }
                    i2 = i + 1;
                    i5 = i6 + 1;
                }
            }
            i3++;
            i2 = i;
        }
        this.minimapBitmap = new MinimapBitmap(this.minimapBitmap, 0, (3 - parcelOverlay.ParcelData_Field.SequenceID) * 64, iArr);
        if (this.userManager != null) {
            this.userManager.getMinimapBitmapPool().setData(SubscriptionSingleKey.Value, this.minimapBitmap);
        }
    }

    @SLEventQueueMessageHandler(eventName = SLCapEventQueue.CapsEventType.ParcelProperties)
    public void HandleParcelProperties(LLSDNode lLSDNode) {
        boolean z;
        boolean z2;
        try {
            LLSDNode lLSDNodeByKey = lLSDNode.byKey("ParcelData");
            int i = 0;
            z = false;
            while (i < lLSDNodeByKey.getCount()) {
                try {
                    try {
                        ParcelData parcelData = new ParcelData(lLSDNodeByKey.byIndex(i));
                        int parcelID = parcelData.getParcelID();
                        this.parcels.put(Integer.valueOf(parcelID), parcelData);
                        boolean[] parcelBitmap = parcelData.getParcelBitmap();
                        z2 = z;
                        for (int i2 = 0; i2 < 4096; i2++) {
                            try {
                                if (parcelBitmap[i2]) {
                                    this.parcelIDs[i2] = parcelID;
                                    if (i2 == this.myAvatarParcelDataIndex) {
                                        z2 = true;
                                    }
                                }
                            } catch (LLSDException e) {
                                z = z2;
                                e = e;
                                Debug.Warning(e);
                                z2 = z;
                                i++;
                                z = z2;
                            }
                        }
                    } catch (LLSDException e2) {
                        e = e2;
                    }
                    i++;
                    z = z2;
                } catch (LLSDException e3) {
                    e = e3;
                    e.printStackTrace();
                }
            }
        } catch (LLSDException e4) {
            e = e4;
            z = false;
        }
        if (z) {
            requestUpdateAvatarParcelData();
        }
    }

    public Float getDistanceToUser(@Nullable UUID uuid) {
        if (uuid == null) {
            return null;
        }
        UserLocation userLocation = this.userPositions.get(uuid);
        return userLocation != null ? Float.valueOf(userLocation.distance) : Float.valueOf(Float.NaN);
    }

    @Nullable
    public LLVector3 getNearbyAgentLocation(UUID uuid) {
        SLObjectInfo avatarObject;
        if (this.gridConn != null && this.gridConn.parcelInfo != null && (avatarObject = this.gridConn.parcelInfo.getAvatarObject(uuid)) != null) {
            return avatarObject.getAbsolutePosition();
        }
        if (!Objects.equal(uuid, this.circuitInfo.agentID) || this.myAvatarPosition == null) {
            return null;
        }
        return new LLVector3(this.myAvatarPosition.getX(), this.myAvatarPosition.getY(), this.myAvatarPosition.getZ());
    }

    public List<ChatterID> getNearbyChatterList() {
        ArrayList arrayList = new ArrayList(this.userPositions.size());
        Iterator<T> it = this.userPositions.values().iterator();
        while (it.hasNext()) {
            arrayList.add(((UserLocation) it.next()).chatterID);
        }
        return arrayList;
    }

    public void requestUpdateAvatarParcelData() {
        this.agentCircuit.execute(new Runnable() { // from class: com.lumiyaviewer.lumiya.slproto.modules.-$Lambda$eaDiotW55nmaHN5_b1ikeJpLLsk
            private final /* synthetic */ void $m$0() {
                ((SLMinimap) this).m222com_lumiyaviewer_lumiya_slproto_modules_SLMinimapmthref0();
            }

            @Override // java.lang.Runnable
            public final void run() {
                $m$0();
            }
        });
    }
}
