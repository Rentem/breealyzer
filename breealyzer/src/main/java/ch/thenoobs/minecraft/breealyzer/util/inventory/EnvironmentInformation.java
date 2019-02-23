package ch.thenoobs.minecraft.breealyzer.util.inventory;

import java.util.List;

import forestry.api.core.EnumHumidity;
import forestry.api.core.EnumTemperature;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class EnvironmentInformation {

	private Biome biome;
	private EnumTemperature temperature;
	private float exactHumidity;
	private EnumHumidity humidity;
	private int blockLightValue;
	private Boolean isSkyVisible;
	private Boolean canWork;
	private List<BlockPos> flowerPositions;

	public Biome getBiome() {
		return this.biome;
	}

	public void setBiome(Biome biome) {
		this.biome = biome;
	}

	public int getBlockLightValue() {
		return this.blockLightValue;
	}

	public void setBlockLightValue(int blockLightValue) {
		this.blockLightValue = blockLightValue;
	}

	public EnumHumidity getHumidity() {
		return this.humidity;
	}

	public void setHumidity(EnumHumidity humidity) {
		this.humidity = humidity;
	}

	public float getExactHumidity() {
		return this.exactHumidity;
	}

	public void setExactHumidity(float exactHumidity) {
		this.exactHumidity = exactHumidity;
	}

	public EnumTemperature getTemperature() {
		return this.temperature;
	}

	public void setTemperature(EnumTemperature temperature) {
		this.temperature = temperature;
	}
	
	public Boolean getIsSkyVisible()
	{
		return this.isSkyVisible;
	}
	
	public void setIsSkyVisible(Boolean isSkyVisible) 	{
		this.isSkyVisible = isSkyVisible;
	}
	
	public Boolean getCanWork() {
		return this.canWork;
	}

	public void setCanWork(boolean canWork) {
		this.canWork = canWork;
	}
	
	public List<BlockPos> getFlowerPositions()
	{
		return this.flowerPositions;
	}
	
	public void setFlowerPositions(List<BlockPos> flowerPositions) {
		this.flowerPositions = flowerPositions;
	}
}
