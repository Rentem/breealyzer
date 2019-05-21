package ch.thenoobs.minecraft.breealyzer.util.allelescoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;

import ch.thenoobs.minecraft.breealyzer.util.Log;
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
import forestry.api.genetics.IChromosomeType;

public class BeeSelector {
	private Map<IChromosomeType, AlleleScorer> chromosomeScorers;
	private Map<IChromosomeType, ChromosomeWeight> chromosomeWeights;
	private List<IChromosomeType> weightSortedChromosomes;

	public ScoringResult selectBreedingPair(List<BeeWrapper> drones, List<BeeWrapper> princesses) {
		if (chromosomeScorers == null) {
			return null; // TODO exception?
		}
		if (chromosomeWeights == null) {
			return null; // TODO exception?
		}
		Log.info("---------Selecting from " + princesses.size() + " princesses and " + drones.size() + " drones");

		List<BeeScore> droneScores = drones.stream().map(BeeScore::new).collect(Collectors.toList());
		List<BeeScore> princessesScores = princesses.stream().map(BeeScore::new).collect(Collectors.toList());

		List<BeeScore> beeList = new ArrayList<>(droneScores.size() + princessesScores.size());
		beeList.addAll(droneScores);
		beeList.addAll(princessesScores);

		for (IChromosomeType chromosome : weightSortedChromosomes) {
			AlleleScorer scorer = chromosomeScorers.get(chromosome);
			scorer.socoreBees(beeList);
		}

		Log.info("---------princesses ");
		princessesScores = calculateAndSortBeeScoreList(princessesScores);

		Log.info("---------drones");
		droneScores = calculateAndSortBeeScoreListRelative(droneScores, princessesScores.get(0));

		BeeScore droneMax = droneScores.stream().reduce(this::getBeeScoreWithBiggerTotal).orElse(null);
		

		Log.info("max drone:");
		printBeeScore(droneMax);

		ScoringResult result = new ScoringResult();
		result.setSelectedPrincess(princessesScores.get(0));
		Log.info("--------- selected princess");
		printBeeScore(result.getSelectedPrincess());
		princessesScores.remove(0);

		Log.info("--------- selected drone");
		if (droneMax.getBeeWrapper().getItemStackAt().getStack().getCount() < 2) {
			Log.info("using max drone: low max count");
			result.setSelectedDrone(droneMax);
			droneScores.remove(droneMax);
		} else 
			if (droneMax.getTotalScore() < result.getSelectedPrincess().getTotalScore()) {
			Log.info("using max drone: better princess");
			result.setSelectedDrone(droneMax);
			droneScores.remove(droneMax);
		} else {
			if (droneMax.getTotalScore() - result.getSelectedPrincess().getTotalScore() > droneScores.get(0).getTotalScore()) {
				Log.info("using max drone: difference");
				result.setSelectedDrone(droneMax);
				droneScores.remove(droneMax);
			} else {
				result.setSelectedDrone(droneScores.get(0));
				droneScores.remove(0);
			}
		}

		printBeeScore(result.getSelectedDrone());
		result.setLeftoverDrones(droneScores);
		result.setLeftoverPrincesses(princessesScores);

		return result;
	}
	
	public boolean isBeePure(BeeScore beeScore) {
		for (IChromosomeType chrom : weightSortedChromosomes) {
			if (!(beeScore.getBee().getGenome().getActiveAllele(chrom) == beeScore.getBee().getGenome().getInactiveAllele(chrom))) {
				return false;
			}
		}
		return true;
	}

	public BeeWrapper selectBeeFromList(List<BeeWrapper> bees) {
		if (chromosomeScorers == null) {
			return null; // TODO exception?
		}
		if (chromosomeWeights == null) {
			return null; // TODO exception?
		}

		List<BeeScore> beeScores = bees.stream().map(BeeScore::new).collect(Collectors.toList());

		for (IChromosomeType chromosome : weightSortedChromosomes) {
			AlleleScorer scorer = chromosomeScorers.get(chromosome);
			scorer.socoreBees(beeScores);
		}

		return calculateAndSortBeeScoreList(beeScores).get(0).getBeeWrapper();
	}

