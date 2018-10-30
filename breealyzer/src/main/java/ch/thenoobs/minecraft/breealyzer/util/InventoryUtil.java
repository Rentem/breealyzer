package ch.thenoobs.minecraft.breealyzer.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import forestry.apiculture.tiles.TileApiary;
import forestry.core.tiles.TileAnalyzer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
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


	public static <T> Map<Item, Integer> getItemsOfType(InventoryHandlerEntityPair inventory, Class<T> itemType) {
		Map<Item, Integer> resultList = new HashMap<Item, Integer>();
		for (int slot = 0; slot < inventory.getInventoryHandler().getSlots(); slot++) {
			final ItemStack stackToPull = inventory.getInventoryHandler().getStackInSlot(slot);
			if (itemType.isInstance(stackToPull.getItem())) {
				resultList.put(stackToPull.getItem(), slot);
			}
		}
		return resultList;
	} 

	public static <T> List<ItemStackAtSlot> getStacksOfType(InventoryHandlerEntityPair inventory, Class<T> itemType) {
		List<ItemStackAtSlot> resultList = new ArrayList<>();
		for (int slot = 0; slot < inventory.getInventoryHandler().getSlots(); slot++) {
			final ItemStack stackToPull = inventory.getInventoryHandler().getStackInSlot(slot);
			if (itemType.isInstance(stackToPull.getItem())) {
				resultList.add(new ItemStackAtSlot(stackToPull, slot));
			}
		}
		return resultList;
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

	public static boolean moveItemAmount(InventoryHandlerEntityPair targetPair, int targetSlot, InventoryHandlerEntityPair sourcePair, int sourceSlot, int amount) {		

		final ItemStack stackToPull = sourcePair.getInventoryHandler().extractItem(sourceSlot, amount, true);
		if (stackToPull.getCount() < amount) {
			return false;
		}
		final ItemStack leftover = targetPair.getInventoryHandler().insertItem(targetSlot, stackToPull, true);
		if (leftover.getCount() > 0) {
			return false;
		}
		final ItemStack effectiveStack = sourcePair.getInventoryHandler().extractItem(sourceSlot, amount, false);
		targetPair.getInventoryHandler().insertItem(targetSlot, effectiveStack, false);
		targetPair.getTileEntity().markDirty();
		sourcePair.getTileEntity().markDirty();
		return stackToPull.isEmpty();
	}

	public static void condenseItems(InventoryHandlerEntityPair target) {
		IItemHandler inventory = target.getInventoryHandler();
		List<ItemStack> stacks = new ArrayList<>();
		for (int i = 0; i < inventory.getSlots(); i++) {
			ItemStack sta = inventory.getStackInSlot(i);
			if (!sta.isEmpty()) {
				stacks.add(sta.copy());
				inventory.extractItem(i, sta.getCount(), false);				
			}
		}

		insertStacks(stacks, target);

		target.getTileEntity().markDirty();
	}

	public static void insertStacks(List<ItemStack> stacks, InventoryHandlerEntityPair target) {
		for (ItemStack stack : stacks) {
			for (int targetSlot = 0; targetSlot < target.getInventoryHandler().getSlots(); targetSlot++) { //TODO throw (chest)overflow onto floor
				stack = target.getInventoryHandler().insertItem(targetSlot, stack, false);
				if (stack.getCount() <= 0) {
					break;
				}
			}
		}
	}

	public static IItemHandler tryGetInventoryHandler(TileEntity te, EnumFacing side) {
		if (te != null) {
			if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) {
				return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
			}
	
			if (te instanceof ISidedInventory) {
				return new SidedInvWrapper((ISidedInventory)te, side);
			}
	
			if (te instanceof IInventory) {
				return new InvWrapper((IInventory)te);
			}
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
		
		return  world.getTileEntity(pos);
	}

	public static InventoryHandlerEntityPair getInventoryHandlerEntityPair(World world, BlockPos pos, EnumFacing side) {
		final TileEntity tileEntity = tryGetTileEntity(world, pos);
		if (tileEntity == null) {
			return null;
		}
		
		return getInventoryHandlerEntityPair(tileEntity, side);
	}
	
	public static InventoryHandlerEntityPair getInventoryHandlerEntityPair(TileEntity tileEntity, EnumFacing side ) {
		final IItemHandler neighbourHandler = tryGetInventoryHandler(tileEntity, side.getOpposite());
		
		if (neighbourHandler != null) { 
			return new InventoryHandlerEntityPair(tileEntity, neighbourHandler);
		}
		
		return null;
	}

	public static List<InventoryHandlerEntityPair> getInventoryHandlersOfTypeInDirection(World world, BlockPos position, Class<?> type, EnumFacing direction, Boolean checkAllSides) {
		final List<InventoryHandlerEntityPair> handlers = new ArrayList<>();

		TileEntity tileEntity = tryGetTileEntity(world, position);
						
		if ((tileEntity != null) && (type.isAssignableFrom(tileEntity.getClass()))) {
			
			InventoryHandlerEntityPair inventoryHandler = getInventoryHandlerEntityPair(tileEntity, direction.getOpposite());
					
			handlers.add(inventoryHandler);
						
			BlockPos newPosition = position.offset(direction);
			
			//System.out.println(String.format("Looking for %s in direction of %s (in position %s (currently %s))", type.getName(), direction.toString(), newPosition.toString(), position.toString()));
			
			final List<InventoryHandlerEntityPair> inventories = getInventoryHandlersOfTypeInDirection(world, newPosition, type, direction, false);
			
			for (InventoryHandlerEntityPair subHandler : inventories) {
				handlers.add(subHandler);
			}				
		}
		
		return handlers;
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
