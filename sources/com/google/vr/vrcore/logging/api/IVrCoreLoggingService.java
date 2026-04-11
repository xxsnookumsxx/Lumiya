package com.google.vr.vrcore.logging.api;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/* JADX INFO: loaded from: classes.dex */
public interface IVrCoreLoggingService extends IInterface {

    public static abstract class Stub extends Binder implements IVrCoreLoggingService {
        private static final String DESCRIPTOR = "com.google.vr.vrcore.logging.api.IVrCoreLoggingService";
        static final int TRANSACTION_log = 2;
        static final int TRANSACTION_logBatched = 3;

        private static class Proxy implements IVrCoreLoggingService {
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.google.vr.vrcore.logging.api.IVrCoreLoggingService
            public void log(VREventParcelable vREventParcelable) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (vREventParcelable == null) {
                        parcelObtain.writeInt(0);
                    } else {
                        parcelObtain.writeInt(1);
                        vREventParcelable.writeToParcel(parcelObtain, 0);
                    }
                    this.mRemote.transact(2, parcelObtain, null, 1);
                } finally {
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.vr.vrcore.logging.api.IVrCoreLoggingService
            public void logBatched(VREventParcelable[] vREventParcelableArr) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    parcelObtain.writeTypedArray(vREventParcelableArr, 0);
                    this.mRemote.transact(3, parcelObtain, null, 1);
                } finally {
                    parcelObtain.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVrCoreLoggingService asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface iInterfaceQueryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            return (iInterfaceQueryLocalInterface != null && (iInterfaceQueryLocalInterface instanceof IVrCoreLoggingService)) ? (IVrCoreLoggingService) iInterfaceQueryLocalInterface : new Proxy(iBinder);
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            switch (i) {
                case 2:
                    parcel.enforceInterface(DESCRIPTOR);
                    log(parcel.readInt() == 0 ? null : VREventParcelable.CREATOR.createFromParcel(parcel));
                    return true;
                case 3:
                    parcel.enforceInterface(DESCRIPTOR);
                    logBatched((VREventParcelable[]) parcel.createTypedArray(VREventParcelable.CREATOR));
                    return true;
                case 1598968902:
                    parcel2.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(i, parcel, parcel2, i2);
            }
        }
    }

    void log(VREventParcelable vREventParcelable) throws RemoteException;

    void logBatched(VREventParcelable[] vREventParcelableArr) throws RemoteException;
}
