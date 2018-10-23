package ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers;

import ch.thenoobs.minecraft.breealyzer.util.allelescoring.ScoringException;
import forestry.api.apiculture.IBee;
import forestry.api.genetics.IAlleleArea;
import forestry.api.genetics.IChromosomeType;

public class AreaAlleleScorer extends AlleleScorer {

	private float maxValue = 0;
	
	public AreaAlleleScorer(IChromosomeType chromosomeType) {
		super(chromosomeType);
	}

	
	@Override
	public float score(IBee bee)throws ScoringException {
		IAlleleArea allele = (IAlleleArea) bee.getGenome().getActiveAllele(getChromosomeType());
		float value = getAlleleValue(allele);
		
		allele = (IAlleleArea) bee.getGenome().getInactiveAllele(getChromosomeType());
		value = value + getAlleleValue(allele);
		
		if (maxValue == 0) {
			return 0;
		}
		value = value / (float)(2*maxValue);
		return value;
	}
	
	private float getAlleleValue(IAlleleArea allele) throws ScoringException {
		float value = allele.getValue().getX()*allele.getValue().getY()*allele.getValue().getZ();
		if (value > maxValue) {
			maxValue = value;
			throw new ScoringException();
		}
		return value;
	}

}
