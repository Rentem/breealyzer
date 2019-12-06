package ch.thenoobs.minecraft.breealyzer.util.allelescoring;

import java.util.HashMap;
import java.util.Map;

import forestry.api.apiculture.IBee;
import forestry.api.genetics.IChromosomeType;

public class BeeScore {
	private BeeWrapper beeWrapper;
	private Map<IChromosomeType, Float> chromosomeScores = new HashMap<>();
	private float totalScore;	
	private float relativeScore;	
	
	public BeeScore(BeeWrapper beeWrapper) {
		this.beeWrapper = beeWrapper;
	}
	
	
	public Map<IChromosomeType, Float> getChromosomeScores() {
		return chromosomeScores;
	}
	
	public BeeWrapper getBeeWrapper() {
		return beeWrapper;
	}

	public IBee getBee() {
		return beeWrapper.getBee();
	}

	public float getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(float totalScore) {
		this.totalScore = totalScore;
	}

	public int getStackSize() {
		return this.getBeeWrapper().getItemStackAt().getStack().getCount();
	}

	public float getRelativeScore() {
		return relativeScore;
	}


	public void setRelativeScore(float relativeScore) {
		this.relativeScore = relativeScore;
	}
}
