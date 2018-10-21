package ch.thenoobs.minecraft.breealyzer.util.allelescoring;

public class ChromosomeWeight {
	private float weight;
	private boolean inverse;
	
	public ChromosomeWeight(float weight, boolean inverse) {
		this.weight = weight;
		this.inverse = inverse;
	}
	
	public float getWeight() {
		return weight;
	}
	public void setWeight(float weight) {
		this.weight = weight;
	}
	public boolean isInverse() {
		return inverse;
	}
	public void setInverse(boolean inverse) {
		this.inverse = inverse;
	}	
}
