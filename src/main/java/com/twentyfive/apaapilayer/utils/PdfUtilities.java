package com.twentyfive.apaapilayer.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.twentyfive.apaapilayer.dtos.BundleInPurchaseDTO;
import com.twentyfive.apaapilayer.dtos.OrderDetailsPrintAPADTO;
import com.twentyfive.apaapilayer.dtos.PieceInPurchaseDTO;
import com.twentyfive.apaapilayer.dtos.ProductInPurchaseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.Customization;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.IngredientsWithCategory;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PdfUtilities {
    private static Environment environment;

    @Autowired
    public PdfUtilities(Environment env) {
        PdfUtilities.environment = env;
    }



    public static ByteArrayOutputStream generatePdfStream(OrderDetailsPrintAPADTO orderDetails) throws DocumentException, IOException {
    Document document = new Document(PageSize.A5);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PdfWriter.getInstance(document, outputStream);
    document.open();

    // === Logo ===
    try (InputStream logoStream = PdfUtilities.class.getResourceAsStream("/static/apa-full-gold.png")) {
        if (logoStream == null) {
            throw new FileNotFoundException("Logo non trovato nel classpath");
        }
        Image logo = Image.getInstance(logoStream.readAllBytes());
        logo.scaleToFit(80, 80); 
        logo.setAbsolutePosition(36, 500); 
        document.add(logo);
    } catch (Exception e) {
        e.printStackTrace();
    }

    // === Font Emoji ===
    Font normalFont;
    try (InputStream fontStream = PdfUtilities.class.getResourceAsStream("/fonts/NotoSansSymbols2-Regular.ttf")) {
        if (fontStream == null) {
            throw new FileNotFoundException("Font non trovata nel classpath");
        }
        BaseFont bfEmoji = BaseFont.createFont(
            "NotoSansSymbols2-Regular.ttf",
            BaseFont.IDENTITY_H,
            BaseFont.EMBEDDED,
            false,
            fontStream.readAllBytes(),
            null
        );
        normalFont = new Font(bfEmoji, 15, Font.NORMAL);
    }

    // === Altri font standard ===
    Font smallBoldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    Font smallNormalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
    Font boldFont = new Font(Font.FontFamily.HELVETICA, 15, Font.BOLD);
    Font numberFont = new Font(Font.FontFamily.HELVETICA, 15, Font.NORMAL);
    Font normalBoldFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    Font largeBoldFont = new Font(Font.FontFamily.HELVETICA, 19, Font.BOLD);

    // === Dati Cliente ===
    addRightAlignedParagraph(document, "ID Ordine: ", orderDetails.getId(), smallBoldFont, smallNormalFont);
    addRightAlignedParagraph(document, "Cliente: ", orderDetails.getFullName(), smallBoldFont, smallNormalFont);
    addRightAlignedParagraph(document, "Email: ", orderDetails.getEmail(), smallBoldFont, smallNormalFont);
    addRightAlignedParagraph(document, "Telefono: ", orderDetails.getPhoneNumber(), smallBoldFont, smallNormalFont);
    addRightAlignedParagraph(document, "Data Ritiro: ", orderDetails.getFormattedPickupDate(), smallBoldFont, smallNormalFont);
    addRightAlignedParagraph(document, "Note: ", orderDetails.getNote(), smallBoldFont, smallNormalFont);
    document.add(new Paragraph("\n"));
    document.add(new LineSeparator());
    document.add(new Paragraph("\n"));

    // === Ordini ===
    document.add(new Paragraph("Ordine:", largeBoldFont));
    document.add(new Paragraph("\n"));

    if (orderDetails.getProducts() != null && !orderDetails.getProducts().isEmpty()) {
        for (ProductInPurchaseDTO product : orderDetails.getProducts()) {
            document.add(new Paragraph(product.getName().toUpperCase(), normalBoldFont));
            document.add(new Paragraph("\n"));
            document.add(createParagraph("Quantità: ", product.getQuantity() + " pz", boldFont, numberFont));

            if (product.getIngredients() != null) {
                for (IngredientsWithCategory ingredients : product.getIngredients()) {
                    document.add(createParagraph(
                        ingredients.getCategoryName() + ": ",
                        ingredients.getIngredientsName().stream().collect(Collectors.joining(", ")),
                        boldFont, normalFont
                    ));
                }
            }

            if (product.getCustomization() == null || product.getCustomization().isEmpty()) {
                document.add(createParagraph("Peso: ", product.getWeight() + " kg", boldFont, numberFont));
                if (product.getShape() != null) {
                    document.add(createParagraph("Forma: ", product.getShape(), boldFont, normalFont));
                }
            } else {
                boolean addingWeightAndShape = true;
                for (Customization customization : product.getCustomization()) {
                    document.add(createParagraph(
                        customization.getName() + ": ",
                        Arrays.stream(customization.getValue()).collect(Collectors.joining(", ")),
                        boldFont, normalFont
                    ));
                    if (addingWeightAndShape) {
                        addingWeightAndShape = false;
                        document.add(createParagraph("Peso: ", product.getWeight() + " kg", boldFont, numberFont));
                        if (product.getShape() != null) {
                            document.add(createParagraph("Forma: ", product.getShape(), boldFont, normalFont));
                        }
                    }
                }
            }

            if (product.getAttachment() != null && !product.getAttachment().isEmpty()) {
                String composedUrl = environment.getProperty("layer.url") + "/download" + product.getAttachment();
                try {
                    Image image = Image.getInstance(new URL(composedUrl));
                    image.scaleToFit(80, 80);
                    document.add(image);
                } catch (MalformedURLException e) {
                    document.add(createParagraph("Url di riferimento non valido: ", composedUrl, boldFont, normalFont));
                } catch (IOException e) {
                    document.add(createParagraph("Problemi a caricare l'immagine: ", composedUrl, boldFont, normalFont));
                }
            }

            document.add(new Paragraph("\n"));
            document.add(new LineSeparator());
            document.add(new Paragraph("\n"));
        }
    }

    // === Bundle ===
    if (orderDetails.getBundles() != null && !orderDetails.getBundles().isEmpty()) {
        for (BundleInPurchaseDTO bundle : orderDetails.getBundles()) {
            document.add(new Paragraph(bundle.getName().toUpperCase(), normalBoldFont));
            document.add(new Paragraph("\n"));
            document.add(createParagraph("Peso: ", bundle.getMeasure().getLabel() + ": " + bundle.getMeasure().getWeight() + " kg", boldFont, numberFont));

            if (bundle.getWeightedProducts() != null) {
                document.add(new Paragraph("Mignon: ", boldFont));
                for (PieceInPurchaseDTO piece : bundle.getWeightedProducts()) {
                    document.add(new Paragraph("x" + piece.getQuantity() + " " + piece.getName(), numberFont));
                }
                document.add(createParagraph("Peso Totale Mignon scelti: ", bundle.getTotalWeight() + " kg", boldFont, numberFont));
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
