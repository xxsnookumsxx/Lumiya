# Lumiya Viewer (64-Bit `arm64-v8a` Modernization Project)

**Package**: `com.lumiyaviewer.lumiya`  
**Base Version**: `3.4.2` (`versionCode 66`)  
**Minimum Android OS**: Android 8.0 Oreo (`minSdkVersion 26`)  
**Target ABI**: Pure 64-bit ARM (`arm64-v8a`)

---

## 1. Architecture & 64-Bit (`arm64-v8a`) Compatibility

Both the compiled native libraries (`lib/arm64-v8a/`) and the Java/JNI layer are 64-bit (`arm64-v8a`) compatible. All 32-bit (`armeabi-v7a`, `armeabi`, `x86`, `mips`) and non-ARM64 binaries have been removed to keep the project strictly 64-bit `arm64-v8a`.

### Native Libraries (`lib/arm64-v8a/`)
| Library | Architecture | Size | Purpose | ELF Alignment |
| :--- | :--- | :--- | :--- | :--- |
| `libopenjpeg.so` | 64-bit ARM (`EM_AARCH64`) | 196.9 KB | JPEG2000 (`.j2c`) texture decoding, DXT1/ETC1 compression & terrain heightmaps (20 JNI exports) | `0x10000` (64 KB aligned — passes Android 15+ 16 KB page check) |
| `librawbuf.so` | 64-bit ARM (`EM_AARCH64`) | 9.8 KB | Native off-heap memory buffers for OpenGL vertex/index arrays (8 JNI exports) | `0x10000` (64 KB aligned — passes Android 15+ 16 KB page check) |
| `libgvr.so` | 64-bit ARM (`EM_AARCH64`) | 1.83 MB | Google VR / Cardboard head-tracking & lens distortion | `0x1000` (4 KB aligned) |

### Java JNI Layer
- `com.lumiyaviewer.lumiya.openjpeg.OpenJPEG`: Stores memory-mapped addresses and sizes as 64-bit `long` (`mmappedAddr`, `mmappedSize`) and passes direct `java.nio.ByteBuffer` references to JNI.
- `com.lumiyaviewer.rawbuffers.DirectByteBuffer`: Uses JNI `NewDirectByteBuffer` / `GetDirectBufferAddress` on `ByteBuffer` instances, storing 64-bit pointers safely inside `Buffer.address`.

---

## 2. What is Broken (Runtime & Source Blockers)

### A. 3D Mesh Objects & Mesh Avatars Never Load (`GetMesh` Deadlock)
- **Files**: `SLCaps.java`, `MeshCache.java`
- **Root Cause**: Lumiya requests the legacy `GetMesh` capability and lacks `GetMesh2`. When a simulator only returns `GetMesh2` (or disables `GetMesh`), `getMeshCapURL()` returns `null` and `MeshCache.java` blocks indefinitely in `capURLlock.wait()`.

### B. Inventory Loading Fails — Cascading into Broken Outfits & Avatar Appearance
- **Files**: `SLInventory.java`, `SLInventoryHTTPFetchRequest.java`, `SLAvatarAppearance.java`
- **Root Cause**:
  1. `SLInventory` requests `FetchInventoryDescendents2` and falls back to `SLInventoryUDPFetchRequest` (`FetchInventoryDescendents` over UDP, which Linden Lab permanently disabled).
  2. Modern Second Life uses **AISv3 (`InventoryAPIv3`)** and updated capability endpoints.
  3. **Cascading Effect**: `SLAvatarAppearance` waits for the **Current Outfit Folder (COF, `typeDefault == 46`)** to finish loading (`cofReady = true`) before running `ForceUpdateAppearance()` or `RequestServerRebake()` (`UpdateAvatarAppearance`). When inventory fails, `cofReady` stays `false`, preventing avatar appearance and attachment syncing.

### C. Texture Fetching (UDP Disabled by Linden Lab)
- **Files**: `SLTextureFetcher.java`, `TextureCompressedCache.java`, `SLHTTPSConnection.java`
- **Root Cause**: Second Life disabled UDP `RequestImage` / `ImageData` / `ImagePacket` transfers in favor of HTTP CDN downloads via the `GetTexture` capability.

