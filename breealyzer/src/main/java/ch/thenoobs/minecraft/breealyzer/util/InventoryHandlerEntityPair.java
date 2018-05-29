package ch.thenoobs.minecraft.breealyzer.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;

public class InventoryHandlerEntityPair {
	private TileEntity tileEntity;
	private IItemHandler inventoryHandler;


	InventoryHandlerEntityPair(TileEntity tileEntity, IItemHandler inventoryHandler) {
		this.tileEntity = tileEntity;
		this.inventoryHandler = inventoryHandler;
	}
	
	public TileEntity getTileEntity() {
		return tileEntity;
	}

	public void setTileEntity(TileEntity tileEntity) {
		this.tileEntity = tileEntity;
	}

	public IItemHandler getInventoryHandler() {
		return inventoryHandler;
	}

	public void setInventoryHandler(IItemHandler inventoryHandler) {
		this.inventoryHandler = inventoryHandler;
	}
}
