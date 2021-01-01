---
layout: default
title: Resources and Resource packs
nav_order: 3
---

# Resources and Resource packs
Resource packs are essential to PolyMc. That's why PolyMc includes a lot of helper methods for working with them.

## Hooks
There are 2 places for your mod to add resources. There is the one included in all Polys. They are meant to register any content specific to that Poly. You can also register resources in the PolyMc entrypoint. It's meant for any resources that don't belong to any specific polyable.

In both of those hooks you'll get passed on a ResourcePackMaker. That is the class where you'll be adding all your resources too.
ResourcePackMaker

The ResourcePackMaker has, in general, 3 types of methods. `getX`, `hasX` and `copyX`. `getX` provides a way to directly access a resource from a mod. This can be used to get the names of models from a blockstate file. To then use in the new blockstate file, for example.
`hasX` can check if a resource exists. `copyX` gets the resource and pastes it directly into the new resource pack. There are subsets of these classes that provide extra utility. See the javadoc for each of these methods. Pay great attention to what value should be given as the path.
``` 
 getFile -----> getAsset

 +------> copyTexture
 |
 copyFile ----> copyAsset -------> copyModel -------> copyItemModel
 |
 |
 +---> importArtificePack

 copyFolder --> importAssetFolder

 checkFile ---> checkAsset
```
*An overview of classes in the `ResourcePackMaker`.*

## Pending system™
Sometimes a file needs to be appended to and edited by multiple locations.
Take CustomModelData for example. Every CMD value that's used needs to be registered to the source item.
That's why the pending system™ exists.
It contains a list of objects representing json mapped to identifiers representing where that json should be.
In the case of CMD, it would get the JsonModel from the pending system™ and add the CMD value to that.
Then at the end of the resource pack generation it will save those to disk.
You're not required to use the pending system.
It should however always be used for minecraft classes.
```
 +------------------------------------------------------+
 |hasPendingModel                                       |
 |putPendingModel -------> getOrDefaultPendingItemModel |
 |getPendingModel                                       |
 |                                                      |
 |hasPendingBlockState                                  |
 |putPendingBlockState --> getOrDefaultPendingBlockState|
 |getPendingBlockState                                  |
 +------------------------------------------------------+
```