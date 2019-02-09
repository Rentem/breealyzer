package ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers;

import java.util.HashMap;
import java.util.Map;

import ch.thenoobs.minecraft.breealyzer.util.allelescoring.ScoringException;
import forestry.api.apiculture.IBee;
import forestry.api.genetics.IAlleleTolerance;
import forestry.api.genetics.IChromosomeType;

public class ToleranceAlleleScorer extends AlleleScorer {
	public static Long toleranceMaxWeight = 10L;
	public static Map<String, Long> toleranceValues = new HashMap<>();
	static {
		toleranceValues.put("None".toLowerCase(), 0L);
		toleranceValues.put("Up 1".toLowerCase(), 1L);
		toleranceValues.put("Up 2".toLowerCase(), 2L);
		toleranceValues.put("Up 3".toLowerCase(), 3L);
		toleranceValues.put("Up 4".toLowerCase(), 4L);
		toleranceValues.put("Up 5".toLowerCase(), 5L);
		toleranceValues.put("Down 1".toLowerCase(), 1L);
		toleranceValues.put("Down 2".toLowerCase(), 2L);
		toleranceValues.put("Down 3".toLowerCase(), 3L);
		toleranceValues.put("Down 4".toLowerCase(), 4L);
		toleranceValues.put("Down 5".toLowerCase(), 5L);
		toleranceValues.put("Both 1".toLowerCase(), 5L);
		toleranceValues.put("Both 2".toLowerCase(), 6L);
		toleranceValues.put("Both 3".toLowerCase(), 7L);
		toleranceValues.put("Both 4".toLowerCase(), 8L);
		toleranceValues.put("Both 5".toLowerCase(), 10L);
		toleranceValues.put("None".toLowerCase(), 0L);
		toleranceValues.put("Up_1".toLowerCase(), 1L);
		toleranceValues.put("Up_2".toLowerCase(), 2L);
		toleranceValues.put("Up_3".toLowerCase(), 3L);
		toleranceValues.put("Up_4".toLowerCase(), 4L);
		toleranceValues.put("Up_5".toLowerCase(), 5L);
		toleranceValues.put("Down_1".toLowerCase(), 1L);
		toleranceValues.put("Down_2".toLowerCase(), 2L);
		toleranceValues.put("Down_3".toLowerCase(), 3L);
		toleranceValues.put("Down_4".toLowerCase(), 4L);
		toleranceValues.put("Down_5".toLowerCase(), 5L);
		toleranceValues.put("Both_1".toLowerCase(), 5L);
		toleranceValues.put("Both_2".toLowerCase(), 6L);
		toleranceValues.put("Both_3".toLowerCase(), 7L);
		toleranceValues.put("Both_4".toLowerCase(), 8L);
		toleranceValues.put("Both_5".toLowerCase(), 10L);
	}
	
	public ToleranceAlleleScorer(IChromosomeType chromosomeType) {
		super(chromosomeType);
	}

	
	@Override
	public float score(IBee bee)throws ScoringException {
		return scoreBee(bee, getChromosomeType());
	}
	
	private static Long getAlleleValue(IAlleleTolerance allele) throws ScoringException {
		String name = allele.getValue().name().toLowerCase();
		Long value = toleranceValues.get(name);
		if (value == null) {
			System.err.println("ToleranceAlleceScorer, toleranceIAllele not found: " + name);
			return 0L;
		}
		return value;
	}
	

	public static float scoreBee(IBee bee, IChromosomeType chromeosomeType) throws ScoringException {
		IAlleleTolerance allele = (IAlleleTolerance) bee.getGenome().getActiveAllele(chromeosomeType);
		Long value = getAlleleValue(allele);
		
		allele = (IAlleleTolerance) bee.getGenome().getInactiveAllele(chromeosomeType);
		value = value + getAlleleValue(allele);
		

		float fValue = value / (float)(2*toleranceMaxWeight);
		return fValue;
	}

}
