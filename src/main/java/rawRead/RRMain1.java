package rawRead;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RRMain1 {
    public static void main(String[] args) {
        var pdfFile = new File("pdf-searchable/12.pdf");
        var allTextExtracted = extractAllTextFromPdf(pdfFile);
        System.out.println(allTextExtracted);
    }
    @SuppressWarnings("all")
    public static String extractAllTextFromPdf(File pdfFile) {
        try {
            var pdfDocument = PDDocument.load(pdfFile);
            var pdfTextStripper = new PDFTextStripper();
            //pdfTextStripper.setSortByPosition(false); //TODO.
            var textExtractedFromPdf =  pdfTextStripper.getText(pdfDocument);
            return textExtractedFromPdf;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}