import java.util.Arrays;
import java.util.Collections;

public class Bridge
{
	public static class Deal
	{
		private final Hand[] hands;
		public Deal(Hand[] hands)
		{
			this.hands = hands;
		}
		public static Deal fromNumeric(String numeric)
		{
			final int[] nLeft = new int[4];
			for (int i = 0; i < 52; ++i)
				++nLeft[numeric.charAt(i) - '0'];

			final Card[][] handsCards = new Card[4][];
			for (int i = 0; i < 4; ++i)
				handsCards[i] = new Card[nLeft[i]];
			for (int i = 0; i < 52; ++i)
			{
				int suitId = numeric.charAt(i) - '0';
				handsCards[suitId][--nLeft[suitId]] = Card.values()[51 - i];
			}

			final Hand[] hands = new Hand[4];
			for (int i = 0; i < 4; ++i)
				hands[i] = new Hand(handsCards[i]);

			return new Deal(hands);
		}
		public Hand[] getHands()
		{
			return this.hands;
		}
	}

	public static class Hand
	{
		private final Card[][] cardsBySuits;
		public Hand(Card[] cards)
		{
			final int[] nLeft = new int[4];
			for (Card card : cards)
				++nLeft[card.getSuit().ordinal()];

			final Card[][] cardsBySuits = new Card[4][];
			for (int i = 0; i < 4; ++i)
				cardsBySuits[i] = new Card[nLeft[i]];

			for (Card card : cards)
				cardsBySuits[card.getSuit().ordinal()][--nLeft[card.getSuit().ordinal()]] = card;
			for (int i = 0; i < 4; ++i)
				Arrays.sort(cardsBySuits[i], Collections.reverseOrder());
			this.cardsBySuits = cardsBySuits;
		}
		public Card[] getSuitHolding(Suit suit) { return cardsBySuits[suit.ordinal()]; }
		public Card[][] getCardsBySuits() { return this.cardsBySuits; }
		public String toString()
		{
			final StringBuilder s = new StringBuilder();
			for (int i = 3; i >= 0; --i)
			{
				final Card[] holding = getSuitHolding(Suit.values()[i]);
				for (Card card : holding)
					s.append(card.getRank().getSymbol());
				if (i != 0) s.append('.');
			}
			return s.toString();
		}
		public static Hand fromText(String text)
		{
			final Card[] cards = new Card[text.length() - 3];
			Suit curSuit = Suit.SPADES;
			int nInserted = 0;
			for (int i = 0; i < text.length(); ++i)
				if (text.charAt(i) == '.')
					curSuit = Suit.values()[(curSuit.ordinal() + 3) % 4];
				else
					cards[nInserted++] = Card.from(curSuit, Rank.fromSymbol(text.charAt(i)));

			return new Hand(cards);
		}
		public static String suitHoldingToString(Card[] cards)
		{
			final StringBuilder s = new StringBuilder();
			//Arrays.sort(cards, Collections.reverseOrder());
			for (int i = 0; i < cards.length; ++i)
				s.append(cards[i].getRank().getSymbol());
			return s.toString();
		}
		public Hand remove(Card card)
		{
			int count = 0;
			for (int i = 0; i < 4; ++i)
				count += cardsBySuits[i].length;
			final Card[] cards = new Card[count - 1];
			int nAdded = 0;
			for (int i = 0; i < 4; ++i)
				for (Card cur : cardsBySuits[i])
					if (cur != card)
						cards[nAdded++] = cur;
			return new Hand(cards);
		}
	}

	public static enum Card
	{
		C2,C3,C4,C5,C6,C7,C8,C9,CT,CJ,CQ,CK,CA,
		D2,D3,D4,D5,D6,D7,D8,D9,DT,DJ,DQ,DK,DA,
		H2,H3,H4,H5,H6,H7,H8,H9,HT,HJ,HQ,HK,HA,
		S2,S3,S4,S5,S6,S7,S8,S9,ST,SJ,SQ,SK,SA;
		private final Suit suit;
		private final Rank rank;
		private Card()
		{
			this.suit = Suit.fromSymbol(name().charAt(0));
			this.rank = Rank.fromSymbol(name().charAt(1));
		}
		public Suit getSuit() { return this.suit; }
		public Rank getRank() { return this.rank; }
		public static Card from(Suit suit, Rank rank)
		{
			return Card.valueOf("" + suit.getSymbol() + rank.getSymbol());
		}
		public static Card fromText(String text)
		{
			return Card.from(Suit.fromSymbol(text.charAt(0)), Rank.fromSymbol(text.charAt(1)));
		}
	}

