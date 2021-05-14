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
		Set<Piece> remaining = new HashSet<>();
		remaining.add(mrX.piece());
		for (Player detective: detectives) {
			remaining.add(detective.piece());
		}
		return new MyGameState(setup, ImmutableSet.copyOf(remaining), ImmutableList.of(), mrX, detectives);
	}

	private final static class MyGameState implements GameState {
		final private GameSetup setup;
		final private ImmutableSet<Piece> remaining;
		final private ImmutableList<LogEntry> log;
		final private Player mrX;
		final private List<Player> detectives;
		final private ImmutableSet<Move> moves;
		final private ImmutableSet<Piece> winner;

		private MyGameState(
				final GameSetup setup,
				final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives) {
			HashSet<String> uniqueDetectives = new HashSet<>();
			HashSet<Integer> uniqueLocations = new HashSet<>();
			this.setup = setup;
			if (this.setup.graph.nodes().isEmpty() && this.setup.graph.edges().isEmpty()) throw new
					IllegalArgumentException("Graph is empty");
			if (this.setup.rounds.isEmpty()) throw new IllegalArgumentException("Rounds is empty");
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			if (this.mrX == null) throw new NullPointerException("Mr.X is null");
			if (!this.mrX.isMrX()) throw new IllegalArgumentException("There is no Mr.X");
			this.detectives = detectives;
			if (this.detectives.isEmpty()) throw new IllegalArgumentException("There are no detectives");
			for (Player detective : detectives) {
				if (detective.piece() == null) throw new NullPointerException("Detective is null");
				if (detective.has(ScotlandYard.Ticket.DOUBLE)) throw new
						IllegalArgumentException("Detective has double ticket");
				if (detective.has(ScotlandYard.Ticket.SECRET)) throw new
						IllegalArgumentException("Detective has secret ticket");
				if (!uniqueDetectives.add(detective.piece().webColour())) throw new
						IllegalArgumentException("There are duplicate detectives");
				if (!uniqueLocations.add(detective.location())) throw new
						IllegalArgumentException("There are duplicate locations");
			}
			this.winner = getWinner();
			this.moves = getAvailableMoves();
		}

		public ImmutableSet<Piece> getRemaining() {
			return remaining;
		}

		@Override
		@Nonnull
		public GameSetup getSetup() {
			return setup;
		}

		@Override
		@Nonnull
		public ImmutableSet<Piece> getPlayers() {
			Set<Piece> players = new HashSet<>();
			players.add(mrX.piece());
			for (Player detective : detectives) {
				players.add(detective.piece());
			}
			return ImmutableSet.copyOf(players);
		}

		@Override
		@Nonnull
		public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
			for (Player player : detectives) {
				if (player.piece() == detective) {
					return Optional.of(Optional.of(player.location())).orElse(Optional.empty());
				}
			}
			return Optional.empty();
		}

		@Override
		@Nonnull
		public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			if (piece.isMrX()) {
				return Optional.of(new MyBoard(mrX));
			} else {
				for (Player detective : detectives) {
					if (detective.piece() == piece) {
						return Optional.of(new MyBoard(detective));
					}
				}
			}
			return Optional.empty();
		}

		@Override
		@Nonnull
		public ImmutableList<LogEntry> getMrXTravelLog() {
			return log;
		}

		@Override
		@Nonnull
		public ImmutableSet<Piece> getWinner() {
			List<Piece> winners = new ArrayList<>();
			ImmutableSet<Move> AvailableMoves = getAvailableMoves();
			ImmutableMap<ScotlandYard.Ticket, Integer> emptyTickets = ImmutableMap.of(TAXI, 0, BUS, 0, UNDERGROUND, 0, DOUBLE, 0, SECRET, 0);

			if(detectives.stream().anyMatch(p -> p.location() == mrX.location())) {
				for (Player detective : detectives) {
					winners.add(detective.piece());
				}
			} else if(detectives.stream().allMatch(p -> p.tickets().equals(emptyTickets))) {
				winners.add(mrX.piece());
			} else if (AvailableMoves.isEmpty() && !remaining.isEmpty()) {
				if (!remaining.contains(mrX.piece())) {
					winners.add(mrX.piece());
				} else if (remaining.contains(mrX.piece())) {
					for (Player detective : detectives) winners.add(detective.piece());
				}
			}
			else if(getAvailableMoves().isEmpty() && log.size() == setup.rounds.size()) {
				winners.add(mrX.piece());
			}

			return ImmutableSet.copyOf(winners);
		}

		@Override
		@Nonnull
		public ImmutableSet<Move> getAvailableMoves() {
			Set<Move> moves = new HashSet<>();
			if(winner == null || winner.isEmpty()){
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
				if (moves.isEmpty() && remaining.size() < detectives.size() && log.size() < setup.rounds.size()){
					moves.addAll(makeSingleMoves(setup, detectives, mrX, mrX.location()));
					moves.addAll(makeDoubleMoves(setup, detectives, mrX, mrX.location()));
				}
			}
			return ImmutableSet.copyOf(moves);

		}

		@Override
		public GameState advance(Move move) {
			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);

			Player newPlayer = null;
			Player newMrX = mrX;
			List<Player> newDetectives = new ArrayList<>();
			List<ScotlandYard.Ticket> ticketList;
			Set<Piece> newRemaining = new HashSet<>();

			ticketList = StreamSupport.stream(move.tickets().spliterator(), false)
					.collect(Collectors.toList());

			List<LogEntry> newLog = new ArrayList<>(log);

			int dest = move.visit(new Move.Visitor<>(){
				@Override
				public Integer visit(Move.SingleMove move) {
					return move.destination;
				}

				@Override
				public Integer visit(Move.DoubleMove move) {
					return move.destination2;
				}
			});

			if (remaining.contains(move.commencedBy())) {
				if (move.commencedBy().isMrX()) {
					newPlayer = updatePlayer(mrX, ticketList, dest, false);
				} else {
					for (Player detective : detectives) {
						if (move.commencedBy() == detective.piece()) {
							newPlayer = updatePlayer(detective, ticketList, dest, false);
							break;
						}
					}
					newMrX = updatePlayer(mrX, ticketList, mrX.location(), true);
				}
				assert newPlayer != null;
				if (newPlayer.piece().isMrX()) {
					for (Player detective : detectives) {
						newRemaining.add(detective.piece());
					}
					newMrX = newPlayer;

					if(!move.isDouble()) {
						if(setup.rounds.get(newLog.size())) {
							newLog.add(LogEntry.reveal(ticketList.get(0), newMrX.location()));
						} else {
							newLog.add(LogEntry.hidden(ticketList.get(0)));
						}
					} else if(move.isDouble()){
						if(setup.rounds.get(newLog.size())) {
							newLog.add(LogEntry.reveal(ticketList.get(0), ((Move.DoubleMove) move).destination1));
						} else {
							newLog.add(LogEntry.hidden(ticketList.get(0)));
						}
						if(setup.rounds.get(newLog.size())) {
							newLog.add(LogEntry.reveal(ticketList.get(1), newMrX.location()));
						} else {
							newLog.add(LogEntry.hidden(ticketList.get(1)));
						}
					}

				} else if(newPlayer.piece().isDetective()) {
	
					if (remaining.contains(mrX.piece())) {
						newRemaining.add(mrX.piece());
					}

					for (Player detective : detectives) {
						if (remaining.contains(detective.piece()) && detective.piece() !=
								newPlayer.piece()) {
							newRemaining.add(detective.piece());
						}
					}
				}
			}

			for (Player detective : detectives) {
				if (detective.piece() != move.commencedBy()) {
					newDetectives.add(detective);
				} else if (detective.piece() == move.commencedBy()) {
					newDetectives.add(updatePlayer(detective, ticketList, dest, false));
				}
			}

			if (newRemaining.isEmpty() && newLog.size() != setup.rounds.size()) newRemaining = getPlayers();

			return new MyGameState(setup, ImmutableSet.copyOf(newRemaining), ImmutableList.copyOf(newLog), newMrX, newDetectives);
		}


		public Player updatePlayer(Player player, List<ScotlandYard.Ticket> ticketList, int newLocation,
								   boolean isIncreasing) {
			int newTaxi = player.tickets().get(TAXI);
			int newBus = player.tickets().get(BUS);
			int newUnderground = player.tickets().get(UNDERGROUND);
			int newSecret = player.tickets().get(SECRET);
			int newDouble = player.tickets().get(DOUBLE);

			for (ScotlandYard.Ticket ticket : ticketList) {
				if (ticket == TAXI) {
					if (isIncreasing) newTaxi++;
					else newTaxi--;
				} else if (ticket == BUS) {
					if (isIncreasing) newBus++;
					else newBus--;
				} else if (ticket == UNDERGROUND) {
					if (isIncreasing) newUnderground++;
					else newUnderground--;
				} else if (ticket == SECRET) {
					newSecret--;
				} else if (ticket == DOUBLE) {
					newDouble--;
				}
			}

			return new Player(player.piece(), ImmutableMap.of(TAXI, newTaxi, BUS, newBus, UNDERGROUND, newUnderground,
					SECRET, newSecret, DOUBLE, newDouble), newLocation);
		}

		private final static class MyBoard implements TicketBoard {
			final private Player player;
			private MyBoard(final Player player) {
				this.player = player;
			}

			@Override
			public int getCount(@Nonnull ScotlandYard.Ticket ticket) {
				return player.tickets().get(ticket);
			}
		}

	}

	private static ImmutableSet<Move.SingleMove> makeSingleMoves(
			GameSetup setup,
			List<Player> detectives,
			Player player,
			int source){
		List<Move.SingleMove> singleMoves = new ArrayList<>();
		Set<Integer> toRemove = new HashSet<>();
		for(int destination : setup.graph.adjacentNodes(source)) {
			for (Player detective : detectives) {
				if (detective.location() == destination) toRemove.add(destination);
			}
			for(ScotlandYard.Transport t : Objects.requireNonNull(setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()))) {
				if(!toRemove.contains(destination) && player.has(t.requiredTicket()))
					singleMoves.add(new Move.SingleMove(player.piece(), source, t.requiredTicket(), destination));
			}
			if (player.has(SECRET)) {
				for(ScotlandYard.Transport t : Objects.requireNonNull(setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()))) {
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
			for (Move.SingleMove singleMove : dest1) {
				dest2 = List.copyOf(makeSingleMoves(setup, detectives, player, singleMove.destination));
				for (Move.SingleMove move : dest2) {
					if (singleMove.ticket == move.ticket && player.hasAtLeast(singleMove.ticket, 2))
						doubleMoves.add(new Move.DoubleMove(player.piece(), source, singleMove.ticket,
								singleMove.destination, move.ticket, move.destination));
					else if (singleMove.ticket != move.ticket)
						doubleMoves.add(new Move.DoubleMove(player.piece(), source, singleMove.ticket,
								singleMove.destination, move.ticket, move.destination));
				}
			}
		}
		return ImmutableSet.copyOf(doubleMoves);
	}
}