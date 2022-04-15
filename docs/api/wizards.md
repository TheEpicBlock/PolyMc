# Wizards
At an abstract level, wizards are objects that exist in a location in the world and can send 
packets to players in watching distance. (Ignore the name, I was going to name them block entities, but that's already taken. So we're calling them wizards)
You're probably just going to use wizards to spawn armor stands. But technically you could also spawn particles or other entities.

Wizards can be attached to both entities and blocks. 
For example, you could have an aquarium block that spawns a fish when it's placed. 
Or you could make summon a zombie to represent your modded entity.

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

!!! note
    PolyMc automatically handles cases such as when your block is pushed by a piston or if your block is placed inside a falling sand entity.
    This is why you should implement `onMove` even for blocks.

## Entities
Attaching a wizard to an entity is similar to attaching one to attaching one to a block. 
It's recommended to implement `EntityWizard` instead of `Wizard` so you have access to the entity. 
See also: [entities](entities.md).

## Ticking
The `onTick` function isn't called by default. You'll have to override `needsTicking` as well. 
??? example "Ticking Example"
    This example spawns a particle in the center of a block every tick.
    ```java
    public class MyTickingWizard extends Wizard {
        @Override
        public void onTick() {
            this.getPlayersWatchingChunk().forEach(player -> {
                    player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.WAX_ON,
                    false,
                    this.getPosition().x,
                    this.getPosition().y+0.5,
                    this.getPosition().z,
                    0, 0, 0, 0, 0));
            });
        }

        @Override
        public boolean needsTicking() {
            return true;
        }
    }
    ```
!!! note
    The way ticking works will likely be changed in the future to allow for packets to be sent faster than 20hz.

## Virtual Entity API
The virtual entity api is an api to easily send packets as if they come from an actual entity.
This gives full control over the packets sent and removes the need to construct an `Entity` object.

### Example
```java
public class MyWizard extends Wizard {
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

    @Override
    public void onMove() {
        this.getPlayersWatchingChunk().forEach((player) -> virtualItemFrame.move(player, this.getPosition(), (byte)0, (byte)0, true));
    }
}
```