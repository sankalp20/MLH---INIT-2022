public class Player {
    private String firstName;
    private String lastName;
    private int[] attributes;
    private int mins;
    private int points;
    private int assists;
    private int rebounds;
    private int blocks;
    private int steals;
    private int turnovers;
    private int shotAttempts;
    private int shotMakes;
    private int threePtAttempts;
    private int threePtMakes;
    private int freeThrowAttempts;
    private int freeThrowMakes;

    //construct a player object from names and attributes
    public Player(String firstName_, String lastName_, int[] attributes_) {
        firstName = firstName_;
        lastName = lastName_;
        attributes = attributes_;
        points = 0;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
    
    //determines whether the shooter will attempt to shoot or pass
    public int shotOrPass (Player defender, Team opposingTeam) {
    	double passFrequency = (attributes[3]*1.1/(attributes[0]+attributes[1]+attributes[2])) * 1.0;
    	double outcome = Math.random();
    	if(outcome < passFrequency) {
    		return 5;
    	}
    	return shot(defender, opposingTeam,0);
    }
    
    //shoots foul shots
    public int shootFoulShots(int numShots) {
    	int foulShotRating = attributes[8];
    	double foulShotPct = 0.45+0.05*foulShotRating;
    	boolean lastShotMissed = false;
    	int result = 0;
    	for(int i = 0; i < numShots; i++) {
    		double outcome = Math.random();
    		if(outcome < foulShotPct) {
    			result++;
    			freeThrowMakes++;
    			lastShotMissed = false;
    		}
    		else {
    			lastShotMissed = true;
    		}
    		freeThrowAttempts++;
    	}
    	points += result;
    	if(lastShotMissed) {
    		//have to return something weird so that the game knows it has to rebound
    		return 10+result;
    	}
    	//otherwise just return the number of points
    	return result;
    }
    
    //shoot a layup/drive, success depends on shooter ratings, defender ratings, and 
    public int shootClose(Player defender, Team opposingTeam, int passerRating) {
    	//calculate mathematical probabilities
    	int opponentOnBall = defender.getAttributes()[5];
    	int driveRating = attributes[0];
    	double drivingScore = 0.45 + (driveRating)*(0.025);
    	double defenderScore = 0.02*opponentOnBall;
		int oppTeam = opposingTeam.getTeamDefense() - defender.getAttributes()[6];
		double helpDefenseScore = oppTeam*0.005;
		double passerScore = passerRating * 0.01;
		double totalScore = drivingScore - defenderScore - helpDefenseScore + passerScore;
		double outcome = Math.random();
		
		//this indicates shot success, could be a foul or clean basket
		if(outcome < totalScore) {
			//assume we are fouled half the time
			double foulfrequency = 1.0/2;
			double foulOutcome = Math.random();
			if(foulOutcome < foulfrequency) {
				double andOnePct = (totalScore/3)*foulfrequency;
				//and one, count the basket and shoot a foul shot
				if(foulOutcome < andOnePct) {
					shotAttempts++;
					shotMakes++;
					points += 2;
					return 2+shootFoulShots(1);
				}
				else {
					return shootFoulShots(2);
				}
			}
			else {
				//normal make
				shotAttempts++;
	    		shotMakes++;
	    		points += 2;
	    		return 2;
			}
		}
		//means the on-ball defender blocked it
		else if(outcome < totalScore + defenderScore/2) {
			defender.addBlock();
			shotAttempts++;
    		return 0;
		}
		//means someone else on the other team blocked it
		else if(outcome < totalScore + defenderScore/2 + helpDefenseScore) {
			opposingTeam.distributeBlock(defender);
			shotAttempts++;
			return 0;
		}
		//otherwise normal miss
		else {
			shotAttempts++;
			return 0;
		}
    }
    
    //midrange shot, success depends on shooter and defender ratings
    public int shootMid(Player defender, Team opposingTeam, int passerRating) {
    	//calculate make frequency
    	int opponentOnBall = defender.getAttributes()[5];
    	int midrangeRating = attributes[1];
    	double baselineMid = 0.3 + midrangeRating * (0.25/10);
		double withDefense = baselineMid - 0.015*(opponentOnBall) + 0.075*passerRating;
		double outcome = Math.random();
		if(outcome < withDefense) {
			//make
			shotAttempts++;
    		shotMakes++;
    		points += 2;
    		return 2;
		}
		else {
			//miss
			shotAttempts++;
    		return 0;
		}
    }
    
    //three point shot, success depends on shooter and defender ratings
    public int shootThree(Player defender, Team opposingTeam, int passerRating) {
    	int opponentOnBall = defender.getAttributes()[5];
    	int threePtRating = attributes[2];
    	double baselineThree = 0.25 + threePtRating*(0.25/10);
		double withDefense = baselineThree - 0.015*(opponentOnBall) + 0.1*passerRating;
		double outcome = Math.random();
		if(outcome < withDefense) {
			//make
			shotAttempts++;
    		shotMakes++;
    		threePtAttempts++;
    		threePtMakes++;
    		points += 3;
    		return 3;
		}
		else {
			//miss
			shotAttempts++;
    		threePtAttempts++;
    		return 0;
		}		
    }
    
    
    //shoot and determine which type of shot we are shooting
    public int shot(Player defender, Team opposingTeam, int passerRating) {
    	//first, calculate turnover percentage
    	double outcome = Math.random();
    	int turnover = attributes[7];
    	int opponentOnBall = defender.getAttributes()[5];
    	double turnoverpct = 0.01*(opponentOnBall-turnover)+0.15;
    	if(outcome < turnoverpct/2) {
    		//normal turnover
    		turnovers++;
    		return -1;
    	}
    	if(outcome < turnoverpct) {
    		//turnover and fastbreak make for the other team
    		turnovers++;
    		defender.addSteal();
    		defender.addPoints();
    		defender.addPoints();
    		defender.addShot();
    		defender.addShotMake();
    		return -2;
    	}
    	//calculate frequencies for each type of shot
    	double driveRating = 1.25*attributes[0];
    	int midrangeRating = attributes[1];
    	int threePtRating = attributes[2];
    	double sum = (1.25)*driveRating + midrangeRating + threePtRating;
    	
    	double driveFrequency = (driveRating) * 1.0 / (sum);
    	
    	if(outcome < (1-turnoverpct)*driveFrequency + turnoverpct) {
    		return shootClose(defender, opposingTeam, passerRating);    		
    	}
    	double midRangeFreq = (midrangeRating) * 1.0 / sum;
    	if(outcome < (1-turnoverpct)*midRangeFreq + turnoverpct + driveFrequency){
    		return shootMid(defender, opposingTeam, passerRating);
    	}
    	
    	else {
    		return shootThree(defender, opposingTeam, passerRating);
    	}
    }
    
    public void addSteal () {
    	steals++;
    }
    
    public void addAssist() {
    	assists++;
    }
    
    public void addRebound() {
    	rebounds++;
    }
    
    public void addPoints() {
    	points++;
    }
    
    public void addBlock() {
    	blocks++;
    }
    
    public void addShot() {
    	shotAttempts++;
    }
    
    public void addShotMake() {
    	shotMakes++;
    }
    
    //reset stats for the next game
    public void resetStats() {
    	mins = 0;
    	points = 0;
        assists = 0;
        rebounds = 0;
        blocks = 0;
        steals = 0;
        turnovers = 0;
        shotAttempts = 0;
       	shotMakes = 0;
        threePtAttempts = 0;
        threePtMakes = 0;
        freeThrowAttempts = 0;
        freeThrowMakes = 0;
    }
    
    public void addMinutes(int mins_) {
    	mins += mins_;
    }
    
    public int[] getAttributes () {
    	return attributes;
    }
    
    public void printPlayer () {
    	System.out.printf("%12s%12s  %2d   %3d   %3d/%3d   %3d/%2d  %3d/%2d   %3d   %3d   %3d  %3d  %3d\n", firstName, lastName, mins, points,
    			shotMakes, shotAttempts, threePtMakes, threePtAttempts, freeThrowMakes, freeThrowAttempts, rebounds, assists, steals,  blocks, turnovers);
    }
    
    //initialize player from a string
    public static Player intializePlayer (String playerString) {
    	String[] tokens = playerString.split(" ");
    	int[] attributes = new int[tokens.length - 2];
    	for(int i = 2; i < tokens.length; i++) {
    		attributes[i-2] = Integer.parseInt(tokens[i]);
    	}
    	Player result = new Player(tokens[0], tokens[1], attributes);
    	return result;
    }
}