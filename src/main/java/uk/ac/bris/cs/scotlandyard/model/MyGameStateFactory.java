package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.Graph;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
import java.lang.Object;

import java.util.*;
import java.util.stream.Collectors;

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
			if(this.remaining.isEmpty()) throw new IllegalArgumentException("Pieces is empty");
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
				for (int i = 0; i < detectives.size(); i++) {
					if(detectives.get(i).location() == mrX.location()) {
						for (int j = 0; i < detectives.size(); j++) {
							winners.add(detectives.get(j).piece());
						}
					}
				}
				return ImmutableSet.copyOf(winners);
			}

			@Override
			public ImmutableSet<Move> getAvailableMoves() {
				return null;
			}

			@Override
			public GameState advance(Move move) {
				return null;
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

//	private static ImmutableSet<Move.SingleMove> makeSingleMoves(
//			GameSetup setup,
//			List<Player> detectives,
//			Player player,
//			int source){
//		final var singleMoves = new ArrayList<Move.SingleMove>();
//		ScotlandYard.Transport transport;
//		List edgeValues;
//		for(int destination : setup.graph.adjacentNodes(source)) {
//			// TODO find out if destination is occupied by a detective
//			//  if the location is occupied, don't add to the list of moves to return
//			for (int i = 0; i < detectives.size(); i ++) {
//				if(detectives.get(i).location() != destination) {
//					edgeValues = List.copyOf(setup.graph.edgeValue(source,
//							detectives.get(i).location()).orElse(null));
//					for(int j = 0; j < edgeValues.size(); j++) {
//						transport = (ScotlandYard.Transport) edgeValues.get(j);
//						if(player.has(transport.requiredTicket())) {
//							singleMoves.add(new Move.SingleMove(player.piece(), source, transport.requiredTicket(),
//									detectives.get(i).location()));
//						} else if (player.has(SECRET)) {
//
//						}
//					}
//				}
//			}
//		}
//		return ImmutableSet.copyOf(singleMoves);
//	}
}
