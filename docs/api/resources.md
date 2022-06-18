# Resources

You can append resources when a resource pack is generated in multiple places. 
Most common is the `addToResourcePack` function inside a poly. 
There's also a `registerModSpecificResources` inside your entrypoint, 
but it's recommended to use [shared values](misc.md#shared-values) instead.

In all of these cases, you'll get two classes; `ModdedResources` and a `PolyMcResourcePack`.

`ModdedResources` gives you access to the modded files. You can query for a variety of things. 
It also allows you to access the client jar, giving you access to Minecraft's own assets. 
Be very careful with copyright issues.

`PolyMcResourcePack` represents the resource pack that's being generated. 
You can retrieve files, edit them, and add new files.

# Retrieving vanilla assets
!!! warning
    Please use vanilla assets sparingly! 
    PolyMc only copies over very basic stuff that isn't really copyrightable, such as `item/generated` type item models.
    The only texture PolyMc copies over is the leather armour texture, which is because all armours need to be inside one file for FancyPants.
    I highly doubt Mojang is going to take any legal action, after all we're doing some pretty cool stuff, but it might be best to play it safe and give people a heads-up if the resource pack is going to include a bunch of Mojang textures.

PolyMc automatically downloads the client jar, so it can access its assets. 
You can access these assets via the api using the `getClientJar` and `includeClientJar` methods inside `ModdedResources`.
The first one will return only assets inside the client jar whilst the latter will include both vanilla assets and other modded assets.