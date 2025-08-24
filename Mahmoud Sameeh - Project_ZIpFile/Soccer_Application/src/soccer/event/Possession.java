package soccer.event;
import soccer.play.Player;
import soccer.play.Team;

public class Possession extends GameEvent {
    public Possession(Team team, Player player, int minute) {
        super("Possession", team, player, minute);
    }
}
