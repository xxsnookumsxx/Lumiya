package com.lumiyaviewer.lumiya.utils;

import com.google.common.logging.nano.Vr;
import com.lumiyaviewer.lumiya.slproto.SLMessage;

/* JADX INFO: loaded from: classes.dex */
public class SimpleStringParser {
    private int curPos = 0;
    private String spaceChars;
    private String string;

    public static class StringParsingException extends Exception {
        public StringParsingException(String str) {
            super(str);
        }
    }

    public SimpleStringParser(String str, String str2) {
        this.string = str;
        this.spaceChars = str2;
    }

    public boolean endOfString() {
        return this.curPos >= this.string.length();
    }

    public SimpleStringParser expectToken(String str, String str2) throws StringParsingException {
        String strNextToken = nextToken(str2);
        if (strNextToken.equals(str)) {
            return this;
        }
        throw new StringParsingException("Expected '" + str + "', got '" + strNextToken + "'");
    }

    public int getHexToken(String str) throws StringParsingException {
        String strNextToken = nextToken(str);
        try {
            return Integer.parseInt(strNextToken, 16);
        } catch (NumberFormatException e) {
            StringParsingException stringParsingException = new StringParsingException("Cannot parse expected integer: " + strNextToken);
            stringParsingException.initCause(e);
            throw stringParsingException;
        }
    }

    public int getIntToken(String str) throws StringParsingException {
        String strNextToken = nextToken(str);
        try {
            return Integer.parseInt(strNextToken);
        } catch (NumberFormatException e) {
            StringParsingException stringParsingException = new StringParsingException("Cannot parse expected integer: " + strNextToken);
            stringParsingException.initCause(e);
            throw stringParsingException;
        }
    }

    public String getPipeTerminatedString(String str) throws StringParsingException {
        String strNextToken = nextToken(str);
        int iLastIndexOf = strNextToken.lastIndexOf(Vr.VREvent.VrCore.ErrorCode.CONTROLLER_GATT_NOTIFY_FAILED);
        return iLastIndexOf >= 0 ? strNextToken.substring(0, iLastIndexOf) : strNextToken;
    }

    public String getSubstring(int i) throws StringParsingException {
        byte[] bArrStringToVariableUTF = SLMessage.stringToVariableUTF(this.string.substring(this.curPos));
        if (bArrStringToVariableUTF.length < i) {
            throw new StringParsingException("End of string reached: wanted " + i + ", still has " + bArrStringToVariableUTF.length);
        }
        byte[] bArr = new byte[i];
        System.arraycopy(bArrStringToVariableUTF, 0, bArr, 0, i);
        String strStringFromVariableUTF = SLMessage.stringFromVariableUTF(bArr);
        this.curPos += strStringFromVariableUTF.length();
        return strStringFromVariableUTF;
    }

    public String nextToken(String str) throws StringParsingException {
        int i = 0;
        if (this.curPos >= this.string.length()) {
            throw new StringParsingException("End of string reached");
        }
        while (this.curPos < this.string.length() && this.spaceChars.indexOf(this.string.charAt(this.curPos)) >= 0) {
            this.curPos++;
        }
        while (this.curPos < this.string.length() && str.indexOf(this.string.charAt(this.curPos)) < 0) {
            i++;
            this.curPos++;
        }
        return this.string.substring(this.curPos - i, this.curPos);
    }

    public void skipAllDelimiters(String str) {
        while (this.curPos < this.string.length() && str.indexOf(this.string.charAt(this.curPos)) >= 0) {
            this.curPos++;
        }
    }

    public void skipOneDelimiter(String str) {
        if (this.curPos >= this.string.length() || str.indexOf(this.string.charAt(this.curPos)) < 0) {
            return;
        }
        this.curPos++;
    }
}
