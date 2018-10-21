package ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers;

import ch.thenoobs.minecraft.breealyzer.util.allelescoring.ScoringException;
import forestry.api.apiculture.IBee;
import forestry.api.genetics.IAlleleBoolean;
import forestry.api.genetics.IChromosomeType;

public class BooleanAlleleScorer extends AlleleScorer {
	
	public BooleanAlleleScorer(IChromosomeType chromosomeType) {
		super(chromosomeType);
	}
	
	@Override
	public float score(IBee bee)throws ScoringException {
		IAlleleBoolean allele = (IAlleleBoolean) bee.getGenome().getActiveAllele(getChromosomeType());
		float value = getAlleleValue(allele);
		
		allele = (IAlleleBoolean) bee.getGenome().getInactiveAllele(getChromosomeType());
		value = value + getAlleleValue(allele);
		

		value = value / 2;
		return value;
	}
	
	private float getAlleleValue(IAlleleBoolean allele) throws ScoringException {
		boolean value = allele.getValue();
		if (value) {
			return 1;
		}
		return 0;
	}

}
