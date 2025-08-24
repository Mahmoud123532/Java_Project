package app;
import soccer.play.League;
import soccer.util.PlayerDatabase;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        System.out.println("===================================");
        System.out.println(" Welcome to Soccer Application ");
        System.out.println("===================================");
        System.out.println("1. Recall past data");
        System.out.println("2. Start new league/match");
        System.out.println("3. Delete ALL past data");
        System.out.print("Choose an option: ");

        int choice;
        try {
            choice = Integer.parseInt(in.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Exiting.");
            return;
        }

        try {
            if (choice == 1) {
                recallPastData();
            } else if (choice == 2) {
                League league = new League();
                league.setupLeague(in);
                league.playLeague(in);
                System.out.println();
                System.out.println("=== Team Goals/Points (this league teams) ===");
                league.printGoalsPointsForCurrentTeams();

                System.out.println("\n All data saved to SQLite. Re-run and choose Recall to see tables.");
            } else if (choice == 3) {
                deleteAllData();
                System.out.println(" All past data has been deleted.");
            } else {
                System.out.println("Invalid option.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void recallPastData() throws SQLException {
        try (Connection c = PlayerDatabase.getInstance().connect();
             Statement st = c.createStatement()) {
            System.out.println("\n=== Teams & Players ===");
            try (ResultSet rsTeams = st.executeQuery(
                    "SELECT id, name FROM teams ORDER BY id ASC")) {
                boolean anyTeam = false;
                while (rsTeams.next()) {
                    anyTeam = true;
                    int teamId = rsTeams.getInt("id");
                    String teamName = rsTeams.getString("name");
                    System.out.println(teamName);
                    try (PreparedStatement psP = c.prepareStatement(
                            "SELECT name FROM players WHERE team_id=? ORDER BY id ASC")) {
                        psP.setInt(1, teamId);
                        try (ResultSet rsP = psP.executeQuery()) {
                            boolean anyPlayer = false;
                            while (rsP.next()) {
                                anyPlayer = true;
                                System.out.println("  - " + rsP.getString("name"));
                            }
                            if (!anyPlayer) {
                                System.out.println("  (no players)");
                            }
                        }
                    }
                }
                if (!anyTeam) {
                    System.out.println("(no teams)");
                }
            }

            System.out.println("\n=== League Matches ===");
            LinkedHashMap<String, Integer> totalsByTeamName = new LinkedHashMap<>();
            int displayIndex = 1;
            try (ResultSet rs = st.executeQuery(
                    "SELECT t1.name AS home, t2.name AS away, " +
                            "m.home_goals, m.away_goals " +
                            "FROM matches m " +
                            "JOIN teams t1 ON m.home_team_id=t1.id " +
                            "JOIN teams t2 ON m.away_team_id=t2.id " +
                            "ORDER BY m.id ASC")) {
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    String home = rs.getString("home");
                    String away = rs.getString("away");
                    int hg = rs.getInt("home_goals");
                    int ag = rs.getInt("away_goals");
                    System.out.printf("Match #%d: %s %d - %d %s%n",
                            displayIndex++, home, hg, ag, away);

                    totalsByTeamName.putIfAbsent(home, 0);
                    totalsByTeamName.putIfAbsent(away, 0);
                    totalsByTeamName.put(home, totalsByTeamName.get(home) + hg);
                    totalsByTeamName.put(away, totalsByTeamName.get(away) + ag);
                }
                if (!any) {
                    System.out.println("(no matches)");
                }
            }

            if (!totalsByTeamName.isEmpty()) {
                System.out.println();
                for (Map.Entry<String, Integer> e : totalsByTeamName.entrySet()) {
                    String teamName = e.getKey();
                    int goals = e.getValue();
                    int points = goals * 3;
                    System.out.printf("%-12s %3d Goals %6d Points%n", teamName, goals, points);
                }
            }

            Integer lastMatchId = null;
            try (ResultSet rs = st.executeQuery("SELECT MAX(id) FROM matches")) {
                if (rs.next()) {
                    int v = rs.getInt(1);
                    lastMatchId = rs.wasNull() ? null : v;
                }
            }

            if (lastMatchId != null) {
                System.out.println("\n=== Events for last match ===");
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT e.id, e.type, t.name AS team, p.name AS player, e.minute " +
                                "FROM events e " +
                                "LEFT JOIN teams t ON e.team_id=t.id " +
                                "LEFT JOIN players p ON e.player_id=p.id " +
                                "WHERE e.match_id=? ORDER BY e.minute ASC, e.id ASC")) {
                    ps.setInt(1, lastMatchId);
                    try (ResultSet rs = ps.executeQuery()) {
                        System.out.printf("%-10s | %-12s | %-15s | %-4s%n", "Type", "Team", "Player", "Min");
                        System.out.println("--------------------------------------------------");
                        boolean any = false;
                        while (rs.next()) {
                            any = true;
                            String type = rs.getString("type");
                            String team = rs.getString("team") == null ? "" : rs.getString("team");
                            String player = rs.getString("player") == null ? "" : rs.getString("player");
                            int minute = rs.getInt("minute");
                            System.out.printf("%-10s | %-12s | %-15s | %2d'%n", type, team, player, minute);
                        }
                        if (!any) {
                            System.out.println("(no events)");
                        }
                    }
                }
            } else {
                System.out.println("\n=== No matches recorded yet ===");
            }
        }
    }

    private static void deleteAllData() throws SQLException {
        try (Connection c = PlayerDatabase.getInstance().connect();
             Statement st = c.createStatement()) {
            st.executeUpdate("DELETE FROM events");
            st.executeUpdate("DELETE FROM matches");
            st.executeUpdate("DELETE FROM players");
            st.executeUpdate("DELETE FROM teams");
        }
    }
}
