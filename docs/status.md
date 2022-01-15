---
layout: default
title: Status of the project so far
nav_order: 2
---

# Status of the project so far
<img align="left" width="20" height="20" src="https://raw.githubusercontent.com/wiki/TheEpicBlock/PolyMc/Images/Green.png"> PolyMc provides good support for this. You can decently use a mod with these with a vanilla client, as far as that is possible.  
<img align="left" width="20" height="20" src="https://raw.githubusercontent.com/wiki/TheEpicBlock/PolyMc/Images/Orange.png"> These won't crash the client. You can still play with these but you might not be able to fully use them.  
<img align="left" width="20" height="20" src="https://raw.githubusercontent.com/wiki/TheEpicBlock/PolyMc/Images/Red.png"> PolyMc doesn't support this yet. They might crash the client, they might not.

*all of the information here applies to the latest development build*

## Items <img align="right" style="height:20px" width="400" height="20" src="https://raw.githubusercontent.com/wiki/TheEpicBlock/PolyMc/Images/Green.png">
99% of items work fully on a vanilla client.
You might encounter issues with items that use custom renders or edit their appearance with custom properties.
In general, you should be completely fine with using items with PolyMc.

## Blocks <img align="right" style="height:20px" width="400" height="20" src="https://raw.githubusercontent.com/wiki/TheEpicBlock/PolyMc/Images/Orange.png">
Blocks need to consider their clientside collision shapes. And be careful not to replace any vanilla blocks.
A few block shapes have been implemented in PolyMc:
* Full blocks: 799
* Doors: 8
* Trapdoors: 8
* Uncollidable blocks: 116
* Path blocks: 5
* Slabs: 5
* Stairs: 4
Note that some transparent textures might display incorrectly.

## GUIs <img align="right" style="height:20px" width="400" height="20" src="https://raw.githubusercontent.com/wiki/TheEpicBlock/PolyMc/Images/Orange.png">
GUIs do work currently but out of the box do not function as well as other parts of PolyMc.
Currently it can only copy over the inventory slots of screens. Other functionality can be implemented manually through PolyMc's api but is not done automatically.

## Entities <img align="right" style="height:20px" width="400" height="20" src="https://raw.githubusercontent.com/wiki/TheEpicBlock/PolyMc/Images/Red.png">
There is no support for entities in PolyMc yet. Any modded entity will display as a pig.

## Enchantments <img align="right" style="height:20px" width="400" height="20" src="https://raw.githubusercontent.com/wiki/TheEpicBlock/PolyMc/Images/Green.png">
Custom enchantments are supported and should display correctly on the item.
Unless the enchantment has some clientside ability, it should also be fully functional.

## Sounds <img align="right" style="height:20px" width="400" height="20" src="https://raw.githubusercontent.com/wiki/TheEpicBlock/PolyMc/Images/Green.png">
PolyMc supports custom sounds.
Unless a mod uses its own sound system it should work without issues.

## Recipes <img align="right" style="height:20px" width="400" height="20" src="https://raw.githubusercontent.com/wiki/TheEpicBlock/PolyMc/Images/Green.png">
Custom recipes will work in a crafting table or other crafting station.

## Packets <img align="right" style="height:20px" width="400" height="20" src="https://raw.githubusercontent.com/wiki/TheEpicBlock/PolyMc/Images/Green.png">
Custom packets don't do harm and will be ignored by the client. 
Using the config they can be disabled completely. 
Look [here](https://github.com/TheEpicBlock/PolyMc/wiki/Config#custompacketdisabler) for the advantages of doing this.