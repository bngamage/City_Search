//Copyright 2016, University of Freiburg,
//Chair of Algorithms and Data Structures.
//Author: Hannah Bast <bast@cs.uni-freiburg.de>.

/**
 * First steps towards a q-gram index, written during class.
 */
public class Document {

  int id;
  int score;
  int ped;
  double lat;
  double lon;

  /**
   * Gets the distance between tw points.
   */
  public Document(int id, int ped, int score, double lat, double lon) {
    this.id = id;
    this.ped = ped;
    this.score = score;
    this.lat = lat;
    this.lon = lon;
  }
}
