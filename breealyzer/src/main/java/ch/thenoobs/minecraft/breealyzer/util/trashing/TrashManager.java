package ch.thenoobs.minecraft.breealyzer.util.trashing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.thenoobs.minecraft.breealyzer.util.InventoryHandler;
import ch.thenoobs.minecraft.breealyzer.util.InventoryUtil;
import ch.thenoobs.minecraft.breealyzer.util.ItemStackAt;
import ch.thenoobs.minecraft.breealyzer.util.Log;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.BeeScore;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.BeeWrapper;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.ScoringException;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers.AreaAlleleScorer;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers.BeeEffectAlleleScorer;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers.BooleanAlleleScorer;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers.FloatAlleleScorer;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers.FlowerAlleleScorer;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers.IntegerAlleleScorer;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers.SpeciesAlleleScorer;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers.ToleranceAlleleScorer;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers.TraitScorer;
import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.IAlleleBeeEffect;
import forestry.api.apiculture.IBee;
import forestry.api.genetics.IAlleleFlowers;
import forestry.api.genetics.IAlleleSpecies;
import forestry.api.genetics.IChromosomeType;

//TODO improve speed for deciding to keep bees (e.g. do not need to calc values everyTime)
public class TrashManager {
	public int numBeesToKeep = 5;
	private Map<IChromosomeType, BiFunction<BeeScore, Map<String, List<BeeScore>>, Boolean>> registerdGlobalTrashConditions = new HashMap<>();

	public TrashManager() {
		reqisterDefaultConditions();
	}

	public void trashBees(List<BeeScore> bees, InventoryHandler beeTrash, InventoryHandler seedBank) {
		Log.info("Trashing Bees");
		Map<Boolean, List<BeeScore>> sortedBees = sortBeesForTrashing(bees);
		if (sortedBees.containsKey(true)) {
			List<ItemStackAt> beeStacks = sortedBees.get(true).stream().map(BeeScore::getBeeWrapper).map(BeeWrapper::getItemStackAt).collect(Collectors.toList());
			Log.info("Keeping {} bees", beeStacks.size());
			InventoryUtil.moveItemStackAtsToTarget(beeStacks, seedBank);
		}
		if (sortedBees.containsKey(false)) {
			List<ItemStackAt> beeStacks = sortedBees.get(false).stream().map(BeeScore::getBeeWrapper).map(BeeWrapper::getItemStackAt).collect(Collectors.toList());
			Log.info("Trashing {} bees", beeStacks.size());
			InventoryUtil.moveItemStackAtsToTarget(beeStacks, beeTrash);
		}
	}

	/**
	 * false -> trash .......... true -> keep
	 * 
	 * @return
	 */
	public Map<Boolean, List<BeeScore>> sortBeesForTrashing(List<BeeScore> bees) {
		Map<IChromosomeType, Map<String, List<BeeScore>>> selectedBees = new HashMap<>();
		for (BeeScore bee : bees) {
			for (Entry<IChromosomeType, BiFunction<BeeScore, Map<String, List<BeeScore>>, Boolean>> entry : registerdGlobalTrashConditions.entrySet()) {
				selectedBees.putIfAbsent(entry.getKey(), new HashMap<String, List<BeeScore>>());
				entry.getValue().apply(bee, selectedBees.get(entry.getKey()));
			}

		}
		List<BeeScore> bestScores;
		if (bees.size() > 5) {
			 bestScores = bees.subList(0, 4);
		} else {
			bestScores = Collections.emptyList();
		}
		Stream<BeeScore> selectedBeeStream = selectedBees.values().stream().flatMap(m -> m.values().stream()).flatMap(List::stream);
		List<BeeScore> uniqueBees = Stream.concat(bestScores.stream(), selectedBeeStream).distinct().collect(Collectors.toList());
		return bees.stream().collect(Collectors.groupingBy(bee -> uniqueBees.contains(bee)));
	}

