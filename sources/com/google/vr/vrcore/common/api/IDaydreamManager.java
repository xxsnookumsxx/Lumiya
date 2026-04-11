package com.google.vr.vrcore.common.api;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.vr.vrcore.common.api.IDaydreamListener;
import com.google.vr.vrcore.common.api.ITransitionCallbacks;

/* JADX INFO: loaded from: classes.dex */
public interface IDaydreamManager extends IInterface {

    public static abstract class Stub extends Binder implements IDaydreamManager {
        private static final String DESCRIPTOR = "com.google.vr.vrcore.common.api.IDaydreamManager";
        static final int TRANSACTION_deprecatedLaunchInVr = 4;
        static final int TRANSACTION_exitFromVr = 10;
        static final int TRANSACTION_handleInsertionIntoHeadset = 11;
        static final int TRANSACTION_handleRemovalFromHeadset = 12;
        static final int TRANSACTION_launchInVr = 7;
        static final int TRANSACTION_launchVrHome = 8;
        static final int TRANSACTION_launchVrTransition = 9;
        static final int TRANSACTION_prepareVr = 3;
        static final int TRANSACTION_registerDaydreamIntent = 5;
        static final int TRANSACTION_registerListener = 1;
        static final int TRANSACTION_unregisterDaydreamIntent = 6;
        static final int TRANSACTION_unregisterListener = 2;

