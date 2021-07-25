package mainPackage;

import model.WordInfo;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import util.*;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static util.CastUtil.castMagic;
import static util.FunctionalUtil.with;

public class Main4 {
    private static final String OUTPUT_FILE_PATH = "output/lessons.json";
    private static final String LINE_BREAK = "\\n";
    private static final String EMPTY_MARKER = "<empty>";
    private static final int PDF_FILE_START = 1;
    private static final int PDF_FILE_END = 50;
    private static final String STR_REGION = "region";
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
        var lessonsJsonStr = copyLessonsToClipboardAsJson(lessons);
        saveLessonsToDisk(lessonsJsonStr);
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
    public static String copyLessonsToClipboardAsJson(
            Map<Integer, List<WordInfo>> lessons
    ) {
        String json = JSONUtil.toJsonString(lessons);
        ClipboardUtil.copyToClipboard(json);
        System.out.println("Copied lessons (as JSON) to clipboard !");
        return json;
    }
    private static void saveLessonsToDisk(String lessonsJsonStr) throws IOException {
        File file = new File(OUTPUT_FILE_PATH);
        var lines = Collections.singletonList(lessonsJsonStr);
        Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
        System.out.println("Written lessons to file "+OUTPUT_FILE_PATH);
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
            var filePathStr = "pdf/bai "+fileIndex+".pdf";
            System.out.println("Processing file "+filePathStr);
            PDDocument document = PDDocument.load(new File(filePathStr));
            int numOfPages = document.getNumberOfPages();
            for (int pageIndex=0; pageIndex<numOfPages; pageIndex++) {
                PDPage onePage = document.getPage(pageIndex);
                if (pageIndex % 2 == 0/*Zero-based page, so odd page is hira, even page is kanji.*/) {
                    for (int rowIndex=0; rowIndex<7; rowIndex++) {
                        var region = new Rectangle2D.Double(
                                BASE_X, BASE_Y + rowIndex*CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);
                        var text = extractTextByRegion(region, stripper, onePage);
                        addKanji(text, kanjiList);
                    }
                    for (int rowIndex=0; rowIndex<7; rowIndex++) {
                        var region = new Rectangle2D.Double(
                                BASE_X + CELL_WIDTH, BASE_Y + rowIndex*CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);
                        var text = extractTextByRegion(region, stripper, onePage);
                        addKanji(text, kanjiList);
                    }
                } else {
                    for (int rowIndex=0; rowIndex<7; rowIndex++) {
                        var region = new Rectangle2D.Double(
                                BASE_X + CELL_WIDTH, BASE_Y + rowIndex*CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);
                        var text = extractTextByRegion(region, stripper, onePage);
                        addHNM(text, hiraganaList, nomList, meaningList);
                    }
                    for (int rowIndex=0; rowIndex<7; rowIndex++) {
                        var region = new Rectangle2D.Double(
                                BASE_X, BASE_Y + rowIndex*CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);
                        var text = extractTextByRegion(region, stripper, onePage);
                        addHNM(text, hiraganaList, nomList, meaningList);
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
    private static void addKanji(String text, List<String> kanjiList) {
        kanjiList.add(extractTextKanji(text));
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
    private static String extractTextByRegion(
            Rectangle2D region,
            PDFTextStripperByArea stripper,
            PDPage onePage
    ) throws IOException {
        stripper.addRegion(STR_REGION, region);
        stripper.extractRegions(onePage);
        String text = stripper.getTextForRegion(STR_REGION);
        stripper.removeRegion(STR_REGION);
        return text;
    }
}