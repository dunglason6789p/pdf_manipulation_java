package mainPackage;

import model.WordInfo;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import util.ClipboardUtil;
import util.JSONUtil;
import util.ListUtil;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static util.FunctionalUtil.with;

public class Main3 {
    private static final String LINE_BREAK = "\\n";
    private static final String EMPTY_MARKER = "<empty>";
    private static final int PDF_FILE_START = 1;
    private static final int PDF_FILE_END = 50;
    private static final String STR_REGION_A = "regionA";
    private static final String STR_REGION_B = "regionB";
    private static final double BASE_X = toPT(0.56);
    private static final double BASE_Y = toPT(0.34);
    private static final double CELL_WIDTH = toPT(3.55);
    private static final double CELL_HEIGHT = toPT(1.52);

    public static void main(String[] args) throws IOException {
        Map<Integer, List<List<String>>> rawLessonsData = readPDFLessons(PDF_FILE_START, PDF_FILE_END);
        Map<Integer, List<WordInfo>> lessons = processRawLessonsData(rawLessonsData);
        printLessonsAsTable(lessons);
        copyLessonsToClipboardAsJson(lessons);
    }

    public static void copyLessonsToClipboardAsJson(
            Map<Integer, List<WordInfo>> lessons
    ) {
        String json = JSONUtil.toJsonString(lessons);
        ClipboardUtil.copyToClipboard(json);
        System.out.println("Copied lessons (as JSON) to clipboard !");
    }

    public static void printLessonsAsTable(
            Map<Integer, List<WordInfo>> lessons
    ) {
        List<Integer> sortedFileIndex = with(new ArrayList<>(lessons.keySet()), it->it.sort(Comparator.naturalOrder()));
        for (int fileIndex: sortedFileIndex) {
            System.out.println("Lesson "+fileIndex);
            List<WordInfo> wordInfoList = lessons.get(fileIndex);
            for (WordInfo wordInfo: wordInfoList) {
                if (!EMPTY_MARKER.equals(wordInfo.kanji)) {
                    System.out.printf("%s\t%s\t%s\t%s%n",
                            wordInfo.kanji,
                            wordInfo.hiragana,
                            wordInfo.nom,
                            wordInfo.meaning
                    );
                }
            }
        }
    }

    public static Map<Integer, List<WordInfo>> processRawLessonsData(Map<Integer, List<List<String>>> rawLessonsData) {
        Map<Integer, List<WordInfo>> returnMap = new HashMap<>();
        List<Integer> sortedFileIndex = with(new ArrayList<>(rawLessonsData.keySet()), it->it.sort(Comparator.naturalOrder()));
        for (int lessonIndex: sortedFileIndex) {
            List<List<String>> lessonData = rawLessonsData.get(lessonIndex);
            if (lessonData.size() != 4) {
                throw new IllegalStateException("Malformed lessonData! fileIndex="+lessonIndex);
            }
            List<String> kanjiList = lessonData.get(0);
            List<String> hiraganaList = lessonData.get(1);
            List<String> nomList = lessonData.get(2);
            List<String> meaningList = lessonData.get(3);
            if (kanjiList.size() != hiraganaList.size() || hiraganaList.size() != nomList.size() || nomList.size() != meaningList.size()) {
                throw new IllegalStateException(
                        String.format("Malformed lessonData! fileIndex=%d, list sizes: %d-%d-%d-%d",
                                lessonIndex, kanjiList.size(), hiraganaList.size(), nomList.size(), meaningList.size()));
            }
            for (int i=0; i<lessonData.get(0).size(); i++) {
                String kanji = ListUtil.getOrNull(kanjiList, i);
                if (!EMPTY_MARKER.equals(kanji)) {
                    WordInfo wordInfo = new WordInfo(
                            kanji,
                            ListUtil.getOrNull(hiraganaList, i),
                            ListUtil.getOrNull(nomList, i),
                            ListUtil.getOrNull(meaningList, i)
                    );
                    returnMap.computeIfAbsent(lessonIndex, key->new ArrayList<>()).add(wordInfo);
                }
            }
        }
        return returnMap;
    }

    /**Return rawLessonsData.*/
    public static Map<Integer, List<List<String>>> readPDFLessons(
            final int pdfFileStart, final int pdfFileEnd
    ) throws IOException {
        Map<Integer, List<List<String>>> lessons = new HashMap<>();
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);
        for (int fileIndex=pdfFileStart; fileIndex<=pdfFileEnd; fileIndex++) {
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
                    if (pageIndex % 2 == 0) {
                        kanjiList.add(extractTextKanji(textA));
                        kanjiList.add(extractTextKanji(textB));
                    } else {
                        addHNM(textB, hiraganaList, nomList, meaningList);
                        addHNM(textA, hiraganaList, nomList, meaningList);
                    }
                }
            }
            document.close();
            List<List<String>> listList= new ArrayList<>();
            listList.add(kanjiList);
            listList.add(hiraganaList);
            listList.add(nomList);
            listList.add(meaningList);
            lessons.put(fileIndex, listList);
        }
        return lessons;
    }
    private static double toPT(double inch) {
        return inch*72;
    }
    private static String extractTextKanji(String text) {
        return Arrays.stream(text.split(LINE_BREAK))
                .map(String::trim)
                .filter(it->!it.isEmpty())
                .findFirst().orElse(EMPTY_MARKER);
    }
    private static List<String> extractTextHiraNomMeaning(String text) {
        return Arrays.stream(text.split(LINE_BREAK))
                .map(String::trim)
                .filter(it->!it.isEmpty())
                .map(it->it.replace("Ƣ","Ư").replace("ƣ","ư"))
                .collect(Collectors.toList());
    }
    private static void addHNM(
            String text,
            List<String> hiraganaList,
            List<String> nomList,
            List<String> meaningList
    ) {
        List<String> extractsFromB = extractTextHiraNomMeaning(text);
        String hiragana = ListUtil.getOrNull(extractsFromB, 0);
        String nom = ListUtil.getOrNull(extractsFromB, 1);
        String meaning = ListUtil.getOrNull(extractsFromB, 2);
        hiraganaList.add(hiragana);
        nomList.add(nom);
        meaningList.add(meaning);
    }
}