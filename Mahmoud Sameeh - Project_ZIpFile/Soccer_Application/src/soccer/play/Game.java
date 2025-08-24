package soccer.play;
import soccer.event.GameEvent;
import soccer.event.Goal;
import soccer.util.PlayerDatabase;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Game {
    private final Team home;
    private final Team away;
    private final List<GameEvent> events = new ArrayList<>();
    private int homeGoals = 0;
    private int awayGoals = 0;
    private boolean scoreLocked = false; // true when user provided final score manually

    public Game(Team home, Team away) {
        this.home = home;
        this.away = away;
    }

    public void setFinalScore(int hg, int ag) {
        this.homeGoals = hg;
        this.awayGoals = ag;
        this.scoreLocked = true;
    }

    public void addEvent(GameEvent e) {
        events.add(e);
        if (!scoreLocked && e instanceof Goal) {
            Team t = e.getTeam();
            if (t != null && t.getName().equalsIgnoreCase(home.getName())) homeGoals++;
            else awayGoals++;
        }
    }

    public int getHomeGoals() { return homeGoals; }
    public int getAwayGoals() { return awayGoals; }
    public List<GameEvent> getEvents() { return events; }

    public int saveToDatabase() {
        try {
            PlayerDatabase db = PlayerDatabase.getInstance();
            int matchId = db.insertMatch(home.getId(), away.getId(), homeGoals, awayGoals);

            for (GameEvent ev : events) {
                Integer teamId = (ev.getTeam() == null) ? null : ev.getTeam().getId();
                Integer playerId = null;
                if (ev.getPlayer() != null) {
                    Player p = ev.getPlayer();
                    if (p.getId() == 0) {
                        playerId = db.upsertPlayerForTeam(teamId, p.getName());
                        p.setId(playerId);
                    } else {
                        playerId = p.getId();
                    }
                }
                db.addEvent(matchId, ev.getType(), teamId, playerId, ev.getMinute());
            }
            return matchId;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return -1;
        }
    }
}
