package ch.thenoobs.minecraft.breealyzer.blocks.tileentities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ch.thenoobs.minecraft.breealyzer.blocks.BreealyzerBlock;
import ch.thenoobs.minecraft.breealyzer.util.InventoryHandlerEntityPair;
import ch.thenoobs.minecraft.breealyzer.util.InventoryUtil;
import ch.thenoobs.minecraft.breealyzer.util.ItemStackAtSlot;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.BeeSelector;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.BeeWrapper;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.IBee;
import forestry.apiculture.items.ItemBeeGE;
import forestry.apiculture.tiles.TileApiary;
import forestry.core.tiles.TileAnalyzer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

public class BreealyzerTE extends TileEntity implements ITickable {
	private int tickModulo = 100;
	private int tickCnt = 0;
	
	private InventoryHandlerEntityPair lootInventoryPair;
	private InventoryHandlerEntityPair beeInventoryPair;
	private InventoryHandlerEntityPair beeTrashPair;
	
	private List<InventoryHandlerEntityPair> analyzers;
	private List<InventoryHandlerEntityPair> apiaries;


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

			IBlockState blockState = world.getBlockState(this.pos);
			
			EnumFacing facing = blockState.getValue(BreealyzerBlock.FACING);			
			
			if (facing == EnumFacing.DOWN || facing == EnumFacing.UP) {
				facing = EnumFacing.NORTH;
			}
			EnumFacing outputSide = facing.rotateY();
			EnumFacing inputSide = outputSide.getOpposite();

			EnumFacing analyzerSide = EnumFacing.DOWN;
			EnumFacing apiarySide = EnumFacing.UP;

			lootInventoryPair = InventoryUtil.getInventoryHandlerEntityPair(world, pos.offset(inputSide), inputSide.getOpposite());
			if (lootInventoryPair == null) {
				return;
			}

			
			beeInventoryPair = InventoryUtil.getInventoryHandlerEntityPair(world, pos.offset(outputSide), outputSide.getOpposite());
			if (beeInventoryPair == null) {
				return;
			}
			
			if (apiaries == null) {
				apiaries = InventoryUtil.getInventoryHandlersOfTypeInDirection(world, pos.offset(apiarySide), TileApiary.class, apiarySide, false);
				System.out.println(String.format("Found %s apiaries to use.", apiaries.size()));
			}
			
			if (apiaries.size() < 1) {
				return;
			}
			
			if (analyzers == null) {
				analyzers = InventoryUtil.getInventoryHandlersOfTypeInDirection(world, pos.offset(analyzerSide), TileAnalyzer.class, analyzerSide, false);
				System.out.println(String.format("Found %s analyzers to use.", analyzers.size()));
			}
			
			if (analyzers.size() < 1) {
				return;
			}

			clearApiaries();

			InventoryUtil.condenseItems(beeInventoryPair);
			
			analyzeBees();

