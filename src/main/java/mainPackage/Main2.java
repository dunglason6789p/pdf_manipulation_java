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
public class Main2 {
    private static final double BASE_X = toPT(0.56);
    private static final double BASE_Y = toPT(0.34);
    private static final double CELL_WIDTH = toPT(3.55);
    private static final double CELL_HEIGHT = toPT(1.52);
    public static void main(String[] args) throws IOException {
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);
        PDDocument document = PDDocument.load(new File("pdf-test/pdf5.pdf"));
        int numOfPages = document.getNumberOfPages();
        for (int pageIndex=0; pageIndex<numOfPages; pageIndex++) {
            PDPage onePage = document.getPage(pageIndex);
            for (int rowIndex=0; rowIndex<7; rowIndex++) {
                double baseY = BASE_Y + rowIndex*CELL_HEIGHT;
                Rectangle2D regionA = new Rectangle2D.Double(BASE_X, baseY, CELL_WIDTH, CELL_HEIGHT);
                Rectangle2D regionB = new Rectangle2D.Double(BASE_X + CELL_WIDTH, baseY, CELL_WIDTH, CELL_HEIGHT);
                stripper.addRegion("regionA", regionA);
                stripper.addRegion("regionB", regionB);
                stripper.extractRegions(onePage);
                String textA = stripper.getTextForRegion("regionA");
                String textB = stripper.getTextForRegion("regionB");
                stripper.removeRegion("regionA");
                stripper.removeRegion("regionB");
                // Log.
                System.out.println("Page"+pageIndex+"-Row"+rowIndex+"-textA:");
                System.out.println(textA);
                System.out.println("Page"+pageIndex+"-Row"+rowIndex+"-textB:");
                System.out.println(textB);
            }
        }
    }
    private static double toPT(double inch) {
        return inch*72;
    }
}
