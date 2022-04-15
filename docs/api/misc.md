# Misc
## Shared Values
The PolyRegistry keeps track of certain shared values. For example, the `CustomModelDataManager`. 
Shared values allow you to allocate things like CMD values, blockstates, or specific colours of armor. 
You could also use shared values to, for example, generate a models for every block in the game, 
and then you can reuse these models across your polys.

It's rare for you to want to create your own shared value, but if you do want to do it, you'll need to create a `SharedValuesKey`.
This key contains a builder for your class, which will contain all the data you want to store, and a resource factory. 
That last one requires a bit more explanation, in general shared values are stored only in the `PolyRegistry` 
and are thrown away once the registration phase is done. (You should never keep a reference to a shared value).
*But* you might want to use your data to generate resources. In that case you *do* want to keep the information needed 
to generate those resources. 
The resource factory should take in your shared values and return a class which only contains the information 
needed for resource generation.
If you don't need resource generation, simply use `null`, you can also take a look at `ArmorColorManager` for an example.
For `ArmorColorManager` all of the info is still needed during resource gen so the resource factory simply returns itself.