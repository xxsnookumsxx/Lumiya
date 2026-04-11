package com.google.vr.vrcore.performance.api;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/* JADX INFO: loaded from: classes.dex */
public interface IPerformanceService extends IInterface {

    public static abstract class Stub extends Binder implements IPerformanceService {
        private static final String DESCRIPTOR = "com.google.vr.vrcore.performance.api.IPerformanceService";
        static final int TRANSACTION_getCurrentThrottlingRelativeTemperature = 1;
        static final int TRANSACTION_reportFrameDrops = 2;

        private static class Proxy implements IPerformanceService {
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            @Override // com.google.vr.vrcore.performance.api.IPerformanceService
            public float getCurrentThrottlingRelativeTemperature() throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                    return parcelObtain2.readFloat();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.google.vr.vrcore.performance.api.IPerformanceService
            public void reportFrameDrops(long j, long j2, int i) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    parcelObtain.writeLong(j);
                    parcelObtain.writeLong(j2);
                    parcelObtain.writeInt(i);
                    this.mRemote.transact(2, parcelObtain, null, 1);
                } finally {
                    parcelObtain.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPerformanceService asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface iInterfaceQueryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            return (iInterfaceQueryLocalInterface != null && (iInterfaceQueryLocalInterface instanceof IPerformanceService)) ? (IPerformanceService) iInterfaceQueryLocalInterface : new Proxy(iBinder);
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
                    float currentThrottlingRelativeTemperature = getCurrentThrottlingRelativeTemperature();
                    parcel2.writeNoException();
                    parcel2.writeFloat(currentThrottlingRelativeTemperature);
                    return true;
                case 2:
                    parcel.enforceInterface(DESCRIPTOR);
                    reportFrameDrops(parcel.readLong(), parcel.readLong(), parcel.readInt());
                    return true;
                case 1598968902:
                    parcel2.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(i, parcel, parcel2, i2);
            }
        }
    }

    float getCurrentThrottlingRelativeTemperature() throws RemoteException;

    void reportFrameDrops(long j, long j2, int i) throws RemoteException;
}
