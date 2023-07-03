# Hammer Multipart

Hammer Multipart Library is a Minecraft library that enables placing multiple parts into a single block.

## Features:
- **Multiple Parts**: Allows placing multiple parts within a single block.
- **Custom Renderers**: Provides custom renderers to display the multipart block visually.
- **Models**: Supports defining custom models for each part of the multipart block.
- **Interaction**: Allows interaction with individual parts of the block.
- **Hitbox Overrides**: Enables overriding hitboxes for each part.
- **Tickers**: Supports adding tickers to control behavior and updates of the multipart block.

## How do I use it?

To put simply, you should check out the buit-in examples in ``/src/main/java/org/zeith/multipart/impl/parts/``.

There you could find ladder part, torch and soul torch parts already implemented.

Gradle:

First of all, add Zeith maven repo:
```groovy
maven {
    name = "Zeitheron Maven"
    url = "https://maven.zeith.org/"
}
```

Then include HammerLib and HammerMultipart:
```groovy
implementation "org.zeith.hammerlib:HammerLib-${config.mc_version}:${config.hl_version}:deobf"
implementation "org.zeith.hammermultipart:HammerMultipart-${config.mc_version}:${config.hmp_version}:deobf"
```

You should be all set by now!