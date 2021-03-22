package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;

/**
 * cw-model
 * Stage 1: Complete this class
 */

public final class MyGameStateFactory implements Factory<GameState> {
    @Nonnull @Override public GameState build(
            GameSetup setup,
            Player mrX,
            ImmutableList<Player> detectives) {
        return new MyGameState(setup, ImmutableSet.of(Piece.MrX.MRX), ImmutableList.of(), mrX, detectives);
    }

    //	private class that implements methods of the GameState interface.
    private final class MyGameState implements GameState {
        private GameSetup setup;
        private ImmutableSet<Piece> remaining;
        private ImmutableList<LogEntry> log;
        private Player mrX;
        private List<Player> detectives;
        private int CurrentRound;
        private ImmutableList<Player> everyone;
        private ImmutableSet<Move> moves;
        private ImmutableSet<Piece> winner;

        private MyGameState(
                final GameSetup setup,
                final ImmutableSet<Piece> remaining,
                final ImmutableList<LogEntry> log,
                final Player mrX,
                final List<Player> detectives) {
            HashSet<String> uniqueDetectives = new HashSet<>();
            HashSet<Integer> uniqueLocations = new HashSet<>();
            this.setup = setup;
            if(this.setup.graph.nodes().isEmpty() && this.setup.graph.edges().isEmpty()) throw new
                    IllegalArgumentException("Graph is empty");
            if(this.setup.rounds.isEmpty()) throw new IllegalArgumentException("Rounds is empty");
            this.remaining = remaining;
            this.log = log;
            this.mrX = mrX;
            if(this.mrX == null) throw new NullPointerException("Mr.X is null");
            if(!this.mrX.isMrX()) throw new IllegalArgumentException("There is no Mr.X");
            this.detectives = detectives;
            if(this.detectives.isEmpty()) throw new IllegalArgumentException("There are no detectives");
            for(int i = 0; i < detectives.size(); i++) {
                if(detectives.get(i).piece() == null) throw new NullPointerException("Detective is null");
                if(detectives.get(i).has(ScotlandYard.Ticket.DOUBLE)) throw new
                        IllegalArgumentException("Detective has double ticket");
                if(detectives.get(i).has(ScotlandYard.Ticket.SECRET)) throw new
                        IllegalArgumentException("Detective has secret ticket");
                if(!uniqueDetectives.add(detectives.get(i).piece().webColour())) throw new
                        IllegalArgumentException("There are duplicate detectives");
                if(!uniqueLocations.add(detectives.get(i).location())) throw new
                        IllegalArgumentException("There are duplicate locations");
            }
            this.moves = getAvailableMoves();
        }

        @Override
        public GameSetup getSetup() {
            return setup;
        }

        @Override
        public ImmutableSet<Piece> getPlayers() {
            Set<Piece> players = new HashSet<>();
            players.add(mrX.piece());
            for (int i = 0; i < detectives.size(); i++) {
                players.add(detectives.get(i).piece());
            }
            return ImmutableSet.copyOf(players);
        }

        @Override
        public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
            for(int i = 0; i < detectives.size(); i++) {
                if(detectives.get(i).piece() == detective) {
                    return Optional.of(Optional.of(detectives.get(i).location())).orElse(Optional.empty());
                }
            }
            return Optional.empty();
        }

        @Override
        public Optional<TicketBoard> getPlayerTickets(Piece piece) {
            if(piece.isMrX()) {
                return Optional.of(new MyBoard(mrX));
            } else {
                for(int i = 0; i < detectives.size(); i++) {
                    if(detectives.get(i).piece() == piece) {
                        return Optional.of(new MyBoard(detectives.get(i)));
                    }
                }
            }
            return Optional.empty();
        }

        @Override
        public ImmutableList<LogEntry> getMrXTravelLog() {
            return log;
        }

        //			MrX can't win
        @Override
        public ImmutableSet<Piece> getWinner() {
            List<Piece> winners = new ArrayList<>();
            ImmutableSet<Move> AvailableMoves = getAvailableMoves();
            for (int i = 0; i < detectives.size(); i++) {
                if(detectives.get(i).location() == mrX.location()) {
                    for (int j = 0; i < detectives.size(); j++) {
                        winners.add(detectives.get(j).piece());
                    }
                }
            }
            if(AvailableMoves.isEmpty()){
                if(!remaining.contains(mrX.piece())){
                    winners.add(mrX.piece());
                }else if(remaining.contains(mrX.piece())){
                    for (int i = 0; i < detectives.size(); i++)
                        winners.add(detectives.get(i).piece());
                }
            }
            return ImmutableSet.copyOf(winners);
        }

