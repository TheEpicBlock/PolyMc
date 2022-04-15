# PolyMc
PolyMc aims to be a compatibility layer between a modded server and the vanilla client.
Allowing already existing mods to add blocks, items and other content without having to install the mod.

The advantage of PolyMc is to separate the serverside mechanics from the hacks needed to display the mod on the client.
PolyMc doesn't edit anything serverside, this means that mods adding blocks genuinely add those blocks, they'll have their own namespace and everything.
It is only on the packet level that that block gets replaced with the block used for the client

PolyMc is compatible with [Fabric](https://fabricmc.net) and [Quilt](https://quiltmc.org/) and is licensed under the 
[LGPL](https://github.com/TheEpicBlock/PolyMc/blob/master/LICENSE). You can find the source code [here](https://github.com/TheEpicBlock/PolyMc/).

