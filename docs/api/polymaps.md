# PolyMaps
The PolyMap is what stores all the polys that PolyMc uses. These Polys in turn define how different things are transformed.
By default, PolyMc will generate a single PolyMap using the `PolyRegistry`. This acts as a builder for the PolyMap.
You can add stuff to the `PolyRegistry` via the `registerPolys` method in your entrypoint.

After the entrypoint is called, PolyMc will iterate the registries and attempt to auto generate polys for anything that doesn't have them.

## Custom PolyMaps
You can actually assign different PolyMaps to different players. The PolyMap used for a player is determined by the 
`PolyMapProvider.EVENT` event.

```java title="example"
PolyMapProvider.EVENT.register(player -> {
		if (isSpecial(player)) {
			return MY_SPECIAL_POLYMAP;
        } else {
			return null;
        }
});
```
Using this feature you can also completely disable PolyMc for a player. This is done by returning a `NOPPolyMap`.
Other uses for this include, for example, a PolyMap that doesn't use a resource pack.
Simply instantiate a new `PolyRegistry`, somehow make sure none of the polys use resource packs, 
build the PolyMap from the registry, and then you're good to go.