# Getting started with PolyMc
## Installation
<script>
  async function downloadLatest() {
    var apiData = await fetch("https://api.github.com/repos/TheEpicBlock/PolyMc/releases/latest")
    window.location = (await apiData.json()).assets[0].browser_download_url
  }
</script>

PolyMc is a normal mod and can be installed into the mods folder.

  [Download latest version :material-download:](javascript:downloadLatest()){ .md-button  .md-button--primary }
  [View all versions :material-file-eye:](https://github.com/TheEpicBlock/PolyMc/releases){ .md-button }

## Generating the resource pack

PolyMc requires a resource pack to be installed on the client to function[^1]. 
You can generate the resource pack by running the `/polymc generate resources` command. 
It will appear in the root of your server in a folder named `resource`. How you distribute the resource pack is up to you.

!!! warning
    Please check if you have the legal right to redistribute the assets of the mods you’re using.

[^1]: It’s technically possible to use PolyMc without a resource pack, 
      but you'd have to use the api for that.

## Need help, info or support?
Check the [#PolyMc channel](https://discord.gg/hbp9Gv2) in the Fabric Serverside Development discord!

<a href="https://discord.gg/hbp9Gv2">                              <img alt="link to the discord" src="https://img.shields.io/badge/Fabric_server--side_development-PolyMc-7289DA?logo=discord&logoColor=white&style=flat-square"></a>
<a href="https://github.com/TheEpicBlock/PolyMc/issues/new/choose"><img alt="link to creating a new github issue" src="https://img.shields.io/github/issues-raw/TheEpicBlock/PolyMc?color=succes&logo=github&style=flat-square"></a>
<a href="https://github.com/TheEpicBlock/PolyMc/releases">         <img alt="GitHub release (latest SemVer)" src="https://img.shields.io/github/v/release/TheEpicBlock/PolyMc?sort=semver"></a>
<a href="https://github.com/TheEpicBlock/PolyMc/commits/master">   <img alt="Time since last commit on github" src="https://img.shields.io/github/last-commit/TheEpicBlock/PolyMc?style=flat-square"></a>
