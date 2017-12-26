import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.Topological;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class WordNet {
  private final Digraph wordnet;
  private final SAP sap;
  private final Map<String, Set<Integer>> nounMap;
  private final Map<Integer, String> synsetMap;

  // constructor takes the name of the two input files
  public WordNet(String synsets, String hypernyms) {
    if (synsets == null || hypernyms == null) throw new IllegalArgumentException();

    In inSynsets = new In(synsets);
    In inHypernyms = new In(hypernyms);

    String[] synsetsLines = inSynsets.readAllLines();

    wordnet = new Digraph(synsetsLines.length);
    nounMap = new HashMap<>();
    synsetMap = new HashMap<>();

    while (inHypernyms.hasNextLine()) {
      String[] hyperLine = inHypernyms.readLine().split(",");

      for (int i = 1; i < hyperLine.length; i++) {
        wordnet.addEdge(Integer.parseInt(hyperLine[0]), Integer.parseInt(hyperLine[i]));
      }
    }

    if (!new Topological(wordnet).hasOrder()) throw new IllegalArgumentException();

    for (int i = 0, root = 0; i < wordnet.V(); i++) {
      if (wordnet.outdegree(i) == 0) root++;
      if (root > 1) throw new IllegalArgumentException();
    }

    for (String line : synsetsLines) {
      String[] splitLine = line.split(",");
      int id = Integer.parseInt(splitLine[0]);
      String[] synset = splitLine[1].split(" ");
      synsetMap.put(id, splitLine[1]);

      for (String syn : synset) {
        Set<Integer> mapping = nounMap.putIfAbsent(syn, new HashSet<Integer>(Arrays.asList(id)));

        if (mapping != null) mapping.add(id);
      }
    }

    sap = new SAP(wordnet);
  }

      // returns all WordNet nouns
  public Iterable<String> nouns() {
    return nounMap.keySet();
  }

      // is the word a WordNet noun?
  public boolean isNoun(String word) {
    if (word == null) throw new IllegalArgumentException();
    return nounMap.containsKey(word);
  }

      // distance between nounA and nounB (defined below)
  public int distance(String nounA, String nounB) {
    if (nounA == null || nounB == null || !isNoun(nounA) || !isNoun(nounB)) throw new IllegalArgumentException();
    return sap.length(nounMap.get(nounA), nounMap.get(nounB));
  }

      // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
      // in a shortest ancestral path (defined below)
  public String sap(String nounA, String nounB) {
    if (nounA == null || nounB == null || !isNoun(nounA) || !isNoun(nounB)) throw new IllegalArgumentException();
    return synsetMap.get(sap.ancestor(nounMap.get(nounA), nounMap.get(nounB)));
  }

      // do unit testing of this class
  public static void main(String[] args) {}
}
