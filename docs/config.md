# Config
## Disabled mixins
This is a way to disable specific mixins within PolyMc. Doing this isn’t recommended unless you know what the mixins do.
Most mixins have an explanation in their javadoc.
resource pack

## misc
### processSyncedBlockEventServerSide

The server can send a block event packet to a client. This instructs the client to execute an event on that location. This is used for example with note blocks to make the sound and spawn the particle.
This config has a list of blocks in the namespace:block format. The block event for those block will be executed on the server instead of the client.
The results of this will vary per block. (The block event is specified in the Block#onSyncedBlockEvent)

## remapVanillaBlockIds
This will ensure vanilla blocks keep the right ids. Switch this to true if a mod is messing up the ids, and you want it not to be messed up.

## enableWizardThreading
Executes wizard updates on a different thread. This is experimental and *will crash* at this point in time. If you don't use add-on mods that add wizard you don't need to worry about this.

## maxPacketsPerSecond
Provides a rough estimate of the maximum amount of packets that should be sent per second. This isn't a hard limit.