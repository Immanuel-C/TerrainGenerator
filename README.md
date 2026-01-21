# Terrain Generator

This is my final project for my grade 12 computer science class. 
It's not fully completed but its due.


It is made using OpenGL loaded with LWJGL and Swing. The interop between
the two is also provided by LWJGL through AWTGLCanvas.

#### How to run

- For this project to run gradle must be installed.
- JDK 21 is the minimum required version and is recommended as it's 
 supported by all the dependencies but newer versions should work fine.
- You can run it with eclipse, IntelliJ or with gradlew.

## OpenGL Overall Design

OpenGL is designed to be a **state machine** that keeps track of a current state and does actions
on the state.

#### What is a state machine?

A state machine is a system that keeps track of the current state.

The state can include things like: 

- What buffer is currently bound (active)?.
- Which shader program is in use?
- Which textures are bound (Doesn't appear in this project)?
- The settings for rendering.
- The size of the view port.
- And a ton of other things.

Think of it as a way to set up OpenGL's settings before doing some action.
This makes OpenGL's api drastically simpler compared to modern graphics API's at the cost
of flexibility in terms of performance and multithreading (All operations must be done on a single thread) 
but makes it more flexible for memory since it was originally made for computers in the 90s so memory was limited.
Though it's design may be confusing for people that have never seen something like it since were modifying some
invisible state.

The typical way an operation is done on an object is:

- Generate the object on the GPU using some sort of generation function (e.g. glGenBuffers) 
- Bind the object so OpenGL knows to use it.
- Upload data or configure the object.
- Use it for rendering
- Finally, optionally unbind it. Binding another object of the same type will cause all operations to only be on that type and unbinds the previous object automatically.

### What is a shader?

 - **A shader is just a specialized program that runs on the GPU at its simplest form.**
 - They are written in shading languages. For OpenGL that language would be GLSL.
 - These shaders then can be combined into a shader program and used in drawing or dispatching
commands. **The command that is used determines the order in which the shaders execute.**
 - These programs are a part of a larger pipeline. This project only uses the render pipeline.

### What is the render pipeline?

- The render pipeline is not something you create but is one of the states that OpenGL manages.
- When a function is called but there is no object that is being modified/configured by it, and it's not a command for the GPU. Then it
is most likely modifying the global render pipelines state (Some outliers that don't follow this).
- The shader program that is provided to the render pipeline is used to fill in the programmable stages in the pipeline
the two that are required are the vertex and fragment stages (shaders). There are also fixed stages that we can modify
using functions that OpenGL provide, but we can't create our own shader for it. There are is also the geometry and tesselation shaders but these can be
overridden but are usually not since they aren't very useful and cause performance to drop significantly.

- For more info on the stages of the pipeline here is an article https://wikis.khronos.org/opengl/Rendering_Pipeline_Overview.