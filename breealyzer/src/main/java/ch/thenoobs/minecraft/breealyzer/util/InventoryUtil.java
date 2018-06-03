package ch.thenoobs.minecraft.breealyzer.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import forestry.apiculture.items.ItemBeeGE;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import scala.actors.threadpool.Arrays;

public class InventoryUtil {

	
	public static void emptyInventory(InventoryHandlerEntityPair targetPair, InventoryHandlerEntityPair sourcePair) {
		for (int sourceSlot = 0; sourceSlot < sourcePair.getInventoryHandler().getSlots(); sourceSlot++) {
			moveStack(targetPair, sourcePair, sourceSlot);
		}
	}

	public static void moveStack(InventoryHandlerEntityPair targetPair, InventoryHandlerEntityPair sourcePair, int sourceSlot) {
		final ItemStack stackToPull = sourcePair.getInventoryHandler().getStackInSlot(sourceSlot);
		if (stackToPull.isEmpty()) {
			return;
		}
		for (int targetSlot = 0; targetSlot < targetPair.getInventoryHandler().getSlots(); targetSlot++) {
			if (moveStack(targetPair, targetSlot, sourcePair, sourceSlot)) {
				break;
			}
		}
	}
	
	public static boolean moveStack(InventoryHandlerEntityPair targetPair, int targetSlot, InventoryHandlerEntityPair sourcePair, int sourceSlot) {		
		Integer movedAmount = 0;
		boolean result =  moveStack(targetPair, targetSlot, sourcePair, sourceSlot, movedAmount);
		System.out.println(movedAmount);
		return result;
	}

	public static boolean moveStack(InventoryHandlerEntityPair targetPair, int targetSlot, InventoryHandlerEntityPair sourcePair, int sourceSlot, Integer movedAmount) {		
		if (movedAmount == null) {
			movedAmount = 0;
		}
		final ItemStack stackToPull = sourcePair.getInventoryHandler().getStackInSlot(sourceSlot);
		if (stackToPull.isEmpty()) {
			return false;
		}
		final ItemStack leftover = targetPair.getInventoryHandler().insertItem(targetSlot, stackToPull, true);
		int amount = stackToPull.getCount() - leftover.getCount();
		if (amount == 0) {
			return false;
		}
		final ItemStack effectiveStack = sourcePair.getInventoryHandler().extractItem(sourceSlot, amount, false);
		targetPair.getInventoryHandler().insertItem(targetSlot, effectiveStack, false);
		targetPair.getTileEntity().markDirty();
		sourcePair.getTileEntity().markDirty();
		return stackToPull.isEmpty();
	}

	public static IItemHandler tryGetInventoryHandler(TileEntity te, EnumFacing side) {
		if (te == null) {
			return null;
		}
	
		if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) {
			return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
		}
	
		if (te instanceof ISidedInventory) {
			return new SidedInvWrapper((ISidedInventory)te, side);
		}
	
		if (te instanceof IInventory) {
			return new InvWrapper((IInventory)te);
		}
	
		return null;
	}

	public static IItemHandler tryGetInventoryHandler(World world, BlockPos pos, EnumFacing side) {
		if (!world.isBlockLoaded(pos)) {
			return null;
		}
		final TileEntity te = world.getTileEntity(pos);
	
		return tryGetInventoryHandler(te, side);
	}

	public static TileEntity tryGetTileEntity(World world, BlockPos pos) {
		if (!world.isBlockLoaded(pos)) {
			return null;
		}
		final TileEntity te = world.getTileEntity(pos);
		return te;
	}

	public static InventoryHandlerEntityPair getInventoryHandlerEntityPair(World world, BlockPos pos, EnumFacing side) {
		final TileEntity tileEntity = tryGetTileEntity(world, pos);
		if (tileEntity == null) {
			return null;
		}
		final IItemHandler neighbourHandler = tryGetInventoryHandler(tileEntity, side.getOpposite());
		if (neighbourHandler != null) { 
			return new InventoryHandlerEntityPair(tileEntity, neighbourHandler);
		}
		return null;
	}
	
	public static List<InventoryHandlerEntityPair> getNeighbourInventoryHandlerEntity(World world, BlockPos pos) {
		Collection<EnumFacing> sidesToCheck = Arrays.asList(EnumFacing.HORIZONTALS);
	
	
		final List<InventoryHandlerEntityPair> handlers = new ArrayList<>();
		for (EnumFacing side : sidesToCheck) {
			final InventoryHandlerEntityPair inventoryHandlerEntityPair = getInventoryHandlerEntityPair(world,  pos.offset(side), side.getOpposite());
			
			if (inventoryHandlerEntityPair != null) { 
				handlers.add(inventoryHandlerEntityPair); 
			}
		}
	
		return handlers;
	}
	
}
