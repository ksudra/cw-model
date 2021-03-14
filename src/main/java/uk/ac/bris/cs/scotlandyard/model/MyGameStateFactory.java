package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.Graph;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
import java.lang.Object;

import java.util.*;
import java.util.stream.Collectors;

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
//			if(this.detectives.stream().distinct().collect(Collectors.toList()) != this.detectives) throw new
//					IllegalArgumentException("There are duplicate detectives");
			for(int i = 0; i < detectives.size(); i++) {
				if(detectives.get(i).piece() == null) throw new NullPointerException("Detective is null");
				if(detectives.get(i).has(ScotlandYard.Ticket.DOUBLE)) throw new
						IllegalArgumentException("Detective has double ticket");
				if(detectives.get(i).has(ScotlandYard.Ticket.SECRET)) throw new
						IllegalArgumentException("Detective has secret ticket");
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
				int loc = 0;
				for(int i = 0; i < detectives.size(); i++) {
					if(detectives.get(i).piece() == detective) {
						loc = detectives.get(i).location();
					}
				}
				return Optional.of(Optional.of(loc)).orElse(Optional.empty());
			}

			@Override
			public Optional<TicketBoard> getPlayerTickets(Piece piece) {
				return Optional.empty();
			}

			@Override
			public ImmutableList<LogEntry> getMrXTravelLog() {
				return log;
			}

			@Override
			public ImmutableSet<Piece> getWinner() {
				return null;
			}

			@Override
			public ImmutableSet<Move> getAvailableMoves() {
				return null;
			}

			@Override
			public GameState advance(Move move) {
				return null;
			}
	}
}
