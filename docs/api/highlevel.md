---
layout: default
title: Concepts / high-level overview
nav_order: 3
---

# Concepts and a high-level overview of the system

## A PolyMap
The `PolyMap` class is essential to the functionality of PolyMc. It defines a list of Polys for everything that can be polyd.

### Polys
A poly defines how a block, item or other polyable thing should be converted from the serverside to the clientside.

## PolyMap generation
PolyMc generates a default PolyMap on startup. It's exposed as `PolyMc#getMainMap()`. 
At startup, PolyMc will create an `PolyRegistry` and pass it through all registered entrypoints. 
Then the minecraft registries will be queried and the respective `Generator` class will be called for each modded element.
This method will have some if statements to register the right poly for that element.