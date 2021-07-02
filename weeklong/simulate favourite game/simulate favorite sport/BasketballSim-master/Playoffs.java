import java.util.Collection;

public class Playoffs {
	
	private String[] eastern = {"celtics", "knicks", "76ers", "nets", "raptors",
            "heat","hornets","magic","hawks","wizards",
            "pacers","bulls","bucks","pistons","cavaliers"};
	private String[] western = {"lakers","warriors","clippers","suns","kings",
			"thunder","trailblazers","nuggets","timberwolves","jazz",
			"rockets","mavericks","spurs","pelicans","grizzlies"};
	private Collection<Team> teams;
	private Team[] seedings;
	private Series[] roundOf16;
	private Series[] roundOf8;
	private Series[] semiFinals;
	private Series finals;
	
	public Playoffs(Collection<Team> collection) {
		teams = collection;
	}
	
	//pick the top 8 teams in each conference
	public void pickSeeds () {
		seedings = new Team[16];
		//eastern
		for(int i = 0; i < 8; i++) {
			Team maxWins = null;
			for(Team team : teams) {
				if(maxWins == null)
					maxWins = new Team(); //dummy team w/ 0 wins
				else {
					boolean alreadySelected = false;
					for(int j = 0; j < i; j++) {
						if(seedings[j] == team)
							alreadySelected = true;
					}
					boolean rightConference = false;
					for(String s : eastern)
						if(team.getName().equals(s))
							rightConference = true;
					if(team.getWins() > maxWins.getWins() && !alreadySelected && rightConference)
						maxWins = team;
				}
			}
			seedings[i] = maxWins;
		}
		//western
		for(int i = 8; i < 16; i++) {
			Team maxWins = null;
			for(Team team : teams) {
				if(maxWins == null)
					maxWins = new Team();
				else {
					boolean alreadySelected = false;
					for(int j = 8; j < i; j++) {
						if(seedings[j] == team)
							alreadySelected = true;
					}
					boolean rightConference = false;
					for(String s : western)
						if(team.getName().equals(s))
							rightConference = true;
					if(team.getWins() > maxWins.getWins() && !alreadySelected && rightConference)
						maxWins = team;
				}
			}
			seedings[i] = maxWins;
		}
	}
	
	//simulate the round of 16
	public void pick16() {
		roundOf16 = new Series[8];
		roundOf16[0] = new Series(seedings[0],seedings[7]);
		roundOf16[1] = new Series(seedings[1],seedings[6]);
		roundOf16[2] = new Series(seedings[2],seedings[5]);
		roundOf16[3] = new Series(seedings[3],seedings[4]);
		roundOf16[4] = new Series(seedings[8],seedings[15]);
		roundOf16[5] = new Series(seedings[9],seedings[14]);
		roundOf16[6] = new Series(seedings[10],seedings[13]);
		roundOf16[7] = new Series(seedings[11],seedings[12]);
		for(Series s : roundOf16)
			s.simSeries();
	}
	
	//simulate the round of 8
	public void pick8() {
		roundOf8 = new Series[4];
		roundOf8[0] = new Series(roundOf16[0].getWinner(), roundOf16[3].getWinner());
		roundOf8[1] = new Series(roundOf16[1].getWinner(), roundOf16[2].getWinner());
		roundOf8[2] = new Series(roundOf16[4].getWinner(), roundOf16[7].getWinner());
		roundOf8[3] = new Series(roundOf16[5].getWinner(), roundOf16[6].getWinner());
		for(Series s : roundOf8)
			s.simSeries();
	}
	
	//simulate semis
	public void pickSemis() {
		semiFinals = new Series[2];
		semiFinals[0] = new Series(roundOf8[0].getWinner(),roundOf8[1].getWinner());
		semiFinals[1] = new Series(roundOf8[2].getWinner(),roundOf8[3].getWinner());
		for(Series s : semiFinals)
			s.simSeries();
	}
	
	//simulate finals
	public void pickFinals() {
		finals = new Series(semiFinals[0].getWinner(),semiFinals[1].getWinner());
		finals.simSeries();
	}
	
	//simulate the entire playoffs, starting from first picking the seeds
	public void simPlayoffs() {
		pickSeeds();
		pick16();
		pick8();
		pickSemis();
		pickFinals();
	}
	
	public void printPlayoffs() {
		System.out.println("Eastern Conference Seeds:");
		for(int i = 0; i < 8; i++) {
			System.out.print((i+1)+". ");
			seedings[i].printTeamSeason();
		}
		System.out.println("Western Conference Seedngs:");
		for(int i = 8; i < 16; i++) {
			System.out.print((i-7)+". ");
			seedings[i].printTeamSeason();
		}
		System.out.println("First Round Matchups:");
		System.out.println("East");
		for(int i = 0; i < 4; i++) {
			roundOf16[i].printSeries();
		}
		System.out.println("West");
		for(int i = 4; i < 8; i++) {
			roundOf16[i].printSeries();
		}
		System.out.println();
		System.out.println("Second Round Matchups:");
		System.out.println("East");
		for(int i = 0; i < 2; i++) {
			roundOf8[i].printSeries();
		}
		System.out.println("West");
		for(int i = 2; i < 4; i++) {
			roundOf8[i].printSeries();
		}
		System.out.println();
		System.out.println("Conference Finals:");
		System.out.println("East");
		semiFinals[0].printSeries();
		System.out.println("West");
		semiFinals[1].printSeries();
		System.out.println();
		System.out.println("Finals");
		finals.printSeries();
		
	}
	
	//represents a seven game series
	private class Series {
		private Team high_seed;
		private Team low_seed;
		private int high_wins;
		private int low_wins;
		
		//creates a series, given two teams, selecting a high and a low seed
		private Series (Team team1, Team team2) {
			if(team1.getWins() > team2.getWins()) {
				high_seed = team1;
				low_seed = team2;
			}
			else {
				high_seed = team2;
				low_seed = team1;
			}
			high_wins = 0;
			low_wins = 0;
		}
		
		//simulate the series, with the correct team at home for each game
		private void simSeries() {
			int gamesPlayed = 0;
			while(high_wins < 4 && low_wins < 4) {
				if(gamesPlayed == 2 || gamesPlayed == 3 || gamesPlayed == 5) {
					Game g = new Game(low_seed, high_seed);
					g.simGame();
					if(g.getWinner() == 1) {
						low_wins++;
					}
					else {
						high_wins++;
					}
				}
				else {
					Game g = new Game(high_seed, low_seed);
					g.simGame();
					if(g.getWinner() == 1) {
						high_wins++;
					}
					else {
						low_wins++;
					}
				}
			}
		}
		
		private Team getWinner() {
			if(high_wins == 4) {
				return high_seed;
			}
			else {
				return low_seed;
			}
		}
		
		private Team getLoser() {
			if(high_wins == 4) {
				return low_seed;
			}
			else {
				return high_seed;
			}
		}
		
		private void printSeries() {
			int loserWins;
			if(high_wins == 4)
				loserWins = low_wins;
			else
				loserWins = high_wins;
			System.out.printf("%15s def. %15s (4-%d)\n",getWinner().getName(),getLoser().getName(),loserWins);
		}
	}
}


