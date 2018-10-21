package ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers;

import java.util.HashMap;
import java.util.Map;

import ch.thenoobs.minecraft.breealyzer.util.allelescoring.ScoringException;
import forestry.api.apiculture.IAlleleBeeEffect;
import forestry.api.apiculture.IBee;
import forestry.api.genetics.IChromosomeType;

public class BeeEffectAlleleScorer extends AlleleScorer {
	public static Long effectMaxWeight = 10L;
	public static Map<String, Long> effectValues = new HashMap<>();
	static {
		effectValues.put("None".toLowerCase(), 5L);
		effectValues.put("Repulsion".toLowerCase(), 6L);
	}
	
	public BeeEffectAlleleScorer(IChromosomeType chromosomeType) {
		super(chromosomeType);
	}

	
	@Override
	public float score(IBee bee)throws ScoringException {
		IAlleleBeeEffect allele = (IAlleleBeeEffect) bee.getGenome().getActiveAllele(getChromosomeType());
		Long value = getAlleleValue(allele);
		
		allele = (IAlleleBeeEffect) bee.getGenome().getInactiveAllele(getChromosomeType());
		value = value + getAlleleValue(allele);
		

		float fValue = value / (2*effectMaxWeight);
		return fValue;
	}
	
	private Long getAlleleValue(IAlleleBeeEffect allele) throws ScoringException {
		String name = allele.getAlleleName().toLowerCase();
		Long value = effectValues.get(name);
		if (value == null) {
			System.err.println("BeeEffectAlleceScorer, toleranceIAlleleBeeEffect not found: " + name);
			return 0L;
		}
		return value;
	}

}
