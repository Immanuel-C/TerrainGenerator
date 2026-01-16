package terrain_generator.renderer;

public sealed interface FrameBufferAttachmentType {
    record DepthStencil(boolean samplable, Format format) implements FrameBufferAttachmentType {
        public enum Format {
            Depth32FStencil8, // 40 bits but the GPU actually stores it as 64 bits.
            Depth24Stencil8, // 32 bits
        }
    }
    record Colour(boolean samplable, Format format) implements FrameBufferAttachmentType {
        public enum Format {
            LinearRedGreenBlueAlpha8, // 0 - 255 colour values
        }
    }

}
