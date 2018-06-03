package ch.thenoobs.minecraft.breealyzer.blocks.tileentities;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

import ch.thenoobs.minecraft.breealyzer.util.InventoryHandlerEntityPair;
import ch.thenoobs.minecraft.breealyzer.util.InventoryUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.items.IItemHandler;

public class AutomaticMoverTE extends TileEntity implements ITickable {
	private int tickModulo = 100;
	private int tickCnt = 0;

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
			//			EntityItem item = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(ModItems.ingotCopper));
			//			world.spawnEntity(item);

			EnumFacing target = EnumFacing.UP;

			final InventoryHandlerEntityPair targetPair = InventoryUtil.getInventoryHandlerEntityPair(world, pos.offset(target), target.getOpposite());
			if (targetPair == null) {
				return;
			}
			List<InventoryHandlerEntityPair> neighbors = InventoryUtil.getNeighbourInventoryHandlerEntity(world, pos);
			for (InventoryHandlerEntityPair neighbourPair : neighbors) {
				InventoryUtil.emptyInventory(targetPair, neighbourPair);
				//				for (int sourceSlot = 0; sourceSlot < neigh.getSlots(); sourceSlot++) {
				//					final ItemStack stackToPull = neigh.getStackInSlot(sourceSlot);
				//					if (stackToPull.isEmpty()) {
				//						continue;
				//					}
				//					System.out.println("found in slot: " + sourceSlot);
				//					
				//
				//					int targetSlot = 0;
				//					final ItemStack leftover = targetHandler.insertItem(targetSlot, stackToPull, false);
				//					int amount = stackToPull.getCount() - leftover.getCount();
				//					neigh.extractItem(sourceSlot, amount, false);
				//					
				//					while (!leftover.isEmpty()) {
				//						System.out.println("could not fit all items: " + targetSlot);
				//						targetSlot++;
				//						if (targetHandler.getSlots() <= targetSlot) {
				//							break;
				//						}
				//						final ItemStack stackToPull2 = neigh.getStackInSlot(sourceSlot);
				//						final ItemStack leftover2 = targetHandler.insertItem(targetSlot, stackToPull, false);
				//						amount = stackToPull2.getCount() - leftover2.getCount();
				//						neigh.extractItem(sourceSlot, amount, false);
				//						if (leftover2.isEmpty()) {
				//							break;
				//						}
				//					}
				//					markDirty();
				//				}
			}
		}
	}

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
