# PolyMc
What if you could put mods on a server, without needing it on the client?  
Well, that's impossible. But we can try! *Nothing is impossible!*

**Note:** if you're a mod developer looking to make a mod that'll be solely used serverside, you might want to consider [Polymer](https://github.com/Patbox/polymer). It has some advantages and disadvantages over PolyMc.

## How PolyMc works
PolyMc is unique in that it operates on packet level. This means PolyMc doesn't touch the mechanics of the mod at all.
The server is *genuinely modded*. This results in the server being quite stable, all the hacks PolyMc does to display things
are separated from what's actually happening. 

PolyMc uses a host of techniques to automatically try and display your modded things. PolyMc also features a large api
to customize how items/blocks/entities/whatevers are transformed.

Do you want to get started? [Check out the wiki!](https://theepicblock.github.io/PolyMc/)

## Building
Run `./gradlew runDatagen` once. Then just run `./gradlew build`

<a href="https://discord.gg/hbp9Gv2">![discord](https://img.shields.io/badge/Fabric_server--side_development-PolyMc-7289DA?logo=discord&logoColor=white&style=flat-square)</a> 
<a href="https://github.com/TheEpicBlock/PolyMc/issues/">![issues](https://img.shields.io/github/issues-raw/TheEpicBlock/PolyMc?color=succes&logo=github&style=flat-square)</a> 
<a href="https://github.com/TheEpicBlock/PolyMc/releases/">![latest release](https://img.shields.io/github/v/release/TheEpicBlock/PolyMc?style=flat-square&label=latest%20release)</a> 
<a href="https://github.com/TheEpicBlock/PolyMc/commits/">![GitHub commits since latest release](https://img.shields.io/github/commits-since/TheEpicBlock/PolyMc/latest?style=flat-square)</a> 
<a href="https://github.com/TheEpicBlock/PolyMc/blob/master/LICENSE">![GitHub](https://img.shields.io/github/license/TheEpicBlock/PolyMc?style=flat-square)</a>
