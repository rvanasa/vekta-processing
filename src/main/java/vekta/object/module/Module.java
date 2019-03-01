package vekta.object.module;

import vekta.object.Ship;

public interface Module {
	String getName();
	
	ModuleType getType();
	
	// TODO: show in UI
	boolean isBetter(Module other);
	
	default void accelerate(Ship ship, float amount) {}

	default void turn(Ship ship, float amount) {}
	
	default void keyPress(Ship ship, char key) {}
	
	// TODO: add additional module hooks here
}
