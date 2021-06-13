---
layout: default
title: Item Polys
nav_order: 15
---

# Block Polys
At a basic level, like item polys, block polys take in the servers sided blockstates and output one to be displayed on the client.
Normally PolyMc will attempt to match a modded block to a Block that has a similar shape to the modded one.


## Example: Making a custom stone block appear as a normal stone block.
```java
public void registerPolys(PolyRegistry registry) {
    registry.registerBlockPoly(customStone, new SimpleReplacementPoly(Blocks.STONE.getDefaultState()))
}
```

# The BlockStateManager
Minecraft has a limited amount of unused blockstates.
Any block we want to be displayed on the client needs a blockstate.
The BlockStateManager has the job of tracking what blockstates are available for blocks to use.
Requests can be made to the BlockStateManager to get a blockstate of specific profile and then the BlockStateManager will attempt to get a available blockstate for you.