	public void reqisterDefaultConditions() {
		registerdGlobalTrashConditions.putIfAbsent(EnumBeeChromosome.TOLERATES_RAIN, this::selectBeeRain);
		registerdGlobalTrashConditions.putIfAbsent(EnumBeeChromosome.CAVE_DWELLING, this::selectBeeCave);
		registerdGlobalTrashConditions.putIfAbsent(EnumBeeChromosome.NEVER_SLEEPS, this::selectBeeNeverSleeps);
		registerdGlobalTrashConditions.putIfAbsent(EnumBeeChromosome.HUMIDITY_TOLERANCE, this::selectBeeHumidity);
		registerdGlobalTrashConditions.putIfAbsent(EnumBeeChromosome.TEMPERATURE_TOLERANCE, this::selectBeeTemperature);
		registerdGlobalTrashConditions.putIfAbsent(EnumBeeChromosome.FERTILITY, this::selectBeeFertilty);
		registerdGlobalTrashConditions.putIfAbsent(EnumBeeChromosome.FLOWERING, this::selectBeeFlowering);
		registerdGlobalTrashConditions.putIfAbsent(EnumBeeChromosome.LIFESPAN, this::selectBeeLifespan);
		registerdGlobalTrashConditions.putIfAbsent(EnumBeeChromosome.FLOWER_PROVIDER, this::selectBeeFlowerProvider);
		registerdGlobalTrashConditions.putIfAbsent(EnumBeeChromosome.EFFECT, this::selectBeeEffect);
		registerdGlobalTrashConditions.putIfAbsent(EnumBeeChromosome.TERRITORY, this::selectBeeTerritory);
		registerdGlobalTrashConditions.putIfAbsent(EnumBeeChromosome.SPEED, this::selectBeeSpeed);
		registerdGlobalTrashConditions.putIfAbsent(EnumBeeChromosome.SPECIES, this::selectBeeSpecies);

	}

	public Boolean selectBeeSpecies(BeeScore bee, Map<String, List<BeeScore>> selectedBees) {
		IAlleleSpecies allele1 = (IAlleleSpecies) bee.getBee().getGenome().getActiveAllele(EnumBeeChromosome.SPECIES);
		String name1 = allele1.getAlleleName();
		return selectBeeGeneric(bee, selectedBees, EnumBeeChromosome.SPECIES, SpeciesAlleleScorer::scoreBee, name1, false);
	}

	public Boolean selectBeeSpeed(BeeScore bee, Map<String, List<BeeScore>> selectedBees) {
		return selectBeeGenericDefault(bee, selectedBees, EnumBeeChromosome.SPEED, FloatAlleleScorer::scoreBee);
	}

	public Boolean selectBeeFlowerProvider(BeeScore bee, Map<String, List<BeeScore>> selectedBees) {
		IAlleleFlowers allele1 = (IAlleleFlowers) bee.getBee().getGenome().getActiveAllele(EnumBeeChromosome.FLOWER_PROVIDER);
		String name1 = allele1.getAlleleName();
		return selectBeeGeneric(bee, selectedBees, EnumBeeChromosome.FLOWER_PROVIDER, FlowerAlleleScorer::scoreBee, name1, false);
	}

	public Boolean selectBeeEffect(BeeScore bee, Map<String, List<BeeScore>> selectedBees) {
		IAlleleBeeEffect allele1 = (IAlleleBeeEffect) bee.getBee().getGenome().getActiveAllele(EnumBeeChromosome.EFFECT);
		String name1 = allele1.getAlleleName();
		return selectBeeGeneric(bee, selectedBees, EnumBeeChromosome.EFFECT, BeeEffectAlleleScorer::scoreBee, name1, false);
	}

	public Boolean selectBeeTerritory(BeeScore bee, Map<String, List<BeeScore>> selectedBees) {
		if (selectBeeGenericDefault(bee, selectedBees, EnumBeeChromosome.TERRITORY, AreaAlleleScorer::scoreBee)) {
			return true;
		}
		return selectBeeGeneric(bee, selectedBees, EnumBeeChromosome.TERRITORY, AreaAlleleScorer::scoreBee, "inverse", true);
	}

