jacobian
========

A minimal computer contract bridge communication protocol and implementation.

The jacobian protocol
---------------------

The protocol supports four player programs and a host program.
The host communicates with the players via standard input/output.

Here is the protocol specified in order In EBNF. In addition,
every message should end with a newline. The number
of angle brackets signifies the number of such messages being sent
and the direction specifies whether the recipient is a player
or the host, e.g., `> go` means that `go\n` is sent to a single player.

    >>>> "jacobian"
    <<<< "id ", player_name
    >>>> "new" | "newbidding" | "newplay"

Assuming `newplay` mode (the only one currently implemented)...

    >>>> "seat ", "North" | "East" | "South" | "West"
    >>>> "vul ", "All" | "None" | "EW" | "NS"
    >>>> "hand ", hand
    >>>> "contract ", contract
    >    "go"
    <    "play ", card 
     >>> "show ", seat, " ", card
    >>>> "dummy ", hand
       > "go dummy"
       < "play ", card
    >>>  "show ", seat, " ", card
      >  "go"
      <  "play ", card
    >> > "show ", seat, " ", card
         ...

    player_name = ascii_character, { ascii_character }
    contract = level, strain, double_status, seat
    double_status = "" | "X" | "XX"
    hand = holding, ".", holding, ".", holding, ".", holding
    holding = { rank }
    card = suit, rank
    rank = "A" | "K" | "Q" | "J" | "T" | "9" | "8" | "7" | "6" | "5" | "4" | "3" | "2"
    seat = "N" | "E" | "S" | "W"
    strain = suit | "N"
    level = "1" | "2" | "3" | "4" | "5" | "6" | "7"
    suit = "S" | "H" | "D" | "C"

Currently no support for:

* communication error handling
* bidding
* different scoring/goals
* forming agreements
* disclosure

The jacobian host
-----------------

The host is an implementation of the jacobian protocol for Unix.

Building (not necessary):

    $ sh build.sh

Running:

    $ java -jar jacobian-host.jar 'player args' 'player' 'player' 'player' 3031333123220300123320212213021202022103101101003113 NONE 3NS

The deal specifier was taken adopted from Deal by Thomas Andrews. His explanation:
>It's a line of 52 numbers, where 0, 1, 2, and 3 mean north, east, south, and west, respectively.
>The first digit tells where the ace of spades goes, the second digit tells where the king of
>spades goes, etc.  The deal is numeric specifying the location for each of the 52 cards in order.

Vulnerabilities are EW, NS, NONE and ALL and some examples of contracts are 1NXW, 7SXXE.

References
----------

* [Universal Chess Interface](http://www.shredderchess.com/chess-info/features/uci-universal-chess-interface.html)
* [Chess Engine Communication Protocol](http://www.open-aurec.com/wbforum/WinBoard/engine-intf.html)
* [Portable Bridge Notation](http://www.tistis.nl/pbn/)
* [Networking of Computer Bridge Programs](http://www.bluechipbridge.co.uk/protocol.htm)
* [Deal Quickstart](http://bridge.thomasoandrews.com/deal/quickstart.html)
