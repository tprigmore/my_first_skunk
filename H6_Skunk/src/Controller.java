import java.util.ArrayList;
import edu.princeton.cs.introcs.*;

public class Controller
{
	private static final int SKUNK_PENALTY = 1;
	private static final int DOUBLE_SKUNK_PENALTY = 4;
	private static final int SKUNK_DEUCE_PENALTY = 2;
	private static final int SKUNK = 1;
	private static final int SKUNK_DEUCE = 3;
	private static final int DOUBLE_SKUNK = 2;
	
	public SkunkUI skunkUI;
	public UI ui;
	public int numberOfPlayers;
	public String[] playerNames;
	public ArrayList<Player> players;
	public int kitty;

	public Player activePlayer;
	public int activePlayerIndex;

	public boolean wantsToQuit;
	public boolean oneMoreRoll;

	public Dice skunkDice;

	public Controller(SkunkUI ui)
	{
		this.skunkUI = ui;
		this.ui = ui; // hide behind the interface UI
		
		this.playerNames = new String[20];
		this.players = new ArrayList<Player>();
		this.skunkDice = new Dice();
		this.wantsToQuit = false;
		this.oneMoreRoll = false;
	}

	public boolean run()
	{
		ui.println("Welcome to Skunk 0.47\n");

		gameSetup();
		activePlayerIndex = 0;
		activePlayer = players.get(activePlayerIndex);

		ui.println("Starting game...\n");
		boolean gameNotOver = true;
		boolean wantsToRoll;
		
		while (gameNotOver)
		{
			nextPlayer();
			
			normalTurn(wantsToRoll());

			gameNotOver = printTurnStats(gameNotOver);

			activePlayerIndex = (activePlayerIndex + 1) % numberOfPlayers;
			activePlayer = players.get(activePlayerIndex);

		}
		
		// last round: everyone but last activePlayer gets another shot

		ui.println("**** Last turn for all... ****");

		for (int i = activePlayerIndex, count = 0; count < numberOfPlayers-1; i = (i++) % numberOfPlayers, count++)
		{
			nextPlayerLastTurn();

			lastTurn(wantsToRoll());

			activePlayer.setTurnScore(activePlayer.getRollScore() + skunkDice.getLastRoll());
			ui.println("Final roll of " + skunkDice.toString() + ", giving final game score of "
					+ activePlayer.getRollScore());

		}

		printFinalStats();
		return true;
	}

	private void nextPlayerLastTurn()
	{
		ui.println("Last turn for player " + playerNames[activePlayerIndex] + "...");
		activePlayer.setTurnScore(0);
	}

	private void nextPlayer()
	{
		ui.println("Next player is " + playerNames[activePlayerIndex] + ".");
		activePlayer.setTurnScore(0);
	}

	private void printFinalStats()
	{
		int winner = 0;
		int winnerScore = 0;

		for (int player = 0; player < numberOfPlayers; player++)
		{
			Player nextPlayer = players.get(player);
			ui.println("Final game score for " + playerNames[player] + " is " + nextPlayer.getGameScore());
			if (nextPlayer.getGameScore() > winnerScore)
			{
				winner = player;
				winnerScore = nextPlayer.getGameScore();
			}
		}

		ui.println(
				"Game winner is " + playerNames[winner] + " with score of " + players.get(winner).getGameScore());
		players.get(winner).setNumberChips(players.get(winner).getNumberChips() + kitty);
		ui.println("Game winner earns " + kitty + " chips , finishing with " + players.get(winner).getNumberChips());

		ui.println("\nFinal scoreboard for this game:");
		ui.println("Player name -- Game score -- Total chips");
		ui.println("-----------------------");

		for (int pNumber = 0; pNumber < numberOfPlayers; pNumber++)
		{
			ui.println(playerNames[pNumber] + " -- " + players.get(pNumber).getGameScore() + " -- "
					+ players.get(pNumber).getNumberChips());
		}

		ui.println("-----------------------");
	}

	private boolean printTurnStats(boolean gameNotOver)
	{
		ui.println("End of turn for " + playerNames[activePlayerIndex]);
		ui.println("Score for this turn is " + activePlayer.getTurnScore() + ", added to...");
		ui.println("Previous game score of " + activePlayer.getGameScore());
		activePlayer.setGameScore(activePlayer.getGameScore() + activePlayer.getTurnScore());
		ui.println("Gives new game score of " + activePlayer.getGameScore());

		ui.println("");
		if (activePlayer.getGameScore() >= 100)
			gameNotOver = false;

		ui.println("Scoreboard: ");
		ui.println("Kitty has " + kitty + " chips.");
		ui.println("Player name -- Turn score -- Game score -- Total chips");
		ui.println("-----------------------");

		for (int i = 0; i < numberOfPlayers; i++)
		{
			ui.println(playerNames[i] + " -- " + players.get(i).getTurnScore() + " -- " + players.get(i).getGameScore()
					+ " -- " + players.get(i).getNumberChips());
		}
		ui.println("-----------------------");

		ui.println("Turn passes to right...");
		return gameNotOver;
	}

