# Guis
The PolyMc gui api allows you to replace any modded screenhandler with your own. 
This allows you to do pretty much anything with the slots. Gui polys can be registered like any other poly using the `PolyRegistry#registerGuiPoly` function.

```java
public class ExampleGuiPoly implements GuiPoly {
    @Override
    public ScreenHandler replaceScreenHandler(ScreenHandler base, ServerPlayerEntity player, int syncId) {
        return new ExampleScreenHandler(ScreenHandlerType.GENERIC_9X3, syncId, player.getInventory(), base);
    }

    public static class ExampleScreenHandler extends ScreenHandler {
        protected final ScreenHandler base;
        
        protected ExampleScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandler base) {
            this.base = base;
            for (int y = 0; y < 9; ++y) {
                for (int x = 0; x < 3; ++x) {
                    this.addSlot(new StaticSlot(new ItemStack(Items.BLACK_STAINED_GLASS_PANE)));
                }
            }

            //Player inventory
            for (int y = 0; y < 3; ++y) {
                for (int x = 0; x < 9; ++x) {
                    this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
                }
            }

            //Player hotbar
            for (int hotbar = 0; hotbar < 9; ++hotbar) {
                this.addSlot(new Slot(playerInventory, hotbar, 8 + hotbar * 18, 142));
            }
        }
        
        @Override
        public ItemStack transferSlot(PlayerEntity player, int index) {
            // For this example, you've got to make sure the indexes align with those from the base screenhandler. This depends on what screenhandler is your base.
            base.transferSlot(player, index-x);
        }
    }
}
```

# Using SGui (recommended)
It quickly gets tedious to write your own screenhandlers. 
[SGui](https://github.com/Patbox/sgui) is a small library that's much better for making serverside gui's. 
You can use SGui in a GuiPoly by opening your gui via SGui like normal and then returning `null` to prevent PolyMc from opening any other screenhandlers.
```java
public class SGuiExample implements GuiPoly {
    @Override
    public ScreenHandler replaceScreenHandler(ScreenHandler base, ServerPlayerEntity player, int syncId) {
        var gui = new SimpleGui(ScreenHandlerType.GENERIC_3X3, player, false);
        gui.setTitle(Text.literal("SGui test"));
        gui.open();
        return null;
    }
}
```