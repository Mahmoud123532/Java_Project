package soccer.play;

public class Player {
    private int id;           // DB id (0 = not yet persisted)
    private final String name;
    private Team team;

    public Player(String name) {
        this.name = name;
    }

    public Player(int id, String name, Team team) {
        this.id = id;
        this.name = name;
        this.team = team;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    @Override
    public String toString() {
        if (team != null) return name + " (id=" + id + ", team=" + team.getName() + ")";
        return name + " (id=" + id + ")";
    }
}
