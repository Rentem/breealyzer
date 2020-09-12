package ch.thenoobs.minecraft.breealyzer.util;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import ch.thenoobs.minecraft.breealyzer.util.allelescoring.BeeScore;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.BeeSelector;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.BeeWrapper;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.ScoringResult;
import ch.thenoobs.minecraft.breealyzer.util.inventory.AnalyzerInventoryHandler;
import ch.thenoobs.minecraft.breealyzer.util.inventory.ApiaryInventoryHandler;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.IBee;
import forestry.apiculture.items.ItemBeeGE;
import net.minecraft.item.ItemStack;

public class BeeUtil {
	private static int ANALYZER_MIN_AMOUNT_HONEY = 100;
	private static int ANALYZER_MIN_AMOUNT_POWER = 100;

	private BeeUtil() {
		throw new IllegalStateException("Utility class");
	}

	public static List<ItemStackAt> getUnAnalyzedBees(InventoryHandler beeInventory) {
		List<ItemStackAt> bees = InventoryUtil.getStacksOfType(beeInventory, ItemBeeGE.class);

		return bees.stream().filter(beeStack -> !getBeeFromISTAT(beeStack).isAnalyzed()).collect(Collectors.toList());
	}

	public static IBee getBeeFromISTAT(ItemStackAt iSTAT) {
		return getBeeFromStack(iSTAT.getStack());
	}

	public static IBee getBeeFromStack(ItemStack stack) {
		return ((ItemBeeGE) stack.getItem()).getIndividual(stack);
	}
	
	public static int fillAnalyzers(List<ItemStackAt> bees, InventoryHandler workChest, List<AnalyzerInventoryHandler> analyzers) {
		int cnt = bees.size() - 1;
		int totalAmount = 0;
		while (cnt >= 0) {
			InventoryHandler analyzer = analyzers.get(cnt % analyzers.size());
			int amount = fillAnalyzer(workChest, analyzer, bees.get(cnt).getSlot());
			totalAmount += amount; // TODO catch if analyzer is full
			cnt--;
		}
		return totalAmount;
	}

	public static int fillAnalyzer(InventoryHandler workChest, InventoryHandler analyzer, int sourceSlot) {
		final ItemStack stackToPull = workChest.getItemHandler().getStackInSlot(sourceSlot);
		if (stackToPull.isEmpty()) {
			return 0;
		}
		if (stackToPull.getItem() instanceof ItemBeeGE) {
			IBee bee = getBeeFromStack(stackToPull);
			if (bee.isAnalyzed()) {
				return 0;
			}
		} else {
			return 0;
		}
		int amount = stackToPull.getCount();
		for (int targetSlot = 0; targetSlot < analyzer.getItemHandler().getSlots(); targetSlot++) {
			if (InventoryUtil.moveStack(analyzer, targetSlot, workChest, sourceSlot)) {
				break;
			}
		}
		final ItemStack stackToPull2 = workChest.getItemHandler().getStackInSlot(sourceSlot);
		amount = amount - stackToPull2.getCount();
		return amount;
	}

	public static void clearApiaries(List<ApiaryInventoryHandler> apiaries, InventoryHandler workChestHandler, InventoryHandler lootInventoryHandler) {
		for (ApiaryInventoryHandler apiaryInventoryHandler : apiaries) {
			clearApiary(apiaryInventoryHandler, workChestHandler, lootInventoryHandler);
		}
		InventoryUtil.condenseItems(workChestHandler);
	}

	public static void clearApiary(ApiaryInventoryHandler apiaryInventoryHandler, InventoryHandler workChestHandler, InventoryHandler lootInventoryHandler) {
		for (int sourceSlot = 0; sourceSlot < apiaryInventoryHandler.getItemHandler().getSlots(); sourceSlot++) {
			clearApiary(apiaryInventoryHandler, sourceSlot, workChestHandler, lootInventoryHandler);
		}
	}

