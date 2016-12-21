// Copyright 2016, University of Freiburg,
// Chair of Algorithms and Data Structures.
// Authors: Hannah Bast <bast@cs.uni-freiburg.de>,

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * First steps towards a q-gram index, written during class.
 */
public class QGramIndex {
  /**
   * Create an empty QGramIndex.
   */
  public QGramIndex(int q) {
    this.invertedLists = new TreeMap<String, ArrayList<Integer>>();
    this.q = q;
    this.padding = new String(new char[q - 1]).replace("\u0000", "$");

    this.words = new ArrayList<String>();
    this.scores = new ArrayList<Integer>();
    this.latitudes = new ArrayList<Double>();
    this.longititudes = new ArrayList<Double>();
  }

  /**
   * Build index from given list of entites (one line per entity, columns are:
   * entity name, score, ...).
   */
  public void buildFromFile(String fileName) throws IOException {
    FileReader fileReader = new FileReader(fileName);
    BufferedReader bufferedReader = new BufferedReader(fileReader);
    String line;
    int entityId = 0;
    while ((line = bufferedReader.readLine()) != null) {
      String name = line.split("\t")[0];
      int score = Integer.parseInt(line.split("\t")[1]);
      double latitude = Double.parseDouble(line.split("\t")[2]);
      double longitude = Double.parseDouble(line.split("\t")[3]);

      words.add(name);
      scores.add(score);
      latitudes.add(latitude);
      longititudes.add(longitude);

      for (String qGram : getQGrams(name)) {
        if (!invertedLists.containsKey(qGram)) {
          invertedLists.put(qGram, new ArrayList<Integer>());
        }

        invertedLists.get(qGram).add(entityId);
      }
      entityId += 1;
    }
  }

  /**
   * Compute q-grams for padded, normalized version of given string.
   */
  public ArrayList<String> getQGrams(String name) {
    name = padding + name.toLowerCase().replaceAll("\\W", "");
    ArrayList<String> result = new ArrayList<String>();
    for (int i = 0; i < name.length() - q + 1; i++) {
      result.add(name.substring(i, i + q));
    }
    return result;
  };

