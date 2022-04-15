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