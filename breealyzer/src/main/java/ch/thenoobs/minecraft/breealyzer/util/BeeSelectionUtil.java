package ch.thenoobs.minecraft.breealyzer.util;

import java.util.HashMap;
import java.util.Map;

import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.apiculture.IBee;
import forestry.apiculture.items.ItemBeeGE;

public class BeeSelectionUtil {
	public static final Map<String, Long> toleranceValues = new HashMap<>();
	static {
		toleranceValues.put("None", 0L);
		toleranceValues.put("Up 1", 1L);
		toleranceValues.put("Up 2", 2L);
		toleranceValues.put("Up 3", 3L);
		toleranceValues.put("Up 4", 4L);
		toleranceValues.put("Up 5", 5L);
		toleranceValues.put("Down 1", 1L);
		toleranceValues.put("Down 2", 2L);
		toleranceValues.put("Down 3", 3L);
		toleranceValues.put("Down 4", 4L);
		toleranceValues.put("Down 5", 5L);
		toleranceValues.put("Both 1", 5L);
		toleranceValues.put("Both 2", 6L);
		toleranceValues.put("Both 3", 7L);
		toleranceValues.put("Both 4", 8L);
		toleranceValues.put("Both 5", 10L);
	}
	
	public static final Map<String, Long> flowerValue = new HashMap<>();
	static {
		flowerValue.put("None", 5L);
		flowerValue.put("Rocks", 4L);
		flowerValue.put("Flowers", 3L);
		flowerValue.put("Mushroom", 2L);
		flowerValue.put("Cacti", 1L);
		flowerValue.put("Exotic Flowers", 0L);
		flowerValue.put("Jungle", 0L);
		flowerValue.put("Wheat", 0L);
		flowerValue.put("Lily Pad", 0L);
		flowerValue.put("End", 0L);
		flowerValue.put("Up 1", 1L);
		flowerValue.put("Up 2", 2L);
		flowerValue.put("Up 3", 3L);
		flowerValue.put("Up 4", 4L);
		flowerValue.put("Up 5", 5L);
		flowerValue.put("Down 1", 1L);
		flowerValue.put("Down 2", 2L);
		flowerValue.put("Down 3", 3L);
		flowerValue.put("Down 4", 4L);
		flowerValue.put("Down 5", 5L);
		flowerValue.put("Both 1", 5L);
		flowerValue.put("Both 2", 6L);
		flowerValue.put("Both 3", 7L);
		flowerValue.put("Both 4", 8L);
		flowerValue.put("Both 5", 10L);
	}

	
	public static int getBeeValue(IBee bee, Map<String, Long> traitWeight, IAlleleBeeSpecies targetSpecies) {
		int value = 0;
		bee.getGenome().getEffect();
//		bee.doEffect(storedData, housing)
		
		
		return value;		
	}
	
	public static int getSpeciesValue(IBee bee, IAlleleBeeSpecies targetSpecies) {
		int amount = 0;
		if (bee.getGenome().getPrimary().getUID().equals(targetSpecies.getUID())) {
			amount += 1;
		};
		if (bee.getGenome().getSecondary().getUID().equals(targetSpecies.getUID())) {
			amount += 1;
		};
		return amount;
	}
}
