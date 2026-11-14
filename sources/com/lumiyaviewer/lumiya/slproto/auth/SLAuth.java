package com.lumiyaviewer.lumiya.slproto.auth;

import android.os.Build.VERSION;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.LumiyaApp;
import com.lumiyaviewer.lumiya.slproto.https.SLHTTPSConnection;
import com.lumiyaviewer.lumiya.utils.HashUtils;
import com.lumiyaviewer.lumiya.utils.StringUtils;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.LinkedList;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Request.Builder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class SLAuth {
   private SLAuthReply SendLoginRequest(SLAuthParams var1) throws IOException {
      byte var5;
      String var7;
      String var10;
      label165: {
         var5 = 0;
         var10 = var1.passwordHash;
         Debug.Log("md5 hash: " + var10);
         if (var1.startLocation != null) {
            if (var1.startLocation.equals("first")) {
               var7 = "home";
               break label165;
            }

            if (var1.startLocation.startsWith("uri:")) {
               var7 = var1.startLocation;
               break label165;
            }
         }

         var7 = "last";
      }

      LinkedList<SLAuth.LoginRequestField> var11 = new LinkedList<>();
      String var12 = var1.loginName.trim();
      int var4 = var12.length();
      int var2 = 0;

      while (var2 < " ._".length()) {
         int var6 = var12.indexOf(" ._".substring(var2, var2 + 1));
         int var3 = var4;
         if (var6 != -1) {
            var3 = var4;
            if (var6 < var4) {
               var3 = var6;
            }
         }

         var2++;
         var4 = var3;
      }

      String var9 = var12.substring(0, var4);
      String var8 = "";
      if (var4 < var12.length()) {
         var8 = var12.substring(var4 + 1);
      }

      var12 = var9.trim();
      var9 = var8.trim();
      var8 = var9;
      if (var9.equalsIgnoreCase("")) {
         var8 = "Resident";
      }

      var11.add(new SLAuth.LoginRequestField("first", var12));
      var11.add(new SLAuth.LoginRequestField("last", var8));
      var11.add(new SLAuth.LoginRequestField("passwd", var10));
      var11.add(new SLAuth.LoginRequestField("start", var7));
      StringBuilder var29 = new StringBuilder().append("Lumiya ");
      if (Debug.isDebugBuild()) {
         var8 = "Test";
      } else {
         var8 = "Release";
      }

      var9 = var29.append(var8).toString();
      var8 = LumiyaApp.getAppVersion();

      for (int var18 = StringUtils.countOccurrences(var8, '.'); var18 < 3; var18++) {
         var8 = var8 + ".0";
      }

      Debug.Printf("Auth: viewer channel '%s', version '%s'", var9, var8);
      var11.add(new SLAuth.LoginRequestField("channel", var9));
      var11.add(new SLAuth.LoginRequestField("version", var8));
      var11.add(new SLAuth.LoginRequestField("platform", "Android"));
      var11.add(new SLAuth.LoginRequestField("platform_version", VERSION.RELEASE));
      var11.add(new SLAuth.LoginRequestField("mac", HashUtils.MD5_Hash("android_id")));
      var11.add(new SLAuth.LoginRequestField("user-agent", "Lumiya"));
      var11.add(new SLAuth.LoginRequestField("id0", var1.clientID.toString()));
      var11.add(new SLAuth.LoginRequestField("agree_to_tos", "true"));
      var11.add(new SLAuth.LoginRequestField("viewer_digest", "f50cfcc3-d6ce-4f16-a822-b91271de4c48"));
      var8 = var1.loginURL;
      var9 = "login_to_simulator";
      SLAuthReply var35 = null;

      for (int var19 = var5; var19 < 5; var19++) {
         StringBuilder var36 = new StringBuilder();
         var36.append("<?xml version=\"1.0\"?>\n");
         var36.append("<methodCall>");
         var36.append("<methodName>").append(var9).append("</methodName>");
         var36.append("<params>");
         var36.append("<param>");
         var36.append("<value>");
         var36.append("<struct>");

         for (SLAuth.LoginRequestField var32 : var11) {
            var36.append("<member>");
            var36.append("<name>");
            var36.append(var32.name);
            var36.append("</name>");
            var36.append("<value>");
            var36.append("<string>");
            var36.append(var32.value);
            var36.append("</string>");
            var36.append("</value>");
            var36.append("</member>");
         }

         var36.append("<member>");
         var36.append("<name>options</name>");
         var36.append("<value>");
         var36.append("<array><data>");
         var36.append("<value><string>buddy-list</string></value>");
         var36.append("<value><string>display_names</string></value>");
         var36.append("<value><string>inventory-root</string></value>");
         var36.append("<value><string>inventory-lib-root</string></value>");
         var36.append("<value><string>max-agent-groups</string></value>");
         var36.append("</data></array>");
         var36.append("</value>");
         var36.append("</member>");
         var36.append("</struct>");
         var36.append("</value>");
         var36.append("</param>");
         var36.append("</params>");
         var36.append("</methodCall>");
         var9 = var36.toString();
         Debug.Log("Start location: " + var7);
         Debug.Log("Auth request: " + var9);
         Request var26 = new Builder()
            .url(var8)
            .header("Connection", "close")
            .post(RequestBody.create(MediaType.parse("text/xml"), var9))
            .header("Content-Type", "text/xml")
            .build();
         Response var39 = SLHTTPSConnection.getOkHttpClient().newCall(var26).execute();
         if (var39 == null) {
            throw new IOException("Null response");
         }

         try {
            if (!var39.isSuccessful()) {
               StringBuilder var17 = new StringBuilder();
               IOException var21 = new IOException(var17.append("Login error code ").append(var39.code()).toString());
               throw var21;
            }

            try {
               XmlPullParser var27 = XmlPullParserFactory.newInstance().newPullParser();
               BufferedInputStream var34 = new BufferedInputStream(var39.body().byteStream(), 65536);
               var27.setInput(var34, null);
               var35 = new SLAuthReply(var1.gridName, var1.loginURL, var27);
               if (!var35.isIndeterminate || var35.nextMethod == null || var35.nextURL == null) {
                  return var35;
               }

               var9 = var35.nextMethod;
               var8 = var35.nextURL;
            } catch (XmlPullParserException var15) {
               Debug.Warning(var15);
               IOException var20 = new IOException("Login reply parse error", var15);
               throw var20;
            }
         } finally {
            var39.close();
         }
      }

      return var35;
   }

   public static String getPasswordHash(String var0) {
      String var1 = var0.trim();
      var0 = var1;
      if (var1.length() > 16) {
         var0 = var1.substring(0, 16);
      }

      return "$1$" + HashUtils.MD5_Hash(var0);
   }

   public SLAuthReply Login(SLAuthParams var1) throws IOException {
      try {
         return this.SendLoginRequest(var1);
      } catch (Exception var2) {
         var2.printStackTrace();
         throw new IOException("Failed to login to simulator");
      }
   }

   private static class LoginRequestField {
      public final String name;
      public final String value;

      private LoginRequestField(String var1, String var2) {
         this.name = var1;
         this.value = var2;
      }
   }
}
