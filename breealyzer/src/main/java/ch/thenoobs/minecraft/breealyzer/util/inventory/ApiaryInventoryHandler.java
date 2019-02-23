package ch.thenoobs.minecraft.breealyzer.util.inventory;

import java.util.ArrayList;

import ch.thenoobs.minecraft.breealyzer.util.InventoryHandler;
import forestry.apiculture.tiles.TileApiary;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;

public class ApiaryInventoryHandler extends InventoryHandler {
	private TileApiary tileEntity;
	private EnvironmentInformation environmentInformation;

	public ApiaryInventoryHandler(TileApiary tileEntity, IItemHandler inventoryHandler) {
		super(tileEntity, inventoryHandler);
		
		this.tileEntity = tileEntity;		
	}

	public TileApiary getTileEntity() {
		return this.tileEntity;
	}

	public EnvironmentInformation getEnvironment() {
		if (this.environmentInformation == null) {
			this.environmentInformation = new EnvironmentInformation();
			this.environmentInformation.setBiome(this.tileEntity.getBiome());
			this.environmentInformation.setHumidity(this.tileEntity.getHumidity());
			this.environmentInformation.setExactHumidity(this.tileEntity.getExactHumidity());
			this.environmentInformation.setTemperature(this.tileEntity.getTemperature());
		}
		
		this.environmentInformation.setBlockLightValue(this.tileEntity.getBlockLightValue());
		this.environmentInformation.setIsSkyVisible(this.tileEntity.canBlockSeeTheSky());
				
		this.environmentInformation.setCanWork(this.tileEntity.getBeekeepingLogic().canWork());
		
		this.environmentInformation.setFlowerPositions(new ArrayList<BlockPos>(this.tileEntity.getBeekeepingLogic().getFlowerPositions()));	
		
		//this.environmentInformation.setHasFlowers(this.tileEntity.getBeekeepingLogic().getFlowerPositions().size() > 0);

		return environmentInformation;	
	}
}
	