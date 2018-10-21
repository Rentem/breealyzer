package ch.thenoobs.minecraft.breealyzer.util.allelescoring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers.AlleleScorer;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers.AreaAlleleScorer;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers.BeeEffectAlleleScorer;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers.BooleanAlleleScorer;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers.FloatAlleleScorer;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers.FlowerAlleleScorer;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers.IntegerAlleleScorer;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers.SpeciesAlleleScorer;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers.ToleranceAlleleScorer;
import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.IBee;
import forestry.api.genetics.IChromosomeType;

public class BeeSelector {
	private Map<IChromosomeType, AlleleScorer> chromosomeScorers;
	private Map<IChromosomeType, ChromosomeWeight> chromosomeWeights;


	public BeeWrapper selectBeeFromList(List<BeeWrapper> bees) {
		if (chromosomeScorers == null) {
			return null; //TODO exception?
		}
		if (chromosomeWeights == null) {
			return null; //TODO exception?
		}

		List<BeeScore> beeScores = bees.stream().map(BeeScore::new).collect(Collectors.toList());


		for (AlleleScorer scorer : chromosomeScorers.values()) {
			scorer.socoreBees(beeScores);
		}

		BeeScore resultBee = beeScores.stream()
				.map(this::calculateTotalScore)
				.peek(beeScore -> System.out.println(beeScore.getBee().getDisplayName() + " " + beeScore.getTotalScore()))
				.reduce(this::returnBeeWithBiggerScore)
				.orElse(null);

		if (resultBee == null) {
			return null;
		}
		
		return resultBee.getBeeWrapper();
	}
	
	private BeeScore returnBeeWithBiggerScore(BeeScore bee1, BeeScore bee2)  {
		if (bee1.getTotalScore() < bee2.getTotalScore()) {
			return bee2;
		}
		return bee1;
	}

	private BeeScore calculateTotalScore(BeeScore beeScore) {
		float total = beeScore.getChromosomeScores().entrySet().stream()
				.map(entry -> calculateScore(entry.getValue(), getWeightFromMap(entry.getKey())))
				.reduce((f1,f2) -> f1 + f2).orElse((float) 0);
		
		beeScore.setTotalScore(total);
		return beeScore;
	}

	private ChromosomeWeight getWeightFromMap(IChromosomeType cType) {
		ChromosomeWeight weight = chromosomeWeights.get(cType);
		if (weight == null) {
			System.err.println("No weight found for chromosome: " + cType.getName());
		}
		return weight;
	}

	private float calculateScore(float rawScore, ChromosomeWeight weight) {
		if (weight == null) {
			return 0;
		}
		if (weight.isInverse()) {
			if (rawScore == 0) {
				return 1;
			}
			return (1 / rawScore) * weight.getWeight();
		} else {
			return rawScore * weight.getWeight();
		}
	}

	public void initWeights() {
		chromosomeWeights = new HashMap<>();
		chromosomeWeights.put(EnumBeeChromosome.SPECIES, new ChromosomeWeight(8192, false));
		chromosomeWeights.put(EnumBeeChromosome.FERTILITY, new ChromosomeWeight(4096, false));
		chromosomeWeights.put(EnumBeeChromosome.LIFESPAN, new ChromosomeWeight(2048, true));
		chromosomeWeights.put(EnumBeeChromosome.NEVER_SLEEPS, new ChromosomeWeight(1024, false));
		chromosomeWeights.put(EnumBeeChromosome.TOLERATES_RAIN, new ChromosomeWeight(512, false));
		chromosomeWeights.put(EnumBeeChromosome.CAVE_DWELLING, new ChromosomeWeight(128, false));
		chromosomeWeights.put(EnumBeeChromosome.SPEED, new ChromosomeWeight(64, false));
		chromosomeWeights.put(EnumBeeChromosome.FLOWER_PROVIDER, new ChromosomeWeight(32, false));
		chromosomeWeights.put(EnumBeeChromosome.TEMPERATURE_TOLERANCE, new ChromosomeWeight(16, false));
		chromosomeWeights.put(EnumBeeChromosome.HUMIDITY_TOLERANCE, new ChromosomeWeight(8, false));
		chromosomeWeights.put(EnumBeeChromosome.FLOWERING, new ChromosomeWeight(4, false));
		chromosomeWeights.put(EnumBeeChromosome.EFFECT, new ChromosomeWeight(2, false));
		chromosomeWeights.put(EnumBeeChromosome.TERRITORY, new ChromosomeWeight(1, false));
	}

	public void initScoreres(String targetSpecies) {		
		chromosomeScorers = new HashMap<>();
		if (targetSpecies != null) {
			chromosomeScorers.put(EnumBeeChromosome.SPECIES, new SpeciesAlleleScorer(EnumBeeChromosome.SPECIES, targetSpecies));
		}
		addSorter(EnumBeeChromosome.CAVE_DWELLING, BooleanAlleleScorer::new);
		addSorter(EnumBeeChromosome.EFFECT, BeeEffectAlleleScorer::new);
		addSorter(EnumBeeChromosome.FERTILITY, IntegerAlleleScorer::new);
		addSorter(EnumBeeChromosome.FLOWER_PROVIDER, FlowerAlleleScorer::new);
		addSorter(EnumBeeChromosome.FLOWERING, IntegerAlleleScorer::new);
		addSorter(EnumBeeChromosome.HUMIDITY_TOLERANCE, ToleranceAlleleScorer::new);
		addSorter(EnumBeeChromosome.LIFESPAN, IntegerAlleleScorer::new);
		addSorter(EnumBeeChromosome.NEVER_SLEEPS, BooleanAlleleScorer::new);
		addSorter(EnumBeeChromosome.SPEED, FloatAlleleScorer::new);
		addSorter(EnumBeeChromosome.TEMPERATURE_TOLERANCE, ToleranceAlleleScorer::new);
		addSorter(EnumBeeChromosome.TERRITORY, AreaAlleleScorer::new);
		addSorter(EnumBeeChromosome.TOLERATES_RAIN, BooleanAlleleScorer::new);
	}

	private void addSorter(IChromosomeType cType, Function<IChromosomeType, ? extends AlleleScorer> scorerConstructor) {
		chromosomeScorers.put(cType, scorerConstructor.apply(cType));
	}
}
