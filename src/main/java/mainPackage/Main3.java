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
        Map<Integer, List<List<String>>> lessons = new HashMap<>();
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);
        for (int fileIndex=PDF_FILE_START; fileIndex<=PDF_FILE_END; fileIndex++) {
            List<String> kanjiList = new ArrayList<>();
            List<String> hiraganaList = new ArrayList<>();
            List<String> nomList = new ArrayList<>();
            List<String> meaningList = new ArrayList<>();
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
                    if (pageIndex % 2 == 0) {
                        String kanji = extractTextKanji(textA);
                        kanjiList.add(kanji);
                    } else {
                        List<String> extractsFromB = extractTextHiraNomMeaning(textB);
                        String hiragana = ListUtil.getOrNull(extractsFromB, 0);
                        String nom = ListUtil.getOrNull(extractsFromB, 1);
                        String meaning = ListUtil.getOrNull(extractsFromB, 2);
                        hiraganaList.add(hiragana);
                        nomList.add(nom);
                        meaningList.add(meaning);
                    }
                }
            }
            List<List<String>> listList= new ArrayList<>();
            listList.add(kanjiList);
            listList.add(hiraganaList);
            listList.add(nomList);
            listList.add(meaningList);
            lessons.put(fileIndex, listList);
        }
        String json = JSONUtil.toJsonString(lessons);
        System.out.println(json);
        // Retrieve.
        for (int fileIndex=PDF_FILE_START; fileIndex<=PDF_FILE_END; fileIndex++) {
            System.out.println("Lesson "+fileIndex);
            List<List<String>> lessonData = lessons.get(fileIndex);
            if (lessonData.size() != 4) {
                System.err.println("Malformed lessonData! fileIndex="+fileIndex);
                continue;
            }
            List<String> kanjiList = lessonData.get(0);
            List<String> hiraganaList = lessonData.get(1);
            List<String> nomList = lessonData.get(2);
            List<String> meaningList = lessonData.get(3);
            if (kanjiList.size() != hiraganaList.size() || hiraganaList.size() != nomList.size() || nomList.size() != meaningList.size()) {
                System.err.println("Malformed lessonData! fileIndex="+fileIndex
                        +", list sizes: "+kanjiList.size()+"-"+hiraganaList.size()+"-"+nomList.size()+"-"+meaningList.size());
            }
            for (int i=0; i<lessonData.get(0).size(); i++) {
                System.out.println(String.format("%s\t\t%s\t\t%s\t\t%s",
                        ListUtil.getOrNull(kanjiList, i),
                        ListUtil.getOrNull(hiraganaList, i),
                        ListUtil.getOrNull(nomList, i),
                        ListUtil.getOrNull(meaningList, i)
                ));
            }
        }
    }
    private static double toPT(double inch) {
        return inch*72;
    }
    private static String extractTextKanji(String textA) {
        return Arrays.stream(textA.split("/n"))
                .map(String::trim)
                .filter(it->!it.isEmpty())
                .findFirst().orElse("<empty>");
    }
    private static List<String> extractTextHiraNomMeaning(String textA) {
        return Arrays.stream(textA.split("/n"))
                .map(String::trim)
                .filter(it->!it.isEmpty())
                .collect(Collectors.toList());
    }
}