        @Override
        public ImmutableSet<Move> getAvailableMoves() {
            Set<Move> moves = new HashSet<>();
            if (remaining.contains(mrX.piece())) {
                moves.addAll(makeSingleMoves(setup, detectives, mrX, mrX.location()));
                moves.addAll(makeDoubleMoves(setup, detectives, mrX, mrX.location()));
            } else if (!remaining.contains(mrX.piece())) {
                for (int i = 0; i < detectives.size(); i++) {
                    if (remaining.contains(detectives.get(i).piece())) {
                        moves.addAll(makeSingleMoves(setup, detectives, detectives.get(i), detectives.get(i).location()));
                    }
                }
            }
                return ImmutableSet.copyOf(moves);

        }

        @Override
        public GameState advance(Move move) {
            for (int i = 0; i < getAvailableMoves().size(); i++) {
//			for (int j = 0; j < state.getAvailableMoves().size(); j++) {
//				System.out.println(getAvailableMoves().asList().get(i));
//
            }
            if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);

            Player newPlayer = null;
            Player newMrX = mrX;
            List<Player> newDetectives = new ArrayList<>();
            Map<ScotlandYard.Ticket, Integer> newTickets = new HashMap<>();
            Map<ScotlandYard.Ticket, Integer> mrXTickets = new HashMap<>();
            List<ScotlandYard.Ticket> ticketList = new ArrayList<>();
            List<LogEntry> newLog = new ArrayList<>();
            Set<Piece> newRemaining = new HashSet<>();

            ticketList = StreamSupport.stream(move.tickets().spliterator(), false)
                    .collect(Collectors.toList());

            newLog.addAll(log);
            CurrentRound = newLog.size();
//			for (int i = 0; i < log.size(); i++) {
//				System.out.println(newLog.get(i));
//			}
//			if (!newLog.isEmpty()) System.out.println(newLog.get(currentRound));
//			//System.out.println(setup.rounds.get(currentRound));
//			System.out.println(currentRound);
            //if (!newLog.isEmpty() && newLog.size() < setup.rounds.size()) System.out.println(setup.rounds.get(newLog.size() - 1));

            if (remaining.contains(move.commencedBy())) {
                if (!getAvailableMoves().contains(move)) throw new IllegalArgumentException("Illegal move: " + move);
                if (move.commencedBy().isMrX()) {
                    newPlayer = updatePlayer(mrX, ticketList, move.getDestination(), false);

                } else {
                    for (int i = 0; i < detectives.size(); i++) {
                        if (move.commencedBy() == detectives.get(i).piece()) {
                            newPlayer = updatePlayer(detectives.get(i), ticketList, move.getDestination(), false);

                        }
                    }
                    //newMrX = updatePlayer(mrX, ticketList, move.getDestination(), true);
                    newMrX = updatePlayer(mrX, ticketList, mrX.location(), true);
                }

//				System.out.println(move.getDestination());

                //if (!newLog.isEmpty()) System.out.println(setup.rounds.get(newLog.size()));

//				System.out.println(move.isMoveType());

                if (newPlayer.piece().isMrX()) {
//					System.out.println(setup.rounds.get(currentRound));
                    for (int i = 0; i < detectives.size(); i++) {
                        newRemaining.add(detectives.get(i).piece());
                        //newDetectives.add(detectives.get(i));
                    }
                    newMrX = newPlayer;



                    if(!move.isMoveType()) {
//						System.out.println(currentRound);
                        if(setup.rounds.get(CurrentRound)) {

                            //System.out.println(setup.rounds.get(log.size()));
                            newLog.add(LogEntry.reveal(ticketList.get(0), newMrX.location()));
                            //System.out.println(LogEntry.reveal(ticketList.get(0), newMrX.location()));
                        } else {
                            newLog.add(LogEntry.hidden(ticketList.get(0)));
                            //System.out.println(LogEntry.hidden(ticketList.get(0)));
                        }
//						System.out.println(currentRound);
                        CurrentRound++;
//						System.out.println(currentRound);
                    } else if(move.isMoveType()){
                        if(setup.rounds.get(CurrentRound)) {
                            newLog.add(LogEntry.reveal(ticketList.get(0), ((Move.DoubleMove) move).destination1));
                        } else {
                            newLog.add(LogEntry.hidden(ticketList.get(0)));
                        }

                        CurrentRound++;
                        if(setup.rounds.get(CurrentRound)) {
                            newLog.add(LogEntry.reveal(ticketList.get(1), newMrX.location()));
                        } else {
                            newLog.add(LogEntry.hidden(ticketList.get(1)));
                        }
                        CurrentRound++;
                    }

                } else if(newPlayer.piece().isDetective()) {
                    CurrentRound = log.size();
                    if (remaining.contains(mrX.piece())) {
                        newRemaining.add(mrX.piece());
                    }

                    for (int i = 0; i < detectives.size(); i++) {
                        if(remaining.contains(detectives.get(i).piece()) && detectives.get(i).piece() !=
                                newPlayer.piece()) {
                            newRemaining.add(detectives.get(i).piece());
                        }
                    }

                }

            }

