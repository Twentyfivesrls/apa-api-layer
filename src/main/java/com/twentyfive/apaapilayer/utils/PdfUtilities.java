package com.twentyfive.apaapilayer.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.twentyfive.apaapilayer.dtos.BundleInPurchaseDTO;
import com.twentyfive.apaapilayer.dtos.OrderDetailsPrintAPADTO;
import com.twentyfive.apaapilayer.dtos.PieceInPurchaseDTO;
import com.twentyfive.apaapilayer.dtos.ProductInPurchaseDTO;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.Customization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class PdfUtilities {
    public static ByteArrayOutputStream generatePdfStream(OrderDetailsPrintAPADTO orderDetails) throws DocumentException, IOException {
        Document document = new Document();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        // Add image at the top left
        try {
            Image logo = Image.getInstance("src/main/resources/static/apa-logo.png"); // Path to your image
            logo.scaleToFit(100, 100); // Resize the image to fit the top left corner
            logo.setAbsolutePosition(36, 750); // Position the image
            document.add(logo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Write order details
        Font smallBoldFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        Font smallNormalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
        Font normalBoldFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
        Font largeBoldFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);

        // Customer info aligned to the right and smaller
        addRightAlignedParagraph(document, "ID Ordine: ", orderDetails.getId(), smallBoldFont, smallNormalFont);
        addRightAlignedParagraph(document, "Cliente: ", orderDetails.getFullName(), smallBoldFont, smallNormalFont);
        addRightAlignedParagraph(document, "Email: ", orderDetails.getEmail(), smallBoldFont, smallNormalFont);
        addRightAlignedParagraph(document, "Telefono: ", orderDetails.getPhoneNumber(), smallBoldFont, smallNormalFont);
        addRightAlignedParagraph(document, "Data Ritiro: ", orderDetails.getFormattedPickupDate(), smallBoldFont, smallNormalFont);
        addRightAlignedParagraph(document, "Note: ", orderDetails.getNote(), smallBoldFont, smallNormalFont);
        document.add(new Paragraph("\n"));
        document.add(new LineSeparator());
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("Ordine:", largeBoldFont));
        document.add(new Paragraph("\n"));
        // Write product details if available
        if (orderDetails.getProducts() != null && !orderDetails.getProducts().isEmpty()) {

            for (ProductInPurchaseDTO product : orderDetails.getProducts()) {
                document.add(new Paragraph(product.getName().toUpperCase(), normalBoldFont));
                document.add(new Paragraph("\n"));
                document.add(createParagraph("Quantità: ", String.valueOf(product.getQuantity()), boldFont, normalFont));
                if (product.getCustomization() == null || product.getCustomization().isEmpty()) {
                    document.add(createParagraph("Peso: ", product.getWeight() + " kg", boldFont, normalFont));
                    if(product.getShape()!=null){
                        document.add(createParagraph("Forma: ", product.getShape(), boldFont, normalFont));
                    }
                } else {
                    for(Customization customization : product.getCustomization()){
                        document.add(createParagraph(customization.getName() + ": ", Arrays.stream(customization.getValue())
                                .sequential()
                                .collect(Collectors.joining(", ")), boldFont, normalFont));
                        if(customization.getName().equals("Base")) {
                            document.add(createParagraph("Peso: ", product.getWeight() + " kg", boldFont, normalFont));
                            document.add(createParagraph("Forma: ", product.getShape(), boldFont, normalFont));
                        }
                    }
                }
                if(product.getNotes()!=null){
                    document.add(createParagraph("Note aggiuntive: ", product.getNotes(), boldFont, normalFont));
                }
                if (product.getAttachment() != null && !product.getAttachment().isEmpty()) {
                    try {
                        Image image = Image.getInstance(new URL(product.getAttachment()));
                        image.scaleToFit(100, 100); // Fixed small dimensions
                        document.add(image);
                    } catch (MalformedURLException e) {
                        // Handle URL exception
                        document.add(createParagraph("Url di riferimento non valido: ", product.getAttachment(), boldFont, normalFont));
                    } catch (IOException e) {
                        // Handle image fetching exception
                        document.add(createParagraph("Problemi a caricare l'immagine: ", product.getAttachment(), boldFont, normalFont));
                    }
                }
                document.add(new Paragraph("\n"));
                document.add(new LineSeparator());
                document.add(new Paragraph("\n"));
            }
        }

        // Write bundle details if available
        if (orderDetails.getBundles() != null && !orderDetails.getBundles().isEmpty()) {
            for (BundleInPurchaseDTO bundle : orderDetails.getBundles()) {
                document.add(new Paragraph(bundle.getName().toUpperCase(), normalBoldFont));
                document.add(new Paragraph("\n"));
                document.add(createParagraph("Quantità: ", String.valueOf(bundle.getQuantity()), boldFont, normalFont));
                document.add(createParagraph("Peso: ", bundle.getMeasure().getLabel()+": "+bundle.getMeasure().getWeight() + " kg", boldFont, normalFont));
                if (bundle.getWeightedProducts()!=null){
                    document.add(new Paragraph("Mignon: ", boldFont));
                    for (PieceInPurchaseDTO piece: bundle.getWeightedProducts()){
                        document.add(new Paragraph("x"+piece.getQuantity()+" "+piece.getName(), normalFont));
                    }
                    document.add(createParagraph("Peso Totale Mignon scelti: ", bundle.getTotalWeight()+ " kg", boldFont, normalFont));
                }
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
