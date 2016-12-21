// Copyright 2016, University of Freiburg,
// Chair of Algorithms and Data Structures.
// Authors: Hannah Bast <bast@cs.uni-freiburg.de>, Bhashitha Gamage <gamage25@gmail.com>

import org.junit.Test;
import org.junit.Assert;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * One unit test for each non-trivial method in the QGramIndex class.
 */
public class QGramIndexTest {

  @Test
  public void qGrams() {
    QGramIndex qgi = new QGramIndex(3);
    Assert.assertEquals("[$$l, $li, lir, iru, rum]",
      qgi.getQGrams("lirum").toString());
  }

  @Test
  public void urlDecode() throws IOException {
    String output = SearchServerMain.urlDecode("z%C3%BCrich");
    Assert.assertEquals(output, "zürich");
    output = SearchServerMain.urlDecode("L%C3%B8kken");
    Assert.assertEquals(output, "Løkken");
    output = SearchServerMain.urlDecode("a+o");
    Assert.assertEquals(output, "a o");
    output = SearchServerMain.urlDecode("%C3%A1+%C3%A9");
    Assert.assertEquals(output, "á é");
    output = SearchServerMain.urlDecode("%C3%A1%20%C3%A9");
    Assert.assertEquals(output, "á é");
  }

  @Test
  public void checkPrefixEditDistance() throws IOException {
    QGramIndex qgi = new QGramIndex(3);
    int test = qgi.checkPrefixEditDistance("tübi", "tubi", 1);
    Assert.assertEquals(test, 1);
    test = qgi.checkPrefixEditDistance("tübi", "tubi", 1);
    Assert.assertEquals(test, 1);
    test = qgi.checkPrefixEditDistance("tübi", "tøbi", 1);
    Assert.assertEquals(test, 1);
    test = qgi.checkPrefixEditDistance("tübi", "tübi", 0);
    Assert.assertEquals(test, 0);
  }

  @Test
  public void jsonStringEscape() throws IOException {
    QGramIndex qgi = new QGramIndex(3);
    FileReader fileReader = new FileReader("testInput.txt");
    BufferedReader bufferedReader1 = new BufferedReader(fileReader);
    String line;
    String input1 = null;
    String output = null;
    while ((line = bufferedReader1.readLine()) != null) {
      input1 = line;
    }
    FileReader fileReader2 = new FileReader("testOutput.txt");
    BufferedReader bufferedReader2 = new BufferedReader(fileReader2);
    while ((line = bufferedReader2.readLine()) != null) {
      output = line;
    }
    String output1 = qgi.jsonStringEscape(input1);
    Assert.assertEquals(output, output1);
  }
}
