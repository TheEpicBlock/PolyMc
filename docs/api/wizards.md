---
layout: default
title: Wizards
nav_order: 19
---

# Wizards
Wizards are objects that exist in a location in the world and can send packets to players in watching distance.
This can be used, for example, to spawn an armour stand at that location via the virtual entity api. 

Wizards can be attached to both entities and blocks.

## Basics
Wizards have 3 main methods you can override. `addPlayer`, `removePlayer` and `onMove`.
`addPlayer` and `removePlayer` are called when a player enters/exits the watch distance.
If you're spawning entities, you should use `addPlayer` to send a summon packet and `removePlayer` to send a remove packet.
Use `onMove` will be called every tick for entities, and when a block is moved (for example with a piston). 
Use this method to move your entity.

## Blocks
### Adding
Wizards can be added to blocks via the block's block poly.
```java
@Override
public boolean hasWizard() {
    return true;
}

@Override
public Wizard createWizard(ServerWorld world, Vec3d pos, WizardState state) {
    return new MyWizard(world, pos, state);
}
```
### Wizard state
A wizard attached to a block doesn't have to be confined to the grid. 
It may be attached to a moving piston or a falling sand entity. 
You can check this using the `Wizard#getState()` function. 
A wizard's state can be changed on the fly.

## Entities
Wizards attached to entities will always have the `MISC_MOVING` state. 
You can add wizards to entities using entity polys.

## Virtual Entity API
The virtual entity api is an api to easily send packets as if they come from an actual entity.
This gives full control over the packets sent and removes the need to construct a full entity object.

### Example
```java
private final VItemFrame virtualItemFrame = new VItemFrame();

@Override
public void addPlayer(ServerPlayerEntity playerEntity) {
    virtualItemFrame.spawn(playerEntity, this.getPosition(), Direction.DOWN);
    virtualItemFrame.sendItemStack(playerEntity, new ItemStack(Items.STICK));
    virtualItemFrame.makeInvisible(playerEntity);
}

@Override
public void removePlayer(ServerPlayerEntity playerEntity) {
    virtualItemFrame.remove(playerEntity);
}
```