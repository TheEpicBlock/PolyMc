# Item polys
The default poly generation for items works really well, so I don't know why you'd want to 
make a custom item poly. But I'm not here to judge. 

One basic thing you might want to do is to use a different base item for your modded item. This is easy enough:
```java
public void registerPolys(PolyRegistry registry) {
    registry.registerItemPoly(moddedItem, new CustomModelDataPoly(registery.getCMDManager(), moddedItem, Items.DIAMOND_SWORD))
}
```

!!! note
    If your item is damageable, use `DamageableItemPoly` instead of `CustomModelDataPoly`.

If you want to create your own item poly, you should implement the `ItemPoly` interface. 
Never modify the input itemstack directly, always make a copy before doing so.

## Global item polys
Global item polys are applied to all items before they are sent to the client. 
Global item polys implement ItemTransformer and can be registered using PolyRegistry#registerGlobalItemPoly. 
Keep in mind that, like normal item polys, global item polys should never modify the input itemstack directly.

## The CustomModelDataManager

The CMD manager is made to prevent polys from using the same CMD values on the same items. 
At the simplest level, you can request a CMD value to use for a specific item. 
You can also request a number of values, the number returned is the first value you may use, see the javadoc for more detail. 
The CMD manager can also round-robin from a list of items. 
This is the preferred way of doing things as this prevents recipe conflicts 
(the recipe book doesn’t take the CMD value into account, this isn’t a huge deal but is a minor annoyance to clients).

To get the CustomModelDataManager, use `PolyRegistry.getSharedValues(CustomModelDataManager.KEY)`.