package chat.shared;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileStorage<T> {

    private String fileName;
    private T t;

    public FileStorage(String fileName) {
        Path currentRelativePath = Paths.get("");
        String path = currentRelativePath.toAbsolutePath().toString();
        this.fileName = path + "/src/chat/shared/data/" + fileName;
    }

    public FileStorage(String fileName, T t) {
        this(fileName);
        this.t = t;
    }

    public synchronized void save() throws IOException {
        this.save(t);
    }

    public synchronized void save(T t) {
        try (
            FileOutputStream fos = new FileOutputStream(fileName, false);
            ObjectOutputStream out = new ObjectOutputStream(fos)
        ){
            out.writeObject(t);
        } catch (IOException ex) {}
    }

    public synchronized T fetch() {
        try (
            FileInputStream fis = new FileInputStream(fileName);
            ObjectInputStream in  = new ObjectInputStream(fis)
        ){
            t = (T) in.readObject();
            return t;
        } catch (IOException | ClassNotFoundException ex) {
            return null;
        }
    }
}
