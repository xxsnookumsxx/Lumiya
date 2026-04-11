package com.lumiyaviewer.lumiya.ui.chat;

import java.io.File;

/* JADX INFO: loaded from: classes.dex */
class ExportResult {
    final File outputFile;
    final String rawText;
    final String rawTextTitle;

    ExportResult(File file, String str, String str2) {
        this.outputFile = file;
        this.rawText = str;
        this.rawTextTitle = str2;
    }
}
