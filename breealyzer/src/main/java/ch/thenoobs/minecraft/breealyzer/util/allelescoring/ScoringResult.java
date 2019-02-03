package ch.thenoobs.minecraft.breealyzer.util.allelescoring;

import java.util.List;

public class ScoringResult {
	private BeeScore selectedPrincess;
	private BeeScore selectedDrone;
	private List<BeeScore> leftoverPrincesses;
	private List<BeeScore> leftoverDrones;
	public BeeScore getSelectedPrincess() {
		return selectedPrincess;
	}
	public void setSelectedPrincess(BeeScore selectedPrincess) {
		this.selectedPrincess = selectedPrincess;
	}
	public BeeScore getSelectedDrone() {
		return selectedDrone;
	}
	public void setSelectedDrone(BeeScore selectedDrone) {
		this.selectedDrone = selectedDrone;
	}
	public List<BeeScore> getLeftoverPrincesses() {
		return leftoverPrincesses;
	}
	public void setLeftoverPrincesses(List<BeeScore> leftoverPrincesses) {
		this.leftoverPrincesses = leftoverPrincesses;
	}
	public List<BeeScore> getLeftoverDrones() {
		return leftoverDrones;
	}
	public void setLeftoverDrones(List<BeeScore> leftoverDrones) {
		this.leftoverDrones = leftoverDrones;
	}
	
	
	
}