  public int checkPrefixEditDistance(String prefix, String string, int delta) {

    int rows = prefix.length() + 1;
    // int coloumns = string.length() + 1;

    // configuring sequence modified lenghths.
    int importantColumns = prefix.length() + delta + 1;
    if (string.length() < importantColumns) {
      importantColumns = string.length() + 1;
    }

    int[][] distanceMatrix = new int[rows][importantColumns];

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < importantColumns; j++) {
        if (i == 0) {

          distanceMatrix[i][j] = j;
        } else if (j == 0) {
          distanceMatrix[i][j] = i;

        } else {
          // no penalty
          int penalty = 0;

          int minimumValue =
            Math.min(distanceMatrix[i][j - 1], distanceMatrix[i - 1][j - 1]);
          minimumValue = Math.min(minimumValue, distanceMatrix[i - 1][j]);

          if (prefix.charAt(i - 1) == string.charAt(j - 1)) {

            // if minimum value doesnt come form diagonal, then add one as
            // penalty.
            if (minimumValue != distanceMatrix[i - 1][j - 1]) {
              penalty = 1;
            }

          } else {
            penalty = 1;
          }

          distanceMatrix[i][j] = minimumValue + penalty;
        }
      }

    }

    int ped = Integer.MAX_VALUE;
    for (int i = 0; i < importantColumns; i++) {
      if (distanceMatrix[rows - 1][i] < ped) {

        ped = distanceMatrix[rows - 1][i];
      }
    }

    if (ped > (delta + 1)) {

      ped = delta + 1;
    }

    return ped;
  }

  public String findMatches(String query, int delta) {

    query = normalizeString(query);
    // Need to iterate through all entries to get the matching entities
    ArrayList<String> queryQgrams = getQGrams(query);

    ArrayList<Integer> appendInvertedList = new ArrayList<Integer>();

    // retrieve the inverted lists for query qgrams and append all together,
    for (int i = 0; i < queryQgrams.size(); i++) {

      if (invertedLists.get(queryQgrams.get(i)) != null) {
        appendInvertedList.addAll(invertedLists.get(queryQgrams.get(i)));

      }
    }

    int benchmarkValue = query.length() - (q * delta);

    HashMap<Integer, Integer> mergedInvertedList =
      computeUnion(appendInvertedList);

    for (Iterator<Map.Entry<Integer, Integer>> it =
      mergedInvertedList.entrySet().iterator(); it.hasNext();) {
      Map.Entry<Integer, Integer> entry = it.next();

      if (entry.getValue().intValue() < benchmarkValue) {
        it.remove();

      }
    }
    // get PED of candidate docemnts.
    ArrayList<Document> results = new ArrayList<Document>();
    for (Entry<Integer, Integer> entry : mergedInvertedList.entrySet()) {
      // System.out.println(
      // " " + entry.getKey().toString() + " " + entry.getValue().toString());

      int ped = checkPrefixEditDistance(query,
        normalizeString(words.get(entry.getKey())), delta);

      if (ped <= delta) {
        Document doc =
          new Document(entry.getKey(), ped, scores.get(entry.getKey()),
            latitudes.get(entry.getKey()), longititudes.get(entry.getKey()));
        results.add(doc);
      }
    }

    // printing the results.
    // for (int i = 0; i < results.size(); i++) {
    // System.out.println(" " + results.get(i).id + " " + results.get(i).ped
    // + " " + results.get(i).score);
    // }

    // System.out.println(results);

    // sorting result, first by PED and then by score.
    results = sortResults(results);
    String jsonFormattedResponse;
    if (results.size() == 0) {
      jsonFormattedResponse =
        "{\"query\":" + "\"" + query + "\",\"results\":[]}";
    } else {
      jsonFormattedResponse = "{\"query\":" + "\"" + query + "\",\"results\":[";

      int maximumResults = results.size() < 10 ? results.size() : 10;

      for (int i = 0; i < maximumResults; i++) {

        // if (i == 5) {
        // break;

        // }
        jsonFormattedResponse += "{\"city\":" + "\""
          + jsonStringEscape(words.get(results.get(i).id)) + "\",";
        jsonFormattedResponse += "\"score\":" + results.get(i).score + ",";
        jsonFormattedResponse += "\"lat\":" + results.get(i).lat + ",";
        jsonFormattedResponse += "\"lon\":" + results.get(i).lon + ",";
        jsonFormattedResponse += "\"ped\":" + results.get(i).ped + "}";

        System.out.println(words.get(results.get(i).id));
        if (i + 1 < maximumResults) {
          jsonFormattedResponse += ",";

        } else {
          System.out.println("");
        }
      }
      jsonFormattedResponse += "]}";
      System.out.println("@@ total PED seraches: " + mergedInvertedList.size());

      System.out.println(".\n");
      System.out.println("@@ total number of results: " + results.size());
    }
    return jsonFormattedResponse;
  }

  public HashMap<Integer, Integer>
    computeUnion(ArrayList<Integer> documentList) {
    // The inverted lists.
    HashMap<Integer, Integer> union = new HashMap<>();

    for (Integer doc : documentList) {
      // Integer docID = new Integer(doc.id);
      if (!union.containsKey(doc)) {
        union.put(doc, 1);
      } else {
        union.put(doc, new Integer(union.get(doc).intValue() + 1));

      }
    }
    return union;
  }

  public ArrayList<Document> sortResults(ArrayList<Document> results) {

    Collections.sort(results, new Comparator<Document>() {
      @Override
      public int compare(Document doc1, Document doc2) {
        return Integer.compare(doc1.ped, doc2.ped);
      }
    }.thenComparing(new Comparator<Document>() {
      @Override
      public int compare(Document doc1, Document doc2) {
        return Integer.compare(doc2.score, doc1.score);
      }
    }));

    return results;
  }

  public String normalizeString(String input) {
    return input.toLowerCase().replaceAll("\\W", "");

  }

  String jsonStringEscape(String unescaped) {
    String string1 = null;
    String string2 = null;
    if (unescaped.contains("\\") || unescaped.contains("\"")) {
      string1 = unescaped.replaceAll("\\\\", "\\\\\\\\");
      string2 = string1.replaceAll("\"", "\\\\\"");
    } else {
      string2 = unescaped;
    }
    return string2;

  }

  // The value of q.
  protected int q;

  // The padding (q - 1 times $).
  protected String padding;

  // The inverted lists.
  protected TreeMap<String, ArrayList<Integer>> invertedLists;

  ArrayList<String> words;
  ArrayList<Integer> scores;
  ArrayList<Double> latitudes;
  ArrayList<Double> longititudes;

};
