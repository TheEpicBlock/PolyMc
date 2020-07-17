# PolyMc
This mod works as a polyfill library of sorts. It replaces modded items/other stuff with vanilla items before they get sent to the client.  
For the rest of the server code it'll look like nothing has changed. Item ids and such will stay the same. 

This project has the ambitious goal of making any mod compatible with a vanilla client. Of course there will always be hiccups, but at least we're trying. At the moment we support items quite well, with blocks coming soon™. Look [here](https://github.com/TheEpicBlock/PolyMc/wiki/Status) for the current status of the project.

PolyMc also provides an api to let mods manually define methods to make things compatible with vanilla. A mod isn't required to use the api, PolyMC will try it's best to do it automatically. But there might be specific situations where a special approach is much better, that's what the api is for. [This is the page for the api. It'll be population soon...](https://github.com/TheEpicBlock/PolyMc/wiki/Api)
