package distributed_dc_search;

import distributed_dc_search.model.DocumentData;
import distributed_dc_search.search.TFIDF;

import javax.print.Doc;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

public class Test {

    public static String BOOKS_DIR = "src/main/resources/books";

    public static String f_query = "The best detective that catches many criminals using his deductive methods";
    public static String s_query = "The girl that falls through a rabbit hole into a fantasy wonderland";

    public static String t_query = "A war between Russia and France in the cold winter";

    public static void main(String[] args) throws FileNotFoundException {
        File documentsDir = new File(BOOKS_DIR);

        List<String> documents= Arrays.stream(Objects.requireNonNull(documentsDir.list()))
                .map(documnetName -> BOOKS_DIR + "/" + documnetName)
                .collect(Collectors.toList());


        List<String> terms = TFIDF.getWordsFromLine(s_query);
        findMostRelevantDocuments(documents,terms);
    }

    private static void findMostRelevantDocuments(List<String> documents, List<String> terms) throws FileNotFoundException {
        Map<String, DocumentData> results = new HashMap<>();

        for (String document: documents) {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(document));
            List<String> lines = bufferedReader.lines().collect(Collectors.toList());
            List<String> words = new ArrayList<>();
            for (String line : lines) {
                words.addAll(TFIDF.getWordsFromLine(line));
            }
            DocumentData documentData = TFIDF.createDocumentData(words,terms);
            results.put(document,documentData);
        }



        Map<Double,List<String>> documentsByScore = TFIDF.getDocumentsScores(terms,results);
        printResults(documentsByScore);
    }

    private static void printResults(Map<Double, List<String>> documentsByScore) {
        for (Map.Entry<Double,List<String>> doScorePair : documentsByScore.entrySet()) {
            double score = doScorePair.getKey();
            for (String document : doScorePair.getValue()) {
                System.out.println(String.format("Book - %s - score : %f",document.split("/")[4],score));
            }
        }
    }
}
