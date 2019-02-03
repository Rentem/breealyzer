package ch.thenoobs.minecraft.breealyzer.util.inventory;

import ch.thenoobs.minecraft.breealyzer.util.InventoryHandlerEntityPair;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;

public class AnalyzerInventoryHandler  extends InventoryHandlerEntityPair {
	public AnalyzerInventoryHandler(TileEntity tileEntity, IItemHandler inventoryHandler) {
		super(tileEntity, inventoryHandler); 
	}
}
