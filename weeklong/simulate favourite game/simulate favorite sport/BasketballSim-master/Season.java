import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.nio.charset.*;


public class Season {
	//driver for simulating a season
	public static void main (String[] args) {
		//create a dictionary of teams by their name
		Map<String,Team> teams = new HashMap<String,Team>();
		List<String> teamStrings = null;
		try {
			//read the file of team strings
			teamStrings = Files.readAllLines(Paths.get("teams.txt"), StandardCharsets.US_ASCII);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(String teamStr : teamStrings) {
			//read the team files, convert them to team objects, and store them into our dictionary
			Team newTeam = Team.readFromFile("Teams/"+teamStr+".txt", teamStr);
			teams.put(teamStr, newTeam);
		}
		
		//read through the nba schedule file
		try (BufferedReader br = new BufferedReader(new FileReader("nba_schedule.txt"))) {
		    String gameStr;
		    while ((gameStr = br.readLine()) != null) {
		       // process the line, representing one nba game
				String[] tokens = gameStr.split(",");
				String[] homeTokens = tokens[3].split(" ");
				Team homeTeam = teams.get(homeTokens[homeTokens.length-1]);
				String[] awayTokens = tokens[5].split(" ");
				Team awayTeam = teams.get(awayTokens[awayTokens.length-1]);
				Game game = new Game(homeTeam, awayTeam);
				game.simGame();
				//add a win to whoever wins, a loss to whoever loses
				if(game.getWinner() == 1) {
					homeTeam.addWin();
					awayTeam.addLoss();
				}
				else {
					homeTeam.addLoss();
					awayTeam.addWin();
				}
		    }
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//print out regular season records
		for(Team t : teams.values()) {
			t.printTeamSeason();
		}
		System.out.println();
		//sim through playoffs
		Playoffs p = new Playoffs(teams.values());
		p.simPlayoffs();
		p.printPlayoffs();
	}
}
