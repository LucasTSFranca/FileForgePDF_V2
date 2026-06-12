package service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import resource.Converter;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

/**
 * Converte arquivos HTML para PDF.
 * Extrai o texto do HTML removendo as tags.
 *
 * @author lucas
 */
public class HtmlToPdfConverter implements Converter {

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

            String htmlContent =
                    Files.readString(file.toPath());

            // AQUI VAI REMOVER AS TAGS E DEIXAR SÓ O TEXTO
            String textContent = htmlContent
                    .replaceAll("<br\\s*/?>", "\n")
                    .replaceAll("</p>", "\n\n")
                    .replaceAll("</div>", "\n")
                    .replaceAll("</li>", "\n")
                    .replaceAll("<[^>]+>", "")
                    .replaceAll("&nbsp;", " ")
                    .replaceAll("&amp;", "&")
                    .replaceAll("&lt;", "<")
                    .replaceAll("&gt;", ">")
                    .replaceAll("&quot;", "\"")
                    .trim();

            Document pdf = new Document();
            PdfWriter.getInstance(
                    pdf,
                    new FileOutputStream(outputPath)
            );

            pdf.open();

            // TRANSFORMA CADA LINHA EM UM PARAGRAFO
            String[] lines = textContent.split("\n");
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    pdf.add(new Paragraph(" "));
                } else {
                    pdf.add(new Paragraph(trimmed));
                }
            }

            pdf.close();

            return outputPath;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao converter HTML para PDF", e
            );
        }
    }
}
