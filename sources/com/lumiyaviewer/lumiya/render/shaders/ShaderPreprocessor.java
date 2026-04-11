package com.lumiyaviewer.lumiya.render.shaders;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
public class ShaderPreprocessor {
    private final ImmutableMap<String, String> definedMacros;

    public ShaderPreprocessor(Map<String, String> map) {
        this.definedMacros = ImmutableMap.copyOf((Map) map);
    }

    @Nullable
    private String processCode(BufferedReader bufferedReader, @Nullable StringBuilder sb) throws IOException {
        String str = null;
        while (true) {
            String line = bufferedReader.readLine();
            if (line == null) {
                return str;
            }
            String strTrim = line.trim();
            if (strTrim.startsWith("#endif") || strTrim.startsWith("#else")) {
                return strTrim;
            }
            if (strTrim.startsWith("#ifdef") || strTrim.startsWith("#ifndef")) {
                boolean zStartsWith = strTrim.startsWith("#ifdef");
                boolean zContainsKey = this.definedMacros.containsKey(strTrim.substring(strTrim.indexOf(32)).trim());
                String strProcessCode = processCode(bufferedReader, zStartsWith == zContainsKey ? sb : null);
                if (Objects.equal(strProcessCode, "#else")) {
                    strProcessCode = processCode(bufferedReader, zStartsWith != zContainsKey ? sb : null);
                }
                if (!Objects.equal(strProcessCode, "#endif")) {
                    throw new IOException("#endif expected");
                }
                str = strTrim;
            } else if (sb != null) {
                String strReplace = strTrim;
                for (Map.Entry<String, String> entry : this.definedMacros.entrySet()) {
                    strReplace = strReplace.replace(entry.getKey(), entry.getValue());
                }
                sb.append(strReplace).append("\r\n");
                str = strReplace;
            } else {
                str = strTrim;
            }
        }
    }

    public String processCode(BufferedReader bufferedReader) throws IOException {
        StringBuilder sb = new StringBuilder();
        processCode(bufferedReader, sb);
        return sb.toString();
    }
}
