package service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import resource.Converter;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;


public class CsvToPdfConverter implements Converter {

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

            BufferedReader preReader =
                    new BufferedReader(new FileReader(file));
            String firstLine = preReader.readLine();
            preReader.close();

            if (firstLine == null || firstLine.isBlank()) {
                throw new RuntimeException(
                        "Arquivo CSV vazio."
                );
            }
            // SEPARADORES
            String separator = firstLine.contains(";") ? ";" : ",";
            String[] headers = firstLine.split(separator, -1);
            int numColumns = headers.length;

            Document pdf = new Document();
            PdfWriter.getInstance(
                    pdf,
                    new FileOutputStream(outputPath)
            );

            pdf.open();

            //PADRONIZA A TABELA
            Font titleFont = new Font(
                    Font.HELVETICA, 14, Font.BOLD
            );
            pdf.add(new Paragraph(
                    file.getName(), titleFont
            ));
            pdf.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(numColumns);
            table.setWidthPercentage(100);

            Font headerFont = new Font(
                    Font.HELVETICA, 10, Font.BOLD,
                    Color.WHITE
            );
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(
                        new Paragraph(header.trim(), headerFont)
                );
                cell.setBackgroundColor(
                        new Color(60, 60, 80)
                );
                cell.setPadding(6);
                table.addCell(cell);
            }

            Font dataFont = new Font(
                    Font.HELVETICA, 9, Font.NORMAL
            );

            //CLASSE BUFFER, USADO PARA NÃO QUEBRAR A TABELA (ESTÁ FUNCIONANDO NO MOMENTO, AINDA EM TESTES)
            BufferedReader reader =
                    new BufferedReader(new FileReader(file));
            reader.readLine(); 

            String line;
            boolean alternate = false;
            //ESCREVE A TABELA
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(separator, -1);
                for (int i = 0; i < numColumns; i++) {
                    String value = i < values.length
                            ? values[i].trim()
                            : "";
                    PdfPCell cell = new PdfPCell(
                            new Paragraph(value, dataFont)
                    );
                    if (alternate) {
                        cell.setBackgroundColor(
                                new Color(240, 240, 245)
                        );
                    }
                    cell.setPadding(4);
                    table.addCell(cell);
                }
                alternate = !alternate;
            }
            reader.close();

            pdf.add(table);
            pdf.close();

            return outputPath;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao converter CSV para PDF", e
            );
        }
    }
}
