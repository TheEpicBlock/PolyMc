---
layout: default
title: PolyMc's Config
nav_order: 5
---

# PolyMc's Config
## Disabled mixins
This is a way to disable specific mixins within PolyMc. Doing this isn't recommended unless you know what the mixins do.  
Most mixins have an explanation in their javadoc.
## resource pack
Settings related to the generation of resource packs.
#### `advancedDiscovery`
Enables the advanced resource pack discoverer.  
The advanced resource pack copies all assets of all mods to a temporary file and then picks from that.
This is required for compatibility with some mods.
## misc
#### `processSyncedBlockEventServerSide`
The server can send a block event packet to a client. This instructs the client to execute an event on that location. This is used for example with note blocks to make the sound and spawn the particle.  
This config has a list of blocks in the `namespace:block` format. The block event for those block will be executed on the server instead of the client.  
The results of this will vary per block. (The block event is specified in the `Block#onSyncedBlockEvent`)