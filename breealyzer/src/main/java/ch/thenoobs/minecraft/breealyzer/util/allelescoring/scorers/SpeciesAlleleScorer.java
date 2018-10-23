package ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers;

import ch.thenoobs.minecraft.breealyzer.util.allelescoring.ScoringException;
import forestry.api.apiculture.IBee;
import forestry.api.genetics.IAlleleSpecies;
import forestry.api.genetics.IChromosomeType;

public class SpeciesAlleleScorer extends AlleleScorer {
	String targetSpecies;
	
	public SpeciesAlleleScorer(IChromosomeType chromosomeType, String targetSpecies) {
		super(chromosomeType);
		this.targetSpecies = targetSpecies;
	}
	
	@Override
	public float score(IBee bee)throws ScoringException {
		IAlleleSpecies allele = (IAlleleSpecies) bee.getGenome().getActiveAllele(getChromosomeType());
		float value = getAlleleValue(allele);
		
		allele = (IAlleleSpecies) bee.getGenome().getInactiveAllele(getChromosomeType());
		value = value + getAlleleValue(allele);
		

		float fValue = value / (float)2;
		return fValue;
	}
	
	private float getAlleleValue(IAlleleSpecies allele) throws ScoringException {
		String value = allele.getName();
		if (value.equalsIgnoreCase(targetSpecies)) {
			return 1;
		}
		return 0;
	}

}
