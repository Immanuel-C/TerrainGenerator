package terrain_generator.renderer;

public sealed interface FrameBufferAttachmentType {
    record DepthStencil(boolean samplable) implements FrameBufferAttachmentType {
        public enum Format {
            Depth32FStencil8, // 40 bits but the GPU actually stores it as 64 bits.
            Depth24Stencil8, // 32 bits
        }
    }
    record Colour(boolean samplable) implements FrameBufferAttachmentType {
        public enum Format {
            LinearRedGreenBlueAlpha8, // 0 - 1
            /*
            * Takes linear colour and outputted by shader and puts it through a piece wise function.
            * C_srgb = 12.92 * C_linear if C_linear <= 0.0031308
            * C_srgb = 1.055 * C_linear ^ (1/2.4) - 0.055 if C_linear > 0.0031308
            * Human eyes are more sensitive to dark colors and monitors this equation
            * makes the linear colour follow the gamma curve (https://medium.com/@Jacob_Bell/programmers-guide-to-gamma-correction-4c1d3a1724fb)
            */
            StandardRedGreenBlueAlpha8,
        }
    }

}
