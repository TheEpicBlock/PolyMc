---
layout: default
title: examples
nav_order: 20
---
# Examples

## Creating a custom PolyMap
The easiest way to do a custom PolyMaps is to use a `PolyRegistry` to build a `PolyMapImpl`.
You can also make an instance of `NOPPolyMap` if you want your PolyMap to not edit packets at all.
Or implement the `PolyMap` interface yourself.

To make PolyMc use your custom PolyMap, register an event in `PolyMapProvider.EVENT`. Example:
```java
PolyMapProvider.EVENT.register((player) ->{
		if (isSpecial(player)) {
			return new MySpecialPolyMap();
        } else {
			return null;
        }
});
```
Returning `null` here uses the next best map. If no other events are registered it will default to PolyMc's main map.