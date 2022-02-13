---
layout: default
title: Getting started with using PolyMc
nav_order: 3
---

# Installing PolyMc
PolyMc works with the fabric modloader. You can install it as a server <a href="https://fabricmc.net/wiki/tutorial:installing_minecraft_fabric_server">here</a>.
Install the latest PolyMc version [from here](https://github.com/TheEpicBlock/PolyMc/releases) and place it in the mods folder. Together with any mod you'd like to use with PolyMc. 

## Generating the resource pack
PolyMc requires a resource pack to be installed on the client to function*. You can generate the resource pack by running the `/polymc generate resources` command.
It will appear in the root of your server in a folder named `resource`. How you distribute the resource pack is up to you.

**Warning**: please check if you have the legal right to redistribute the assets of the mods you're using.

## Need help, info or support?
Check the <a href="https://discord.gg/hbp9Gv2">#PolyMc channel</a> in the Fabric Serverside Development discord. Or <a href="https://github.com/TheEpicBlock/PolyMc/issues/new/choose">open up an issue on github!</a><br>
<a href="https://discord.gg/hbp9Gv2">                              <img alt="link to the discord" src="https://img.shields.io/badge/Fabric_server--side_development-PolyMc-7289DA?logo=discord&logoColor=white&style=flat-square"></a>
<a href="https://github.com/TheEpicBlock/PolyMc/issues/new/choose"><img alt="link to creating a new github issue" src="https://img.shields.io/github/issues-raw/TheEpicBlock/PolyMc?color=succes&logo=github&style=flat-square"></a>
<a href="https://github.com/TheEpicBlock/PolyMc/releases">         <img alt="GitHub release (latest SemVer)" src="https://img.shields.io/github/v/release/TheEpicBlock/PolyMc?sort=semver"></a>
<a href="https://github.com/TheEpicBlock/PolyMc/commits/master">   <img alt="Time since last commit on github" src="https://img.shields.io/github/last-commit/TheEpicBlock/PolyMc?style=flat-square"></a>

## Tweaking things
If you've got a relatively stable modpack, and you would like to tweak things to work better, I highly suggest creating a mod and interfacing through the [PolyMc api](https://theepicblock.github.io/PolyMc/api/)!  
There are quite some blocks that can be used but aren't used by default, such as command or jigsaw blocks

*\* It's technically possible to use PolyMc without a resource pack, but it would require some extreme hacks and usage of the api. You're on your own.*


