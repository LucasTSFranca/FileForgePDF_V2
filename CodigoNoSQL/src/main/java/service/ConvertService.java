
package service;

import model.FileConvertModel;
import model.FileModel;
import repository.FileConvertRepository;
import repository.FileRepository;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.List;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import javax.imageio.IIOImage;
import javax.imageio.stream.ImageOutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.Image;
import com.lowagie.text.Font;



import resource.Converter;

public class ConvertService {

    private final FileRepository fileRepository;
    private final FileConvertRepository fileConvertRepository;

    public ConvertService() {

        this.fileRepository =
                new FileRepository();

        this.fileConvertRepository =
                new FileConvertRepository();
    }

    public static class ConvertResult {
        private final String originalName;
        private final String originalPath;
        private final double originalSizeKB;
        private final String pdfName;
        private final String pdfPath;
        private final double pdfSizeKB;

        public ConvertResult(String originalName, String originalPath, double originalSizeKB,
                             String pdfName, String pdfPath, double pdfSizeKB) {
            this.originalName = originalName;
            this.originalPath = originalPath;
            this.originalSizeKB = originalSizeKB;
            this.pdfName = pdfName;
            this.pdfPath = pdfPath;
            this.pdfSizeKB = pdfSizeKB;
        }

        public String getOriginalName() { return originalName; }
        public String getOriginalPath() { return originalPath; }
        public double getOriginalSizeKB() { return originalSizeKB; }
        public String getPdfName() { return pdfName; }
        public String getPdfPath() { return pdfPath; }
        public double getPdfSizeKB() { return pdfSizeKB; }

        public double getCompressionPercent() {
            if (originalSizeKB == 0) return 0;
            return ((pdfSizeKB - originalSizeKB) / originalSizeKB) * 100.0;
        }
    }

    // TIPOS DE EXTENSÕES SUPORTADAS
    public static final String[] SUPPORTED_EXTENSIONS = {
            "txt", "jpg", "jpeg", "png", "bmp", "gif",
            "docx", "csv", "html", "htm"
    };

