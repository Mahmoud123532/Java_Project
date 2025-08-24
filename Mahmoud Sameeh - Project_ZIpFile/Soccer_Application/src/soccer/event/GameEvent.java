package soccer.event;
import soccer.play.Player;
import soccer.play.Team;

public abstract class GameEvent {
    protected String type;
    protected Team team;
    protected Player player;
    protected int minute;

    public GameEvent(String type, Team team, Player player, int minute) {
        this.type = type;
        this.team = team;
        this.player = player;
        this.minute = minute;
    }

    public String getType() {
        return type;
    }

    public Team getTeam() {
        return team;
    }

    public Player getPlayer() {
        return player;
    }

    public int getMinute() {
        return minute;
    }

    @Override
    public String toString() {
        String teamName = (team != null) ? team.getName() : "";
        String playerName = (player != null) ? player.getName() : "";
        return String.format("%d' %s | %-12s | %-15s",
                minute, type, teamName, playerName);
    }
}
