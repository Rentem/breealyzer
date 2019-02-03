package ch.thenoobs.minecraft.breealyzer.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;

public class InventoryHandlerEntityPair {
	private TileEntity tileEntity;
	private IItemHandler itemHandler;

	public InventoryHandlerEntityPair(TileEntity tileEntity, IItemHandler itemHandler) {
		this.tileEntity = tileEntity;
		this.itemHandler = itemHandler;
	}
	
	public TileEntity getTileEntity() {
		return tileEntity;
	}

	public void setTileEntity(TileEntity tileEntity) {
		this.tileEntity = tileEntity;
	}

	public IItemHandler getItemHandler() {
		return itemHandler;
	}

	public void setItemHandler(IItemHandler itemHandler) {
		this.itemHandler = itemHandler;
	}
}
