package com.twentyfive.apaapilayer.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.twentyfive.apaapilayer.DTOs.BundleInPurchaseDTO;
import com.twentyfive.apaapilayer.DTOs.OrderDetailsPrintAPADTO;
import com.twentyfive.apaapilayer.DTOs.ProductInPurchaseDTO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class PdfUtils {
    public static ByteArrayOutputStream generatePdfStream(OrderDetailsPrintAPADTO orderDetails) throws DocumentException, IOException {
        Document document = new Document();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        // Write order details
        Font smallBoldFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        Font smallNormalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
        Font largeBoldFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);

        // Customer info aligned to the right and smaller
        addRightAlignedParagraph(document, "ID Ordine: ", orderDetails.getId(), smallBoldFont, smallNormalFont);
        addRightAlignedParagraph(document, "Cliente: ", orderDetails.getFullName(), smallBoldFont, smallNormalFont);
        addRightAlignedParagraph(document, "Email: ", orderDetails.getEmail(), smallBoldFont, smallNormalFont);
        addRightAlignedParagraph(document, "Telefono: ", orderDetails.getPhoneNumber(), smallBoldFont, smallNormalFont);
        addRightAlignedParagraph(document, "Data Ritiro: ", orderDetails.getFormattedPickupDate(), smallBoldFont, smallNormalFont);
        document.add(new Paragraph("\n"));
        document.add(new LineSeparator());
        document.add(new Paragraph("\n"));

        // Write product details if available
        if (orderDetails.getProducts() != null && !orderDetails.getProducts().isEmpty()) {
            document.add(new Paragraph("Ordine:", largeBoldFont));
            document.add(new Paragraph("\n"));
            for (ProductInPurchaseDTO product : orderDetails.getProducts()) {
                document.add(createParagraph("Nome: ", product.getName(), boldFont, normalFont));
                document.add(createParagraph("Quantità: ", String.valueOf(product.getQuantity()), boldFont, normalFont));
                document.add(createParagraph("Forma: ", product.getShape(), boldFont, normalFont));
                document.add(createParagraph("Peso: ", String.valueOf(product.getWeight()), boldFont, normalFont));

                if (product.getCustomization() != null && !product.getCustomization().isEmpty()) {
                    document.add(new Paragraph("Personalizzazioni:", boldFont));
                    for (Map.Entry<String, String> entry : product.getCustomization().entrySet()) {
                        document.add(createParagraph(entry.getKey() + ": ", entry.getValue(), boldFont, normalFont));
                    }
                }
                document.add(createParagraph("Gocce di cioccolato: ", String.valueOf(product.isChocolateChips()), boldFont, normalFont));
                document.add(createParagraph("Note aggiuntive: ", product.getText(), boldFont, normalFont));

                if (product.getAttachment() != null && !product.getAttachment().isEmpty()) {
                    try {
                        Image image = Image.getInstance(new URL(product.getAttachment()));
                        image.scaleToFit(100, 100); // Fixed small dimensions
                        document.add(image);
                    } catch (MalformedURLException e) {
                        // Handle URL exception
                        document.add(createParagraph("Invalid attachment URL: ", product.getAttachment(), boldFont, normalFont));
                    } catch (IOException e) {
                        // Handle image fetching exception
                        document.add(createParagraph("Error fetching attachment image: ", product.getAttachment(), boldFont, normalFont));
                    }
                }
                document.add(new Paragraph("\n"));
                document.add(new LineSeparator());
                document.add(new Paragraph("\n"));
            }
        }

        // Write bundle details if available
        if (orderDetails.getBundles() != null && !orderDetails.getBundles().isEmpty()) {
            document.add(new Paragraph("Bundles:", boldFont));
            for (BundleInPurchaseDTO bundle : orderDetails.getBundles()) {
                document.add(createParagraph("Nome: ", bundle.getName(), boldFont, normalFont));
                document.add(createParagraph("Quantità: ", String.valueOf(bundle.getQuantity()), boldFont, normalFont));
                document.add(createParagraph("Peso: ", String.valueOf(bundle.getMeasure()), boldFont, normalFont));
                document.add(new Paragraph("\n"));
                document.add(new LineSeparator());
                document.add(new Paragraph("\n"));
            }
        }

        document.close();
        return outputStream;
    }

    private static void addRightAlignedParagraph(Document document, String boldText, String normalText, Font boldFont, Font normalFont) throws DocumentException {
        if (normalText == null) {
            normalText = "";
        }
        Chunk boldChunk = new Chunk(boldText, boldFont);
        Chunk normalChunk = new Chunk(normalText, normalFont);
        Paragraph paragraph = new Paragraph();
        paragraph.add(boldChunk);
        paragraph.add(normalChunk);
        paragraph.setAlignment(Element.ALIGN_RIGHT);
        document.add(paragraph);
    }

    private static Paragraph createParagraph(String boldText, String normalText, Font boldFont, Font normalFont) {
        if (normalText == null) {
            normalText = "";
        }
        Chunk boldChunk = new Chunk(boldText, boldFont);
        Chunk normalChunk = new Chunk(normalText, normalFont);
        Paragraph paragraph = new Paragraph();
        paragraph.add(boldChunk);
        paragraph.add(normalChunk);
        return paragraph;
    }
}