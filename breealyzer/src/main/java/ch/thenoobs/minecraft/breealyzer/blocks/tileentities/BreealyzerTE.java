package ch.thenoobs.minecraft.breealyzer.blocks.tileentities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ch.thenoobs.minecraft.breealyzer.blocks.BreealyzerBlock;
import ch.thenoobs.minecraft.breealyzer.util.InventoryHandlerEntityPair;
import ch.thenoobs.minecraft.breealyzer.util.InventoryUtil;
import ch.thenoobs.minecraft.breealyzer.util.ItemStackAt;
import ch.thenoobs.minecraft.breealyzer.util.Log;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.BeeSelector;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.BeeWrapper;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.ScoringResult;
import ch.thenoobs.minecraft.breealyzer.util.inventory.AnalyzerInventoryHandler;
import ch.thenoobs.minecraft.breealyzer.util.inventory.ApiaryInventoryHandler;
import ch.thenoobs.minecraft.breealyzer.util.trashing.TrashManager;
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
	
	private Boolean hasSeedBank = false;
	private Boolean hasTrash = false;
	
	private InventoryHandlerEntityPair lootInventoryPair;
	private InventoryHandlerEntityPair seedBankInventoryPair;
	private InventoryHandlerEntityPair beeInventoryPair;
	private InventoryHandlerEntityPair trashInventoryPair;	
	
	private TrashManager trashManager;
	
	private List<AnalyzerInventoryHandler> analyzers;
	private List<ApiaryInventoryHandler> apiaries;


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
			
			EnumFacing inputSide = facing.rotateY();
			EnumFacing outputSide = inputSide.getOpposite();

			EnumFacing analyzerSide = EnumFacing.DOWN;
			EnumFacing apiarySide = facing.getOpposite();

			lootInventoryPair = InventoryUtil.getInventoryHandlerEntityPair(world, pos.offset(outputSide), outputSide.getOpposite());
			//System.out.println(String.format("Output Inventory found: %s.", (lootInventoryPair != null)));
			
			if (lootInventoryPair == null) {
				return;
			}
			
			beeInventoryPair = InventoryUtil.getInventoryHandlerEntityPair(world, pos.offset(inputSide), inputSide.getOpposite());
			//System.out.println(String.format("Input Inventory found: %s.", (beeInventoryPair != null)));
			
			if (beeInventoryPair == null) {
				return;
			}
						
			apiaries = InventoryUtil.getInventoryHandlersOfTypeInDirection(world, pos.offset(apiarySide), TileApiary.class, apiarySide, false);
			//System.out.println(String.format("Found %s apiaries to use.", apiaries.size()));
			
			if (apiaries.size() < 1) {
				return;
			}

			analyzers = InventoryUtil.getInventoryHandlersOfTypeInDirection(world, pos.offset(analyzerSide), TileAnalyzer.class, analyzerSide, false);
			
			if (analyzers.size() < 1) {
				return;
			}
			
			if (trashManager == null) {
				trashManager = new TrashManager();
			}
			
			seedBankInventoryPair = InventoryUtil.getInventoryHandlerEntityPair(world, pos.offset(inputSide).offset(EnumFacing.UP), outputSide.getOpposite());	
			//System.out.println(String.format("Seed Bank Inventory found: %s.", (seedBankInventoryPair != null)));
			
			if (seedBankInventoryPair != null) {
				this.hasSeedBank = true;
			}
			
			trashInventoryPair = InventoryUtil.getInventoryHandlerEntityPair(world, pos.offset(outputSide).offset(EnumFacing.UP), inputSide.getOpposite());
			//System.out.println(String.format("Trash Inventory found: %s.", (trashInventoryPair != null)));

			if (trashInventoryPair != null) {
				this.hasTrash = true;
			}
			
			clearApiaries();

			InventoryUtil.condenseItems(beeInventoryPair);
			
			analyzeBees();

			if (beeAmountInAnalyzer <= 0) {
				fillApiaries(beeInventoryPair);
			}
			
			
//			trashBees();
		}
	}
	
