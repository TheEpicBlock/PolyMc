# FAQ

## Does this work with Bedrock/GeyserMc?
**tl;dr: no.**

The way bedrock works is quite different from Java. The hacks PolyMc does won't work well for Bedrock.
I believe Geyser has a way to convert resource packs, so you might have some luck with that. 
Ideally a separate mod would be created that handles Bedrock clients, 
considering Bedrock actually supports custom blocks/items/entities, such a mod should work really well. 
Unfortunately nobody actually has had the time to make it.

## Why is this block stone?
PolyMc might not be able to handle the collision shape of the block, or PolyMc might have run out of blocks. 
See [the limitations page](limitations.md).