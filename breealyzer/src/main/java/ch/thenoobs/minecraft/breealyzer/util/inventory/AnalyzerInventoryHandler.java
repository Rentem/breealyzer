package ch.thenoobs.minecraft.breealyzer.util.inventory;

import ch.thenoobs.minecraft.breealyzer.util.InventoryHandler;
import forestry.core.tiles.TileAnalyzer;
import net.minecraftforge.items.IItemHandler;

public class AnalyzerInventoryHandler  extends InventoryHandler {
	private TileAnalyzer tileAnalyzer;
	
	public AnalyzerInventoryHandler(TileAnalyzer tileAnalyzer, IItemHandler inventoryHandler) {
		super(tileAnalyzer, inventoryHandler); 
		
		this.tileAnalyzer = tileAnalyzer;
	}
	
	public TileAnalyzer getTypeEntity()
	{
		return this.tileAnalyzer;
	}
}
