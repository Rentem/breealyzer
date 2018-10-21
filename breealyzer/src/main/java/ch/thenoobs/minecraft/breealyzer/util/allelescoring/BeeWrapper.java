package ch.thenoobs.minecraft.breealyzer.util.allelescoring;

import forestry.api.apiculture.IBee;

public class BeeWrapper {
	private IBee bee;
	private Object object;
	
	public BeeWrapper(IBee bee, Object object) {
		this.bee = bee;
		this.object = object;
	}
	
	public IBee getBee() {
		return bee;
	}
	public void setBee(IBee bee) {
		this.bee = bee;
	}
	public Object getObject() {
		return object;
	}
	public void setObject(Object object) {
		this.object = object;
	}
}