//	private void trashBees() {		
//		Log.info("Trashing Bees");
//		List<BeeWrapper> bees = InventoryUtil.getStacksOfType(beeInventoryPair, ItemBeeGE.class).stream()
//				.filter(iSAT -> getTypeFromStackAtSlot(iSAT) == EnumBeeType.DRONE)
//				.map(BreealyzerTE::wrapBee)
//				.collect(Collectors.toList());
//		trashManager.trashBees(bees, lootInventoryPair, beeInventoryPair);
//	}

	private ScoringResult selectBees(List<ItemStackAt> drones, List<ItemStackAt> princesses, BeeSelector selector) {
		List<BeeWrapper> wrappedDrones = drones.stream().map(BreealyzerTE::wrapBee).collect(Collectors.toList());
		List<BeeWrapper> wrappedPrincesses = princesses.stream().map(BreealyzerTE::wrapBee).collect(Collectors.toList());
		ScoringResult results = selector.selectBreedingPair(wrappedDrones, wrappedPrincesses);
		return results;
	}

	private static BeeWrapper wrapBee(ItemStackAt iSTAT) {
		BeeWrapper newWrapper = new BeeWrapper(((ItemBeeGE)iSTAT.getStack().getItem()).getIndividual(iSTAT.getStack()), iSTAT);
		return newWrapper;
	}

	private EnumBeeType getTypeFromStackAtSlot(ItemStackAt iSTAT) {
		return ((ItemBeeGE)iSTAT.getStack().getItem()).getType();
	}

	private void analyzeBees() {
		List<ItemStackAt> unAnalyzedBees = getUnAnalyzedBees();
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

	private List<ItemStackAt> getUnAnalyzedBees() {
		List<ItemStackAt> bees = InventoryUtil.getStacksOfType(beeInventoryPair, ItemBeeGE.class);
		
		return bees.stream().filter(beeStack -> !getBeeFromISTAT(beeStack).isAnalyzed()).collect(Collectors.toList());
	}
	
	private int fillAnalyzers(List<ItemStackAt> bees, InventoryHandlerEntityPair workChest, List<AnalyzerInventoryHandler> analyzers) {
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
	
	
	private IBee getBeeFromISTAT(ItemStackAt iSTAT) {
		return ((ItemBeeGE)iSTAT.getStack().getItem()).getIndividual(iSTAT.getStack());
	}
	
	private int fillAnalyzer(InventoryHandlerEntityPair workChest, InventoryHandlerEntityPair anlayzer) {
		int amount = 0;
		for (int sourceSlot = 0; sourceSlot < workChest.getItemHandler().getSlots(); sourceSlot++) {
			amount += fillAnalyzer(workChest, anlayzer, sourceSlot);
		}	
		return amount;
	}

	public int fillAnalyzer(InventoryHandlerEntityPair workChest, InventoryHandlerEntityPair analyzer, int sourceSlot) {			
		final ItemStack stackToPull = workChest.getItemHandler().getStackInSlot(sourceSlot);
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
		for (int targetSlot = 0; targetSlot < analyzer.getItemHandler().getSlots(); targetSlot++) {
			if (InventoryUtil.moveStack(analyzer, targetSlot, workChest, sourceSlot)) {
				break;
			}
		}
		final ItemStack stackToPull2 = workChest.getItemHandler().getStackInSlot(sourceSlot);
		amount = amount - stackToPull2.getCount();
		return amount;
	}


	private int clearAnalyzer(InventoryHandlerEntityPair workChest, InventoryHandlerEntityPair analyzer) {
		int amount = 0;
		for (int sourceSlot = 0; sourceSlot < analyzer.getItemHandler().getSlots(); sourceSlot++) {
			final ItemStack stackToPull = analyzer.getItemHandler().getStackInSlot(sourceSlot);

			int a = stackToPull.getCount();
			InventoryUtil.moveStack(workChest, analyzer, sourceSlot);

			final ItemStack stackToPull2 = analyzer.getItemHandler().getStackInSlot(sourceSlot);
			amount += a - stackToPull2.getCount();

		}	
		return amount;
	}

	private void fillApiaries(InventoryHandlerEntityPair beeInventoryPair) {				
		List<InventoryHandlerEntityPair> usableApiaries = new ArrayList<>();
		
		for (InventoryHandlerEntityPair apiaryPair : this.apiaries) {
			if (apiaryPair.getItemHandler().getStackInSlot(0).isEmpty()) {
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
		List<ItemStackAt> bees = InventoryUtil.getStacksOfType(beeInventoryPair, ItemBeeGE.class);
		
		Map<EnumBeeType, List<ItemStackAt>> beeMap = bees.stream().collect(Collectors.groupingBy(this::getTypeFromStackAtSlot));

		List<ItemStackAt> drones = beeMap.get(EnumBeeType.DRONE);
		List<ItemStackAt> princesses = beeMap.get(EnumBeeType.PRINCESS);
		
		if (drones == null || princesses == null) {
			return;
		}
		
		BeeSelector selector = new BeeSelector();
		selector.initScoreres("Cultivated");
		selector.initWeights();
		
		ScoringResult scoringResult = selectBees(drones, princesses, selector);
		ItemStackAt selectedDrone = scoringResult.getSelectedDrone().getBeeWrapper().getItemStackAt();
		ItemStackAt selectedPrincess =  scoringResult.getSelectedPrincess().getBeeWrapper().getItemStackAt();

		InventoryUtil.moveItemAmount(apiaryInventoryPair, 0, beeInventoryPair, selectedPrincess.getSlot(), 1);
		InventoryUtil.moveItemAmount(apiaryInventoryPair, 1, beeInventoryPair, selectedDrone.getSlot(), 1);
		
		trashManager.trashBees(scoringResult.getLeftoverDrones(), lootInventoryPair, beeInventoryPair);
	}
	
	public void clearApiaries() {
		for (InventoryHandlerEntityPair apiaryInventoryPair : this.apiaries) {
			this.clearApiary(apiaryInventoryPair);
		}
	}
	
	public void clearApiary(InventoryHandlerEntityPair apiaryPair) {
		for (int sourceSlot = 0; sourceSlot < apiaryPair.getItemHandler().getSlots(); sourceSlot++) {
			clearApiary(apiaryPair, sourceSlot);	
		}		
	}

	public void clearApiary(InventoryHandlerEntityPair apiaryPair, int sourceSlot) {			
		final ItemStack stackToPull = apiaryPair.getItemHandler().getStackInSlot(sourceSlot);
		if (stackToPull.isEmpty()) {
			return;
		}
		InventoryHandlerEntityPair targetPair;
		if (stackToPull.getItem() instanceof ItemBeeGE) {
			targetPair = beeInventoryPair;
		} else {
			targetPair = lootInventoryPair;
		}
		for (int targetSlot = 0; targetSlot < targetPair.getItemHandler().getSlots(); targetSlot++) {
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
