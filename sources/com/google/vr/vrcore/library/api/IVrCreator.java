package com.google.vr.vrcore.library.api;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.vr.vrcore.library.api.IObjectWrapper;
import com.google.vr.vrcore.library.api.IVrNativeLibraryLoader;

/* JADX INFO: loaded from: classes.dex */
public interface IVrCreator extends IInterface {

    public static abstract class Stub extends Binder implements IVrCreator {
        private static final String DESCRIPTOR = "com.google.vr.vrcore.library.api.IVrCreator";
        static final int TRANSACTION_DEPRECATED_newNativeLibraryLoader = 3;
        static final int TRANSACTION_newNativeLibraryLoader = 4;

        private static class Proxy implements IVrCreator {
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // com.google.vr.vrcore.library.api.IVrCreator
            public IVrNativeLibraryLoader DEPRECATED_newNativeLibraryLoader(IObjectWrapper iObjectWrapper) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    parcelObtain.writeStrongBinder(iObjectWrapper != null ? iObjectWrapper.asBinder() : null);
                    this.mRemote.transact(3, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                    return IVrNativeLibraryLoader.Stub.asInterface(parcelObtain2.readStrongBinder());
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.google.vr.vrcore.library.api.IVrCreator
            public IVrNativeLibraryLoader newNativeLibraryLoader(IObjectWrapper iObjectWrapper, IObjectWrapper iObjectWrapper2) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    parcelObtain.writeStrongBinder(iObjectWrapper == null ? null : iObjectWrapper.asBinder());
                    parcelObtain.writeStrongBinder(iObjectWrapper2 != null ? iObjectWrapper2.asBinder() : null);
                    this.mRemote.transact(4, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                    return IVrNativeLibraryLoader.Stub.asInterface(parcelObtain2.readStrongBinder());
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVrCreator asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface iInterfaceQueryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            return (iInterfaceQueryLocalInterface != null && (iInterfaceQueryLocalInterface instanceof IVrCreator)) ? (IVrCreator) iInterfaceQueryLocalInterface : new Proxy(iBinder);
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            switch (i) {
                case 3:
                    parcel.enforceInterface(DESCRIPTOR);
                    IVrNativeLibraryLoader iVrNativeLibraryLoaderDEPRECATED_newNativeLibraryLoader = DEPRECATED_newNativeLibraryLoader(IObjectWrapper.Stub.asInterface(parcel.readStrongBinder()));
                    parcel2.writeNoException();
                    parcel2.writeStrongBinder(iVrNativeLibraryLoaderDEPRECATED_newNativeLibraryLoader != null ? iVrNativeLibraryLoaderDEPRECATED_newNativeLibraryLoader.asBinder() : null);
                    return true;
                case 4:
                    parcel.enforceInterface(DESCRIPTOR);
                    IVrNativeLibraryLoader iVrNativeLibraryLoaderNewNativeLibraryLoader = newNativeLibraryLoader(IObjectWrapper.Stub.asInterface(parcel.readStrongBinder()), IObjectWrapper.Stub.asInterface(parcel.readStrongBinder()));
                    parcel2.writeNoException();
                    parcel2.writeStrongBinder(iVrNativeLibraryLoaderNewNativeLibraryLoader != null ? iVrNativeLibraryLoaderNewNativeLibraryLoader.asBinder() : null);
                    return true;
                case 1598968902:
                    parcel2.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(i, parcel, parcel2, i2);
            }
        }
    }

    IVrNativeLibraryLoader DEPRECATED_newNativeLibraryLoader(IObjectWrapper iObjectWrapper) throws RemoteException;

    IVrNativeLibraryLoader newNativeLibraryLoader(IObjectWrapper iObjectWrapper, IObjectWrapper iObjectWrapper2) throws RemoteException;
}
