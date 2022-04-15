# Block polys

Block polys take in the servers sided blockstates and output one to be displayed on the client. 
This can be any vanilla block you'd like. If your block kinda resembles a wool block, 
you can make it look like a wool block. Usually you'd want to add some distinction to it though.

??? example "Wool block example"
    ```java
    public void registerPolys(PolyRegistry registry) {
        registry.registerBlockPoly(customStone, new SimpleReplacementPoly(Blocks.WHITE_WOOL.getDefaultState()))
    }    
    ```

## Wizards
You can attach a :sparkles:wizard:sparkles: to a block to summon entities or particles at it. [See the section on wizards](../api/wizards.md).

## The BlockStateManager
Minecraft has a limited amount of unused blockstates. Any block we want to be displayed on the client needs a blockstate. 
The BlockStateManager has the job of tracking what blockstates are available for blocks to use. 
Requests can be made to the BlockStateManager to get a blockstate of specific profile and then the BlockStateManager will 
attempt to get an available blockstate for you.

To get the BlockStateManager, use `PolyRegistry.getSharedValues(BlockStateManager.KEY)`.

## Block Profiles
**TODO**