	public Boolean selectBeeLifespan(BeeScore bee, Map<String, List<BeeScore>> selectedBees) {
		if (selectBeeGenericDefault(bee, selectedBees, EnumBeeChromosome.LIFESPAN, IntegerAlleleScorer::scoreBee)) {
			return true;
		}
		return selectBeeGeneric(bee, selectedBees, EnumBeeChromosome.LIFESPAN, IntegerAlleleScorer::scoreBee, "inverse", true);
	}

	public Boolean selectBeeFlowering(BeeScore bee, Map<String, List<BeeScore>> selectedBees) {
		if (selectBeeGenericDefault(bee, selectedBees, EnumBeeChromosome.FLOWERING, IntegerAlleleScorer::scoreBee)) {
			return true;
		}
		return selectBeeGeneric(bee, selectedBees, EnumBeeChromosome.FLOWERING, IntegerAlleleScorer::scoreBee, "inverse", true);
	}

	public Boolean selectBeeFertilty(BeeScore bee, Map<String, List<BeeScore>> selectedBees) {
		return selectBeeGenericDefault(bee, selectedBees, EnumBeeChromosome.FERTILITY, IntegerAlleleScorer::scoreBee);
	}

	public Boolean selectBeeHumidity(BeeScore bee, Map<String, List<BeeScore>> selectedBees) {
		return selectBeeGenericDefault(bee, selectedBees, EnumBeeChromosome.HUMIDITY_TOLERANCE, ToleranceAlleleScorer::scoreBee);
	}

	public Boolean selectBeeTemperature(BeeScore bee, Map<String, List<BeeScore>> selectedBees) {
		return selectBeeGenericDefault(bee, selectedBees, EnumBeeChromosome.TEMPERATURE_TOLERANCE, ToleranceAlleleScorer::scoreBee);
	}

	public Boolean selectBeeRain(BeeScore bee, Map<String, List<BeeScore>> selectedBees) {
		return selectBeeGenericDefault(bee, selectedBees, EnumBeeChromosome.TOLERATES_RAIN, BooleanAlleleScorer::scoreBee);
	}

	public Boolean selectBeeCave(BeeScore bee, Map<String, List<BeeScore>> selectedBees) {
		return selectBeeGenericDefault(bee, selectedBees, EnumBeeChromosome.CAVE_DWELLING, BooleanAlleleScorer::scoreBee);
	}

	public Boolean selectBeeNeverSleeps(BeeScore bee, Map<String, List<BeeScore>> selectedBees) {
		return selectBeeGenericDefault(bee, selectedBees, EnumBeeChromosome.NEVER_SLEEPS, BooleanAlleleScorer::scoreBee);
	}

	public Boolean selectBeeGenericDefault(BeeScore bee, Map<String, List<BeeScore>> selectedBeesMap, IChromosomeType chromosomeType, TraitScorer scorer) {
		return selectBeeGeneric(bee, selectedBeesMap, chromosomeType, scorer, "default", false);
	}

	public Boolean selectBeeGeneric(BeeScore bee, Map<String, List<BeeScore>> selectedBeesMap, IChromosomeType chromosomeType, TraitScorer scorer, String key, boolean inverse) {
		try {
			float beeScore = scorer.score(bee.getBee(), chromosomeType);
			if (beeScore == 0) {
				return false;
			}
			selectedBeesMap.putIfAbsent(key, new ArrayList<>());
			List<BeeScore> selectedBees = selectedBeesMap.get(key);
			if (selectedBees.size() < numBeesToKeep) {
				selectedBees.add(bee);
				return true;
			}

			int index;
			for (index = numBeesToKeep - 1; index >= 0; index--) {
				float compBeeScore = scorer.score(selectedBees.get(index).getBee(), chromosomeType);
				if (inverse) {
					if (compBeeScore < beeScore) {
						break;
					}
				} else {
					if (compBeeScore > beeScore) {
						break;
					}
				}
			}
			if (index == numBeesToKeep - 1) {
				return false;
			}
			selectedBees.add(index + 1, bee);
			selectedBees.remove(numBeesToKeep);
			return true;
		} catch (ScoringException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

}
