# The PolyMc api
The PolyMc api allows you to use PolyMc's systems to edit the way PolyMc behaves.
If a mod already works fine with PolyMc, you don't need to implement anything using the api. 
But if certain things don't work, you might be able to use the api to fix those manually. 
After all, not everything can be automated.

There are also certain functions inside PolyMc that are only available via the api. Notably the [wizard system](wizards.md).

If you've got any questions about the api, don't hesitate to ask them in the [#PolyMc channel](https://discord.gg/hbp9Gv2) of the Fabric Serverside Development discord.

## Importing PolyMc
PolyMc's versioning scheme is `{polymc-version}+{mc-version}`. You can look [here for a list of versions.](https://maven.theepicblock.nl/nl/theepicblock/PolyMc/)

```groovy
repositories {
    maven {
        url "https://maven.theepicblock.nl"
        content { includeGroup("nl.theepicblock") }
    }
}

dependencies {
   modImplementation "nl.theepicblock:PolyMc:VERSION"
}
```

## Entrypoint
PolyMc provides an entry point for you to interact with it. Implement `PolyMcEntrypoint` and add it to your `fabric.mod.json`:
```json
{
  "entrypoints": {
    "polymc": [
      "io.github.theepicblock.ExampleEntrypoint"
    ]
  }
}
```
