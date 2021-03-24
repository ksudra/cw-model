package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory  implements Factory<Model> {
	public ScotlandYard.Factory<Board.GameState> gameStateFactory;

	@Nonnull @Override public Model build(GameSetup setup,
	                                      Player mrX,
	                                      ImmutableList<Player> detectives) {
		gameStateFactory.build(setup, mrX, detectives);

		return new MyModel(gameStateFactory.build(setup, mrX, detectives), ImmutableSet.of());
	}

	private final class MyModel implements Model{
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
			return null;
		}

		@Override
		public void registerObserver(@Nonnull Observer observer) {

		}

		@Override
		public void unregisterObserver(@Nonnull Observer observer) {

		}

		@Nonnull
		@Override
		public ImmutableSet<Observer> getObservers() {
			return observers;
		}

		@Override
		public void chooseMove(@Nonnull Move move) {

		}
	}
}
