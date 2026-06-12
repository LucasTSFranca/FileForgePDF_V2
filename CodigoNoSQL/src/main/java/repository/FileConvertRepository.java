package repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.FileConvertModel;

public class FileConvertRepository {
    
    private static final List<FileConvertModel> lista = new ArrayList<>();
    private static long nextId = 1;
    
    public synchronized void save(FileConvertModel file) {
        file.setId(nextId++);
        if (file.getDt_criacao() == null) {
            file.setDt_criacao(LocalDate.now());
        }
        lista.add(file);
    }
    
    public synchronized ArrayList<FileConvertModel> showAll() {
        return new ArrayList<>(lista);
    }
    
    public synchronized void delete(Long id) {
        lista.removeIf(file -> file.getId().equals(id));
    }
}
