package ch.thenoobs.minecraft.breealyzer.util.inventory;

import ch.thenoobs.minecraft.breealyzer.util.InventoryHandler;
import forestry.core.tiles.TileAnalyzer;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandler;

public class AnalyzerInventoryHandler  extends InventoryHandler {
	private TileAnalyzer tileAnalyzer;
	private IFluidTank honeyTank;
	
	public AnalyzerInventoryHandler(TileAnalyzer tileAnalyzer, IItemHandler inventoryHandler) {
		super(tileAnalyzer, inventoryHandler); 
		
		this.tileAnalyzer = tileAnalyzer;
		this.honeyTank = this.tileAnalyzer.getTankManager().getTank(0);
	}
	
	public TileAnalyzer getTypeEntity()
	{
		return this.tileAnalyzer;
	}
	
	public int getTankFluidAmount()
	{
		int result = 0;
		
		if (this.honeyTank != null)
		{
			result = this.honeyTank.getFluidAmount();
		}
		
		return result;
	}
	
	public Boolean getIsBusy()
	{
		return this.tileAnalyzer.workCycle();
		
		//return this.tileAnalyzer.get
	}
}
