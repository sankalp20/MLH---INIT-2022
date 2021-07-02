import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.*;

public class Team {
    private Player[] players;
    private Player[] onCourt;
    private int wins;
    private int losses;
    private String name;
    
    public Team () {
    	players = new Player[12];
    	onCourt = new Player[5];
    	wins = 0;
    	losses = 0;
    }

    public Team (Player[] players_) {
        players = players_;
        onCourt = new Player[5];
        for (int k = 0; k < 5; k++) {
        	onCourt[k] = players[k];
        }
    }
    
    //construct team object from list of players, current record, and name
    public Team (Player[] players_, int wins_, int losses_, String name_) {
    	players = players_;
        onCourt = new Player[5];
        for (int k = 0; k < 5; k++) {
        	onCourt[k] = players[k];
        }
        wins = wins_;
        losses = losses_;
        name = name_;
    }
    
    //randomly select a shooter based on their offensive ratings
    public int getShooter() {
    	//get the total distribution
    	int totalDistribution = 0;
    	for(Player p : onCourt) {
    		int[] a = p.getAttributes();
    		 //rough measure of player's offensive skill, weighted towards pass (point guards)
    		int freq = a[0]+a[1]+a[2]+6*a[3];
    		totalDistribution += freq;
    	}
    	double current = 0.0;
    	double outcome = Math.random();
    	//then randomly select a player, better players will get more shots
    	for(int i = 0; i < 5; i++) {
    		int[] a = onCourt[i].getAttributes();
    		int freq = a[0]+a[1]+a[2]+6*a[3];
    		if(current + (freq*1.0)/totalDistribution >= outcome)
    			return i;
    		current += (freq*1.0)/totalDistribution;
    	}
    	throw new RuntimeException("Something went wrong in 'getShooter");
    }
    
    //randomly select the recipient of a pass, cannot be the passer who is given
    public int getRecipientOfPass (Player assister) {
    	//sum up total ratings and get select a player, better players will get more shots
    	int totalDistribution = 0;
    	for(Player p : onCourt) {
    		if(p == assister) {
    			continue;
    		}
    		int[] a = p.getAttributes();
    		//rough measure of player's offensive skill, without including passing rating
    		int freq = a[0]+a[1]+a[2]; 
    		totalDistribution += freq;
    	}
    	double current = 0.0;
    	double outcome = Math.random();
    	for(int i = 0; i < 5; i++) {
    		if(onCourt[i] == assister) {
    			continue;
    		}
    		int[] a = onCourt[i].getAttributes();
    		int freq = a[0]+a[1]+a[2];
    		if(current + (freq*1.0)/totalDistribution >= outcome)
    			return i;
    		current += (freq*1.0)/totalDistribution;
    	}
    	throw new RuntimeException("Something went wrong in 'getRecpientofPass'");
    }
    
    //get a player object from a position (0-4)
    public Player getPos (int pos) {
    	return onCourt[pos];
    }
    
    public void printTeam() {
    	System.out.println("      First        Last   Min   Pt    FGM/FGA     3Pt    FT     Reb    Ast   Stl  Blk  TO");
    	for(int i = 0; i < players.length; i++) {
    		players[i].printPlayer();
    	}
    }
    
    //print the name and record
    public void printTeamSeason() {
    	System.out.printf("%25s %d-%d\n", name, wins, losses);
    }
    
    //get the total team help defense
    public int getTeamDefense() {
    	int sum = 0;
    	for(Player p: onCourt)
    		sum += p.getAttributes()[6];
    	return sum;
    }
    
    //get the total team rebound rating
    public int sumRebound() {
    	int total = 0;
    	for(Player p : onCourt) 
    		total += p.getAttributes()[4];
    	return total;
    }
    
    //distribute a rebound to someone, skewed towards better rebounders
    public int distributeRebound() {
    	int total = sumRebound();
    	double outcome = Math.random() * total;
    	for(int i = 0; i < 5; i++) {
    		if(onCourt[i].getAttributes()[4] >= outcome)
    			return i;
    		outcome -= onCourt[i].getAttributes()[4];
    	}
    	return -1;
    }
    
    //distribute the block to someone other than the on ball defender, skewed towards better blockers
    public void distributeBlock(Player onBallDefender) {
    	int total = getTeamDefense();
    	total -= onBallDefender.getAttributes()[6];
    	double outcome = Math.random() * total;
    	for(int i = 0; i < 5; i++) {
    		if(onCourt[i] == onBallDefender)
    			continue;
    		if(onCourt[i].getAttributes()[6] >= outcome) {
    			onCourt[i].addBlock();
    			return;
    		}
    		outcome -= onCourt[i].getAttributes()[6];
    	}
    }
    
    //substitute out the current onCourt players for the ones on the bench
    public void sub() {
    	for(int i = 0; i < 5; i++) {
    		if(players[i] == onCourt[i]) {
    			//starters are in
    			onCourt[i] = players[i+5];
    		}
    		else {
    			//bench is in
    			onCourt[i] = players[i];
    		}
    	}
    }
    
    public void addWin() {
    	wins++;
    }
    
    public void addLoss() {
    	losses++;
    }
    
    public int getWins() {
    	return wins;
    }
    
    public String getName() {
    	return name;
    }
    
    //distribute the minutes for a regular, non-overtime game
    //with my simplification, starters always play 36 mins, bench players play 12 mins
    public void distributeMinutes() {
    	for(int i = 0; i < 5; i++) {
    		players[i].addMinutes(36);
    		players[i+5].addMinutes(12);
    	}
    }
    
    //add minutes to the starters
    public void distributeOvertime() {
    	for(int i = 0; i < 5; i++) {
    		players[i].addMinutes(5);
    	}
    }
    
    //read the team from a text file containing the player ratings
    public static Team readFromFile(String filepath, String name_) {
    	List<String> playerStrings = null;
		try {
			playerStrings = Files.readAllLines(Paths.get(filepath), StandardCharsets.US_ASCII);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String initialRecord = playerStrings.get(0);
		String[] records = initialRecord.split(" ");
		int wins = Integer.parseInt(records[0]);
		int losses = Integer.parseInt(records[1]);
    	Player[] players = new Player[10];
    	for (int i = 1; i < playerStrings.size(); i++) {
			String str = playerStrings.get(i);
    		players[i-1] = Player.intializePlayer(str);
    	}
    	return new Team(players, wins, losses, name_);
    }
}