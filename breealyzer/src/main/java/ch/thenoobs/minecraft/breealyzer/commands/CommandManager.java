package ch.thenoobs.minecraft.breealyzer.commands;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import ch.thenoobs.minecraft.breealyzer.util.InventoryHandler;
import ch.thenoobs.minecraft.breealyzer.util.InventoryUtil;
import ch.thenoobs.minecraft.breealyzer.util.ItemStackAt;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.BeeScore;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.BeeSelector;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.BeeWrapper;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.ScoringResult;

public class CommandManager {

	
	public static void purifyBee(List<ItemStackAt> drones, List<ItemStackAt> princesses, Consumer<List<BeeScore>> droneTrashMethod, InventoryHandler apiaryInventoryPair) {
		if (drones == null || princesses == null) {
			return;
		}
		
		BeeSelector selector = new BeeSelector();
		selector.initScoreres("Cultivated");
		selector.initWeights();
		
		ScoringResult scoringResult = selectBees(drones, princesses, selector);
		if (scoringResult.getLeftoverDrones().get(0).getRelativeScore() == 0 && scoringResult.getSelectedDrone().getRelativeScore() == 0 && selector.isBeePure(scoringResult.getSelectedDrone())) {
			return;
		}
		ItemStackAt selectedDrone = scoringResult.getSelectedDrone().getBeeWrapper().getItemStackAt();
		ItemStackAt selectedPrincess =  scoringResult.getSelectedPrincess().getBeeWrapper().getItemStackAt();

		InventoryUtil.moveItemAmount(apiaryInventoryPair, 0, selectedPrincess.getInventory(), selectedPrincess.getSlot(), 1);
		InventoryUtil.moveItemAmount(apiaryInventoryPair, 1, selectedDrone.getInventory(), selectedDrone.getSlot(), 1);
		
		droneTrashMethod.accept(scoringResult.getLeftoverDrones());
	}
	
	private static ScoringResult selectBees(List<ItemStackAt> drones, List<ItemStackAt> princesses, BeeSelector selector) {
		List<BeeWrapper> wrappedDrones = drones.stream().map(InventoryUtil::wrapBee).collect(Collectors.toList());
		List<BeeWrapper> wrappedPrincesses = princesses.stream().map(InventoryUtil::wrapBee).collect(Collectors.toList());
		ScoringResult results = selector.selectBreedingPair(wrappedDrones, wrappedPrincesses);
		return results;
	}


}