	private void normalTurn(boolean wantsToRoll)
	{
		while (wantsToRoll)
		{
			activePlayer.setRollScore(0);
			skunkDice.roll();
			if (skunkDice.getLastRoll() == DOUBLE_SKUNK)
			{
				ui.println("Two Skunks! You lose the turn, zeroing out both turn and game scores and paying 4 chips to the kitty");
				wantsToRoll = doScoring(DOUBLE_SKUNK_PENALTY);
				break;
			}
			else if (skunkDice.getLastRoll() == SKUNK_DEUCE)
			{
				ui.println(
						"Skunks and Deuce! You lose the turn, zeroing out the turn score and paying 2 chips to the kitty");
				wantsToRoll = doScoring(SKUNK_DEUCE_PENALTY);
				break;
			}
			else if (skunkDice.getDie1().getLastRoll() == SKUNK || skunkDice.getDie2().getLastRoll() == SKUNK)
			{
				ui.println("One Skunk! You lose the turn, zeroing out the turn score and paying 1 chip to the kitty");
				wantsToRoll = doScoring(SKUNK_PENALTY);
				break;

			}

			activePlayer.setRollScore(skunkDice.getLastRoll());
			activePlayer.setTurnScore(activePlayer.getTurnScore() + skunkDice.getLastRoll());
			ui.println(
					"Roll of " + skunkDice.toString() + ", gives new turn score of " + activePlayer.getTurnScore());

			wantsToRoll = wantsToRoll();

		}
	}

	private void lastTurn(boolean wantsToRoll)
	{
		while (wantsToRoll)
		{
			skunkDice.roll();
			ui.println("Roll is " + skunkDice.toString() + "\n");

			if (skunkDice.getLastRoll() == 2)
			{
				ui.println("Two Skunks! You lose the turn, zeroing out both turn and game scores and paying 4 chips to the kitty");
				wantsToRoll = doScoring(DOUBLE_SKUNK_PENALTY);
				break;
			}
			else if (skunkDice.getLastRoll() == 3)
			{
				ui.println(
						"Skunks and Deuce! You lose the turn, zeroing out the turn score and paying 2 chips to the kitty");
				wantsToRoll = doScoring(SKUNK_DEUCE_PENALTY);

			}
			else if (skunkDice.getDie1().getLastRoll() == 1 || skunkDice.getDie2().getLastRoll() == 1)
			{
				ui.println("One Skunk!  You lose the turn, zeroing out the turn score and paying 1 chip to the kitty");
				wantsToRoll = doScoring(SKUNK_PENALTY);
			}
			else
			{
				activePlayer.setTurnScore(activePlayer.getRollScore() + skunkDice.getLastRoll());
				ui.println("Roll of " + skunkDice.toString() + ", giving new turn score of "
						+ activePlayer.getTurnScore());

				ui.println("Scoreboard: ");
				ui.println("Kitty has " + kitty);
				ui.println("Player name -- Turn score -- Game score -- Total chips");
				ui.println("-----------------------");

				for (int pNumber = 0; pNumber < numberOfPlayers; pNumber++)
				{
					ui.println(playerNames[pNumber] + " -- " + players.get(pNumber).turnScore + " -- "
							+ players.get(pNumber).getGameScore() + " -- " + players.get(pNumber).getNumberChips());
				}
				ui.println("-----------------------");

				wantsToRoll = wantsToRoll();
			}

		}
	}

	private boolean wantsToRoll() {
		String wantsToRollStr = ui.promptReadAndReturn("Roll? y or n");
		boolean wantsToRoll = 'y' == wantsToRollStr.toLowerCase().charAt(0);

		return (wantsToRoll);

	}
	
	private boolean doScoring(int penalty)
	{
		kitty += penalty;
		activePlayer.setNumberChips(activePlayer.getNumberChips() - penalty);
		activePlayer.setTurnScore(0);
		if (penalty == DOUBLE_SKUNK_PENALTY)
			activePlayer.setGameScore(0);
		return(false);
	}

	private void gameSetup()
	{
		String numberPlayersString = skunkUI.promptReadAndReturn("How many players?");
		this.numberOfPlayers = Integer.parseInt(numberPlayersString);

		for (int playerNumber = 0; playerNumber < numberOfPlayers; playerNumber++)
		{
			ui.print("Enter name of player " + (playerNumber + 1) + ": ");
			playerNames[playerNumber] = StdIn.readLine();
			this.players.add(new Player(50));
		}
	}

}
