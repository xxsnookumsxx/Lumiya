package com.google.vr.vrcore.controller.api;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.vr.vrcore.controller.api.IControllerListener;

/* JADX INFO: loaded from: classes.dex */
public interface IControllerService extends IInterface {

    public static abstract class Stub extends Binder implements IControllerService {
        private static final String DESCRIPTOR = "com.google.vr.vrcore.controller.api.IControllerService";
        static final int TRANSACTION_initialize = 1;
        static final int TRANSACTION_registerListener = 5;
        static final int TRANSACTION_unregisterListener = 6;

        private static class Proxy implements IControllerService {
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

            @Override // com.google.vr.vrcore.controller.api.IControllerService
            public int initialize(int i) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    parcelObtain.writeInt(i);
                    this.mRemote.transact(1, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                    return parcelObtain2.readInt();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.vr.vrcore.controller.api.IControllerService
            public boolean registerListener(int i, String str, IControllerListener iControllerListener) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    parcelObtain.writeInt(i);
                    parcelObtain.writeString(str);
                    parcelObtain.writeStrongBinder(iControllerListener != null ? iControllerListener.asBinder() : null);
                    this.mRemote.transact(5, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                    return parcelObtain2.readInt() != 0;
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.vr.vrcore.controller.api.IControllerService
            public boolean unregisterListener(String str) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    parcelObtain.writeString(str);
                    this.mRemote.transact(6, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                    return parcelObtain2.readInt() != 0;
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IControllerService asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface iInterfaceQueryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            return (iInterfaceQueryLocalInterface != null && (iInterfaceQueryLocalInterface instanceof IControllerService)) ? (IControllerService) iInterfaceQueryLocalInterface : new Proxy(iBinder);
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
                    int iInitialize = initialize(parcel.readInt());
                    parcel2.writeNoException();
                    parcel2.writeInt(iInitialize);
                    return true;
                case 5:
                    parcel.enforceInterface(DESCRIPTOR);
                    boolean zRegisterListener = registerListener(parcel.readInt(), parcel.readString(), IControllerListener.Stub.asInterface(parcel.readStrongBinder()));
                    parcel2.writeNoException();
                    parcel2.writeInt(zRegisterListener ? 1 : 0);
                    return true;
                case 6:
                    parcel.enforceInterface(DESCRIPTOR);
                    boolean zUnregisterListener = unregisterListener(parcel.readString());
                    parcel2.writeNoException();
                    parcel2.writeInt(zUnregisterListener ? 1 : 0);
                    return true;
                case 1598968902:
                    parcel2.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(i, parcel, parcel2, i2);
            }
        }
    }

    int initialize(int i) throws RemoteException;

    boolean registerListener(int i, String str, IControllerListener iControllerListener) throws RemoteException;

    boolean unregisterListener(String str) throws RemoteException;
}