### D. AWS Cloud Migration Issues (Hardcoded Dead IPs & Hostname Rewriting)
- **Files**: `SLHTTPSConnection.java`, `SLCaps.java`
- **Root Cause**:
  - `SLHTTPSConnection` uses a custom `SLDNS` resolver with a hardcoded fallback IP `216.82.57.58` for `login.agni.lindenlab.com` (decommissioned datacenter IP prior to Linden Lab's 2020 AWS migration).
  - `SLCaps.repairCapabilityURL` rewrites unqualified simulator hostnames to `sim*.agni.lindenlab.com` instead of AWS `simhost-*.agni.secondlife.io`.

### E. Missing "Bakes on Mesh" (BoM) & Universal Wearable Support
- **Files**: `BakedTextureIndex.java`, `AvatarTextureFaceIndex.java`, `SLWearableType.java`
- **Root Cause**:
  - Bento skeleton support (all 133 bones in `SLSkeletonBoneID.java` and 55 attachment points in `SLAttachmentPoint.java`) is already present.
  - **Bakes on Mesh (BoM)** is missing: `BakedTextureIndex.java` only has the original 6 channels (`Head`, `Upper`, `Lower`, `Eyes`, `Skirt`, `Hair`) and lacks the 5 BoM channels (`LeftArm`, `LeftLeg`, `Aux1`, `Aux2`, `Aux3`), Universal wearable types, and magic BoM texture UUID mapping on mesh attachments.

### F. Dead External Plugins (Voice & Google Drive Sync)
- **Voice Chat** (`SLVoice.java`): Relies on external plugin `com.lumiyaviewer.lumiya.voice` (unavailable on Google Play; Second Life is transitioning to WebRTC voice).
- **Cloud Sync** (`DriveSyncService.java`): Relies on external plugin `com.lumiyaviewer.lumiya.cloud` and the deprecated Google Drive Android API.

### G. 36 JADX Un-decompiled Methods in `sources/`
- 36 methods across 36 Lumiya classes failed JADX control-flow recovery during the initial export and contain `throw new UnsupportedOperationException("Method not decompiled: ...")`.
- Full bytecode for all 36 methods is preserved in `base.apk` inside `Lumiya_com,lumiyaviewer,lumiya_3,4,2.apks` (`classes.dex`) and will be reconstructed in Phase 1.

---

## 3. What is Outdated

1. **Android Target SDK & Manifest Rules (`AndroidManifest.xml`)**:
   - Minimum SDK is now set to **API 26 (Android 8.0)**.
   - Raising `targetSdkVersion` to modern Android (API 33–35) requires `android:exported="true|false"` on components with `<intent-filter>`, `PendingIntent.FLAG_IMMUTABLE`, `foregroundServiceType` on `GridConnectionService` and `StreamingMediaService`, `RECEIVER_NOT_EXPORTED` on `registerReceiver()`, and Scoped Storage updates.
2. **Bundled Decompiled Third-Party Libraries in `sources/`**:
   - 1,704 of the 2,996 Java files in `sources/` are decompiled third-party libraries (`android.support.*`, `com.google.common.*`, `com.google.gson.*`, `okhttp3.*`, `okio.*`, `butterknife.*`, `de.greenrobot.dao.*`, `com.google.vr.*`, `com.astuetz.*`, `com.nineoldandroids.*`, `uk.co.senab.photoview.*`), while 1,292 files are Lumiya's own code (`com.lumiyaviewer.*`).
3. **3D Graphics & Environment Shaders**:
   - Uses OpenGL ES 2.0 / 3.0 shaders with static Windlight XML presets (`assets/windlight/`) rather than EEP (Extended Environment Project) and glTF / PBR materials.
4. **Predefined Grid List (`res/values/arrays.xml`)**:
   - Lists defunct OpenSim grids (`Inworldz`, `Avination`, `New World Grid`) and plain `http://` URLs.

---

## 4. Five-Phase Modernization Roadmap

### Phase 1: Clean Gradle Build (`minSdk 26`, `arm64-v8a`) & Recover 36 Decompilation Errors (✅ Completed)
1. ✅ Configured the Android Gradle build system (`settings.gradle`, `build.gradle`, `gradle.properties`, `gradlew`, `stable-ids.txt`) for `minSdkVersion 26`, `targetSdkVersion 26`, and `abiFilters 'arm64-v8a'`.
2. ✅ Extracted all 5,258 third-party library bytecode classes from `base.apk` into `libs/bundled-3p-libs.jar`, removed the 1,704 decompiled third-party source files from `sources/`, and restored properly decoded resources and 9-patch drawables in `res/` via Apktool (`stable-ids.txt` + `attrs.xml` `<declare-styleable>`).
3. ✅ Reconstructed all 36 un-decompiled files (41 methods) in `sources/com/lumiyaviewer/` from `base.apk` (`classes.dex`) using Vineflower/CFR + bytecode recovery, and fixed all JADX decompilation syntax/type/exception artifacts across all 1,292 Lumiya classes (`0` compilation errors). Both `.\gradlew.bat assembleDebug` and `.\gradlew.bat assembleRelease` now build clean 64-bit `arm64-v8a` APKs.

### Phase 2: Fix Broken Second Life Protocol & Capabilities (✅ Completed)
1. ✅ **Mesh Loading (`SLCaps.java`, `MeshCache.java`, `WorldViewRenderer.java`)**:
   - Added `GetMesh2` and `ViewerAsset` capabilities to `SLCaps.java` with fallback resolution (`GetMesh2` -> `GetMesh` -> `ViewerAsset`).
   - Updated `WorldViewRenderer.java` to set the mesh capability URL on `RenderContext` both on `onAgentCircuit` and when the OpenGL surface is created/recreated in `onSurfaceCreated`.
   - Fixed `MeshCache.java` infinite wait deadlock on `capURLlock.wait()` and added the missing `return;` after `completeRequest(new MeshData(this.outputFile))` so downloaded meshes are not overwritten with `completeRequest(null)`.
2. ✅ **Inventory & COF Loading (`SLInventory.java`, `SLInventoryHTTPFetchRequest.java`, `SLAssetType.java`, `SLInventoryType.java`)**:
   - Fixed two critical JADX boolean inversions in `SLInventoryHTTPFetchRequest.java` (`DatabaseCommitThread.run()` skipping `retainChildren()` when `z == true`, and `SLInventoryHTTPFetchRequest$1.run()` passing `isCancelled = true` to `completeFetch()` on every non-interrupted run).
   - Fixed `FolderEntryContentHandler` and `ItemEntryContentHandler` fallback `parentUUID` (`folderUUID` instead of `folderEntry.parentUUID`), added handling for `preferred_type` (`case 6`) and special folder type strings (`"current"` -> `FT_CURRENT_OUTFIT = 46`, `"outfit"`, `"my_outfits"`, etc.), and added `AT_SETTINGS` (`56`) and `AT_MATERIAL` (`57`) asset/inventory types.
   - Updated `SLInventory.java` so `rootFolderFetchNeeded` is triggered on every new session and `onRootFolderFetched` waits for the active `sessionID` before unsubscribing, ensuring the root folder and Current Outfit Folder (`cofReady`) load reliably on login.
3. ✅ **Networking, DNS & HTTP Texture Fetching (`SLHTTPSConnection.java`, `SLCaps.java`, `SLTextureFetcher.java`, `TextureCompressedCache.java`)**:
   - Fixed `SLHTTPSConnection.SLDNS.tryResolveOverHTTP` failing on CNAME chains over Google DoH, added `8.8.8.8`/`8.8.4.4` to `DNSforDNS`, and removed the dead `216.82.57.58` fallback IP for `login.agni.lindenlab.com`.
   - Updated `SLCaps.java` to recognize `.secondlife.io` AWS simulator hosts and retry seed capability requests up to 3 times.
   - Updated `SLTextureFetcher.java` and `TextureCompressedCache.java` to support `GetTexture` and `ViewerAsset` HTTP CDN fetching, including automatic fallback to `GetTexture`/`ViewerAsset` for baked avatar textures.

### Phase 3: Add Bakes on Mesh (BoM) & Modern Avatar Support (✅ Completed)
1. ✅ **Universal Wearable & BoM Texture Channels (`SLWearableType.java`, `AvatarTextureFaceIndex.java`, `BakedTextureIndex.java`)**:
   - Added `WT_UNIVERSAL` (`typeCode = 16`) to `SLWearableType.java` (and updated `RLVCmdGetOutfit.java`).
   - Extended `AvatarTextureFaceIndex.java` with all 16 official Second Life Bakes on Mesh `ETextureIndex` constants (`TEX_HEAD_UNIVERSAL_TATTOO` = 29 through `TEX_AUX3_BAKED` = 44).
   - Extended `BakedTextureIndex.java` with the 5 BoM channels (`BAKED_LEFT_ARM`, `BAKED_LEFT_LEG`, `BAKED_AUX1`, `BAKED_AUX2`, `BAKED_AUX3`) and mapped all 11 baked channels to their official Second Life `IMG_USE_BAKED_*` magic UUIDs.
2. ✅ **64-Bit `TextureEntry` Face Bitfields (`SLTextureEntry.java`, `SLTextureEntryFace.java`)**:
   - Fixed a critical 32-bit `int` bitfield overflow in `SLTextureEntry.ReadFaceBitfield` and `WriteFaceBitfield` when parsing `AvatarAppearance` packets with BoM baked texture indices `40..44` by upgrading `SLTextureEntry` to 64-bit `long` face bitfields (`MAX_FACES = 64`).
3. ✅ **Local Baker Fix & Universal Layer Support (`BakeLayer.java`, `BakeLayers.java`, `BakeProcess.java`)**:
   - Fixed a critical JADX boolean inversion in `BakeLayer.Bake()` (`if (!z6)`) that previously skipped drawing valid local and base layers during local baking.
   - Added Universal tattoo layers (`head_universal_tattoo`, `upper_universal_tattoo`, `lower_universal_tattoo`, `skirt_tattoo`, `hair_tattoo`, `eyes_tattoo`) and `BAKED_LEFT_ARM`..`BAKED_AUX3` layer sets to `BakeLayers.java` and `BakeProcess.java`.
4. ✅ **BoM Attachment Texture Resolution & Base System Mesh Hiding (`DrawableObject.java`, `DrawableAttachments.java`, `DrawableAvatar.java`)**:
   - Resolved `IMG_USE_BAKED_*` magic UUIDs on attached mesh faces to the avatar's active baked texture UUIDs (`AvatarTextures`), automatically refreshing when avatar bakes update.
   - Implemented automatic hiding of underlying classic system avatar meshes (`MESH_ID_HAIR`, `MESH_ID_HEAD` + `MESH_ID_EYELASH`, `MESH_ID_UPPER_BODY`, `MESH_ID_LOWER_BODY`, `MESH_ID_EYEBALL_LEFT` + `MESH_ID_EYEBALL_RIGHT`, `MESH_ID_SKIRT`) when an attachment uses the corresponding `IMG_USE_BAKED_*` channel (`LLVOAvatar::updateMeshVisibility`).

### Phase 4: Android OS Modernization
1. Update `AndroidManifest.xml` (`android:exported`, `foregroundServiceType`, `usesCleartextTraffic="true"` for HTTP OpenSim grids, `POST_NOTIFICATIONS`).
2. Add `FLAG_IMMUTABLE` to `PendingIntent` calls and modernize cache directory selection in `GlobalOptions.java`.
3. Update the predefined grids list in `res/values/arrays.xml`.

### Phase 5: Full RLV / Restrained Love Viewer (RLVa) Modernization
1. Update RLV protocol version reporting (`RLVCmdVersion.java`, `RLVController.java`) and support `@versionnum`, `@versionnew`, and modern RLV/RLVa version handshakes.
2. Update RLV attachment and wearable commands (`@getattach`, `@getoutfit`, `@remattach`, `@detach`, `@remoutfit`, `@attach`, `@attachover`, `@attachall`, `@attachallover`, `@detachall`, `@detachallthis`, `@getinv`, `@getinvworn`, `@findfolder`, `@findfolders`, `@getpath`, `@getpathnew`) to support all 55 Bento/modern attachment points (`RLVCmdGetAttach.java` currently caps at `40` instead of `SLAttachmentPoint.NUM_POINTS = 56`) and `WT_PHYSICS` / `WT_UNIVERSAL` wearable types.
3. Expand RLV restrictions and command coverage (`@unsit`, `@sit`, `@sittp`, `@standtp`, `@tpto`, `@tploc`, `@tplure`, `@tpv`, `@accepttp`, `@acceptpermission`, `@edit`, `@rez`, `@Touch`/`@touchall`/`@touchworld`/`@touchthis`/`@touchfar`, `@fartouch`, `@shownames`, `@NAMETAGS`, `@camdistmax`/`@camdistmin`, `@setgroup`, `@getgroup`, `@ clear` and exception handling) and verify full RLVa/#RLV shared folder compliance.
