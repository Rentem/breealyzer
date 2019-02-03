package ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers;

import ch.thenoobs.minecraft.breealyzer.util.allelescoring.ScoringException;
import forestry.api.apiculture.IBee;
import forestry.api.genetics.IAlleleBoolean;
import forestry.api.genetics.IAlleleInteger;
import forestry.api.genetics.IChromosomeType;

public class IntegerAlleleScorer extends AlleleScorer {

	private int maxValue = 0;
	
	public IntegerAlleleScorer(IChromosomeType chromosomeType) {
		super(chromosomeType);
	}

	
	@Override
	public float score(IBee bee)throws ScoringException {
		IAlleleInteger allele = (IAlleleInteger) bee.getGenome().getActiveAllele(getChromosomeType());
		int value = getAlleleValue(allele);
		
		allele = (IAlleleInteger) bee.getGenome().getInactiveAllele(getChromosomeType());
		value = value + getAlleleValue(allele);
		
		if (maxValue == 0) {
			return 0;
		}
		float fValue = value / (float)(2*maxValue);
		return fValue;
	}
	
	private int getAlleleValue(IAlleleInteger allele) throws ScoringException {
		int value = allele.getValue();
		if (value > maxValue) {
			maxValue = value;
			throw new ScoringException();
		}
		return value;
	}
	
	public static float scoreBee(IBee bee, IChromosomeType chromeosomeType) throws ScoringException {
		IAlleleInteger allele = (IAlleleInteger) bee.getGenome().getActiveAllele(chromeosomeType);
		int value = allele.getValue();
		
		allele = (IAlleleInteger) bee.getGenome().getInactiveAllele(chromeosomeType);
		value = value + allele.getValue();
		
		return value;
	}

}
