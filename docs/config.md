# Config
## Disabled mixins
This is a way to disable specific mixins within PolyMc. Doing this isn’t recommended unless you know what the mixins do.
Most mixins have an explanation in their javadoc.

## misc
### misc.processSyncedBlockEventServerSide

The server can send a block event packet to a client. This instructs the client to execute an event on that location. This is used for example with note blocks to make the sound and spawn the particle.
This config has a list of blocks in the namespace:block format. The block event for those block will be executed on the server instead of the client.
The results of this will vary per block. (The block event is specified in the Block#onSyncedBlockEvent)

## remapVanillaBlockIds
This will ensure vanilla blocks keep the right ids. Switch this to true if a mod is messing up the ids, and you want it not to be messed up.

## enableWizardThreading
Executes wizard updates on a different thread. This is experimental and *will crash* at this point in time. If you don't use add-on mods that add wizard you don't need to worry about this.

## maxPacketsPerSecond
Provides a rough estimate of the maximum amount of packets that should be sent per second. This isn't a hard limit.

## forceBlockIdIntControl
Configures how PolyMc modifies some of the packets. 
There's no real reason to set this to true unless you use [polyvalent](https://github.com/skerit/polyvalent).
Setting this to true may cause compatibility issues with other mods.

## blockItemMatching
Will poly block items to vanilla block items with the same placement behaviour and sound.
For example, a modded ore might sound like stone, so it could get polyd with a vanilla stone block
such as cobblestone or granite.

When block item matching is disabled, the server has to reply to you right-clicking before the block appears on the client.
With block item matching enabled, the client will immediately place the vanilla block for you,
but the block won't look like a modded block until the server has replied.
Thus, enabling this option is a tradeoff between latency/ping and aesthetics.