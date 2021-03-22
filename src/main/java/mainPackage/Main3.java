package mainPackage;

import mainPackage.model.MyTextData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import util.JSONUtil;
import util.ListUtil;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Main3 {
    private static final int PDF_FILE_START = 1;
    private static final int PDF_FILE_END = 50;
    private static final String STR_REGION_A = "regionA";
    private static final String STR_REGION_B = "regionB";
    private static final double BASE_X = toPT(0.56);
    private static final double BASE_Y = toPT(0.34);
    private static final double CELL_WIDTH = toPT(3.55);
    private static final double CELL_HEIGHT = toPT(1.52);
    public static void main(String[] args) throws IOException {
        Map<Integer, List<MyTextData>> lessons = new HashMap<>();
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);
        for (int fileIndex=PDF_FILE_START; fileIndex<=PDF_FILE_END; fileIndex++) {
            List<MyTextData> myTextDataList = new ArrayList<>();
            PDDocument document = PDDocument.load(new File("pdf/bai "+fileIndex+".pdf"));
            int numOfPages = document.getNumberOfPages();
            for (int pageIndex=0; pageIndex<numOfPages; pageIndex++) {
                PDPage onePage = document.getPage(pageIndex);
                for (int rowIndex=0; rowIndex<7; rowIndex++) {
                    double baseY = BASE_Y + rowIndex*CELL_HEIGHT;
                    Rectangle2D regionA = new Rectangle2D.Double(BASE_X, baseY, CELL_WIDTH, CELL_HEIGHT);
                    Rectangle2D regionB = new Rectangle2D.Double(BASE_X + CELL_WIDTH, baseY, CELL_WIDTH, CELL_HEIGHT);
                    stripper.addRegion(STR_REGION_A, regionA);
                    stripper.addRegion(STR_REGION_B, regionB);
                    stripper.extractRegions(onePage);
                    String textA = stripper.getTextForRegion(STR_REGION_A);
                    String textB = stripper.getTextForRegion(STR_REGION_B);
                    stripper.removeRegion(STR_REGION_A);
                    stripper.removeRegion(STR_REGION_B);
                    // Log.
                    System.out.println("Page"+pageIndex+"-Row"+rowIndex+"-textA:");
                    System.out.println(textA);
                    System.out.println("Page"+pageIndex+"-Row"+rowIndex+"-textB:");
                    System.out.println(textB);
                    // Extract and store text.
                    String kanji = extractTextA(textA);
                    List<String> extractsFromB = extractTextB(textB);
                    MyTextData myTextData = new MyTextData(
                            kanji,
                            ListUtil.getOrNull(extractsFromB, 0),
                            ListUtil.getOrNull(extractsFromB, 1),
                            ListUtil.getOrNull(extractsFromB, 2)
                    );
                    myTextDataList.add(myTextData);
                }
            }
            lessons.put(fileIndex, myTextDataList);
        }
        String json = JSONUtil.toJsonString(lessons);
        System.out.println(json);
    }
    private static double toPT(double inch) {
        return inch*72;
    }
    private static String extractTextA(String textA) {
        return Arrays.stream(textA.split("/n"))
                .map(String::trim)
                .filter(it->!it.isEmpty())
                .findFirst().orElse("<empty>");
    }
    private static List<String> extractTextB(String textA) {
        return Arrays.stream(textA.split("/n"))
                .map(String::trim)
                .filter(it->!it.isEmpty())
                .collect(Collectors.toList());
    }
}