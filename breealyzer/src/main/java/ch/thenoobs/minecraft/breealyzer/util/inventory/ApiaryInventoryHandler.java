package ch.thenoobs.minecraft.breealyzer.util.inventory;

import ch.thenoobs.minecraft.breealyzer.util.InventoryHandler;
import forestry.apiculture.tiles.TileApiary;
import net.minecraftforge.items.IItemHandler;

public class ApiaryInventoryHandler extends InventoryHandler {
	private TileApiary tileEntity;

	public ApiaryInventoryHandler(TileApiary tileEntity, IItemHandler inventoryHandler) {
		super(tileEntity, inventoryHandler);
		
		this.tileEntity = tileEntity;
	}

	public TileApiary getTileEntity() {
		return this.tileEntity;
	}

	public EnvironmentInformation getEnvironment() {
		EnvironmentInformation environmentInformation = new EnvironmentInformation();

		environmentInformation.setBiome(this.tileEntity.getBiome());
		environmentInformation.setBlockLightValue(this.tileEntity.getBlockLightValue());
		environmentInformation.setHumidity(this.tileEntity.getHumidity());
		environmentInformation.setExactHumidity(this.tileEntity.getExactHumidity());
		environmentInformation.setTemperature(this.tileEntity.getTemperature());
		
		return environmentInformation;
	}
}
