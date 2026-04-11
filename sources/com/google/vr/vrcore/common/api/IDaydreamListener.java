package com.google.vr.vrcore.common.api;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/* JADX INFO: loaded from: classes.dex */
public interface IDaydreamListener extends IInterface {

    public static abstract class Stub extends Binder implements IDaydreamListener {
        private static final String DESCRIPTOR = "com.google.vr.vrcore.common.api.IDaydreamListener";
        static final int TRANSACTION_applyFade = 3;
        static final int TRANSACTION_dumpDebugData = 5;
        static final int TRANSACTION_getTargetApiVersion = 1;
        static final int TRANSACTION_recenterHeadTracking = 4;
        static final int TRANSACTION_requestStopTracking = 2;
        static final int TRANSACTION_resumeHeadTracking = 6;

        private static class Proxy implements IDaydreamListener {
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // com.google.vr.vrcore.common.api.IDaydreamListener
            public void applyFade(int i, long j) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    parcelObtain.writeInt(i);
                    parcelObtain.writeLong(j);
                    this.mRemote.transact(3, parcelObtain, null, 1);
                } finally {
                    parcelObtain.recycle();
                }
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            @Override // com.google.vr.vrcore.common.api.IDaydreamListener
            public void dumpDebugData() throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, parcelObtain, null, 1);
                } finally {
                    parcelObtain.recycle();
                }
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.google.vr.vrcore.common.api.IDaydreamListener
            public int getTargetApiVersion() throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                    return parcelObtain2.readInt();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.vr.vrcore.common.api.IDaydreamListener
            public void recenterHeadTracking() throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, parcelObtain, null, 1);
                } finally {
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.vr.vrcore.common.api.IDaydreamListener
            public HeadTrackingState requestStopTracking() throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                    return parcelObtain2.readInt() == 0 ? null : HeadTrackingState.CREATOR.createFromParcel(parcelObtain2);
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.vr.vrcore.common.api.IDaydreamListener
            public void resumeHeadTracking(HeadTrackingState headTrackingState) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (headTrackingState == null) {
                        parcelObtain.writeInt(0);
                    } else {
                        parcelObtain.writeInt(1);
                        headTrackingState.writeToParcel(parcelObtain, 0);
                    }
                    this.mRemote.transact(6, parcelObtain, null, 1);
                } finally {
                    parcelObtain.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDaydreamListener asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface iInterfaceQueryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            return (iInterfaceQueryLocalInterface != null && (iInterfaceQueryLocalInterface instanceof IDaydreamListener)) ? (IDaydreamListener) iInterfaceQueryLocalInterface : new Proxy(iBinder);
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            switch (i) {
                case 1:
                    parcel.enforceInterface(DESCRIPTOR);
                    int targetApiVersion = getTargetApiVersion();
                    parcel2.writeNoException();
                    parcel2.writeInt(targetApiVersion);
                    return true;
                case 2:
                    parcel.enforceInterface(DESCRIPTOR);
                    HeadTrackingState headTrackingStateRequestStopTracking = requestStopTracking();
                    parcel2.writeNoException();
                    if (headTrackingStateRequestStopTracking == null) {
                        parcel2.writeInt(0);
                    } else {
                        parcel2.writeInt(1);
                        headTrackingStateRequestStopTracking.writeToParcel(parcel2, 1);
                    }
                    return true;
                case 3:
                    parcel.enforceInterface(DESCRIPTOR);
                    applyFade(parcel.readInt(), parcel.readLong());
                    return true;
                case 4:
                    parcel.enforceInterface(DESCRIPTOR);
                    recenterHeadTracking();
                    return true;
                case 5:
                    parcel.enforceInterface(DESCRIPTOR);
                    dumpDebugData();
                    return true;
                case 6:
                    parcel.enforceInterface(DESCRIPTOR);
                    resumeHeadTracking(parcel.readInt() != 0 ? HeadTrackingState.CREATOR.createFromParcel(parcel) : null);
                    return true;
                case 1598968902:
                    parcel2.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(i, parcel, parcel2, i2);
            }
        }
    }

    void applyFade(int i, long j) throws RemoteException;

    void dumpDebugData() throws RemoteException;

    int getTargetApiVersion() throws RemoteException;

    void recenterHeadTracking() throws RemoteException;

    HeadTrackingState requestStopTracking() throws RemoteException;

    void resumeHeadTracking(HeadTrackingState headTrackingState) throws RemoteException;
}
