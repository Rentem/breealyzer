package ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers;

import ch.thenoobs.minecraft.breealyzer.util.allelescoring.ScoringException;
import forestry.api.apiculture.IBee;
import forestry.api.genetics.IAlleleFloat;
import forestry.api.genetics.IAlleleInteger;
import forestry.api.genetics.IChromosomeType;

public class FloatAlleleScorer extends AlleleScorer {

	private float maxValue = 0;
	
	public FloatAlleleScorer(IChromosomeType chromosomeType) {
		super(chromosomeType);
	}

	
	@Override
	public float score(IBee bee)throws ScoringException {
		IAlleleFloat allele = (IAlleleFloat) bee.getGenome().getActiveAllele(getChromosomeType());
		float value = getAlleleValue(allele);
		
		allele = (IAlleleFloat) bee.getGenome().getInactiveAllele(getChromosomeType());
		value = value + getAlleleValue(allele);
		
		if (maxValue == 0) {
			return 0;
		}
		value = value / (2*maxValue);
		return value;
	}
	
	private float getAlleleValue(IAlleleFloat allele) throws ScoringException {
		float value = allele.getValue();
		if (value > maxValue) {
			maxValue = value;
			throw new ScoringException();
		}
		return value;
	}
	
	public static float scoreBee(IBee bee, IChromosomeType chromeosomeType) throws ScoringException {
		IAlleleFloat allele = (IAlleleFloat) bee.getGenome().getActiveAllele(chromeosomeType);
		float value = allele.getValue();
		
		allele = (IAlleleFloat) bee.getGenome().getInactiveAllele(chromeosomeType);
		value = value + allele.getValue();
		
		return value;
	}

}
