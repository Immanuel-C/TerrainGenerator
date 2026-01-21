package terrain_generator.renderer;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL46;
import terrain_generator.UniformNotFoundException;
import terrain_generator.UnimplementedException;
import terrain_generator.utils.Resource;
import terrain_generator.utils.ResourceType;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL45.*;

public class ShaderProgram extends Resource {
    private int shaderProgram;

    // Take in a collection of ShaderInfo's and create the program.
    public ShaderProgram(Collection<ShaderInfo> shaderInfos) {
        super(new ResourceType.ShaderProgram());
        this.create(shaderInfos);
    }

    @Override
    public void recreate(Collection<Resource> dependencies) {
        // Destroy the original shader program. This is fine to do without checking if the shader program is bound since this method will never be called async but will always be
        // called on the render thread meaning that it will never be destroyed will it is being used by the GPU's driver.
        this.destroy();
        // Create a new shader program.
        this.create(dependencies
                .stream()
                .filter(dependency -> dependency.getType().getClass() == ResourceType.ShaderInfo.class) // Filter out any dependencies that is not a ShaderInfo class.
                .map(dependency -> (ShaderInfo)dependency) // Convert the Resource into a ShaderInfo class.
                .collect(Collectors.toList()) // Convert to a list of dependencies
        );
    }

    private void create(Collection<ShaderInfo> shaderInfos) {
        // Create a program on the gpu and get ID.
        this.shaderProgram = glCreateProgram();

        // Create an array list of shaders with an initial capacity of the size of the shaderInfos collection.
        ArrayList<Integer> shaders = new ArrayList<>(shaderInfos.size());
        shaderInfos
                .stream()
                .distinct() // only get unique shader infos.
                .map(this::createShader) // Convert the shader info into a shader ID provided by OpenGL through the createShader method.
                .forEach(shader -> { // For each shader id add it to the list of shaders and attach the shader to the shader program.
                    shaders.add(shader);
                    // This creates a link between the shader program and the shader.
                    glAttachShader(this.shaderProgram, shader);
                });

        // Link the program which checks for compatability of the shaders and creates
        // and executable for the GPU and initializes uniform ID's.
        glLinkProgram(this.shaderProgram);

        // Check if the linking was successful
        int success = glGetProgrami(this.shaderProgram, GL_LINK_STATUS);

        // C does not have booleans int's are used as booleans instead. 0 == false, i != 0 is true.
        // This is useful with pointers as you could do if (!p) {...} to check if the pointer is null.
        if (success == 0) {
            // If it was not throw a new exception.
            String shaderProgramLog = glGetProgramInfoLog(this.shaderProgram);
            throw new RuntimeException("Shader program failed to compile:\n\n" + shaderProgramLog);
        }

        // Now that the source code is compiled by the GPU's driver we can delete the actual
        // shaders since the post linked data is stored inside the shader program and the compiled shaders are not needed now.
        shaders.forEach(GL46::glDeleteShader);
    }

    public void bind() {
        glUseProgram(this.shaderProgram);
    }

    public void unBind() {
        glUseProgram(0);
    }

    @Override
    public void destroy() {
        glDeleteProgram(this.shaderProgram);
    }

    /*
    * The upload* methods upload a certain data type to the OpenGL shader program which then can be referenced in the shader code.
    * The uniformName is the name of the uniform variable inside the shader and the first argument is the data to be uploaded.
    */

    public void uploadMatrix4f(Matrix4f matrix, String uniformName)  throws UniformNotFoundException {
        // This is garbage collected so no need for to free.
        // A Matrix4f has 4 rows and 4 columns of floats.
        FloatBuffer uniformBuffer = BufferUtils.createFloatBuffer(4*4);
        // JOML, the linear algebra library has a method to convert matrices into a float buffer.
        uniformBuffer = matrix.get(uniformBuffer);

        // Transpose the matrix from row major to column major. Since JOML is already in column major this is not needed.
        // Provide the ID of the uniform to OpenGL.
        glUniformMatrix4fv(this.getUniformLocation(uniformName), false, uniformBuffer);
    }

    public void uploadMatrix3f(Matrix3f matrix, String uniformName)  throws UniformNotFoundException {
        FloatBuffer uniformBuffer = BufferUtils.createFloatBuffer(3*3);
        uniformBuffer = matrix.get(uniformBuffer);

        glUniformMatrix3fv(this.getUniformLocation(uniformName), false, uniformBuffer);
    }

    public void uploadFloat(float val, String uniformName)  throws UniformNotFoundException {
        glUniform1f(this.getUniformLocation(uniformName), val);
    }

    public void uploadVec3(Vector3f val, String uniformName) throws UniformNotFoundException {
        glUniform3f(this.getUniformLocation(uniformName), val.x, val.y, val.z);
    }

    private int getUniformLocation(String uniformName) throws UniformNotFoundException {
        // Ask OpenGL for the uniform location inside a certain shader program.
        int location = glGetUniformLocation(this.shaderProgram, uniformName);

        // if the shader program is -1 that means OpenGL could not find a uniform with the name of uniformName.
        if (location == -1)
            throw new UniformNotFoundException("Uniform name provided " + uniformName + " is not found in program " + this.shaderProgram + "\n");

        return location;
    }


    private int createShader(ShaderInfo shaderInfo) {
        int glType = ShaderProgram.getGLShaderType(shaderInfo);

        // Create a new shader.
        int shader = glCreateShader(glType);

        // Check if the provided shader info has source code if it doesn't throw an exception
        String source = shaderInfo
                .getSource()
                .orElseThrow(() -> new IllegalStateException("A shader source must be present when creating a shader"));

        // Provide the source code to OpenGL.
        glShaderSource(shader, source);
        // Compile the source code on the GPU.
        glCompileShader(shader);

        // Check if the shader compiled without errors.
        int success = glGetShaderi(shader, GL_COMPILE_STATUS);

        // If it did not get the error and throw an exception with the log provided.
        if (success == 0) {
            String shaderLog = glGetShaderInfoLog(shader);
            throw new RuntimeException(shaderInfo.getPath() + " failed to compile:\n\n" + shaderLog);
        }

        return shader;
    }

    private static int getGLShaderType(ShaderInfo shaderInfo) {
        int glType;
        // Check if the class of the shaderInfo.type is an Instance of ResourceType.Shader that is a child of ResourceType.
        // Then java provides a way to extract the records fields without creating a Resource.Shader variable.
        if (shaderInfo.getType() instanceof ResourceType.ShaderInfo(ResourceType.ShaderInfo.Type shaderType))
            // Convert my shader types to shader types OpenGL understands.
             glType = switch (shaderType) {
                case Compute -> GL_COMPUTE_SHADER;
                case Vertex -> GL_VERTEX_SHADER;
                case Fragment -> GL_FRAGMENT_SHADER;
                /*
                  Always handle cases default cases even if they cannot happen now. This ensures if other shader types are
                  added that I will always implement them.
                */
                default -> throw new UnimplementedException("Unimplemented shader type: " + shaderType);
            };
        else
            throw new RuntimeException("Invalid Shader Type: " + shaderInfo.getType());

        return glType;
    }

}
