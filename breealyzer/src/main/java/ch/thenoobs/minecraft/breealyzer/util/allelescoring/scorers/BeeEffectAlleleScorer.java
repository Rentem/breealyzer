package ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers;

import java.util.HashMap;
import java.util.Map;

import ch.thenoobs.minecraft.breealyzer.util.Log;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.ScoringException;
import forestry.api.apiculture.IAlleleBeeEffect;
import forestry.api.apiculture.IBee;
import forestry.api.genetics.IChromosomeType;

public class BeeEffectAlleleScorer extends AlleleScorer {
	public static Long effectMaxWeight = 10L;
	public static Map<String, Long> effectValues = new HashMap<>();
	static {
		effectValues.put("None".toLowerCase(), 5L);
		effectValues.put("beatific".toLowerCase(), 7L);
		effectValues.put("fertile".toLowerCase(), 7L);
		effectValues.put("Repulsion".toLowerCase(), 6L);
		effectValues.put("radioact.".toLowerCase(), 1L);
		effectValues.put("freezing".toLowerCase(), 1L);
		effectValues.put("lightning".toLowerCase(), 1L);
		effectValues.put("water".toLowerCase(), 1L);
		
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
		

		float fValue = value / (float)(2*effectMaxWeight);
		return fValue;
	}
	
	private Long getAlleleValue(IAlleleBeeEffect allele) throws ScoringException {
		String name = allele.getAlleleName().toLowerCase();
		Long value = effectValues.get(name);
		if (value == null) {
			Log.warning(	"BeeEffectAlleScorer, BeeEffect not found: " + name);
			return 0L;
		}
		return value;
	}

	public static float scoreBee(IBee bee, IChromosomeType chromosomeType) {
		IAlleleBeeEffect allele1 = (IAlleleBeeEffect) bee.getGenome().getActiveAllele(chromosomeType);		
		IAlleleBeeEffect allele2 = (IAlleleBeeEffect) bee.getGenome().getInactiveAllele(chromosomeType);
		

		String name1 = allele1.getAlleleName();
		String name2 = allele2.getAlleleName();
		
		if (name1.equalsIgnoreCase(name2)) {
			return 1;
		}
		return 0.5f;
	}
	
}
