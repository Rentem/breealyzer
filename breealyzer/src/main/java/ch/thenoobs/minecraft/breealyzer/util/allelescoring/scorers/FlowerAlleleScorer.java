package ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers;

import java.util.HashMap;
import java.util.Map;

import ch.thenoobs.minecraft.breealyzer.util.allelescoring.ScoringException;
import forestry.api.apiculture.IBee;
import forestry.api.genetics.IAlleleFlowers;
import forestry.api.genetics.IChromosomeType;

public class FlowerAlleleScorer extends AlleleScorer {
	public static Long flowerMaxWeight = 5L;
	public static Map<String, Long> flowerValue = new HashMap<>();
	static {
		flowerValue.put("None".toLowerCase(), 5L);
		flowerValue.put("Rocks".toLowerCase(), 4L);
		flowerValue.put("Flowers".toLowerCase(), 3L);
		flowerValue.put("Leaves".toLowerCase(), 3L);
		flowerValue.put("Mushroom".toLowerCase(), 2L);
		flowerValue.put("Cacti".toLowerCase(), 1L);
		flowerValue.put("Exotic Flowers".toLowerCase(), 0L);
		flowerValue.put("Jungle".toLowerCase(), 0L);
		flowerValue.put("Wheat".toLowerCase(), 0L);
		flowerValue.put("Lily Pad".toLowerCase(), 0L);
		flowerValue.put("Lily Pads".toLowerCase(), 0L);
		flowerValue.put("End".toLowerCase(), 0L);
	}
	
	public FlowerAlleleScorer(IChromosomeType chromosomeType) {
		super(chromosomeType);
	}

	
	@Override
	public float score(IBee bee)throws ScoringException {
		IAlleleFlowers allele = (IAlleleFlowers) bee.getGenome().getActiveAllele(getChromosomeType());
		Long value = getAlleleValue(allele);
		
		allele = (IAlleleFlowers) bee.getGenome().getInactiveAllele(getChromosomeType());
		value = value + getAlleleValue(allele);
		

		float fValue = value / (float)(2*flowerMaxWeight);
		return fValue;
	}
	
	private Long getAlleleValue(IAlleleFlowers allele) throws ScoringException {
		String name = allele.getAlleleName().toLowerCase();
		Long value = flowerValue.get(name);
		if (value == null) {
			System.err.println("FlowerAlleceScorer, flower not found: " + name);
			return 0L;
		}
		return value;
	}

	
	public static float scoreBee(IBee bee, IChromosomeType chromosomeType) {
		IAlleleFlowers allele1 = (IAlleleFlowers) bee.getGenome().getActiveAllele(chromosomeType);		
		IAlleleFlowers allele2 = (IAlleleFlowers) bee.getGenome().getInactiveAllele(chromosomeType);
		

		String name1 = allele1.getAlleleName();
		String name2 = allele2.getAlleleName();
		
		if (name1.equalsIgnoreCase(name2)) {
			return 1;
		}
		return 0.5f;
	}
}
