# packet-maps

Library for doing cool stuff with Minecraft maps using packets

## Preamble

This library was not intended to be published this quickly, but I am using it currently in a project of mine, so I might as well make it public on
GitHub. The code is lacking documentation, and it is pretty messy in some places. I will try to clean it up over time.

## Advantage of using packets as opposed to the Bukkit map api

Bukkit's map api is slow and its `MapPalette` enum only has a limited range of colors. With packets, you have much more possibilities and not as much
server load. You are also modifying real maps when using the Bukkit api which could cause problems with the vanilla gameplay. With packets, you don't
have that problem, the changes are only client side and won't affect the 'real' maps.

## Modules

This project consists of six modules:

- core
- nms-base
- nms-v1_16_3
- nms-v1_16_4
- plugin
- tools

The `core` module contains stuff like a MapScreen class and caches.\
The `nms-base` module contains common classes that are needed by version-specific implementations.\
The `nms-v1_16_3` is an implementation for the Minecraft version 1.16.3.\
The `nms-v1_16_4` is an implementation for the Minecraft version 1.16.4.\
The `plugin` module is a simple implementation of this library.\
The `tools` module contains tools that make the development of `packet-maps` significantly easier.

> **Note**: The `plugin` module is ***NOT*** meant for usage on a production server. It is mainly used by me to test the libraries functionalities. You COULD use it, but you should expect bugs and/or unwanted behaviour.

## Using the library

Check out the [wiki](https://github.com/cerus/packet-maps/wiki)!

## packet-maps in action

[Playing GIFs](https://www.youtube.com/watch?v=tvmkfxCtFQc)

[Playing a (very) short video](https://www.youtube.com/watch?v=kwjzMHfWp38)

[Basic 3D rendering](https://www.youtube.com/watch?v=MSdLTMkaQ5Q)

### Rendering the Minecraft world onto a map

Note: this is a separate plugin using `packet-maps` as its backbone.

[Rendering the Minecraft world onto a map](https://www.youtube.com/watch?v=AsvD89DS_rI)

[Rendering the Minecraft world onto a map - Update: texture & shadow showcase](https://youtu.be/c1oOKlEkebI)