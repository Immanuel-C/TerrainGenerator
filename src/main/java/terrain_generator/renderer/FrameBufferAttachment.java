package terrain_generator.renderer;

public class FrameBufferAttachment {
    int attachment;
    FrameBufferAttachmentType type;

    public FrameBufferAttachment(FrameBufferAttachmentType type, boolean samplable) {
        this.type = type;

        if (samplable)
            this.createTexture();
        else
            this.createRenderBuffer();
    }

    private void createRenderBuffer() {
    }

    private void createTexture() {

    }
}
