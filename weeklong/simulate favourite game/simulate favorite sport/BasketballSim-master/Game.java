import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Game {
    private Team home_team;
    private Team away_team;
    private int away_score;
    private int home_score;
    private boolean homePos;
    private int secsLeft;
    private int overtimes;

    //initialize game w/ team objects
    public Game(Team home_team_, Team away_team_) {
        home_team = home_team_;
        away_team = away_team_;
        home_score = 0;
        away_score = 0;
        homePos = true;
        secsLeft = 48 * 60;
    }
    
    //initialize game given filenames rather than team objects
    public Game(String home_team_filepath, String home_name, String away_team_filepath, String away_name) {
    	home_team = Team.readFromFile(home_team_filepath, home_name);
    	away_team = Team.readFromFile(away_team_filepath, away_name);
    	home_score = 0;
        away_score = 0;
        homePos = true;
        secsLeft = 48 * 60;
    }
    
    //rebound a missed shot, team on defense has a higher chance to grab it
    public void rebound() {
    	int home_rebound = home_team.sumRebound();
		int away_rebound = away_team.sumRebound();
    	if(homePos) {
    		double randomRebound = Math.random() * (home_rebound + 3*away_rebound);
    		if(randomRebound < 3*away_rebound) {
    			//other team rebound
    			int rebounderPos = away_team.distributeRebound();
    			away_team.getPos(rebounderPos).addRebound();
    		}
    		else {
    			//offensive rebound
    			int rebounderPos = home_team.distributeRebound();
    			home_team.getPos(rebounderPos).addRebound();
    			//50/50 whether putback or kick out
    			double putback = Math.random();
    			if(putback < 0.5) {
    				Player rebounder = home_team.getPos(rebounderPos);
    				int putbackOutcome = rebounder.shootClose(away_team.getPos(rebounderPos), away_team,0);
    				if(putbackOutcome >= 10) {
    					home_score += putbackOutcome - 10;
    				}
    				else {
    					home_score += putbackOutcome;
    				}
    			}
    		}
    	}
    	else {
			double randomRebound = Math.random() * (3*home_rebound + away_rebound);
			if(randomRebound < 3*home_rebound) {
				//other team rebound
				int rebounderPos = home_team.distributeRebound();
				home_team.getPos(rebounderPos).addRebound();
			}
			else {
				int rebounderPos = away_team.distributeRebound();
				away_team.getPos(rebounderPos).addRebound();
				//50/50 whether putback or kick out
				double putback = Math.random();
				if(putback < 0.5) {
					Player rebounder = away_team.getPos(rebounderPos);
					int putbackOutcome = rebounder.shootClose(home_team.getPos(rebounderPos), home_team, 0);
					if(putbackOutcome >= 10) {
    					away_score += putbackOutcome - 10;
    				}
    				else {
    					away_score += putbackOutcome;
    				}
				}
			}
    	}

		
    }

    //runs through the simulation logic, team selects shooter -> shooter chooses shot, pass, turnover -> rebound possibly
    public void simPosession() {
    	if(homePos){
    		int shooterPos = home_team.getShooter();
    		Player shooter = home_team.getPos(shooterPos);
    		int outcome = shooter.shotOrPass(away_team.getPos(shooterPos), away_team);
    		if(outcome == -1) { // normal turnover
    			homePos = false;
    		}
    		else if(outcome == -2) { //turnover and fast-break
    			away_score += 2;
    		}
    		else if(outcome == 5) {
    			int newShooterPos = home_team.getRecipientOfPass(shooter);
    			Player newShooter = home_team.getPos(newShooterPos);
    			int passerRating = shooter.getAttributes()[3];
    			int newOutcome = newShooter.shot(away_team.getPos(newShooterPos), away_team, passerRating);
    			if(newOutcome == -1) { // normal turnover
        			homePos = false;
        		}
        		else if(newOutcome == -2) { //turnover and fast-break
        			away_score += 2;
        		}
        		else if(newOutcome == 0) {
        			rebound();
        			homePos = false;
        		}
        		else if(newOutcome >= 10) {
        			home_score += newOutcome-10;
        			rebound();
        			homePos = false;
        		}
        		else{
        			home_score += newOutcome;
        			shooter.addAssist();
        			homePos=false;
        		}
    		}
    		else { //shot attempt
    			if(outcome == 0) {
    				rebound();
    			}
    			else if(outcome >= 10) {
    				home_score += outcome-10;
    				rebound();
    			}
    			else {
    				home_score += outcome;
    			}
    			homePos = false;
    			
    		}
    	}
    	else {
    		int shooterPos = away_team.getShooter();
    		Player shooter = away_team.getPos(shooterPos);
    		int outcome = shooter.shotOrPass(home_team.getPos(shooterPos), home_team);
    		if(outcome == -1) { // normal turnover
    			homePos = true;
    		}
    		else if(outcome == -2) { //turnover and fast-break
    			home_score += 2;
    		}
    		else if(outcome == 5) {
    			int newShooterPos = away_team.getRecipientOfPass(shooter);
    			Player newShooter = away_team.getPos(newShooterPos);
    			int passerRating = shooter.getAttributes()[3];
    			int newOutcome = newShooter.shot(home_team.getPos(newShooterPos), home_team, passerRating);
    			if(newOutcome == -1) { // normal turnover
        			homePos = true;
        		}
        		else if(newOutcome == -2) { //turnover and fast-break
        			home_score += 2;
        		}
        		else if(newOutcome == 0) {
        			rebound();
        			homePos = true;
        		}
        		else if(newOutcome >= 10) {
        			away_score += newOutcome-10;
        			rebound();
        			homePos = true;
        		}
        		else{
        			away_score += newOutcome;
        			shooter.addAssist();
        			homePos=true;
        		}
    		}
    		else { //shot attempt
    			if(outcome == 0) {
    				rebound();
    			}
    			else if(outcome >= 10) {
    				away_score += outcome-10;
    				rebound();
    			}
    			else {
    				away_score += outcome;
    			}
    			homePos = true;
    		}
    	}
    }
    
    //simulate entire game, playing overtime if necessary and doing substitutions
    public void simGame () {
    	boolean subbed = false;
    	while(secsLeft > 0) {
    		simPosession();
    		secsLeft -= 16;
    		if(secsLeft < 60*12 && !subbed) {
    			subbed = true;
    			home_team.sub();
    			away_team.sub();
    		}
    	}
    	home_team.sub();
    	away_team.sub();
    	home_team.distributeMinutes();
    	away_team.distributeMinutes();
    	while(home_score == away_score) {
    		secsLeft = 5*60;
    		overtimes++;
    		while(secsLeft > 0) {
    			simPosession();
    			secsLeft -= 16;
    		}
    		away_team.distributeOvertime();
    		home_team.distributeOvertime();
    	}
    }
    
    //1 if home team wins, 0 if away wins
    public int getWinner() {
    	if(home_score > away_score)
    		return 1;
    	return 0;
    }
    
    public void printGame () {
    	if(overtimes > 0) {
    		System.out.println("Final Score (" + overtimes+ "OT): " + home_team.getName() + " " + home_score + " " + away_team.getName()+ " " + away_score);
    	}
    	else {
        	System.out.println("Final Score: " + home_team.getName() + " " + home_score + " " + away_team.getName()+ " " + away_score);
    	}
    	System.out.println(home_team.getName());
    	home_team.printTeam();
    	System.out.println("");
    	System.out.println(away_team.getName());
    	away_team.printTeam();
    }
    
    public static void main (String[] args) {
    	Team a = Team.readFromFile("Teams/bulls.txt", "bulls");
    	Team b = Team.readFromFile("Teams/warriors.txt", "warriors");
    	Game g = new Game(a,b);
    	g.simGame();
    	g.printGame();
	}
}