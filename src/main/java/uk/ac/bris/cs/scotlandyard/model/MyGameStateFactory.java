package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

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
		// TODO
		throw new RuntimeException("Implement me!");

	}

//	private class that implements methods of the GameState interface.
	private final class MyGameState implements GameState {
		@Nonnull
		@Override
		public GameSetup getSetup() {
			return null;
		}

		@Nonnull
		@Override
		public ImmutableSet<Piece> getPlayers() {
			return null;
		}

		@Nonnull
		@Override
		public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
			return Optional.empty();
		}

		@Nonnull
		@Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			return Optional.empty();
		}

		@Nonnull
		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() {
			return null;
		}

		@Nonnull
		@Override
		public ImmutableSet<Piece> getWinner() {
			return null;
		}

		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			return null;
		}

		@Nonnull
		@Override
		public GameState advance(Move move) {
			return null;
		}

		//Method to read plain text graph, based on the method from worksheet 3
		ImmutableValueGraph<Integer, ScotlandYard.Transport> readGraph(String content) {
			List<String> lines = content.lines().collect(Collectors.toList());
			if (lines.isEmpty()) throw new IllegalArgumentException("No lines");
			int currentLine = 0;

			String[] topLine = lines.get(currentLine++).split(" ");
			int numberOfNodes = Integer.parseInt(topLine[0]);
			int numberOfEdges = Integer.parseInt(topLine[1]);

			ImmutableValueGraph.Builder<Integer, ScotlandYard.Transport> builder = ValueGraphBuilder
					.undirected()
					.expectedNodeCount(numberOfNodes)
					.immutable();


			for (int i = 0; i < numberOfNodes; i++) {
				String line = lines.get(currentLine++);
				if (line.isEmpty()) continue;
				builder.addNode(Integer.parseInt(line));
			}

//			Create a list of arrays to store all edges as undirected
			List<int[]> list = new ArrayList<>();
			int[] a= new int[2];

//			Adds all nodes to list of undirected edges.
			for (int i = 0; i < numberOfEdges; i++) {
				String line = lines.get(currentLine++);
				if (line.isEmpty()) continue;

				String[] s = line.split(" ");
				a[0] = Integer.parseInt(s[0]);
				a[1] = Integer.parseInt(s[1]);
				list.add(a);
			}

			for (int i = 0; i < numberOfEdges; i++) {
				String line = lines.get(currentLine++);
				if (line.isEmpty()) continue;

				String[] s = line.split(" ");
				ScotlandYard.Transport transport = null;
				a[0] = Integer.parseInt(s[0]);
				a[1] = Integer.parseInt(s[1]);
				list.add(a);

//				Determines the type of transport based on the plain text file
				if(s[2] == "Taxi") {
					transport = ScotlandYard.Transport.TAXI;
				} else  if (s[2] == "Bus") {
					transport = ScotlandYard.Transport.BUS;
				} else  if (s[2] == "Underground") {
					transport = ScotlandYard.Transport.UNDERGROUND;
				} else {
					transport = ScotlandYard.Transport.FERRY;
				}

//				Adds edge to graph if the line contains to nodes and an edge value.
				if (s.length != 3) throw new IllegalArgumentException("Bad edge line:" + line);
				builder.putEdgeValue(Integer.parseInt(s[0]),
						Integer.parseInt(s[1]),
						transport);
			}
			return builder.build();
		}
	}
}
