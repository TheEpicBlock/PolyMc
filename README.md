# PolyMc
This mod works as a polyfill library of sorts. It replaces modded items/other stuff with vanilla items before they get sent to the client.  
For the rest of the server code it'll look like nothing has changed. Item ids and such will stay the same. 

At the current stage pretty much all modded items can be made fully compatible with the vanilla client. PolyMC automatically generates a resourcepack to use.  
Look [here](https://github.com/TheEpicBlock/PolyMc/projects/2) to see the current progress of the project. 

An api is planned. It'll allow you to define custom implementation of how items/other stuff should be translated to their vanilla format. It'll come soon™. 
