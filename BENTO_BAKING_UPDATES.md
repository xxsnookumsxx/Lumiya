# Bento/Avatar Baking System Updates for Modern Second Life

## Overview

This update modernizes the Lumiya avatar baking system to support modern Second Life features including Bento mesh avatars, PBR (Physically Based Rendering) materials, and improved server-side baking via the UpdateAvatarAppearance CAP.

## Changes Made

### 1. Extended AvatarTextureFaceIndex (`AvatarTextureFaceIndex.java`)

Added new texture face indices for modern SL features:

**Bento Mesh Parts:**
- `TEX_LEFT_ARM_BAKE` - Left arm texture for Bento avatars
- `TEX_RIGHT_ARM_BAKE` - Right arm texture for Bento avatars
- `TEX_LEFT_LEG_BAKE` - Left leg texture for Bento avatars
- `TEX_RIGHT_LEG_BAKE` - Right leg texture for Bento avatars
- `TEX_SPINE_BAKE` - Spine/torso texture for Bento avatars
- `TEX_PELVIS_BAKE` - Pelvis texture for Bento avatars

**Physics Bakes:**
- `TEX_HEAD_PHYSICS_BAKE` - Physics simulations for head (breasts, belly, butt)
- `TEX_UPPER_PHYSICS_BAKE` - Physics simulations for upper body
- `TEX_LOWER_PHYSICS_BAKE` - Physics simulations for lower body

**PBR Materials:**
- `TEX_HEAD_NORMAL` - Normal map for head
- `TEX_UPPER_NORMAL` - Normal map for upper body
- `TEX_LOWER_NORMAL` - Normal map for lower body
- `TEX_HEAD_SPECULAR` - Specular map for head
- `TEX_UPPER_SPECULAR` - Specular map for upper body
- `TEX_LOWER_SPECULAR` - Specular map for lower body

### 2. Extended BakedTextureIndex (`BakedTextureIndex.java`)

Added new baked texture entries:

**Bento Mesh Bakes:**
- `BAKED_LEFT_ARM`
- `BAKED_RIGHT_ARM`
- `BAKED_LEFT_LEG`
- `BAKED_RIGHT_LEG`
- `BAKED_SPINE`
- `BAKED_PELVIS`

**Physics Bakes:**
- `BAKED_HEAD_PHYSICS`
- `BAKED_UPPER_PHYSICS`
- `BAKED_LOWER_PHYSICS`

**PBR Material Bakes:**
- `BAKED_HEAD_NORMAL`
- `BAKED_UPPER_NORMAL`
- `BAKED_LOWER_NORMAL`
- `BAKED_HEAD_SPECULAR`
- `BAKED_UPPER_SPECULAR`
- `BAKED_LOWER_SPECULAR`

### 3. Extended BakeLayers (`BakeLayers.java`)

Added layer sets for all new baked textures:

- **Bento Mesh Layer Sets**: Basic skin color, shading, and highlight layers for each mesh part (arms, legs, spine, pelvis)
- **Physics Bake Layer Sets**: Base skin layers for physics simulations (placeholder implementation)
- **PBR Layer Sets**: Empty placeholder layer sets for normal and specular maps (future implementation)

**Note**: The new layer sets use existing skin texture assets where possible. PBR layers are placeholders that will need proper normal/specular baking implementation.

### 4. Enhanced BakeProcess (`BakeProcess.java`)

- **Dynamic Texture Array**: Changed `PrepareAvatarTextureEntry()` to use dynamic array sizing based on `AvatarTextureFaceIndex.valuesCustom().length` instead of hardcoded 32
- **Supports New Indices**: The baking process can now handle up to 54 texture face indices (was 32)

### 5. Enhanced SLAvatarAppearance (`SLAvatarAppearance.java`)

**Server-Side Baking Improvements:**

- **Better Path Selection**: Added explicit logic to choose between legacy client-side baking and modern server-side baking
- **COF Version Tracking**: Improved COF (Current Outfit Folder) version tracking with detailed logging
- **Success Confirmation**: Added confirmation logging when server-side bake is successfully applied
- **Force Rebake**: Enhanced force rebake logic with clear logging

**Key Changes:**

1. `ForceUpdateAppearance()`: Now has three distinct paths:
   - Legacy path (no UpdateAvatarAppearance CAP)
   - Modern path with forced rebake
   - Modern path with normal update

2. `HandleAvatarAppearance()`: Enhanced COF version tracking with success confirmation

3. `UpdateServerSideAppearance()`: Improved error handling and success confirmation

## Architecture Notes

### Baking Paths

The system now supports two baking paths:

1. **Legacy Client-Side Baking**:
   - Uses OpenJPEG to bake textures locally
   - Uploads baked textures via `BakeProcess`
   - Falls back to this path when UpdateAvatarAppearance CAP is unavailable
   - Original 6-bake system: HEAD, UPPER, LOWER, EYES, SKIRT, HAIR

2. **Modern Server-Side Baking**:
   - Uses UpdateAvatarAppearance CAP to request server-side baking
   - Server handles all texture baking and applies to avatar
   - Preferred by modern Second Life grids
   - Supports extended 21-bake system with Bento and PBR

### Bento Support

Lumiya already has full Bento skeleton support (133 bones including face, hands, wings, tail, hindlimbs). This update adds the corresponding texture baking support for:
- Individual limb baking (arms, legs)
- Torso segmentation (spine, pelvis)
- Physics simulation textures

### PBR Support

PBR (Physically Based Rendering) textures (normal maps, specular maps) are now supported at the indexing level. The layer sets are placeholders that will need:
1. Normal map baking implementation
2. Specular map baking implementation
3. Asset integration (normal/specular TGA files)

### Backward Compatibility

The changes maintain full backward compatibility:

- Legacy viewers will ignore new baked texture indices
- Client-side baking still works for the original 6 bakes
- Server-side baking is used when available, with fallback to client-side
- No changes to existing mesh formats or skeleton

## Current Limitations

1. **PBR Baking**: Normal and specular map baking is not yet implemented (placeholder layer sets)
2. **Physics Baking**: Physics simulation textures use simple base layers (placeholder)
3. **Mesh Assets**: Bento mesh parts use existing skin textures; may need dedicated mesh files
4. **Server Compatibility**: Server-side baking requires grid support for UpdateAvatarAppearance CAP

## Testing Recommendations

1. **Legacy Path**: Test on grids without UpdateAvatarAppearance CAP
2. **Modern Path**: Test on grids with UpdateAvatarAppearance CAP (main SL grids)
3. **Bento Avatars**: Test with Bento mesh avatars (check texture application)
4. **Physics**: Test with physics-enabled attachments (breasts, belly, butt)
5. **COF Tracking**: Monitor COF version changes during outfit changes
6. **Fallback**: Ensure fallback to client-side baking when server fails

## Future Enhancements

1. Implement proper normal map baking for PBR support
2. Implement specular map baking for PBR support
3. Add physics simulation texture generation
4. Integrate Bento mesh texture atlases
5. Add support for custom Bento mesh attachments
6. Implement material property syncing

## References

- Bento Project: https://wiki.secondlife.com/wiki/Bento_Project
- PBR Materials: https://wiki.secondlife.com/wiki/PBR_Project
- UpdateAvatarAppearance CAP: Second Life CAP documentation
- COF (Current Outfit Folder): https://wiki.secondlife.com/wiki/COF
