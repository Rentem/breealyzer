package ch.thenoobs.minecraft.breealyzer.blocks.tileentities;

import ch.thenoobs.minecraft.breealyzer.util.InventoryHandlerEntityPair;
import ch.thenoobs.minecraft.breealyzer.util.InventoryUtil;
import forestry.apiculture.items.ItemBeeGE;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

public class BreealyzerTE extends TileEntity implements ITickable {
	private int tickModulo = 100;
	private int tickCnt = 0;
	private InventoryHandlerEntityPair analyzerInventoryPair;
	private InventoryHandlerEntityPair apiaryInventoryPair;
	private InventoryHandlerEntityPair lootInventoryPair;
	private InventoryHandlerEntityPair beeInventoryPair;
	private InventoryHandlerEntityPair beeTrashPair;

	@Override
	public void update() {
		tickCnt--;
		if (tickCnt < 0) {
			tickCnt = tickModulo;
			executeTick();
		}
	}

	private void executeTick() {
		if (!world.isRemote) {

			EnumFacing analyzerSide = EnumFacing.DOWN;
			analyzerInventoryPair = InventoryUtil.getInventoryHandlerEntityPair(world, pos.offset(analyzerSide), analyzerSide.getOpposite());
			if (analyzerInventoryPair == null) {
				return;
			}
			
			EnumFacing apiarySide = EnumFacing.UP;
			apiaryInventoryPair = InventoryUtil.getInventoryHandlerEntityPair(world, pos.offset(apiarySide), apiarySide.getOpposite());
			if (apiaryInventoryPair == null) {
				return;
			}

			EnumFacing lootSide = EnumFacing.SOUTH;
			lootInventoryPair = InventoryUtil.getInventoryHandlerEntityPair(world, pos.offset(lootSide), lootSide.getOpposite());
			if (lootInventoryPair == null) {
				return;
			}
			
			EnumFacing beeSide = EnumFacing.NORTH;
			beeInventoryPair = InventoryUtil.getInventoryHandlerEntityPair(world, pos.offset(beeSide), beeSide.getOpposite());
			if (beeInventoryPair == null) {
				return;
			}
			
			clearApiary(apiaryInventoryPair);
			fillAnalyzer(analyzerInventoryPair, beeInventoryPair);
			clearAnalyzer(analyzerInventoryPair);
			fillApiary(apiaryInventoryPair, beeInventoryPair);
		}
	}
	

	
	private void fillApiary(InventoryHandlerEntityPair apiaryInventoryPair2, InventoryHandlerEntityPair beeInventoryPair2) {
		// TODO Auto-generated method stub
		
	}

	private void fillAnalyzer(InventoryHandlerEntityPair workChest, InventoryHandlerEntityPair anlayzer) {
		for (int sourceSlot = 0; sourceSlot < anlayzer.getInventoryHandler().getSlots(); sourceSlot++) {
			fillAnalyzer(workChest, anlayzer, sourceSlot);
		}	
	}
	
	public void fillAnalyzer(InventoryHandlerEntityPair workChest, InventoryHandlerEntityPair analyzer, int sourceSlot) {			
		final ItemStack stackToPull = workChest.getInventoryHandler().getStackInSlot(sourceSlot);
		if (stackToPull.isEmpty()) {
			return;
		}
		if (stackToPull.getItem() instanceof ItemBeeGE) {
			ItemBeeGE beeItem = (ItemBeeGE) stackToPull.getItem();
			if (beeItem.getIndividual(stackToPull).isAnalyzed()) {
				return;
			}
		} else {
			return;
		}
		for (int targetSlot = 0; targetSlot < analyzer.getInventoryHandler().getSlots(); targetSlot++) {
			if (moveStack(analyzer, targetSlot, workChest, sourceSlot)) {
				break;
			}
		}
	}


	private void clearAnalyzer(InventoryHandlerEntityPair analyzerInventoryPair2) {
		InventoryUtil.emptyInventory(beeInventoryPair, analyzerInventoryPair2);
	}

	public void clearApiary(InventoryHandlerEntityPair apiaryPair) {
		for (int sourceSlot = 0; sourceSlot < apiaryPair.getInventoryHandler().getSlots(); sourceSlot++) {
			clearApiary(apiaryPair, sourceSlot);
		}		
	}

	public void clearApiary(InventoryHandlerEntityPair apiaryPair, int sourceSlot) {			
		final ItemStack stackToPull = apiaryPair.getInventoryHandler().getStackInSlot(sourceSlot);
		if (stackToPull.isEmpty()) {
			return;
		}
		InventoryHandlerEntityPair targetPair;
		if (stackToPull.getItem() instanceof ItemBeeGE) {
			targetPair = beeInventoryPair;
		} else {
			targetPair = lootInventoryPair;
		}
		for (int targetSlot = 0; targetSlot < targetPair.getInventoryHandler().getSlots(); targetSlot++) {
			if (InventoryUtil.moveStack(targetPair, targetSlot, apiaryPair, sourceSlot)) {
				break;
			}
		}
	}

//	public boolean moveStack(InventoryHandlerEntityPair targetPair, int targetSlot, InventoryHandlerEntityPair sourcePair, int sourceSlot) {		
//		final ItemStack stackToPull = sourcePair.getInventoryHandler().getStackInSlot(sourceSlot);
//		if (stackToPull.isEmpty()) {
//			return false;
//		}
//		final ItemStack leftover = targetPair.getInventoryHandler().insertItem(targetSlot, stackToPull, true);
//		int amount = stackToPull.getCount() - leftover.getCount();
//		if (amount == 0) {
//			return false;
//		}
//		final ItemStack effectiveStack = sourcePair.getInventoryHandler().extractItem(sourceSlot, amount, false);
//		targetPair.getInventoryHandler().insertItem(targetSlot, effectiveStack, false);
//		targetPair.getTileEntity().markDirty();
//		sourcePair.getTileEntity().markDirty();
//		return stackToPull.isEmpty();
//	}


	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("tickCnt", tickCnt);
		compound.setInteger("tickModulo", tickModulo);
		return super.writeToNBT(compound);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		tickCnt = compound.getInteger("tickCnt");
		tickModulo = compound.getInteger("tickModulo");
		super.readFromNBT(compound);
	}
}
