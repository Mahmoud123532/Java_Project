package soccer.util;
import java.sql.*;

/**
 * SQL query  for SQLite.
 */
public class PlayerDatabase {

    private static PlayerDatabase instance = null;

    private PlayerDatabase() {
        createTablesIfNotExist();
    }

    public static synchronized PlayerDatabase getInstance() {
        if (instance == null) instance = new PlayerDatabase();
        return instance;
    }

    public Connection connect() throws SQLException {
        Connection c = DriverManager.getConnection("jdbc:sqlite:" + Settings.DB_FILE);
        try (Statement st = c.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return c;
    }

    private void createTablesIfNotExist() {
        try (Connection c = connect(); Statement st = c.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");

            // teams
            st.execute("CREATE TABLE IF NOT EXISTS teams(" +
                    " id   INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " name TEXT NOT NULL UNIQUE" +
                    ")");

            // players
            st.execute("CREATE TABLE IF NOT EXISTS players(" +
                    " id      INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " name    TEXT NOT NULL," +
                    " team_id INTEGER," +
                    " UNIQUE(name, team_id)," +
                    " FOREIGN KEY(team_id) REFERENCES teams(id) ON DELETE CASCADE" +
                    ")");

            // matches
            st.execute("CREATE TABLE IF NOT EXISTS matches(" +
                    " id            INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " home_team_id  INTEGER NOT NULL," +
                    " away_team_id  INTEGER NOT NULL," +
                    " home_goals    INTEGER NOT NULL," +
                    " away_goals    INTEGER NOT NULL," +
                    " FOREIGN KEY(home_team_id) REFERENCES teams(id) ON DELETE CASCADE," +
                    " FOREIGN KEY(away_team_id) REFERENCES teams(id) ON DELETE CASCADE" +
                    ")");

            // events
            st.execute("CREATE TABLE IF NOT EXISTS events(" +
                    " id         INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " match_id   INTEGER NOT NULL," +
                    " type       TEXT NOT NULL," +
                    " team_id    INTEGER," +
                    " player_id  INTEGER," +
                    " minute     INTEGER NOT NULL," +
                    " FOREIGN KEY(match_id)  REFERENCES matches(id) ON DELETE CASCADE," +
                    " FOREIGN KEY(team_id)   REFERENCES teams(id)   ON DELETE SET NULL," +
                    " FOREIGN KEY(player_id) REFERENCES players(id) ON DELETE SET NULL" +
                    ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int upsertTeam(String name) throws SQLException {
        String trimmed = name.trim();
        try (Connection c = connect()) {
            try (PreparedStatement ps = c.prepareStatement("INSERT OR IGNORE INTO teams(name) VALUES (?)")) {
                ps.setString(1, trimmed);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("SELECT id FROM teams WHERE name=?")) {
                ps.setString(1, trimmed);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to upsert team: " + name);
    }

    public int upsertPlayerForTeam(int teamId, String playerName) throws SQLException {
        String trimmed = playerName.trim();
        try (Connection c = connect()) {
            try (PreparedStatement ps = c.prepareStatement("INSERT OR IGNORE INTO players(name, team_id) VALUES (?,?)")) {
                ps.setString(1, trimmed);
                ps.setInt(2, teamId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("SELECT id FROM players WHERE name=? AND team_id=?")) {
                ps.setString(1, trimmed);
                ps.setInt(2, teamId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to upsert player: " + playerName + " (teamId=" + teamId + ")");
    }

    public int insertMatch(int homeId, int awayId, int homeGoals, int awayGoals) throws SQLException {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO matches(home_team_id,away_team_id,home_goals,away_goals) VALUES (?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, homeId);
            ps.setInt(2, awayId);
            ps.setInt(3, homeGoals);
            ps.setInt(4, awayGoals);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Failed to insert match");
    }

    public void addEvent(int matchId, String type, Integer teamId, Integer playerId, int minute) throws SQLException {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO events(match_id,type,team_id,player_id,minute) VALUES (?,?,?,?,?)")) {
            ps.setInt(1, matchId);
            ps.setString(2, type);
            if (teamId == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, teamId);
            if (playerId == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, playerId);
            ps.setInt(5, minute);
            ps.executeUpdate();
        }
    }
}
