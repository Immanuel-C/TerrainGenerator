public class ShaderInfo {
    private ShaderType type;
    private String path;

    public ShaderInfo(String path, ShaderType type) {
        this.type = type;
        this.path = path;
    }

    public ShaderType getType() {
        return this.type;
    }

    public String getPath() {
        return this.path;
    }
}
