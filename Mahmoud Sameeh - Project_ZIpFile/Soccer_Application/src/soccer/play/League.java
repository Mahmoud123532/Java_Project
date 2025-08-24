package soccer.play;

import soccer.event.GameEvent;
import soccer.event.Goal;
import soccer.event.Kickoff;
import soccer.event.Possession;
import soccer.util.PlayerDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class League {
    private final List<Team> teams = new ArrayList<>();

    public League() {}

    public List<Team> getTeams() {
        return teams;
    }

    public void setupLeague(Scanner in) throws SQLException {
        PlayerDatabase db = PlayerDatabase.getInstance();
        int nTeams = askInt(in, "How many teams? (2-5): ", 2, 5);

        for (int i = 0; i < nTeams; i++) {
            System.out.print("Enter team #" + (i + 1) + " name: ");
            String name = in.nextLine().trim();

            int teamId = db.upsertTeam(name);
            Team team = new Team(teamId, name);

            int nPlayers = askInt(in, "  How many players for " + name + "? (1-11): ", 1, 11);
            for (int j = 0; j < nPlayers; j++) {
                System.out.print("    Enter player #" + (j + 1) + " name: ");
                String pname = in.nextLine().trim();
                int pid = db.upsertPlayerForTeam(teamId, pname);
                Player p = new Player(pid, pname, team);
                team.addPlayer(p);
            }

            teams.add(team);
        }
    }

    // Full League
    public void playLeague(Scanner in) throws SQLException {
        PlayerDatabase db = PlayerDatabase.getInstance();
        int n = teams.size();

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Team home = teams.get(i);
                Team away = teams.get(j);

                System.out.println("\nMatch: " + home.getName() + " vs " + away.getName());
                Game g = new Game(home, away);
                System.out.print("Do you want to enter final score ? (y/n): ");
                String ans = in.nextLine().trim().toLowerCase();
                if (ans.startsWith("y")) {
                    int hg = askInt(in, "  Enter home goals: ", 0, 99);
                    int ag = askInt(in, "  Enter away goals: ", 0, 99);
                    g.setFinalScore(hg, ag);
                }

                g.addEvent(new Kickoff());
                int lastMinute = 0;
                while (true) {
                    System.out.print("Add another event? (yes/no): ");
                    String add = in.nextLine().trim().toLowerCase();
                    if (!add.startsWith("y")) break;

                    String type = askType(in);
                    Team team = askTeamChoice(in, home, away);

                    System.out.print("Player name : ");
                    String playerName = in.nextLine().trim();
                    Player player = null;
                    if (!playerName.isEmpty()) {
                        player = team.findPlayerByName(playerName);
                        if (player == null) {
                            int pid = db.upsertPlayerForTeam(team.getId(), playerName);
                            player = new Player(pid, playerName, team);
                            team.addPlayer(player);
                        }
                    }

                    int minute = askInt(in, "Minute (>" + lastMinute + ", ≤90): ", lastMinute + 1, 90);
                    lastMinute = minute;

                    GameEvent ev;
                    if (type.equalsIgnoreCase("Goal")) {
                        ev = new Goal(team, player, minute);
                    } else {
                        ev = new Possession(team, player, minute);
                    }
                    g.addEvent(ev);
                }

                int matchId = g.saveToDatabase();
                if (matchId != -1) {
                    System.out.println("Saved match id=" + matchId);
                } else {
                    System.out.println("Failed to save match.");
                }
            }
        }
    }

    public void printGoalsPointsForCurrentTeams() throws SQLException {
        try (Connection c = PlayerDatabase.getInstance().connect()) {
            for (Team t : teams) {
                int goalsHome = 0;
                int goalsAway = 0;
                try (PreparedStatement psH = c.prepareStatement(
                        "SELECT COALESCE(SUM(home_goals),0) FROM matches WHERE home_team_id=?")) {
                    psH.setInt(1, t.getId());
                    try (ResultSet rs = psH.executeQuery()) {
                        if (rs.next()) goalsHome = rs.getInt(1);
                    }
                }
                try (PreparedStatement psA = c.prepareStatement(
                        "SELECT COALESCE(SUM(away_goals),0) FROM matches WHERE away_team_id=?")) {
                    psA.setInt(1, t.getId());
                    try (ResultSet rs = psA.executeQuery()) {
                        if (rs.next()) goalsAway = rs.getInt(1);
                    }
                }
                int goals = goalsHome + goalsAway;
                int points = goals * 3;
                System.out.printf("%-12s %3d Goals %6d Points%n", t.getName(), goals, points);
            }
        }
    }

    private static int askInt(Scanner in, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int v = Integer.parseInt(in.nextLine().trim());
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException ignored) {}
            System.out.println("Enter a number between " + min + " and " + max);
        }
    }

    private static String askType(Scanner in) {
        while (true) {
            System.out.print("Event type (Goal/Possession): ");
            String t = in.nextLine().trim();
            if (t.equalsIgnoreCase("Goal") || t.equalsIgnoreCase("Possession")) return t;
            System.out.println("Invalid type.");
        }
    }

    private static Team askTeamChoice(Scanner in, Team home, Team away) {
        System.out.println("Choose team:");
        System.out.println("  1) " + home.getName());
        System.out.println("  2) " + away.getName());
        int choice = askInt(in, "Your choice: ", 1, 2);
        return (choice == 1) ? home : away;
    }
}
