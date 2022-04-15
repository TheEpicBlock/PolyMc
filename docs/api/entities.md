# Entities
Entity polys extend `EntityPoly`. This poly is basically just a factory for a `Wizard`.
Wizards are explained in greater detail [here](wizards.md), 
they allow you to summon other vanilla entities to represent your modded one and can also do other things like sending particles.
It's recommended to extend `EntityWizard` instead of `Wizard` to make your life easier.

For an example, check out `MissingEntityPoly`, which is the default entity poly. 
It simply spawns a barrier item to let people know that there's an entity there but PolyMc can't display it.