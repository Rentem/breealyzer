package ch.thenoobs.minecraft.breealyzer.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.thenoobs.minecraft.breealyzer.util.allelescoring.BeeWrapper;
import forestry.apiculture.items.ItemBeeGE;
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
	public static void emptyInventory(InventoryHandler targetPair, InventoryHandler sourcePair) {
		for (int sourceSlot = 0; sourceSlot < sourcePair.getItemHandler().getSlots(); sourceSlot++) {
			moveStack(targetPair, sourcePair, sourceSlot);
		}
	}

	public static <T> Map<Item, Integer> getItemsOfType(InventoryHandler inventory, Class<T> itemType) {
		Map<Item, Integer> resultList = new HashMap<Item, Integer>();
		for (int slot = 0; slot < inventory.getItemHandler().getSlots(); slot++) {
			final ItemStack stackToPull = inventory.getItemHandler().getStackInSlot(slot);
			if (itemType.isInstance(stackToPull.getItem())) {
				resultList.put(stackToPull.getItem(), slot);
			}
		}
		return resultList;
	}

	public static <T> List<ItemStackAt> getStacksOfType(InventoryHandler inventory, Class<T> itemType) {
		List<ItemStackAt> resultList = new ArrayList<>();
		for (int slot = 0; slot < inventory.getItemHandler().getSlots(); slot++) {
			final ItemStack stackToPull = inventory.getItemHandler().getStackInSlot(slot);
			if (itemType.isInstance(stackToPull.getItem())) {
				resultList.add(new ItemStackAt(stackToPull, slot, inventory));
			}
		}
		return resultList;
	}

	public static void moveStack(InventoryHandler targetPair, InventoryHandler sourcePair, int sourceSlot) {
		final ItemStack stackToPull = sourcePair.getItemHandler().getStackInSlot(sourceSlot);
		if (stackToPull.isEmpty()) {
			return;
		}
		for (int targetSlot = 0; targetSlot < targetPair.getItemHandler().getSlots(); targetSlot++) {
			if (moveStack(targetPair, targetSlot, sourcePair, sourceSlot)) {
				break;
			}
		}
	}

	public static void moveItemStackAtsToTarget(List<ItemStackAt> stacks, InventoryHandler target) {
		stacks.forEach(s -> moveItemStackAtToTarget(s, target));
		
	}
	
	public static void moveItemStackAtToTarget(ItemStackAt itemStackAt, InventoryHandler target) {
		moveStack(target, itemStackAt.getInventory(), itemStackAt.getSlot());
	}

	public static boolean moveStack(InventoryHandler targetPair, int targetSlot, InventoryHandler sourcePair, int sourceSlot) {

		final ItemStack stackToPull = sourcePair.getItemHandler().getStackInSlot(sourceSlot);
		if (stackToPull.isEmpty()) {
			return false;
		}
		final ItemStack leftover = targetPair.getItemHandler().insertItem(targetSlot, stackToPull, true);
		int amount = stackToPull.getCount() - leftover.getCount();
		if (amount == 0) {
			return false;
		}
		final ItemStack effectiveStack = sourcePair.getItemHandler().extractItem(sourceSlot, amount, false);
		targetPair.getItemHandler().insertItem(targetSlot, effectiveStack, false);
		targetPair.getTileEntity().markDirty();
		sourcePair.getTileEntity().markDirty();
		return stackToPull.isEmpty();
	}

	public static boolean moveItemAmount(InventoryHandler targetPair, int targetSlot, InventoryHandler sourcePair, int sourceSlot, int amount) {

		final ItemStack stackToPull = sourcePair.getItemHandler().extractItem(sourceSlot, amount, true);
		if (stackToPull.getCount() < amount) {
			return false;
		}
		final ItemStack leftover = targetPair.getItemHandler().insertItem(targetSlot, stackToPull, true);
		if (leftover.getCount() > 0) {
			return false;
		}
		final ItemStack effectiveStack = sourcePair.getItemHandler().extractItem(sourceSlot, amount, false);
		targetPair.getItemHandler().insertItem(targetSlot, effectiveStack, false);
		targetPair.getTileEntity().markDirty();
		sourcePair.getTileEntity().markDirty();
		return stackToPull.isEmpty();
	}

	public static void condenseItems(InventoryHandler target) {
		IItemHandler inventory = target.getItemHandler();
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

	public static void insertStacks(List<ItemStack> stacks, InventoryHandler target) {
		for (ItemStack stack : stacks) {
			for (int targetSlot = 0; targetSlot < target.getItemHandler().getSlots(); targetSlot++) { // TODO throw (chest)overflow onto floor
				stack = target.getItemHandler().insertItem(targetSlot, stack, false);
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
				return new SidedInvWrapper((ISidedInventory) te, side);
			}

			if (te instanceof IInventory) {
				return new InvWrapper((IInventory) te);
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

		return world.getTileEntity(pos);
	}

	public static InventoryHandler getInventoryHandler(World world, BlockPos pos, EnumFacing side) {
		final TileEntity tileEntity = tryGetTileEntity(world, pos);
		if (tileEntity == null) {
			return null;
		}

		return getInventoryHandlerEntityPair(tileEntity, side);
	}

	public static <T extends InventoryHandler> T getInventoryHandlerEntityPair(TileEntity tileEntity, EnumFacing side) {
		final IItemHandler neighbourHandler = tryGetInventoryHandler(tileEntity, side.getOpposite());

		if (neighbourHandler != null) {
			return InventoryFactory.GetInvenotryHandler(tileEntity, neighbourHandler);
		}

		return null;
	}

	public static <T extends InventoryHandler> List<T> getInventoryHandlersOfTypeInDirection(World world, BlockPos position, Class<?> type, EnumFacing direction, Boolean checkAllSides) {
		final List<T> handlers = new ArrayList<T>();

		TileEntity tileEntity = tryGetTileEntity(world, position);

		if ((tileEntity != null) && (type.isAssignableFrom(tileEntity.getClass()))) {

			T inventoryHandler = getInventoryHandlerEntityPair(tileEntity, direction.getOpposite());

			handlers.add(inventoryHandler);

			BlockPos newPosition = position.offset(direction);

			final List<T> inventories = getInventoryHandlersOfTypeInDirection(world, newPosition, type, direction, false);

			for (T subHandler : inventories) {
				handlers.add(subHandler);
			}
		}

		return handlers;
	}

	public static List<InventoryHandler> getNeighbourInventoryHandlerEntity(World world, BlockPos pos) {
		Collection<EnumFacing> sidesToCheck = Arrays.asList(EnumFacing.HORIZONTALS);

		final List<InventoryHandler> handlers = new ArrayList<>();
		for (EnumFacing side : sidesToCheck) {
			final InventoryHandler inventoryHandlerEntityPair = getInventoryHandler(world, pos.offset(side), side.getOpposite());

			if (inventoryHandlerEntityPair != null) {
				handlers.add(inventoryHandlerEntityPair);
			}
		}

		return handlers;
	}
	
	public static BeeWrapper wrapBee(ItemStackAt iSTAT) {
		BeeWrapper newWrapper = new BeeWrapper(((ItemBeeGE)iSTAT.getStack().getItem()).getIndividual(iSTAT.getStack()), iSTAT);
		return newWrapper;
	}

}
