package soccer.play;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Team {
    private int id;
    private final String name;
    private final List<Player> players = new ArrayList<>();

    public Team(String name) {
        this.name = name;
    }

    public Team(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }

    public List<Player> getPlayers() { return players; }

    public void addPlayer(Player p) {
        if (p == null) return;
        Optional<Player> existing = players.stream()
                .filter(x -> x.getName().equalsIgnoreCase(p.getName()))
                .findFirst();
        if (!existing.isPresent()) {
            p.setTeam(this);
            players.add(p);
        } else {
            Player ex = existing.get();
            if (ex.getId() == 0 && p.getId() != 0) ex.setId(p.getId());
            if (ex.getTeam() == null) ex.setTeam(this);
        }
    }

    public Player findPlayerByName(String name) {
        if (name == null) return null;
        return players.stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return "Team{" + id + ":" + name + " players=" + players.size() + "}";
    }
}
