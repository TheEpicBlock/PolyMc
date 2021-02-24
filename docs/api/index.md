---
layout: default
title: The PolyMc api
nav_order: 1
---

# The api
You don't need to implement the api to make your mod compatible with PolyMc.
PolyMc will try its best to convert modded items, blocks, etc into things the client can understand.
But it can't account for every edge case. That is what the PolyMc api is for.
It will allow you to use utilities in PolyMc to implement your mod's unique features in a vanilla compatible way.

For any questions, you can reach me in the #PolyMc channel in the badge below or ping me <em>(TheEpicBlock_TEB#0452)</em> on the main fabric discord.
<a href="https://discord.gg/hbp9Gv2"><img alt="link to the discord" src="https://img.shields.io/badge/Fabric_server--side_development-PolyMc-7289DA?logo=discord&logoColor=white&style=flat-square"></a>

## Importing PolyMc into gradle
Firstly you'll need to import the Api from Jitpack. VERSION is the version of PolyMc you need.
<a href="https://github.com/TheEpicBlock/PolyMc/releases/"><img src="https://img.shields.io/github/v/release/TheEpicBlock/PolyMc?style=flat-square&label=latest%20release"></a>

```
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
   modImplementation "com.github.TheEpicBlock:PolyMc:VERSION"
}
```

Then register to the PolyMc entrypoint in the fabric.mod.json.

```json
"entrypoints": {
    "polymc": [
        "io.github.theepicblock.ExampleEntrypoint"
    ]
}
```

Optionally, you can also list PolyMc as a dependency if it's absolutely needed for your mod to function. This will cause a hard failure if PolyMc isn't present. Replace depends with recommends for a soft failure. See

```json
"depends": {
    "polymc": ">=2.3.0"
},
```

PolyMc currently provides 2 hooks in the entrypoint. One is to register polys and one is to register mod specific resources. See <a href="resourcepacks.html">resource packs</a>.