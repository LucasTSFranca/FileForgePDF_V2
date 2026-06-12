package repository;

import java.util.ArrayList;
import java.util.List;
import model.FileModel;

public class FileRepository {
    
    private static final List<FileModel> lista = new ArrayList<>();
    private static long nextId = 1;

    public synchronized void save(FileModel file) {
        file.setId(nextId++);
        lista.add(file);
    }
    
    public synchronized ArrayList<FileModel> listar() {
        return new ArrayList<>(lista);
    }
    
    public synchronized void deleteById(Long id) {
        lista.removeIf(file -> file.getId().equals(id));
    }
    
    public static synchronized void limparFile() {
        lista.clear();
    }
}
