package network;

public class FileInfo {
    private final String name;
    private final byte[] data;

    public FileInfo(String name, byte[] data) {
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }
}
