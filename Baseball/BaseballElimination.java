import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import edu.princeton.cs.algs4.*;

public class BaseballElimination {
    private final List<String> nameToInt;
    private final int[] wins;
    private final int[] losses;
    private final int[] remaining;
    private final int[][] against;
    private final Set<String>[] certificate;

    public BaseballElimination(String filename) {
        Set<String>[] certificate = null;
        List<String> nameToInt = null;
        int[] wins = null;
        int[] losses = null;
        int[] remaining = null;
        int[][] against = null;
        try {
            Scanner inputs = new Scanner(new File(filename));
            int teamCount = Integer.parseInt(inputs.nextLine());

            wins = new int[teamCount];
            losses = new int[teamCount];
            remaining = new int[teamCount];
            nameToInt = new ArrayList<>();
            against = new int[teamCount][teamCount];
            certificate = new HashSet[teamCount];

            int teamIndex = 0;
            while (inputs.hasNextLine()) {
                String[] teamInfo = inputs.nextLine().split("\\s+");
                nameToInt.add(teamInfo[0]);
                wins[teamIndex] = Integer.parseInt(teamInfo[1]);
                losses[teamIndex] = Integer.parseInt(teamInfo[2]);
                remaining[teamIndex] = Integer.parseInt(teamInfo[3]);

                for (int i = 4; i < teamCount + 4; i++) {
                    against[teamIndex][i - 4] = Integer.parseInt(teamInfo[i]);
                }

                teamIndex++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

        this.wins = wins;
        this.losses = losses;
        this.remaining = remaining;
        this.against = against;
        this.nameToInt = nameToInt;
        this.certificate = certificate;
    }

    public int numberOfTeams() {
        return wins.length;
    }

    public Iterable<String> teams() {
        return Collections.unmodifiableList(nameToInt);
    }

    public int wins(String team) {
        return wins[nameToInt.indexOf(team)];
    }

    public int losses(String team) {
        return losses[nameToInt.indexOf(team)];
    }

    public int remaining(String team) {
        return remaining[nameToInt.indexOf(team)];
    }

    public int against(String team1, String team2) {
        return against[nameToInt.indexOf(team1)][nameToInt.indexOf(team2)];
    }

    public boolean isEliminated(String team) {
        if (certificate[nameToInt.indexOf(team)] == null) eliminate(team);
        return !certificate[nameToInt.indexOf(team)].isEmpty();
    }

    public Iterable<String> certificateOfElimination(String team) {
        if (certificate[nameToInt.indexOf(team)] == null) eliminate(team);
        return certificate[nameToInt.indexOf(team)].isEmpty() ? null : certificate[nameToInt.indexOf(team)];
    }

    private void eliminate(String team) {

        Set<String> certification = new HashSet<>();
        List<String> otherTeams = new ArrayList<>(nameToInt);
        otherTeams.remove(team);

        for (String testTeam : otherTeams)
            if (wins(team) + remaining(team) < wins(testTeam))
                certification.add(testTeam);

        if (!certification.isEmpty()) {
            certificate[nameToInt.indexOf(team)] = certification;
            return;
        }

        FlowNetwork net = new FlowNetwork(numberOfTeams() * (numberOfTeams() - 1) / 2 + 2);
        int roundIndex = otherTeams.size();

        int t = net.V() - 1;
        int s = net.V() - 2;

        for (int i = 0; i < otherTeams.size(); i++) {
            net.addEdge(new FlowEdge(i, t, (double) wins(team) + remaining(team) - wins(otherTeams.get(i))));

            for (int j = i + 1; j < otherTeams.size(); j++) {
                net.addEdge(new FlowEdge(s, roundIndex, against(otherTeams.get(i), otherTeams.get(j))));
                net.addEdge(new FlowEdge(roundIndex, i, Double.POSITIVE_INFINITY));
                net.addEdge(new FlowEdge(roundIndex, j, Double.POSITIVE_INFINITY));
                roundIndex++;
            }
        }

        FordFulkerson solver = new FordFulkerson(net, s, t);

        for (int i = 0; i < otherTeams.size(); i++)
            if (solver.inCut(i))
                certification.add(otherTeams.get(i));

        certificate[nameToInt.indexOf(team)] = certification;

    }

    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            } else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}
