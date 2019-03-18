package vekta.object.ship;

import processing.core.PVector;
import vekta.Counter;
import vekta.Faction;
import vekta.Player;
import vekta.RenderLevel;
import vekta.menu.Menu;
import vekta.menu.option.LootMenuOption;

import static vekta.Vekta.getWorld;
import static vekta.Vekta.v;

public class CargoShip extends Ship {
	private static final float DEF_MASS = 1000;
	private static final float DEF_RADIUS = 15;
	private static final float DEF_SPEED = .01F;
	private static final float DEF_TURN = 1;

	private float thrust;
	private float turn;

	private final Counter steerCt = new Counter();

	private final Faction faction;

	public CargoShip(String name, PVector heading, PVector position, PVector velocity, Faction faction) {
		super(name, heading, position, velocity, faction.getColor(), DEF_SPEED, DEF_TURN);

		this.faction = faction;
	}

	public Faction getFaction() {
		return faction;
	}

	@Override
	public float getMass() {
		return DEF_MASS;
	}

	@Override
	public float getRadius() {
		return DEF_RADIUS;
	}

	@Override
	public void onUpdate(RenderLevel level) {
		if(steerCt.cycle()) {
			steerCt.delay((int)v.random(10, 500));
			thrust = v.random(-1, 1);
			turn = v.random(-1, 1);
		}
		accelerate(thrust);
		turn(turn);
	}

	@Override
	public void setupDockingMenu(Player player, Menu menu) {
		menu.add(new LootMenuOption("Loot", player.getInventory(), getInventory()));

		for(EscortShip escort : getWorld().findObjects(EscortShip.class)) {
			if(escort.getTarget() == this) {
				escort.setTarget(player.getShip());
			}
		}
	}

	@Override
	public void drawNearby(float r) {
		drawShip(r, ShipModelType.CARGO_SHIP);
	}
}  
