package ch.thenoobs.minecraft.breealyzer.blocks.tileentities;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ch.thenoobs.minecraft.breealyzer.util.InventoryHandlerEntityPair;
import ch.thenoobs.minecraft.breealyzer.util.InventoryUtil;
import ch.thenoobs.minecraft.breealyzer.util.ItemStackAtSlot;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.BeeSelector;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.BeeWrapper;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.IBee;
import forestry.api.genetics.IChromosome;
import forestry.apiculture.items.ItemBeeGE;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import scala.tools.nsc.symtab.SymbolLoadersStats;

public class BreealyzerTE extends TileEntity implements ITickable {
	private int tickModulo = 100;
	private int tickCnt = 0;
	private InventoryHandlerEntityPair analyzerInventoryPair;
	private InventoryHandlerEntityPair apiaryInventoryPair;
	private InventoryHandlerEntityPair lootInventoryPair;
	private InventoryHandlerEntityPair beeInventoryPair;
	private InventoryHandlerEntityPair beeTrashPair;


	private int beeAmountInAnalyzer;

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

			InventoryUtil.condenseItems(beeInventoryPair);
			
			analyzeBees();

			fillApiary(apiaryInventoryPair, beeInventoryPair);
		}
	}



	private void fillApiary(InventoryHandlerEntityPair apiaryInventoryPair, InventoryHandlerEntityPair beeInventoryPair) {
		if (!apiaryInventoryPair.getInventoryHandler().getStackInSlot(0).isEmpty()) {
			return;
		}
		List<ItemStackAtSlot> bees = InventoryUtil.getStacksOfType(beeInventoryPair, ItemBeeGE.class);
		Map<EnumBeeType, List<ItemStackAtSlot>> beeMap = bees.stream().collect(Collectors.groupingBy(this::getTypeFromStackAtSlot));

		List<ItemStackAtSlot> drones = beeMap.get(EnumBeeType.DRONE);
		List<ItemStackAtSlot> princesses = beeMap.get(EnumBeeType.PRINCESS);
		if (drones == null || princesses == null) {
			return;
		}
		BeeSelector selector = new BeeSelector();
		selector.initScoreres("Cultivated");
		selector.initWeights();
		ItemStackAtSlot selectedDrone = selectBee(drones, selector);
		ItemStackAtSlot selectedPrincess = selectBee(princesses, selector);

		InventoryUtil.moveItemAmount(apiaryInventoryPair, 0, beeInventoryPair, selectedPrincess.getSlot(), 1);
		InventoryUtil.moveItemAmount(apiaryInventoryPair, 1, beeInventoryPair, selectedDrone.getSlot(), 1);
	}

	private ItemStackAtSlot selectBee(List<ItemStackAtSlot> bees, BeeSelector selector) {
		List<BeeWrapper> dronesIBee = bees.stream().map(BreealyzerTE::wrapBee).collect(Collectors.toList());
		BeeWrapper selectedDroneIBee = selector.selectBeeFromList(dronesIBee);
		return (ItemStackAtSlot) selectedDroneIBee.getObject();
	}

	private static BeeWrapper wrapBee(ItemStackAtSlot iSTAT) {
		BeeWrapper newWrapper = new BeeWrapper(((ItemBeeGE)iSTAT.getStack().getItem()).getIndividual(iSTAT.getStack()), iSTAT);
		return newWrapper;
	}

	private EnumBeeType getTypeFromStackAtSlot(ItemStackAtSlot iSTAT) {
		return ((ItemBeeGE)iSTAT.getStack().getItem()).getType();
	}

	private void analyzeBees() {
		int amount = fillAnalyzer(beeInventoryPair, analyzerInventoryPair);
		//		System.out.println("Analyzing " + amount + " bees");
		beeAmountInAnalyzer += amount;
		if (beeAmountInAnalyzer > 0) {
			amount = clearAnalyzer(beeInventoryPair, analyzerInventoryPair);
			//		System.out.println("Analyzed " + amount + " bees");
			beeAmountInAnalyzer -= amount;
		}
		//		System.out.println("Bees in analyzer: " + beeAmountInAnalyzer);
	}

	private int fillAnalyzer(InventoryHandlerEntityPair workChest, InventoryHandlerEntityPair anlayzer) {
		int amount = 0;
		for (int sourceSlot = 0; sourceSlot < workChest.getInventoryHandler().getSlots(); sourceSlot++) {
			amount += fillAnalyzer(workChest, anlayzer, sourceSlot);
		}	
		return amount;
	}

	public int fillAnalyzer(InventoryHandlerEntityPair workChest, InventoryHandlerEntityPair analyzer, int sourceSlot) {			
		final ItemStack stackToPull = workChest.getInventoryHandler().getStackInSlot(sourceSlot);
		if (stackToPull.isEmpty()) {
			return 0;
		}
		if (stackToPull.getItem() instanceof ItemBeeGE) {
			ItemBeeGE beeItem = (ItemBeeGE) stackToPull.getItem();
			if (beeItem.getIndividual(stackToPull).isAnalyzed()) {
//				IChromosome[] chromosomes = beeItem.getIndividual(stackToPull).getGenome().getChromosomes();
//				for (IChromosome chrom : chromosomes) {
//					chrom.toString();
//					//					Enum
//					System.out.println("Chromosome: " + chrom.getActiveAllele().getUID() + " -- " + chrom.getPrimaryAllele().getUnlocalizedName());
//				}
				return 0;
			}
		} else {
			return 0;
		}
		int amount = stackToPull.getCount();
		for (int targetSlot = 0; targetSlot < analyzer.getInventoryHandler().getSlots(); targetSlot++) {
			if (InventoryUtil.moveStack(analyzer, targetSlot, workChest, sourceSlot)) {
				break;
			}
		}
		final ItemStack stackToPull2 = workChest.getInventoryHandler().getStackInSlot(sourceSlot);
		amount = amount - stackToPull2.getCount();
		return amount;
	}


	private int clearAnalyzer(InventoryHandlerEntityPair workChest, InventoryHandlerEntityPair analyzer) {
		int amount = 0;
		for (int sourceSlot = 0; sourceSlot < analyzer.getInventoryHandler().getSlots(); sourceSlot++) {
			final ItemStack stackToPull = analyzer.getInventoryHandler().getStackInSlot(sourceSlot);

			int a = stackToPull.getCount();
			InventoryUtil.moveStack(workChest, analyzer, sourceSlot);

			final ItemStack stackToPull2 = analyzer.getInventoryHandler().getStackInSlot(sourceSlot);
			amount += a - stackToPull2.getCount();

		}	
		return amount;
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
