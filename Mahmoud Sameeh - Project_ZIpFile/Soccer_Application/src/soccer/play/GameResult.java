package soccer.play;

public class GameResult {
    private final Team homeTeam;
    private final Team awayTeam;
    private final int homeGoals;
    private final int awayGoals;

    public GameResult(Team homeTeam, Team awayTeam, int homeGoals, int awayGoals) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;
    }

    public Team getHomeTeam() { return homeTeam; }
    public Team getAwayTeam() { return awayTeam; }
    public int getHomeGoals() { return homeGoals; }
    public int getAwayGoals() { return awayGoals; }

    public String getResultString() { return homeGoals + " - " + awayGoals; }
    public int getPointsForHome() {
        if (homeGoals > awayGoals) return 3;
        if (homeGoals == awayGoals) return 1;
        return 0;
    }
    public int getPointsForAway() {
        if (awayGoals > homeGoals) return 3;
        if (awayGoals == homeGoals) return 1;
        return 0;
    }
}