	private List<BeeScore> calculateAndSortBeeScoreListRelative(List<BeeScore> beeScores, BeeScore compareScore) {
		List<BeeScore> sortedList = beeScores.stream().map(beeScore -> calculateRelativeScore(beeScore, compareScore)).map(this::calculateTotalScore).sorted(this::compareBeeScore)
				.peek(this::printBeeScore)
				// .peek(beeScore -> Log.info(String.format("%-15s%s%-15s",
				// beeScore.getBee().getDisplayName(), " ",
				// Float.toString(beeScore.getTotalScore())) + " -- " +
				// getBeeScoreString(beeScore)))
				.collect(Collectors.toList());
		return sortedList;
	}

	private List<BeeScore> calculateAndSortBeeScoreList(List<BeeScore> beeScores) {
		List<BeeScore> sortedList = beeScores.stream().map(this::calculateTotalScore).sorted(this::compareBeeScore).peek(this::printBeeScore).collect(Collectors.toList());
		return sortedList;
	}

	private void printBeeScore(BeeScore beeScore) {
		Log.info(String.format("%-15s%s%-15s", beeScore.getBee().getDisplayName() + "(" + beeScore.getBeeWrapper().getItemStackAt().getStack().getCount() + ")", " ", getTotalRelativeScoreString(beeScore)) + " -- " + getBeeScoreString(beeScore));
	}

	private static String getTotalRelativeScoreString(BeeScore beeScore) {
		return String.format("%10s%s%-10s", Float.toString(beeScore.getTotalScore()), "/", Float.toString(beeScore.getRelativeScore()));
	}

	private int compareBeeScore(BeeScore bee1, BeeScore bee2) {
		int relativeComp = Float.compare(bee2.getRelativeScore(), bee1.getRelativeScore());
		if (relativeComp != 0) {
			return relativeComp;
		}
		return Float.compare(bee2.getTotalScore(), bee1.getTotalScore());
	}

	private BeeScore getBeeScoreWithBiggerTotal(BeeScore bee1, BeeScore bee2) {
		if (Float.compare(bee2.getTotalScore(), bee1.getTotalScore()) > 0) {
			return bee2;
		}
		return bee1;
	}

	private String getBeeScoreString(BeeScore beeScore) {
		StringJoiner strJoiner = new StringJoiner(" ");
		for (IChromosomeType chrom : weightSortedChromosomes) {
			Float score = beeScore.getChromosomeScores().get(chrom);
			String out = String.format("%12s%s%-12s", chrom.getName(), ": ", Float.toString(score));
			// String out = String.format("%20s%s", score.getKey().getName(),": ");
			strJoiner.add(out);
			// strJoiner.add( + ": " + ));

		}
		return strJoiner.toString();
	}

	private BeeScore calculateTotalScore(BeeScore beeScore) {
		float total = beeScore.getChromosomeScores().entrySet().stream().map(entry -> calculateScore(entry.getValue(), getWeightFromMap(entry.getKey()))).reduce((f1, f2) -> f1 + f2).orElse((float) 0);

		beeScore.setTotalScore(total);
		return beeScore;
	}

	private BeeScore calculateRelativeScore(BeeScore beeScore, BeeScore compareScore) {
		float total = beeScore.getChromosomeScores().entrySet().stream().map(entry -> calculateRelativeScore(compareScore, entry)).reduce((f1, f2) -> f1 + f2).orElse((float) 0);

		beeScore.setRelativeScore(total);
		return beeScore;
	}

	private ChromosomeWeight getWeightFromMap(IChromosomeType cType) {
		ChromosomeWeight weight = chromosomeWeights.get(cType);
		if (weight == null) {
			System.err.println("No weight found for chromosome: " + cType.getName());
		}
		return weight;
	}

	private float calculateRelativeScore(BeeScore compareScore, Entry<IChromosomeType, Float> entry) {
		float compareRawScore = compareScore.getChromosomeScores().get(entry.getKey());
		float rawScore = entry.getValue();
		ChromosomeWeight weight = getWeightFromMap(entry.getKey());
		return calculateRelativeScore(rawScore, compareRawScore, weight);
	}

