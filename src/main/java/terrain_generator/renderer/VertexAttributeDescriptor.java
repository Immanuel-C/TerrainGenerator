package terrain_generator.renderer;

// A VertexAttributeDescriptor describes the attribute inside the VertexDescriptorArray.
// See the VertexDescriptorArray class for an explanation of what an attribute is.
// size represents the number of elements inside the vertex attribute
// offset represents the offset of the current attribute from the start of the vertex in bytes.
// Eg. V = {Position, Colour}, VBO = {V1, V2, ...} . Offset for colour would be size of Position in bytes.
// normalize tells OpenGL to normalize this data which take any data and store it in range of [-1, 1] or a range of [0, 1] if the data is unsigned.
public record VertexAttributeDescriptor(int size, int offset, boolean normalize) {}
