# PolyMc
What if you could put mods on a server, without needing it on the client?  
Well, that's impossible. But we can try!

---
PolyMc is unique in that it operates on packet level. The advantage is that the mechanics are seperated from how it's rendered.  
Usually when you would make custom items, you would pick a CustomModelData value and in the right-click event of that item you might check for that value and do some custom stuff. PolyMc doesn't work like that. With PolyMc, the server is *actually modded*, meaning the custom item is genuinely its own item and you have full control over its properties.  

Doing it this way also means that PolyMc doesn't actually touch the registering of items. It just converts all non-vanilla items/blocks it finds. This means it work with any Fabric mod you can find. And if you have some way of running other types of mods on Fabric, it should work too. Do you want to know which things will work with PolyMc? Look [here](https://github.com/TheEpicBlock/PolyMc/wiki/Status)

Do you want to get started? [Check out the wiki!](https://github.com/TheEpicBlock/PolyMc/wiki/)

<a href="https://discord.gg/hbp9Gv2">![discord](https://img.shields.io/badge/Fabric_server--side_development-PolyMc-7289DA?logo=discord&logoColor=white&style=flat-square)</a> <a href="https://github.com/TheEpicBlock/PolyMc/issues/">![issues](https://img.shields.io/github/issues-raw/TheEpicBlock/PolyMc?color=succes&logo=github&style=flat-square)</a> ![latest release](https://img.shields.io/github/v/release/TheEpicBlock/PolyMc?style=flat-square&label=latest%20release) ![GitHub commits since latest release](https://img.shields.io/github/commits-since/TheEpicBlock/PolyMc/latest?style=flat-square) <a href="https://theepicblock.nl/builds/">![latest release](https://img.shields.io/endpoint?url=https://theepicblock.nl/builds/shield.json&style=flat-square)</a> ![GitHub](https://img.shields.io/github/license/TheEpicBlock/PolyMc?style=flat-square)
