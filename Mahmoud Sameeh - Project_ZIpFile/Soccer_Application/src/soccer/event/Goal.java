package soccer.event;
import soccer.play.Player;
import soccer.play.Team;

public class Goal extends GameEvent {
    public Goal(Team team, Player player, int minute) {
        super("Goal", team, player, minute);
    }
}
