import java.io.*;

public class Host
{
	private Bridge.Deal deal;
	private String[] players;
	private Bridge.Vulnerability vulnerability;
	private Bridge.Contract contract;

	public void setDeal(Bridge.Deal deal) { this.deal = deal; }
	public void setPlayers(String[] players) { this.players = players; }
	public void setVulnerability(Bridge.Vulnerability vulnerability) { this.vulnerability = vulnerability; }
	public void setContract(Bridge.Contract contract) { this.contract = contract; }

	private static class PlayerProcess
	{
		private Process process = null;
		private BufferedReader in = null;
		private PrintWriter out = null;

		public PlayerProcess(String command)
		{
			try
			{
				final ProcessBuilder builder = new ProcessBuilder();
				builder.command().add("bash");
				builder.command().add("-c");
				builder.command().add("stdbuf -o0 " + command);
				builder.redirectErrorStream(true);
				this.process = builder.start();

				in = new BufferedReader(new InputStreamReader(this.process.getInputStream()));
				out = new PrintWriter(new OutputStreamWriter(this.process.getOutputStream()));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		public String communicate(String message) throws IOException
		{
			out.print(message);
			out.flush();
			System.out.print("> " + message);
			String s = in.readLine();
			System.out.println("< " + s);
			return s == "" ? "" : s + "\n";
		}
	}

	private String properVulnerability(String vul)
	{
		return vul == "NONE" ? "None"
			: vul == "ALL" ? "All"
			: vul;
	}

	public void run() throws IOException
	{
		final Host.PlayerProcess[] players = new Host.PlayerProcess[]{
			new Host.PlayerProcess(this.players[0]),
			new Host.PlayerProcess(this.players[1]),
			new Host.PlayerProcess(this.players[2]),
			new Host.PlayerProcess(this.players[3])};

		final Bridge.Hand[] hands = deal.getHands();
		final String[] ids = new String[4];
		for (int i = 0; i < 4; ++i)
		{
			final Host.PlayerProcess player = players[i];
			ids[i] = player.communicate("jacobian\n");
			player.communicate("newplay\n");
			player.communicate("seat " + Bridge.Seat.values()[i] + "\n");
			player.communicate("vul " + properVulnerability(this.vulnerability.toString()) + "\n");
			player.communicate("hand " + hands[i] + "\n");
			player.communicate("contract " + this.contract + "\n");
		}

		final Bridge.Seat declarer = this.contract.getDeclarer();
		final Bridge.Seat dummy = declarer.next().next();
		final Bridge.Strain trumps = this.contract.getStrain();

		Bridge.Seat toLead = declarer.next();
		int nDeclarerTricks = 0;
		for (int trick = 1; trick <= 13; ++trick)
		{
			// Play the trick
			Bridge.Card[] played = new Bridge.Card[4];
			for (int j = 0; j < 4; ++j)
			{
				Host.PlayerProcess cur = players[toLead.ordinal()];
				if (toLead == dummy)
					cur = players[declarer.ordinal()];
				played[toLead.ordinal()] = Bridge.Card.valueOf(cur.communicate("go" + (toLead == dummy ? " dummy\n" : "\n")).split("\\s+")[1]);

				for (Host.PlayerProcess player : players)
					if (player != cur)
						player.communicate("show " + toLead.getSymbol() + " " + played[toLead.ordinal()] + "\n");
				if (trick == 1 && j == 0)
					for (int i = 0; i < 4; ++i)
						players[i].communicate("dummy " + hands[dummy.ordinal()] + "\n");

				toLead = toLead.next();
			}

			// See who won the trick
			final Bridge.Suit ledSuit = played[toLead.ordinal()].getSuit();
			Bridge.Seat leader = toLead;
			Bridge.Card best = played[toLead.ordinal()];
			for (Bridge.Seat seat : Bridge.Seat.values())
			{
				Bridge.Card cur = played[seat.ordinal()];
				Bridge.Suit curSuit = cur.getSuit();
				if (curSuit.getSymbol() == trumps.getSymbol())
				{
					if (best.getSuit().getSymbol() != trumps.getSymbol() || cur.getRank().compareTo(best.getRank()) > 0)
					{
						leader = seat;
						best = cur;
					}
				}
				else if (curSuit == ledSuit && best.getSuit() == ledSuit && cur.getRank().compareTo(best.getRank()) > 0)
				{
					leader = seat;
					best = cur;
				}
			}

			// Put that person on lead
			toLead = leader;
			if (toLead == declarer || toLead == dummy)
				++nDeclarerTricks;
		}
		System.out.println("Declarer won " + nDeclarerTricks + " tricks!");
	}

	public static void main(String[] args) throws IOException
	{
		final Host host = new Host();
		final String[] players = new String[]
		{
			args[0], args[1], args[2], args[3]
		};
		host.setDeal(Bridge.Deal.fromNumeric(args[4]));
		host.setPlayers(players);
		host.setVulnerability(Bridge.Vulnerability.valueOf(args[5]));
		host.setContract(Bridge.Contract.fromText(args[6]));
		host.run();
	}
}
