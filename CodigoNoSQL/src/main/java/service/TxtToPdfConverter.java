/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import resource.Converter;

public class TxtToPdfConverter implements Converter {

    @Override
    public String convert(File file,
                          String outputName,
                          String outputDir) {

        try {

            String content =
                    Files.readString(file.toPath());

            java.io.File directory =
                    new java.io.File(outputDir);

            if (!directory.exists()) {
                directory.mkdirs();
            }

            String outputPath =
                    outputDir +
                    File.separator +
                    outputName +
                    ".pdf";

            Document pdf = new Document();

            PdfWriter.getInstance(
                    pdf,
                    new FileOutputStream(outputPath)
            );

            pdf.open();

            pdf.add(new Paragraph(content));

            pdf.close();

            return outputPath;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Erro ao converter TXT para PDF",
                    e
            );
        }
    }
}
