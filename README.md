# Lumiya — Second Life Viewer for Android (Decompiled Resources)

This repository contains the extracted and decompiled resources from **Lumiya** (version 3.4.2), a Third-party Second Life virtual world viewer client for Android. Lumiya was discontinued, and these resources have been open-sourced for archival and research purposes.

## About Lumiya

Lumiya was a mobile Second Life viewer for Android that allowed users to explore the Second Life virtual world, manage their avatar, chat with other users, and interact with the in-world environment from a mobile device. It supported features such as 3D rendering via OpenGL ES, Google Cardboard VR mode, inventory management, group communication, and Windlight environment presets.

## Repository Structure

```
.
├── assets/
│   ├── anims/          # Avatar animation files (BVH-derived binary format, 27 animations)
│   ├── character/      # 3D avatar mesh models in LBM format
│   │   ├── avatar_eye.lbm
│   │   ├── avatar_head.lbm
│   │   └── avatar_skirt.lbm
│   ├── shaders/        # GLSL ES shader programs for 3D rendering
│   │   ├── sky.vsh / sky.fsh           # Sky dome rendering
│   │   ├── sky_no_clouds.fsh           # Sky without clouds
│   │   ├── stars.vsh / stars.fsh       # Starfield rendering
│   │   ├── water.vsh                   # Water surface rendering
│   │   ├── rigged_mesh.vsh             # Skinned/rigged avatar mesh rendering
│   │   ├── prim.vsh                    # Primitive object rendering
│   │   ├── raw.vsh                     # Raw geometry rendering
│   │   ├── quad.vsh / quad.fsh         # Fullscreen quad rendering
│   │   └── external_texture.vsh        # External texture rendering
│   ├── tga/            # TGA alpha-channel textures for avatar clothing/body parts
│   └── windlight/      # Windlight environment preset files (LLSD XML format)
│       ├── A-6PM.xml   # Evening lighting preset
│       └── A-9PM.xml   # Night lighting preset
├── lib/                # Native shared libraries (.so) for multiple CPU architectures
│   ├── armeabi/        # ARMv5/v6 (libopenjpeg.so)
│   ├── armeabi-v7a/    # ARMv7 (libgvr.so)
│   ├── mips/
│   ├── mips64/
│   ├── x86/            # x86 (libgvr.so, libopenjpeg.so)
│   └── x86_64/
├── res/                # Compiled Android resources
│   ├── layout/         # Activity and fragment XML layouts (86 files)
│   ├── drawable/       # Drawable assets (images, shape drawables)
│   ├── menu/           # Menu definition XML files
│   ├── anim/           # View animation XML definitions
│   ├── animator/       # Property animator XML definitions
│   ├── color/          # Color state list definitions
│   └── xml/            # Preference screen XML definitions
├── resources.arsc      # Compiled Android resource table (4706 string entries)
├── resources.zip       # Zip archive of compiled resources
└── sources.zip         # Zip archive of decompiled source files
```

## Native Libraries

| Library | Purpose |
|---|---|
| `libgvr.so` | Google VR (Cardboard) SDK — enables stereoscopic VR rendering mode |
| `libopenjpeg.so` | OpenJPEG — JPEG 2000 image codec used by Second Life for textures |

Libraries are provided for the following ABI targets: `armeabi`, `armeabi-v7a`, `mips`, `mips64`, `x86`, `x86_64`.

## Shaders

The GLSL ES shaders implement the Windlight atmospheric rendering system used by Second Life:

- **Sky shaders** — Atmosphere scattering simulation with cloud noise, haze, and horizon effects
- **Star shaders** — Procedural starfield for nighttime rendering
- **Water shaders** — Reflective water surface
- **Rigged mesh shaders** — GPU skinning with up to 4 bone influences per vertex, diffuse lighting
- **Primitive shaders** — Basic object rendering

## Windlight Environment Presets

The `assets/windlight/` directory contains LLSD (Linden Lab Structured Data) XML files defining sky/atmosphere parameters including ambient color, atmospheric scattering coefficients (`blue_density`, `blue_horizon`), cloud properties, sun angle, and fog settings.

## Avatar Assets

The `assets/character/` directory holds avatar mesh data in LBM (Linden Binary Mesh) format for the head, eyes, and skirt. The `assets/tga/` directory contains alpha-channel TGA textures used for clothing layer blending (collar, sleeves, skirt slits, highlights, etc.).

## App Features (from UI layouts)

Analysis of the resource layouts reveals the following major UI screens and features:

- **Login & account management** — Multi-account support
- **3D world view** — OpenGL ES viewport with HUD controls
- **VR / Cardboard mode** — Stereoscopic rendering layout (`cardboard_layout.xml`)
- **Inventory management** — Folder/item browser with search
- **Outfit management** — Current outfit viewer and editor
- **Avatar profile & appearance** — Asset info panels
- **Group management** — Group info, roles, permissions, member lists
- **Instant messaging & chat** — Chat history, nearby chat, IM threads
- **Minimap** — Nearby user map overlay
- **Notecard editor** — In-world notecard reading/editing
- **Parcel/land information** — Object popups and location info
- **Mute list management**
- **Transaction log** — L$ transaction history
- **Notifications** — Local and IM notification preferences

## License

This repository contains decompiled resources from the Lumiya application. Lumiya was developed by Mako Cederstrom and published under the package name `com.lumiyaviewer.lumiya`. These resources are shared for archival and research purposes. All original copyrights remain with their respective owners.

## References

- [Second Life](https://secondlife.com/) — the virtual world platform Lumiya connected to
- [Linden Lab](https://www.lindenlab.com/) — creators of Second Life
- [OpenJPEG](https://www.openjpeg.org/) — open-source JPEG 2000 codec
- [Google VR SDK](https://developers.google.com/vr) — Google Cardboard/Daydream VR platform
