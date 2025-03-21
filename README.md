
# Nothirium

Nothirium is a client-side only Minecraft mod designed for version 1.12.2. It revamps Minecraft's chunk rendering engine to leverage modern OpenGL, which significantly enhances performance. While Nothirium shares similarities with the Sodium mod, it is not a port of Sodium.

## Requirements
To run Nothirium, you will need:
- MixinBootstrap
- RenderLib
- Forge Mod Loader for version 1.12.2

If you encounter issues launching the game due to another mod using an older mixin version, consider using Mixin 0.7-0.8 Compatibility.

## Incompatibilities

### Full Incompatibilities
- FarPlane2
- LittleTiles
- Albedo
- Colored Lux

### Partial Incompatibilities
- **Optifine**: Ensure 'Smart Animations' is turned off in the graphics settings. Custom block render layer from Optifine is not compatible with Nothirium.
- **VanillaFix**: Disable 'textureFixes' in the VanillaFix configuration.
- **CensoredASM**: Turn off 'onDemandAnimatedTextures' in the CensoredASM/LoliASM configuration.

## Additional Notes
- Dynamic Lights is compatible with Nothirium, despite not being mentioned on the official CF page.
- For more information and updates, join the [Nothirium Discord](https://discord.gg/YXgPJnJafP).
- View the mod on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/nothirium).

## License
Nothirium is licensed under the Apache License, Version 2.0. See the LICENSE file for more details.