        private static class Proxy implements IDaydreamManager {
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            @Override // com.google.vr.vrcore.common.api.IDaydreamManager
            public boolean deprecatedLaunchInVr(PendingIntent pendingIntent) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (pendingIntent == null) {
                        parcelObtain.writeInt(0);
                    } else {
                        parcelObtain.writeInt(1);
                        pendingIntent.writeToParcel(parcelObtain, 0);
                    }
                    this.mRemote.transact(4, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                    return parcelObtain2.readInt() != 0;
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.vr.vrcore.common.api.IDaydreamManager
            public boolean exitFromVr(PendingIntent pendingIntent) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (pendingIntent == null) {
                        parcelObtain.writeInt(0);
                    } else {
                        parcelObtain.writeInt(1);
                        pendingIntent.writeToParcel(parcelObtain, 0);
                    }
                    this.mRemote.transact(10, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                    return parcelObtain2.readInt() != 0;
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.google.vr.vrcore.common.api.IDaydreamManager
            public void handleInsertionIntoHeadset(byte[] bArr) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    parcelObtain.writeByteArray(bArr);
                    this.mRemote.transact(11, parcelObtain, null, 1);
                } finally {
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.vr.vrcore.common.api.IDaydreamManager
            public void handleRemovalFromHeadset() throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(12, parcelObtain, null, 1);
                } finally {
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.vr.vrcore.common.api.IDaydreamManager
            public boolean launchInVr(PendingIntent pendingIntent, ComponentName componentName) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (pendingIntent == null) {
                        parcelObtain.writeInt(0);
                    } else {
                        parcelObtain.writeInt(1);
                        pendingIntent.writeToParcel(parcelObtain, 0);
                    }
                    if (componentName == null) {
                        parcelObtain.writeInt(0);
                    } else {
                        parcelObtain.writeInt(1);
                        componentName.writeToParcel(parcelObtain, 0);
                    }
                    this.mRemote.transact(7, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                    return parcelObtain2.readInt() != 0;
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.vr.vrcore.common.api.IDaydreamManager
            public boolean launchVrHome() throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(8, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                    return parcelObtain2.readInt() != 0;
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.vr.vrcore.common.api.IDaydreamManager
            public boolean launchVrTransition(ITransitionCallbacks iTransitionCallbacks) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    parcelObtain.writeStrongBinder(iTransitionCallbacks != null ? iTransitionCallbacks.asBinder() : null);
                    this.mRemote.transact(9, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                    return parcelObtain2.readInt() != 0;
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.vr.vrcore.common.api.IDaydreamManager
            public int prepareVr(ComponentName componentName, HeadTrackingState headTrackingState) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName == null) {
                        parcelObtain.writeInt(0);
                    } else {
                        parcelObtain.writeInt(1);
                        componentName.writeToParcel(parcelObtain, 0);
                    }
                    this.mRemote.transact(3, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                    int i = parcelObtain2.readInt();
                    if (parcelObtain2.readInt() != 0) {
                        headTrackingState.readFromParcel(parcelObtain2);
                    }
                    return i;
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.vr.vrcore.common.api.IDaydreamManager
            public void registerDaydreamIntent(PendingIntent pendingIntent) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (pendingIntent == null) {
                        parcelObtain.writeInt(0);
                    } else {
                        parcelObtain.writeInt(1);
                        pendingIntent.writeToParcel(parcelObtain, 0);
                    }
                    this.mRemote.transact(5, parcelObtain, null, 1);
                } finally {
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.vr.vrcore.common.api.IDaydreamManager
            public boolean registerListener(ComponentName componentName, IDaydreamListener iDaydreamListener) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName == null) {
                        parcelObtain.writeInt(0);
                    } else {
                        parcelObtain.writeInt(1);
                        componentName.writeToParcel(parcelObtain, 0);
                    }
                    parcelObtain.writeStrongBinder(iDaydreamListener != null ? iDaydreamListener.asBinder() : null);
                    this.mRemote.transact(1, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                    return parcelObtain2.readInt() != 0;
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.vr.vrcore.common.api.IDaydreamManager
            public void unregisterDaydreamIntent() throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, parcelObtain, null, 1);
                } finally {
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.vr.vrcore.common.api.IDaydreamManager
            public boolean unregisterListener(ComponentName componentName) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName == null) {
                        parcelObtain.writeInt(0);
                    } else {
                        parcelObtain.writeInt(1);
                        componentName.writeToParcel(parcelObtain, 0);
                    }
                    this.mRemote.transact(2, parcelObtain, parcelObtain2, 0);
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

        public static IDaydreamManager asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface iInterfaceQueryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            return (iInterfaceQueryLocalInterface != null && (iInterfaceQueryLocalInterface instanceof IDaydreamManager)) ? (IDaydreamManager) iInterfaceQueryLocalInterface : new Proxy(iBinder);
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
                    boolean zRegisterListener = registerListener(parcel.readInt() == 0 ? null : (ComponentName) ComponentName.CREATOR.createFromParcel(parcel), IDaydreamListener.Stub.asInterface(parcel.readStrongBinder()));
                    parcel2.writeNoException();
                    parcel2.writeInt(!zRegisterListener ? 0 : 1);
                    return true;
                case 2:
                    parcel.enforceInterface(DESCRIPTOR);
                    boolean zUnregisterListener = unregisterListener(parcel.readInt() != 0 ? (ComponentName) ComponentName.CREATOR.createFromParcel(parcel) : null);
                    parcel2.writeNoException();
                    parcel2.writeInt(zUnregisterListener ? 1 : 0);
                    return true;
                case 3:
                    parcel.enforceInterface(DESCRIPTOR);
                    ComponentName componentName = parcel.readInt() != 0 ? (ComponentName) ComponentName.CREATOR.createFromParcel(parcel) : null;
                    HeadTrackingState headTrackingState = new HeadTrackingState();
                    int iPrepareVr = prepareVr(componentName, headTrackingState);
                    parcel2.writeNoException();
                    parcel2.writeInt(iPrepareVr);
                    parcel2.writeInt(1);
                    headTrackingState.writeToParcel(parcel2, 1);
                    return true;
                case 4:
                    parcel.enforceInterface(DESCRIPTOR);
                    boolean zDeprecatedLaunchInVr = deprecatedLaunchInVr(parcel.readInt() != 0 ? (PendingIntent) PendingIntent.CREATOR.createFromParcel(parcel) : null);
                    parcel2.writeNoException();
                    parcel2.writeInt(zDeprecatedLaunchInVr ? 1 : 0);
                    return true;
                case 5:
                    parcel.enforceInterface(DESCRIPTOR);
                    registerDaydreamIntent(parcel.readInt() != 0 ? (PendingIntent) PendingIntent.CREATOR.createFromParcel(parcel) : null);
                    return true;
                case 6:
                    parcel.enforceInterface(DESCRIPTOR);
                    unregisterDaydreamIntent();
                    return true;
                case 7:
                    parcel.enforceInterface(DESCRIPTOR);
                    boolean zLaunchInVr = launchInVr(parcel.readInt() == 0 ? null : (PendingIntent) PendingIntent.CREATOR.createFromParcel(parcel), parcel.readInt() != 0 ? (ComponentName) ComponentName.CREATOR.createFromParcel(parcel) : null);
                    parcel2.writeNoException();
                    parcel2.writeInt(zLaunchInVr ? 1 : 0);
                    return true;
                case 8:
                    parcel.enforceInterface(DESCRIPTOR);
                    boolean zLaunchVrHome = launchVrHome();
                    parcel2.writeNoException();
                    parcel2.writeInt(zLaunchVrHome ? 1 : 0);
                    return true;
                case 9:
                    parcel.enforceInterface(DESCRIPTOR);
                    boolean zLaunchVrTransition = launchVrTransition(ITransitionCallbacks.Stub.asInterface(parcel.readStrongBinder()));
                    parcel2.writeNoException();
                    parcel2.writeInt(zLaunchVrTransition ? 1 : 0);
                    return true;
                case 10:
                    parcel.enforceInterface(DESCRIPTOR);
                    boolean zExitFromVr = exitFromVr(parcel.readInt() != 0 ? (PendingIntent) PendingIntent.CREATOR.createFromParcel(parcel) : null);
                    parcel2.writeNoException();
                    parcel2.writeInt(zExitFromVr ? 1 : 0);
                    return true;
                case 11:
                    parcel.enforceInterface(DESCRIPTOR);
                    handleInsertionIntoHeadset(parcel.createByteArray());
                    return true;
                case 12:
                    parcel.enforceInterface(DESCRIPTOR);
                    handleRemovalFromHeadset();
                    return true;
                case 1598968902:
                    parcel2.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(i, parcel, parcel2, i2);
            }
        }
    }

    boolean deprecatedLaunchInVr(PendingIntent pendingIntent) throws RemoteException;

    boolean exitFromVr(PendingIntent pendingIntent) throws RemoteException;

    void handleInsertionIntoHeadset(byte[] bArr) throws RemoteException;

    void handleRemovalFromHeadset() throws RemoteException;

    boolean launchInVr(PendingIntent pendingIntent, ComponentName componentName) throws RemoteException;

    boolean launchVrHome() throws RemoteException;

    boolean launchVrTransition(ITransitionCallbacks iTransitionCallbacks) throws RemoteException;

    int prepareVr(ComponentName componentName, HeadTrackingState headTrackingState) throws RemoteException;

    void registerDaydreamIntent(PendingIntent pendingIntent) throws RemoteException;

    boolean registerListener(ComponentName componentName, IDaydreamListener iDaydreamListener) throws RemoteException;

    void unregisterDaydreamIntent() throws RemoteException;

    boolean unregisterListener(ComponentName componentName) throws RemoteException;
}