	private float calculateRelativeScore(float rawScore, float compareScore, ChromosomeWeight weight) {
		if (weight == null) {
			return 0;
		}

		float result;
		if (weight.isInverse()) {
			result = compareScore - rawScore;
		} else {
			result = rawScore - compareScore;
		}
		if (result < 0) {
			result = 0;
		}
		return result * weight.getWeight();
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

	public void initSortedChromosomes() {
		weightSortedChromosomes = chromosomeWeights.entrySet().stream().sorted(BeeSelector::getBiggerWeight).map(entry -> entry.getKey()).collect(Collectors.toList());
		StringJoiner stringJoiner = new StringJoiner(", ");
		for (IChromosomeType chrom : weightSortedChromosomes) {
			stringJoiner.add(chrom.toString() + ":" + chromosomeWeights.get(chrom).getWeight() + "(" + chromosomeWeights.get(chrom).isInverse() + ")");
		}
		Log.info("sorted chromosomes: " + stringJoiner.toString());

	}

	private static int getBiggerWeight(Entry<IChromosomeType, ChromosomeWeight> w1, Entry<IChromosomeType, ChromosomeWeight> w2) {
		// if (w1.getValue().getWeight() > w2.getValue().getWeight()) {
		// Log.info(w1.getKey()+ ":" + w1.getValue().getWeight() + ">" + w2.getKey()+
		// ":" + w2.getValue().getWeight());
		// return 1;
		// }
		return -Float.compare(w1.getValue().getWeight(), w2.getValue().getWeight());
		// return w1.getValue().getWeight().comw2.getValue().getWeight();
		// Log.info(w1.getKey()+ ":" + w1.getValue().getWeight() + "<" + w2.getKey()+
		// ":" + w2.getValue().getWeight());
		// return -1;
	}

	public void initWeights() {
		chromosomeWeights = new HashMap<>();
		chromosomeWeights.put(EnumBeeChromosome.FERTILITY, new ChromosomeWeight(100000, false));
		chromosomeWeights.put(EnumBeeChromosome.SPECIES, new ChromosomeWeight(4096, false));
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
		initSortedChromosomes();
	}

	public void initScoreres(String targetSpecies) {
		chromosomeScorers = new HashMap<>();
		if (targetSpecies != null) {
			chromosomeScorers.put(EnumBeeChromosome.SPECIES, new SpeciesAlleleScorer(EnumBeeChromosome.SPECIES, targetSpecies));
		}
		addSorter(EnumBeeChromosome.CAVE_DWELLING, BooleanAlleleScorer::new);
		addSorter(EnumBeeChromosome.NEVER_SLEEPS, BooleanAlleleScorer::new);
		addSorter(EnumBeeChromosome.TOLERATES_RAIN, BooleanAlleleScorer::new);
		addSorter(EnumBeeChromosome.TEMPERATURE_TOLERANCE, ToleranceAlleleScorer::new);
		addSorter(EnumBeeChromosome.HUMIDITY_TOLERANCE, ToleranceAlleleScorer::new);
		addSorter(EnumBeeChromosome.FERTILITY, IntegerAlleleScorer::new);
		addSorter(EnumBeeChromosome.FLOWERING, IntegerAlleleScorer::new);
		addSorter(EnumBeeChromosome.LIFESPAN, IntegerAlleleScorer::new);
		addSorter(EnumBeeChromosome.FLOWER_PROVIDER, FlowerAlleleScorer::new);
		addSorter(EnumBeeChromosome.EFFECT, BeeEffectAlleleScorer::new);
		addSorter(EnumBeeChromosome.TERRITORY, AreaAlleleScorer::new);
		addSorter(EnumBeeChromosome.SPEED, FloatAlleleScorer::new);
	}

	private void addSorter(IChromosomeType cType, Function<IChromosomeType, ? extends AlleleScorer> scorerConstructor) {
		chromosomeScorers.put(cType, scorerConstructor.apply(cType));
	}
}