			if (beeAmountInAnalyzer <= 0) {
				fillApiaries(beeInventoryPair);
			}
		}
	}

	private List<ItemStackAtSlot> selectBees(List<ItemStackAtSlot> drones, List<ItemStackAtSlot> princesses, BeeSelector selector) {
		List<BeeWrapper> wrappedDrones = drones.stream().map(BreealyzerTE::wrapBee).collect(Collectors.toList());
		List<BeeWrapper> wrappedPrincesses = princesses.stream().map(BreealyzerTE::wrapBee).collect(Collectors.toList());
		List<BeeWrapper> results = selector.selectBreedingPair(wrappedDrones, wrappedPrincesses);
		return results.stream().map(wrappedBee -> (ItemStackAtSlot)wrappedBee.getObject()).collect(Collectors.toList());
	}

	private static BeeWrapper wrapBee(ItemStackAtSlot iSTAT) {
		BeeWrapper newWrapper = new BeeWrapper(((ItemBeeGE)iSTAT.getStack().getItem()).getIndividual(iSTAT.getStack()), iSTAT);
		return newWrapper;
	}

	private EnumBeeType getTypeFromStackAtSlot(ItemStackAtSlot iSTAT) {
		return ((ItemBeeGE)iSTAT.getStack().getItem()).getType();
	}

	private void analyzeBees() {
		List<ItemStackAtSlot> unAnalyzedBees = getUnAnalyzedBees();
		int amount = fillAnalyzers(unAnalyzedBees, beeInventoryPair, analyzers);
//		int amount = fillAnalyzer(beeInventoryPair, analyzerInventoryPair);
		//		System.out.println("Analyzing " + amount + " bees");
		beeAmountInAnalyzer += amount;
		if (beeAmountInAnalyzer > 0) {
			for (InventoryHandlerEntityPair analyzer : analyzers) {
				amount = clearAnalyzer(beeInventoryPair, analyzer);
//						System.out.println("Analyzed " + amount + " bees");
				beeAmountInAnalyzer -= amount;
			}
//			amount = clearAnalyzer(beeInventoryPair, analyzerInventoryPair);
//			//		System.out.println("Analyzed " + amount + " bees");
//			beeAmountInAnalyzer -= amount;
		}
		//		System.out.println("Bees in analyzer: " + beeAmountInAnalyzer);
	}

	private List<ItemStackAtSlot> getUnAnalyzedBees() {
		List<ItemStackAtSlot> bees = InventoryUtil.getStacksOfType(beeInventoryPair, ItemBeeGE.class);
		
		return bees.stream().filter(beeStack -> !getBeeFromISTAT(beeStack).isAnalyzed()).collect(Collectors.toList());
	}
	
	private IBee getBeeFromISTAT(ItemStackAtSlot iSTAT) {
		return ((ItemBeeGE)iSTAT.getStack().getItem()).getIndividual(iSTAT.getStack());
	}
	
	private int fillAnalyzers(List<ItemStackAtSlot> bees, InventoryHandlerEntityPair workChest, List<InventoryHandlerEntityPair> analyzers) {
		int cnt = bees.size()-1;
		int totalAmount = 0;
		while (cnt >= 0) {
			InventoryHandlerEntityPair analyzer = analyzers.get(cnt%analyzers.size());
			int amount = fillAnalyzer(workChest, analyzer, bees.get(cnt).getSlot());
			totalAmount += amount; //TODO catch if analyzer is full
			cnt--;
		}
		return totalAmount;
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

	private void fillApiaries(InventoryHandlerEntityPair beeInventoryPair) {				
		List<InventoryHandlerEntityPair> usableApiaries = new ArrayList<>();
		
		for (InventoryHandlerEntityPair apiaryPair : this.apiaries) {
			if (apiaryPair.getInventoryHandler().getStackInSlot(0).isEmpty()) {
				usableApiaries.add(apiaryPair);
			}
		}
		
		if (usableApiaries.size() > 0) {
			for (InventoryHandlerEntityPair apiaryInventoryPair : usableApiaries)
			{
				fillApiary(apiaryInventoryPair, beeInventoryPair);
			}
		}
	}
	
	private void fillApiary(InventoryHandlerEntityPair apiaryInventoryPair, InventoryHandlerEntityPair beeInventoryPair) {		
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
		
		List<ItemStackAtSlot> selectedBees = selectBees(drones, princesses, selector);
		ItemStackAtSlot selectedDrone = selectedBees.get(1);
		ItemStackAtSlot selectedPrincess =  selectedBees.get(0);

		InventoryUtil.moveItemAmount(apiaryInventoryPair, 0, beeInventoryPair, selectedPrincess.getSlot(), 1);
		InventoryUtil.moveItemAmount(apiaryInventoryPair, 1, beeInventoryPair, selectedDrone.getSlot(), 1);
	}
	
	public void clearApiaries() {
		for (InventoryHandlerEntityPair apiaryInventoryPair : this.apiaries) {
			this.clearApiary(apiaryInventoryPair);
		}
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
		compound.setInteger("beeAmountInAnalyzer", beeAmountInAnalyzer);
		return super.writeToNBT(compound);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		tickCnt = compound.getInteger("tickCnt");
		tickModulo = compound.getInteger("tickModulo");
		beeAmountInAnalyzer = compound.getInteger("beeAmountInAnalyzer");
		super.readFromNBT(compound);
	}
}
