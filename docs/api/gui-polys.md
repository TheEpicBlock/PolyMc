---
layout: default
title: Item Polys
nav_order: 18
---

# Gui Polys
A Gui Poly, like any poly, translates a modded id to a vanilla one. in this case, it's the screen handler id.
The default PolyMc poly for gui's is the `NaiveStackListingChestPoly`. It does its job well enough. but if your mod is developed with PolyMc in mind, it is probably worth your time to implement a custom gui poly.

## The path from poly to handler
Most polys have a simple route from poly to vanilla id, just ask the poly for it, but gui poly's  have a little more going on.
When a GuiPoly is called upon. instead of returning a vanilla id it returns a GuiManager. This GuiManager is then called upon to make a ScreenHandler.
This ScreenHandler then does all the interaction between our modded handler and the client.