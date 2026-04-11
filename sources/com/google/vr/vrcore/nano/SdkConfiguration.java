package com.google.vr.vrcore.nano;

import com.google.common.logging.nano.Vr;
import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.ExtendableMessageNano;
import com.google.protobuf.nano.InternalNano;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import java.io.IOException;

/* JADX INFO: loaded from: classes.dex */
public interface SdkConfiguration {

    public static final class SdkConfigurationRequest extends ExtendableMessageNano<SdkConfigurationRequest> {
        private static volatile SdkConfigurationRequest[] _emptyArray;
        public Vr.VREvent.SdkConfigurationParams requestedParams;
        public String sdkVersion;

        public SdkConfigurationRequest() {
            clear();
        }

        public static SdkConfigurationRequest[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new SdkConfigurationRequest[0];
                    }
                }
            }
            return _emptyArray;
        }

        public static SdkConfigurationRequest parseFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            return new SdkConfigurationRequest().mergeFrom(codedInputByteBufferNano);
        }

        public static SdkConfigurationRequest parseFrom(byte[] bArr) throws InvalidProtocolBufferNanoException {
            return (SdkConfigurationRequest) MessageNano.mergeFrom(new SdkConfigurationRequest(), bArr);
        }

        public final SdkConfigurationRequest clear() {
            this.sdkVersion = null;
            this.requestedParams = null;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.google.protobuf.nano.ExtendableMessageNano, com.google.protobuf.nano.MessageNano
        protected final int computeSerializedSize() {
            int iComputeSerializedSize = super.computeSerializedSize();
            if (this.sdkVersion != null) {
                iComputeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.sdkVersion);
            }
            return this.requestedParams == null ? iComputeSerializedSize : iComputeSerializedSize + CodedOutputByteBufferNano.computeMessageSize(2, this.requestedParams);
        }

        @Override // com.google.protobuf.nano.MessageNano
        public final SdkConfigurationRequest mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int tag = codedInputByteBufferNano.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 10:
                        this.sdkVersion = codedInputByteBufferNano.readString();
                        break;
                    case 18:
                        if (this.requestedParams == null) {
                            this.requestedParams = new Vr.VREvent.SdkConfigurationParams();
                        }
                        codedInputByteBufferNano.readMessage(this.requestedParams);
                        break;
                    default:
                        if (!super.storeUnknownField(codedInputByteBufferNano, tag)) {
                            return this;
                        }
                        break;
                        break;
                }
            }
        }

        @Override // com.google.protobuf.nano.ExtendableMessageNano, com.google.protobuf.nano.MessageNano
        public final void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.sdkVersion != null) {
                codedOutputByteBufferNano.writeString(1, this.sdkVersion);
            }
            if (this.requestedParams != null) {
                codedOutputByteBufferNano.writeMessage(2, this.requestedParams);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
}