    //VERIFICA AS EXTENSÕES
    public static boolean isSupported(String fileName) {
        String ext = getExtension(fileName).toLowerCase();
        for (String supported : SUPPORTED_EXTENSIONS) {
            if (supported.equals(ext)) return true;
        }
        return false;
    }
    // PEGA AS EXTENSÕES DOS ARQUIVOS ADICIONADOS
    private static String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) return "";
        return fileName.substring(dot + 1).toLowerCase();
    }

    //CHAMA O METODO PARA CADA EXTENSÃO
    public Converter getConverter(String extension) {
        return switch (extension) {
            case "txt"  -> new TxtToPdfConverter();
            case "jpg", "jpeg", "png", "bmp", "gif"
                        -> new ImageToPdfConverter();
            case "docx" -> new DocxToPdfConverter();
            case "csv"  -> new CsvToPdfConverter();
            case "html", "htm"
                        -> new HtmlToPdfConverter();
            default -> throw new RuntimeException(
                    "Tipo de arquivo não suportado: ." + extension
            );
        };
    }

    //modelo para arquivos originais
    public ConvertResult convertToPdf(String originalPath,
                                String newName,
                                String outputDir) {
        return convertToPdf(originalPath, newName, outputDir, null);
    }

    //modelo para novos arquivos (pdf)
    public ConvertResult convertToPdf(String originalPath,
                                String newName,
                                String outputDir,
                                Double maxTargetSizeMB) {

        File originalFile =
                new File(originalPath);

        //VERIFICA SE EXISTE NO CAMINHO
        if (!originalFile.exists()) {
            throw new RuntimeException(
                    "Arquivo não encontrado: " + originalPath
            );
        }


        double originalSize =
                originalFile.length() / 1024.0;

        FileModel fileModel =
                new FileModel(
                        originalFile.getName(),
                        originalFile.getAbsolutePath(),
                        originalSize
                );
        //SALVA
        fileRepository.save(fileModel);

        String pdfPath = generatePdfWithCompression(
                List.of(originalFile),
                newName,
                outputDir,
                maxTargetSizeMB
        );

        File pdfFile =
                new File(pdfPath);

        double pdfSize =
                pdfFile.length() / 1024.0;

        //BASE DO FCVM
        FileConvertModel convertModel =
                new FileConvertModel(
                        pdfFile.getName(),
                        pdfFile.getAbsolutePath(),
                        pdfSize
                );

        //SALVA O NOVO MODELO COM BASE NO FCVM
        fileConvertRepository.save(convertModel);

        return new ConvertResult(
                originalFile.getName(),
                originalFile.getAbsolutePath(),
                originalSize,
                pdfFile.getName(),
                pdfFile.getAbsolutePath(),
                pdfSize
        );
    }

    //METODO PARA SALVAR NO BANCO 
    public ConvertResult mergeAndConvertToPdf(List<File> files,
                                              String newName,
                                              String outputDir,
                                              Double maxTargetSizeMB) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Lista de arquivos para mesclar está vazia.");
        }


        //SALVA TODOS OS ARQUIVOS DA LISTA
        double totalOriginalSize = 0;
        for (File file : files) {
            double size = file.length() / 1024.0;
            totalOriginalSize += size;

            FileModel fileModel = new FileModel(
                    file.getName(),
                    file.getAbsolutePath(),
                    size
            );
            fileRepository.save(fileModel);
        }

        String pdfPath = generatePdfWithCompression(files, newName, outputDir, maxTargetSizeMB);


        //SALVA AS INFORMAÇÕES DO PDF NO BANCO
        File pdfFile = new File(pdfPath);
        double pdfSize = pdfFile.length() / 1024.0;

        FileConvertModel convertModel = new FileConvertModel(
                pdfFile.getName(),
                pdfFile.getAbsolutePath(),
                pdfSize
        );
        fileConvertRepository.save(convertModel);

        return new ConvertResult(
                "Mesclado (" + files.size() + " arquivos)",
                files.get(0).getAbsolutePath(),
                totalOriginalSize,
                pdfFile.getName(),
                pdfFile.getAbsolutePath(),
                pdfSize
        );
    }

    // AJUSTA O TAMANHO DA PAG (PROTOTIPO AINDA EM TESTE)

    private Rectangle getFirstPageSizeOfFile(File file) {
        String ext = getExtension(file.getName()).toLowerCase();
        if (ext.equals("docx")) {
            try {
                DocxToPdfConverter converter = new DocxToPdfConverter();
                byte[] tempPdfBytes = converter.convertToBytes(file);
                PdfReader reader = new PdfReader(tempPdfBytes);
                Rectangle pageSize = reader.getPageSizeWithRotation(1);
                reader.close();
                return pageSize;
            } catch (Exception e) {
                return PageSize.A4;
            }
        }
        return PageSize.A4;
    }

    // COMPRIME O PDF DE ACORDO COM O TAMANHO PEDIDO
    // ARQUIVO PADRÃO TA EM KB-> A COMPRESSÃO PEDE MB
    private String generatePdfWithCompression(List<File> files, String outputName, String outputDir, Double maxTargetSizeMB) {

        // Se for um único arquivo DOCX, converte diretamente para manter 100% de fidelidade de formatação, links, etc.
        if (files.size() == 1 && getExtension(files.get(0).getName()).toLowerCase().equals("docx")) {
            DocxToPdfConverter converter = new DocxToPdfConverter();
            return converter.convert(files.get(0), outputName, outputDir);
        }

        float scale = 1.0f;
        float quality = 0.9f;
        String outputPath = null;

        int attempts = 5;
        for (int i = 0; i < attempts; i++) {
            outputPath = generatePdf(files, outputName, outputDir, scale, quality);
            if (maxTargetSizeMB == null || maxTargetSizeMB <= 0) {
                break;
            }
            File generatedFile = new File(outputPath);
            double sizeMB = generatedFile.length() / (1024.0 * 1024.0);
            if (sizeMB <= maxTargetSizeMB) {
                break; 
            }

            if (i < attempts - 1) {
                scale = Math.max(0.2f, scale * 0.7f);
                quality = Math.max(0.1f, quality * 0.6f);
                generatedFile.delete(); 
            }
        }
        return outputPath;
    }

    //METODO PARA GERAR O PDF
    private String generatePdf(List<File> files, String outputName, String outputDir, float scale, float quality) {
        try {
            File directory = new File(outputDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String outputPath = outputDir + File.separator + outputName + ".pdf";
            Document pdf = new Document(PageSize.A4, 0, 0, 0, 0);
            
            // Define o tamanho da primeira página com base no primeiro arquivo
            Rectangle initialPageSize = PageSize.A4;
            if (files != null && !files.isEmpty()) {
                initialPageSize = getFirstPageSizeOfFile(files.get(0));
            }
            pdf.setPageSize(initialPageSize);
            
            PdfWriter writer = PdfWriter.getInstance(pdf, new FileOutputStream(outputPath));
            pdf.open();

            boolean isFirstPage = true;

            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                String ext = getExtension(file.getName()).toLowerCase();
                
                if (ext.equals("docx")) {
                    appendDocx(pdf, writer, file, isFirstPage);
                    isFirstPage = false;
                } else {
                    if (!isFirstPage) {
                        pdf.setPageSize(PageSize.A4);
                        pdf.newPage();
                    }
                    isFirstPage = false;
                    
                    switch (ext) {
                        case "txt" -> appendTxt(pdf, file);
                        case "jpg", "jpeg", "png", "bmp", "gif" -> appendImage(pdf, file, scale, quality);
                        case "csv" -> appendCsv(pdf, file);
                        case "html", "htm" -> appendHtml(pdf, file);
                        default -> throw new RuntimeException("Extensão não suportada para mesclagem: ." + ext);
                    }
                }
            }

            pdf.close();
            return outputPath;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }

    //======================METODOS PARA CADA TIPO DE EXTENSÃO=================//

    //TEXTO
    private void appendTxt(Document pdf, File file) throws Exception {
        String content = Files.readString(file.toPath());
        pdf.add(new Paragraph(content));
    }

    //IMAGEM
    private void appendImage(Document pdf, File file, float scale, float quality) throws Exception {
        File fileToUse = file;
        boolean isTemp = false;
        if (scale < 1.0f || quality < 1.0f) {
            fileToUse = compressImage(file, scale, quality);
            isTemp = true;
        }
        try {
            Image image = Image.getInstance(fileToUse.getAbsolutePath());
            float pageWidth = PageSize.A4.getWidth() - 72;
            float pageHeight = PageSize.A4.getHeight() - 72;
            image.scaleToFit(pageWidth, pageHeight);
            image.setAlignment(Image.ALIGN_CENTER);
            pdf.add(image);
        } finally {
            if (isTemp && fileToUse.exists()) {
                fileToUse.delete();
            }
        }
    }

    //COMPRIMIR IMG
    private File compressImage(File originalImage, float scale, float quality) throws Exception {
        BufferedImage image = ImageIO.read(originalImage);
        if (image == null) {
            throw new RuntimeException("Não foi possível ler a imagem: " + originalImage.getName());
        }
        int newWidth = Math.max(1, Math.round(image.getWidth() * scale));
        int newHeight = Math.max(1, Math.round(image.getHeight() * scale));

        java.awt.Image scaledImage = image.getScaledInstance(newWidth, newHeight, java.awt.Image.SCALE_SMOOTH);
        BufferedImage bufferedScaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedScaled.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, newWidth, newHeight);
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        File tempFile = File.createTempFile("pdf_comp_", ".jpg");
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(tempFile)) {
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            writer.setOutput(ios);
            writer.write(null, new IIOImage(bufferedScaled, null, null), param);
            writer.dispose();
        }
        return tempFile;
    }

    //EXCEL CSV
    private void appendCsv(Document pdf, File file) throws Exception {
        try (BufferedReader preReader = new BufferedReader(new FileReader(file))) {
            String firstLine = preReader.readLine();
            if (firstLine == null || firstLine.isBlank()) {
                pdf.add(new Paragraph("CSV Vazio: " + file.getName()));
                return;
            }
            String separator = firstLine.contains(";") ? ";" : ",";
            String[] headers = firstLine.split(separator, -1);
            int numColumns = headers.length;

            PdfPTable table = new PdfPTable(numColumns);
            table.setWidthPercentage(100);

            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(header.trim(), headerFont));
                cell.setBackgroundColor(new Color(60, 60, 80));
                cell.setPadding(6);
                table.addCell(cell);
            }

            Font dataFont = new Font(Font.HELVETICA, 9, Font.NORMAL);
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                reader.readLine(); // skip header
                String line;
                boolean alternate = false;
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(separator, -1);
                    for (int i = 0; i < numColumns; i++) {
                        String value = i < values.length ? values[i].trim() : "";
                        PdfPCell cell = new PdfPCell(new Paragraph(value, dataFont));
                        if (alternate) {
                            cell.setBackgroundColor(new Color(240, 240, 245));
                        }
                        cell.setPadding(4);
                        table.addCell(cell);
                    }
                    alternate = !alternate;
                }
            }
            pdf.add(table);
        }
    }

    //WORLD DOCX — converte via opensagres e importa páginas para manter formatação original
    private void appendDocx(Document pdf, PdfWriter writer, File file, boolean isFirstPageOfDocument) throws Exception {
        // 1. Converter DOCX para PDF temporário em memória (formatação fiel)
        DocxToPdfConverter converter = new DocxToPdfConverter();
        byte[] tempPdfBytes = converter.convertToBytes(file);

        //  Importar as páginas do PDF temporário para o documento principal
        PdfReader reader = new PdfReader(tempPdfBytes);
        int numPages = reader.getNumberOfPages();

        for (int i = 1; i <= numPages; i++) {
            Rectangle pageSize = reader.getPageSizeWithRotation(i);
            int rotation = reader.getPageRotation(i);

            if (isFirstPageOfDocument && i == 1) {
                // olha se abriu corretamente
            } else {
                pdf.setPageSize(pageSize);
                pdf.newPage();
            }

            PdfImportedPage importedPage = writer.getImportedPage(reader, i);
            PdfContentByte cb = writer.getDirectContent();

            // alinha a rotacao do documento
            switch (rotation) {
                case 90 ->
                        cb.addTemplate(importedPage, 0, -1f, 1f, 0, 0, pageSize.getHeight());
                case 180 ->
                        cb.addTemplate(importedPage, -1f, 0, 0, -1f, pageSize.getWidth(), pageSize.getHeight());
                case 270 ->
                        cb.addTemplate(importedPage, 0, 1f, -1f, 0, pageSize.getWidth(), 0);
                case 0 ->
                        cb.addTemplate(importedPage, 1f, 0, 0, 1f, 0, 0);
                default ->
                        cb.addTemplate(importedPage, 1f, 0, 0, 1f, 0, 0);
            }
        }
        reader.close();
    }

    //HMTL
    private void appendHtml(Document pdf, File file) throws Exception {
        String htmlContent = Files.readString(file.toPath());
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
        String[] lines = textContent.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                pdf.add(new Paragraph(" "));
            } else {
                pdf.add(new Paragraph(trimmed));
            }
        }
    }
}
