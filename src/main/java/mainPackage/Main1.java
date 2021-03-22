package mainPackage;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

/*
x1=0.56
y1=0.34
x2=4.11
y2=1.86
=======
height=1.52
width=3.47
=======
num of cols = 2
num of rows = 7
*/
public class Main1 {
    public static void main(String[] args) throws IOException {
        PDDocument document = PDDocument.load(new File("pdf/pdf5.pdf"));
        PDPage onePage = document.getPage(1);
        Rectangle2D region = new Rectangle2D.Double(toPT(0.56), toPT(0.34), toPT(3.55), toPT(1.52));
        String regionName = "region";
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);
        stripper.addRegion(regionName, region);
        stripper.extractRegions(onePage);
        String text = stripper.getTextForRegion(regionName);
        System.out.println(text);
    }
    private static double toPT(double inch) {
        return inch*72;
    }
}