	public static void clearApiary(ApiaryInventoryHandler apiaryInventoryHandler, int sourceSlot, InventoryHandler workChestHandler, InventoryHandler lootInventoryHandler) {
		final ItemStack stackToPull = apiaryInventoryHandler.getItemHandler().getStackInSlot(sourceSlot);

		if (stackToPull.isEmpty()) {
			return;
		}

		InventoryHandler targetPair;

		if (stackToPull.getItem() instanceof ItemBeeGE) {
			targetPair = workChestHandler;
		} else {
			targetPair = lootInventoryHandler;
		}
		for (int targetSlot = 0; targetSlot < targetPair.getItemHandler().getSlots(); targetSlot++) {
			if (InventoryUtil.moveStack(targetPair, targetSlot, apiaryInventoryHandler, sourceSlot)) {
				break;
			}
		}
	}

	public static EnumBeeType getBeeTypeFromStackAtSlot(ItemStackAt iSTAT) {
		return ((ItemBeeGE) iSTAT.getStack().getItem()).getType();
	}

	public static int purifyBee(List<ItemStackAt> drones, List<ItemStackAt> princesses, Consumer<List<BeeScore>> droneTrashMethod, InventoryHandler apiaryInventoryPair, String targetSpecies) {
		if (drones == null || princesses == null) {
			return -1;
		}

		BeeSelector selector = new BeeSelector();
		selector.initScoreres(targetSpecies);
		selector.initWeights();

		ScoringResult scoringResult = selectBees(drones, princesses, selector);
		if (scoringResult.getLeftoverDrones().get(0).getRelativeScore() == 0 && scoringResult.getSelectedDrone().getRelativeScore() == 0 && selector.isBeePure(scoringResult.getSelectedDrone())) {
			if (scoringResult.getSelectedDrone().getBeeWrapper().getItemStackAt().getStack().getCount() > 5) {
				return 0;
			}
		}
		ItemStackAt selectedDrone = scoringResult.getSelectedDrone().getBeeWrapper().getItemStackAt();
		ItemStackAt selectedPrincess = scoringResult.getSelectedPrincess().getBeeWrapper().getItemStackAt();

		InventoryUtil.moveItemAmount(apiaryInventoryPair, 0, selectedPrincess.getInventory(), selectedPrincess.getSlot(), 1);
		InventoryUtil.moveItemAmount(apiaryInventoryPair, 1, selectedDrone.getInventory(), selectedDrone.getSlot(), 1);

		droneTrashMethod.accept(scoringResult.getLeftoverDrones());
		return 1;
	}

	private static ScoringResult selectBees(List<ItemStackAt> drones, List<ItemStackAt> princesses, BeeSelector selector) {
		List<BeeWrapper> wrappedDrones = drones.stream().map(BeeUtil::wrapBee).collect(Collectors.toList());
		List<BeeWrapper> wrappedPrincesses = princesses.stream().map(BeeUtil::wrapBee).collect(Collectors.toList());
		return selector.selectBreedingPair(wrappedDrones, wrappedPrincesses);
	}

	public static BeeWrapper wrapBee(ItemStackAt iSTAT) {
		BeeWrapper newWrapper = new BeeWrapper(BeeUtil.getBeeFromISTAT(iSTAT), iSTAT);
		return newWrapper;
	}

	public static Boolean isAnAnalyzerBusy(List<AnalyzerInventoryHandler> analyzers) {
		for (AnalyzerInventoryHandler handler : analyzers) {
			if (handler.getIsBusy()) {
				Log.info("One or more Analyzers are busy...");
				return true;
			}
		}
		return false;
	}

	public static Boolean areAnalyzersDone(List<AnalyzerInventoryHandler> analyzers) {
		for (AnalyzerInventoryHandler handler : analyzers) {
			if (handler.getIsBusy()) {
				Log.info("One or more Analyzers are busy...");
				return false;
			}
			if (InventoryUtil.hasItemInSlotRange(handler, 0, 5)) {
				if (handler.getTankFluidAmount() < ANALYZER_MIN_AMOUNT_HONEY) {
					Log.info("One or more Analyzers are missing honey...");
					return false;
				}
				if (handler.getTankFluidAmount() < ANALYZER_MIN_AMOUNT_POWER) {
					Log.info("One or more Analyzers are missing power...");
					return false;
				}
				if (InventoryUtil.hasItemInSlotRange(handler, 11, 11)) {
					Log.info("One or more Analyzers is full...");
					return false;
				}
				return false;
			}

		}

		return true;
	}

	public static IBee getBeeFromItemStack(ItemStackAt stack) {
		IBee bee = ((ItemBeeGE) stack.getStack().getItem()).getIndividual(stack.getStack());
		return bee;
	}
}
