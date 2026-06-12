package service;

import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;
import resource.Converter;

import java.io.File;
import java.io.FileOutputStream;


public class ImageToPdfConverter implements Converter {

    @Override
    public String convert(File file,
                          String outputName,
                          String outputDir) {

        try {
            File directory = new File(outputDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String outputPath =
                    outputDir + File.separator +
                    outputName + ".pdf";

            Image image = Image.getInstance(
                    file.getAbsolutePath()
            );

            Document pdf = new Document();
            PdfWriter.getInstance(
                    pdf,
                    new FileOutputStream(outputPath)
            );

            pdf.open();
            // DETERMINA A ESTRUTURA DA IMAGEM(TAMANHO E LOCAL ONDE ELA VAI FICAR)
            float pageWidth = PageSize.A4.getWidth() - 72;  
            float pageHeight = PageSize.A4.getHeight() - 72;

            image.scaleToFit(pageWidth, pageHeight);
            image.setAlignment(Image.ALIGN_CENTER);

            pdf.add(image);
            pdf.close();

            return outputPath;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao converter imagem para PDF", e
            );
        }
    }
}