	public static class Contract
	{
		private final int level;
		private final Strain strain;
		private final Doubling doubling;
		private final Seat declarer;

		public Contract(int level, Strain strain, Doubling doubling, Seat declarer)
		{
			this.level = level;
			this.strain = strain;
			this.doubling = doubling;
			this.declarer = declarer;
		}
		public int getLevel() { return this.level; }
		public Seat getDeclarer() { return this.declarer; }
		public Strain getStrain() { return this.strain; }
		public Doubling getDoubling() { return this.doubling; }
		public String toString()
		{
			return "" + this.level + this.strain.getSymbol() + this.doubling.getSymbol() + this.declarer.getSymbol();
		}
		public static Contract fromText(String text)
		{
			final int level = text.charAt(0) - '0';
			final Strain strain = Strain.fromSymbol(text.charAt(1));
			final Doubling doubling = Doubling.fromText(text.substring(2, text.length() - 1));
			final Seat seat = Seat.fromSymbol(text.charAt(text.length() - 1));
			return new Contract(level, strain, doubling, seat);
		}
	}

	public static enum Doubling
	{
		UNDOUBLED(""), DOUBLED("X"), REDOUBLED("XX");
		private String symbol;
		private Doubling(String symbol) { this.symbol = symbol; }
		public String getSymbol() { return this.symbol; }
		public static Doubling fromText(String text)
		{
			if (text.equals("X"))
				return Doubling.DOUBLED;
			else if (text.equals("XX"))
				return Doubling.REDOUBLED;
			return Doubling.UNDOUBLED;
		}
	}


	public static enum Vulnerability
	{
		ALL, NONE, EW, NS;
	}

	public static enum Seat
	{
		NORTH('N'), EAST('E'), SOUTH('S'), WEST('W');
		private char symbol;
		private Seat(char symbol) { this.symbol = symbol; }
		public Seat next()
		{
			return values()[(ordinal() + 1) % 4];
		}
		public char getSymbol() { return this.symbol; }
		public static Seat fromSymbol(char symbol)
		{
			for (int i = 0; i < values().length; ++i)
				if (symbol == values()[i].symbol)
					return values()[i];
			return null;
		}
	}

	public static enum Rank
	{
		TWO('2'), THREE('3'), FOUR('4'), FIVE('5'), SIX('6'), SEVEN('7'), EIGHT('8'), NINE('9'), TEN('T'), JACK('J'), QUEEN('Q'), KING('K'), ACE('A');
		private final char symbol;
		private Rank(char symbol) { this.symbol = symbol; }
		public static Rank fromSymbol(char symbol)
		{
			for (Rank rank : values())
				if (rank.symbol == symbol)
					return rank;
			return null;
		}
		public char getSymbol() { return this.symbol; }
	}

	public static enum Strain
	{
		CLUBS('C'), DIAMONDS('D'), HEARTS('H'), SPADES('S'), NOTRUMP('N');
		private final char symbol;
		private Strain(char symbol) { this.symbol = symbol; }
		public char getSymbol() { return this.symbol; }
		public static Strain fromSymbol(char symbol)
		{
			for (int i = 0; i < values().length; ++i)
				if (symbol == values()[i].symbol)
					return values()[i];
			return null;
		}
	}

	public static enum Suit
	{
		CLUBS('C'), DIAMONDS('D'), HEARTS('H'), SPADES('S');
		private final char symbol;
		private Suit(char symbol) { this.symbol = symbol; }
		public static Suit fromSymbol(char symbol)
		{
			for (Suit suit : values())
				if (suit.symbol == symbol)
					return suit;
			return null;
		}
		public char getSymbol() { return this.symbol; }
	}
}
