package vekta.terrain;

import vekta.Player;
import vekta.menu.Menu;

public class OceanicTerrain extends Terrain {

	public OceanicTerrain() {
		addFeature("Oceanic");
		if(chance(.9F)) {
			addFeature("Atmosphere");
		}
	}

	@Override
	public String getOverview() {
		return "You can't find anywhere to land; a vast ocean covers this planet.";
	}

	@Override
	public void setupLandingMenu(Player player, Menu menu) {
	}
}
