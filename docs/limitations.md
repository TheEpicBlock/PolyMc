# Limitations of PolyMc
!!! note
    PolyMc only affects what the client sees. The behaviour of things should always just work™.

PolyMc attempts to automatically convert blocks and items into things a vanilla client can understand.
This may or may not actually work, depending on the mod. The only real way to know if a mod will work is to:
<div aria-label="try it and see">
    <video aria-hidden=true controls="" width="100%">
        <source src="https://tryitands.ee/tias.mp4" type="video/mp4">
    </video>
</div>

Here's a list of what PolyMc can approximately do. 
!!! note
    These aren't guarantees, sometimes mods do wierd stuff.

## Items
All items should work perfectly fine. 
You might encounter issues with items that use custom renders or edit their appearance with custom properties.

## Blocks
Your mileage will vary depending on the collision shape of the block. 
It kinda has to match up with what the client thinks.

PolyMc uses block states that go unused on the client side to display modded blocks. Here's an approximate, non-exhaustive, list of 
how many unused block states each collision shape has:

* Full blocks: 979
* Uncollidable blocks: 119
* Doors: 8
* Trapdoors: 8
* Path blocks: 5
* Slabs: 5
* Stairs: 4

## Recipes
Recipes are fully functional. The data PolyMc sends to the recipe book will include the right nbt to display it properly.
One caveat is that the recipe book's autofill doesn't care for nbt, so when filling in modded items it might get it wrong.
PolyMc attempts to mitigate this by using as many different items as possible.

## Sounds
Custom sounds should totally work.

## Enchantments
Custom enchantments should totally work.

## Gui's
Kinda. By default, you'll just get a chest which contains the slots of the original gui. It's not pretty but it works.
If your gui has buttons, or more slots than a chest, you're out of luck currently. 
The system here can totally be made smarter, but I don't have the time.

## Entities
PolyMc can't support custom entities at this time. Instead, you'll just see a nice barrier item. 
PolyMc does have a neat api for adding support to custom entities though.

## Breaking speeds
PolyMc automatically switches to calculating the breaking speed of custom blocks and items on the server when needed. 
This means the client will receive mining fatigue and PolyMc will provide the breaking animation instead, 
so custom tools and blocks will work fine with PolyMc. 
Breaking modded stuff might be slightly more annoying with high ping but that hasn't been much of an issue so far.