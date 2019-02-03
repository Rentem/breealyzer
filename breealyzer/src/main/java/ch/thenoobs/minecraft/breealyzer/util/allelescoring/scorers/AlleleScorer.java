package ch.thenoobs.minecraft.breealyzer.util.allelescoring.scorers;

import java.util.List;

import ch.thenoobs.minecraft.breealyzer.util.allelescoring.BeeScore;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.ScoringException;
import forestry.api.apiculture.IBee;
import forestry.api.genetics.IChromosomeType;

public abstract class AlleleScorer {
	protected IChromosomeType chromosomeType;
	

	public abstract float score(IBee bee) throws ScoringException;

	public AlleleScorer(IChromosomeType chromosomeType) {
		if (chromosomeType == null) {
			throw new NullPointerException();
		}
		this.chromosomeType = chromosomeType;
	}

	
	

	public void score(BeeScore score) throws ScoringException {
		score.getChromosomeScores().put(chromosomeType, score(score.getBee()));
	}

	public void socoreBees(List<BeeScore> beeScores) {
		int cnt = 0;
		while (cnt < beeScores.size()) {
			boolean recalculate = false;
			cnt++;
			for (BeeScore score : beeScores) {
				try {
					score(score);
				} catch (ScoringException e) {
					recalculate = true;
					break;
				}
			}
			if (!recalculate) {
				break;
			}
		}
	}

	public IChromosomeType getChromosomeType() {
		return chromosomeType;
	}
}
