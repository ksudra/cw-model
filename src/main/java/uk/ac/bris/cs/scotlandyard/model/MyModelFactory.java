package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory  implements Factory<Model> {
	MyGameStateFactory gameStateFactory = new MyGameStateFactory();

	@Nonnull @Override public Model build(GameSetup setup,
										  Player mrX,
										  ImmutableList<Player> detectives) {

		gameStateFactory.build(setup, mrX, detectives);
//		System.out.println(gameStateFactory.build(setup, mrX, detectives).getAvailableMoves());

		return new MyModel(gameStateFactory.build(setup, mrX, detectives), ImmutableSet.of());
	}

	private static final class MyModel implements Model{
		private Board.GameState state;
		private ImmutableSet<Observer> observers;
		private MyModel(final Board.GameState state,
						final ImmutableSet<Observer> observers) {
			this.state = state;
			this.observers = observers;
		}

		@Nonnull
		@Override
		public Board getCurrentBoard() {
			return new MyBoard(state);
		}

		@Override
		public void registerObserver(@Nonnull Observer observer) {
			if (observers.contains(observer)) throw new IllegalArgumentException("Observer already registered");
			Set<Observer> newObservers = new HashSet<>(observers);
			newObservers.add(observer);
			this.observers = ImmutableSet.copyOf(newObservers);
		}

		@Override
		public void unregisterObserver(@Nonnull Observer observer) {
//			if (observer ==)
			if (observer == null) throw new NullPointerException("Observer is null");
			if (!observers.contains(observer)) throw new IllegalArgumentException("Observer not registered");
			Set<Observer> newObservers = new HashSet<>(observers);
			newObservers.remove(observer);
			this.observers = ImmutableSet.copyOf(newObservers);
		}

		@Nonnull
		@Override
		public ImmutableSet<Observer> getObservers() {
			return observers;
		}

		@Override
		public void chooseMove(@Nonnull Move move) {
			this.state = state.advance(move);
			if(state.getWinner().isEmpty()) {
				for (Observer observer : this.observers) {
					observer.onModelChanged(getCurrentBoard(), Observer.Event.MOVE_MADE);
				}
			}
			if(!state.getWinner().isEmpty()){
				System.out.println("Game over");
				for (Observer observer : this.observers) {
					observer.onModelChanged(getCurrentBoard(), Observer.Event.GAME_OVER);
				}
			}
		}

		static final class MyBoard implements Board {
			private final GameState gameState;
			private MyBoard(GameState gameState) {
				this.gameState = gameState;
			}

			@Nonnull
			@Override
			public GameSetup getSetup() {
				return gameState.getSetup();
			}

			@Nonnull
			@Override
			public ImmutableSet<Piece> getPlayers() {
				return gameState.getPlayers();
			}

			@Nonnull
			@Override
			public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
				return gameState.getDetectiveLocation(detective);
			}

			@Nonnull
			@Override
			public Optional<TicketBoard> getPlayerTickets(Piece piece) {
				return gameState.getPlayerTickets(piece);
			}

			@Nonnull
			@Override
			public ImmutableList<LogEntry> getMrXTravelLog() {
				return gameState.getMrXTravelLog();
			}

			@Nonnull
			@Override
			public ImmutableSet<Piece> getWinner() {
				return gameState.getWinner();
			}

			@Nonnull
			@Override
			public ImmutableSet<Move> getAvailableMoves() {
				return gameState.getAvailableMoves();
			}
		}

	}
}