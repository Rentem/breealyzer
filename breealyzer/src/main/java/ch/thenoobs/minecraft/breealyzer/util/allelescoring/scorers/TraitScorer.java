package ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers;

import ch.thenoobs.minecraft.breealyzer.util.allelescoring.ScoringException;
import forestry.api.apiculture.IBee;
import forestry.api.genetics.IChromosomeType;

public interface TraitScorer {
		float score(IBee t, IChromosomeType u) throws ScoringException;	
}
