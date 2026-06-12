package service;

import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;
import resource.Converter;


import java.io.File;
import java.nio.file.Files;

public class DocxToPdfConverter implements Converter {

    @Override
    public String convert(File file, String outputName, String outputDir) {

        try {
            File directory = new File(outputDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File target = new File(directory, outputName + ".pdf");

            // pega layout do projeto
            IConverter converter = LocalConverter.builder().build();
            converter.convert(file).as(DocumentType.DOCX)
                     .to(target).as(DocumentType.PDF)
                     .execute();
            converter.shutDown();

            return target.getAbsolutePath();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter DOCX para PDF com documents4j. Certifique-se de que o Microsoft Word está instalado.", e);
        }
    }

    public byte[] convertToBytes(File file) throws Exception {
        try {
            // Cria um arquivo temporário para receber o PDF do documents4j
            File tempPdf = File.createTempFile("documents4j_temp", ".pdf");
            
            IConverter converter = LocalConverter.builder().build();
            converter.convert(file).as(DocumentType.DOCX)
                     .to(tempPdf).as(DocumentType.PDF)
                     .execute();
            converter.shutDown();
            
            byte[] bytes = Files.readAllBytes(tempPdf.toPath());
            tempPdf.delete();
            return bytes;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter DOCX para bytes via MS Word. Certifique-se de que o Microsoft Word está instalado.", e);
        }
    }
}