            for (int i = 0; i < detectives.size(); i++) {
                if(detectives.get(i).piece() != move.commencedBy()) {
                    newDetectives.add(detectives.get(i));
                } else if (detectives.get(i).piece() == move.commencedBy()) {
                    //newDetectives.add(newPlayer);
                    newDetectives.add(updatePlayer(detectives.get(i), ticketList, move.getDestination(), false));
                }
            }

//			if (!newLog.isEmpty() && newLog.size() < setup.rounds.size()) System.out.println(setup.rounds.get(currentRound));

            for (int i = 0; i < newLog.size(); i++) {
//				System.out.println(newLog.get(i));
            }

//			System.out.println(currentRound);
            if (newRemaining.isEmpty()) newRemaining = getPlayers();
//			System.out.println(move);


            return new MyGameState(setup, ImmutableSet.copyOf(newRemaining), ImmutableList.copyOf(newLog), newMrX, newDetectives);
        }


        public Player updatePlayer(Player player, List<ScotlandYard.Ticket> ticketList, int newLocation,
                                   boolean isIncreasing) {
            int newTaxi = player.tickets().get(TAXI);
            int newBus = player.tickets().get(BUS);
            int newUnderground = player.tickets().get(UNDERGROUND);
            int newSecret = player.tickets().get(SECRET);
            int newDouble = player.tickets().get(DOUBLE);

            for (int i = 0; i < ticketList.size(); i++) {
                if (ticketList.get(i) == TAXI) {
                    if (isIncreasing) newTaxi++;
                    else newTaxi--;
                } else if (ticketList.get(i) == BUS) {
                    if (isIncreasing) newBus++;
                    else newBus--;
                } else if (ticketList.get(i) == UNDERGROUND) {
                    if(isIncreasing) newUnderground++;
                    else newUnderground--;
                } else if (ticketList.get(i) == SECRET) {
                    newSecret--;
                } else if (ticketList.get(i) == DOUBLE) {
                    newDouble--;
                }
            }

            return new Player(player.piece(), ImmutableMap.of(TAXI, newTaxi, BUS, newBus, UNDERGROUND, newUnderground,
                    SECRET, newSecret, DOUBLE, newDouble), newLocation);
        }

        private final class MyBoard implements TicketBoard {
            private Player player;
            private int taxi;
            private int bus;
            private int underground;
            private int x2;
            private int secret;
            private MyBoard(final Player player) {
                this.player = player;
                this.taxi = getCount(ScotlandYard.Ticket.TAXI);
                this.bus = getCount(ScotlandYard.Ticket.BUS);
                this.underground = getCount(ScotlandYard.Ticket.UNDERGROUND);
                this.x2 = getCount(ScotlandYard.Ticket.DOUBLE);
                this.secret = getCount(ScotlandYard.Ticket.SECRET);
            }

            @Override
            public int getCount(@Nonnull ScotlandYard.Ticket ticket) {
                return player.tickets().get(ticket);
            }
        }

    }

    private final class MyMove implements Move {
        private Piece piece;
        private DoubleMove doubleMove;
        private SingleMove singleMove;
        private int source;
        private int dest1;
        private int dest2;
        private boolean moveType;
        private ScotlandYard.Ticket ticket1;
        private ScotlandYard.Ticket ticket2;

        private MyMove(SingleMove singleMove) {
            this.singleMove = singleMove;
            this.moveType = singleMove.isMoveType();
            this.piece = commencedBy();
            this.source = singleMove.source;
            this.ticket1 = singleMove.ticket;
            this.dest1 = singleMove.destination;
        }

        private MyMove(DoubleMove doubleMove) {
            this.doubleMove = doubleMove;
            this.moveType = doubleMove.isMoveType();
            this.piece = commencedBy();
            this.ticket1 = doubleMove.ticket1;
            this.dest1 = doubleMove.destination1;
            this.ticket2 = doubleMove.ticket2;
            this.dest2 = doubleMove.destination2;
        }

        @Nonnull
        @Override
        public Piece commencedBy() {
            if (isMoveType()) return doubleMove.piece;
            else return singleMove.piece;
        }

        @Nonnull
        @Override
        public Iterable<ScotlandYard.Ticket> tickets() {
            if (isMoveType()) return doubleMove.tickets();
            else return singleMove.tickets();
        }

        @Override
        public int source() {
            if (isMoveType()) return doubleMove.source;
            else return source;
        }

        @Override
        public int getDestination() {
            if (isMoveType()) return doubleMove.getDestination();
            else return singleMove.getDestination();
        }

        @Override
        public boolean isMoveType() {
            return moveType;
        }

        @Override
        public <T> T visit(Visitor<T> visitor) {
            return null;
        }
    }


        private static ImmutableSet<Move.SingleMove> makeSingleMoves(
            GameSetup setup,
            List<Player> detectives,
            Player player,
            int source){
        final var singleMoves = new ArrayList<Move.SingleMove>();
        Set<Integer> toRemove = new HashSet<>();
        for(int destination : setup.graph.adjacentNodes(source)) {
            // TODO find out if destination is occupied by a detective
            //  if the location is occupied, don't add to the list of moves to return
            for (int i = 0; i < detectives.size(); i++) {
                if(detectives.get(i).location() == destination) toRemove.add(destination);
            }
            //
            for(ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(source,destination,ImmutableSet.of())) {
                // TODO find out if the player has the required tickets
                //  if it does, construct SingleMove and add it the list of moves to return
                if(!toRemove.contains(destination) && player.has(t.requiredTicket()))
                    singleMoves.add(new Move.SingleMove(player.piece(), source, t.requiredTicket(), destination));
            }
            // TODO consider the rules of secret moves here
            //  add moves to the destination via a secret ticket if there are any left with the player
            if (player.has(SECRET)) {
                for(ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(source,destination,ImmutableSet.of())) {
                    if(!toRemove.contains(destination) && t.requiredTicket() != SECRET)
                        singleMoves.add(new Move.SingleMove(player.piece(), source, SECRET, destination));
                }
            }
        }
        return ImmutableSet.copyOf(singleMoves);
    }

    private static ImmutableSet<Move.DoubleMove> makeDoubleMoves (
            GameSetup setup,
            List<Player> detectives,
            Player player,
            int source){
        final var doubleMoves = new ArrayList<Move.DoubleMove>();
        List<Move.SingleMove> dest1;
        List<Move.SingleMove> dest2;
        if(player.has(DOUBLE) && setup.rounds.size() > 1) {
            dest1 = List.copyOf(makeSingleMoves(setup, detectives, player, source));
            for(int i = 0; i < dest1.size(); i++) {
                dest2 = List.copyOf(makeSingleMoves(setup, detectives, player, dest1.get(i).destination));
                for (int j = 0; j < dest2.size(); j++) {
                    if (dest1.get(i).ticket == dest2.get(j).ticket && player.hasAtLeast(dest1.get(i).ticket, 2))
                        doubleMoves.add(new Move.DoubleMove(player.piece(), source, dest1.get(i).ticket,
                                dest1.get(i).destination, dest2.get(j).ticket, dest2.get(j).destination));
                    else if (dest1.get(i).ticket != dest2.get(j).ticket)
                        doubleMoves.add(new Move.DoubleMove(player.piece(), source, dest1.get(i).ticket,
                                dest1.get(i).destination, dest2.get(j).ticket, dest2.get(j).destination));
                }
            }
        }
        return ImmutableSet.copyOf(doubleMoves);
    }
}
