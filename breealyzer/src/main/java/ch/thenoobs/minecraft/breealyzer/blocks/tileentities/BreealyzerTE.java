package ch.thenoobs.minecraft.breealyzer.blocks.tileentities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import ch.thenoobs.minecraft.breealyzer.blocks.BreealyzerBlock;
import ch.thenoobs.minecraft.breealyzer.commands.CommandManager;
import ch.thenoobs.minecraft.breealyzer.util.BeeUtil;
import ch.thenoobs.minecraft.breealyzer.util.InventoryHandler;
import ch.thenoobs.minecraft.breealyzer.util.InventoryUtil;
import ch.thenoobs.minecraft.breealyzer.util.ItemStackAt;
import ch.thenoobs.minecraft.breealyzer.util.Log;
import ch.thenoobs.minecraft.breealyzer.util.allelescoring.BeeScore;
import ch.thenoobs.minecraft.breealyzer.util.inventory.AnalyzerInventoryHandler;
import ch.thenoobs.minecraft.breealyzer.util.inventory.ApiaryInventoryHandler;
import ch.thenoobs.minecraft.breealyzer.util.trashing.TrashManager;
import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.IBee;
import forestry.api.genetics.IAlleleSpecies;
import forestry.apiculture.items.ItemBeeGE;
import forestry.apiculture.tiles.TileApiary;
import forestry.core.tiles.TileAnalyzer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.Constants;

public class BreealyzerTE extends TileEntity implements ITickable {
	private int tickModulo = 100;
	private int tickCnt = 0;
	private Integer registeredCommandID;
	private String name;
	private String uuID;

	private Boolean hasSeedBank = false;
	private Boolean hasTrash = false;

	private InventoryHandler lootInventoryHandler;
	private InventoryHandler seedBankInventoryHandler;
	private InventoryHandler beeInventoryHandler;
	private InventoryHandler trashInventoryHandler;

	private TrashManager trashManager;

	private List<AnalyzerInventoryHandler> analyzers;
	private List<ApiaryInventoryHandler> apiaries;

	private String[] currentCommand;
	private List<String> commandHistory;

	private boolean deleteTrashBees;
	private boolean instaAnalyze = false;

	@Override
	public void update() {
		tickCnt--;
		if (tickCnt < 0) {
			if (tickModulo < 20) {
				tickModulo = 100;
			}
			
			tickCnt = tickModulo;
			executeTick();
		}
	}

	private void executeTick() {
		if (!world.isRemote) {
			registerToCommandManager();
			initExtensions();

			executeCurrentCommand();

			// trashBees();
		}
	}

	private void registerToCommandManager() {
		CommandManager.registerBreeAlyzer(this);
	}

	public String ensureAndGetUUID() {
		if (uuID == null || uuID.isEmpty()) {
			uuID = UUID.randomUUID().toString();
		}
		return uuID;
	}

	// TODO check valid commands, move to CommandManager?
	// TEST ID 362c299e-856b-498d-86fc-3b6f5742cb0a
	public void sendCommand(String[] command, ICommandSender sender) {
		if (command[0].equalsIgnoreCase("toggleDeleteTrashBees")) {
			deleteTrashBees = !deleteTrashBees;
			return;
		}
		if (command[0].equalsIgnoreCase("listKnownSpecies")) {

			Set<String> species = getAvailableBeeSpecies(beeInventoryHandler);
			TextComponentString text = new TextComponentString("Available Species [" + species.size() + "]:");
			sender.sendMessage(text);

			List<String> sortedSpecies = species.stream().sorted().collect(Collectors.toList());
			for (String spec : sortedSpecies) {
				text = new TextComponentString(spec);
				sender.sendMessage(text);
			}
			return;
		}
		
		if (command[0].equalsIgnoreCase("instaAnalyze")) {
			if (command.length == 1) {
				TextComponentString text = new TextComponentString("Please provide value");
				sender.sendMessage(text);
				return;
			}
			
				boolean bool = Boolean.parseBoolean(command[1]);
				instaAnalyze = bool;
			
			
			return;
		}

		Log.info("New command: " + Arrays.toString(command));
		currentCommand = command;
	}

	private void executeCurrentCommand() {
		if (currentCommand == null || currentCommand.length == 0) {
			Log.info("No currently active command");
			return;
		}
		Log.info(Arrays.toString(currentCommand));
		if (currentCommand[0].equalsIgnoreCase("purify")) {
			purifyBee();
		}
		if (currentCommand[0].equalsIgnoreCase("purifyAllBees")) {
			if (currentCommand.length == 1) {
				Set<String> availableSpecies = getAvailableBeeSpecies(beeInventoryHandler);
				if (availableSpecies.isEmpty()) {
					clearCurrentCommand();
					return;
				}
				List<String> sortedSpecies = availableSpecies.stream().sorted().collect(Collectors.toList());

				currentCommand = Arrays.copyOf(currentCommand, 2);

				currentCommand[1] = sortedSpecies.get(0);
			}

			purifyBee();
		}
	}

	// TODO rename
	private void initExtensions() {
		IBlockState blockState = world.getBlockState(this.pos);

		EnumFacing facing = blockState.getValue(BreealyzerBlock.FACING);

		if (facing == EnumFacing.DOWN || facing == EnumFacing.UP) {
			facing = EnumFacing.NORTH;
		}

		EnumFacing inputSide = facing.rotateY();
		EnumFacing outputSide = inputSide.getOpposite();

		EnumFacing analyzerSide = EnumFacing.DOWN;
		EnumFacing apiarySide = facing.getOpposite();

		lootInventoryHandler = InventoryUtil.getInventoryHandler(world, pos.offset(outputSide), outputSide.getOpposite());

		if (lootInventoryHandler == null) {
			return;
		}

		beeInventoryHandler = InventoryUtil.getInventoryHandler(world, pos.offset(inputSide), inputSide.getOpposite());

		if (beeInventoryHandler == null) {
			return;
		}

		apiaries = InventoryUtil.getInventoryHandlersOfTypeInDirection(world, pos.offset(apiarySide), TileApiary.class, apiarySide, false);

		if (apiaries.size() < 1) {
			return;
		}
		/*
		 * else { for (ApiaryInventoryHandler handler : apiaries) {
		 * handler.getEnvironment(); } }
		 */

		analyzers = InventoryUtil.getInventoryHandlersOfTypeInDirection(world, pos.offset(analyzerSide), TileAnalyzer.class, analyzerSide, false);

		if (trashManager == null) {
			trashManager = new TrashManager();
		}

		seedBankInventoryHandler = InventoryUtil.getInventoryHandler(world, pos.offset(inputSide).offset(EnumFacing.UP), outputSide.getOpposite());
		// System.out.println(String.format("Seed Bank Inventory found: %s.",
		// (seedBankInventoryPair != null)));

		if (seedBankInventoryHandler != null) {
			this.hasSeedBank = true;
		}

		trashInventoryHandler = InventoryUtil.getInventoryHandler(world, pos.offset(outputSide).offset(EnumFacing.UP), inputSide.getOpposite());
		// System.out.println(String.format("Trash Inventory found: %s.",
		// (trashInventoryPair != null)));

		if (trashInventoryHandler != null) {
			this.hasTrash = true;
		}

	}

	// TODO integrate properly
	private void purifyBee() {
		if (analyzers.isEmpty()) {
			return;
		}
		if (!BeeUtil.areAnalyzersDone(analyzers)) {
			for (InventoryHandler analyzer : analyzers) {
				InventoryUtil.moveStack(beeInventoryHandler, analyzer, 8);
			}
			return;
		}

		BeeUtil.clearApiaries(apiaries, beeInventoryHandler, lootInventoryHandler);

		analyzeBees();
		if (!BeeUtil.areAnalyzersDone(analyzers)) {
			return;
		}
		fillApiaries(beeInventoryHandler);
	}

	private void analyzeBees() {
		List<ItemStackAt> unAnalyzedBees = BeeUtil.getUnAnalyzedBees(beeInventoryHandler);

		if (instaAnalyze) {
			for (ItemStackAt stack : unAnalyzedBees) {
				IBee bee = BeeUtil.getBeeFromISTAT(stack);
				bee.analyze();
			}

			return;
		}
		BeeUtil.fillAnalyzers(unAnalyzedBees, beeInventoryHandler, analyzers);

		for (InventoryHandler analyzer : analyzers) {
			InventoryUtil.emptyInventoryCountTotalMoved(beeInventoryHandler, analyzer);
		}

	}

	private void fillApiaries(InventoryHandler beeInventoryPair) {
		List<InventoryHandler> usableApiaries = new ArrayList<>();

		for (InventoryHandler apiaryPair : this.apiaries) {
			if (apiaryPair.getItemHandler().getStackInSlot(0).isEmpty()) {
				usableApiaries.add(apiaryPair);
			}
		}

		if (!usableApiaries.isEmpty()) {
			for (InventoryHandler apiaryInventoryPair : usableApiaries) {
				fillApiary(apiaryInventoryPair, beeInventoryPair);
			}
		}
	}

	private void fillApiary(InventoryHandler apiaryInventoryPair, InventoryHandler beeInventoryPair) {
		if (currentCommand == null || currentCommand.length < 2 || currentCommand[1] == null || currentCommand[1].isEmpty()) {
			return;
		}
		List<ItemStackAt> bees = InventoryUtil.getStacksOfType(beeInventoryPair, ItemBeeGE.class);
		//
		Map<EnumBeeType, List<ItemStackAt>> beeMap = bees.stream().collect(Collectors.groupingBy(BeeUtil::getBeeTypeFromStackAtSlot));

		List<ItemStackAt> drones = beeMap.get(EnumBeeType.DRONE);
		List<ItemStackAt> princesses = beeMap.get(EnumBeeType.PRINCESS);

		Consumer<List<BeeScore>> thrashMethod = thrash -> trashManager.trashBees(thrash, lootInventoryHandler, beeInventoryPair, beeInventoryPair, deleteTrashBees);
		int cmdReturn = BeeUtil.purifyBee(drones, princesses, thrashMethod, apiaryInventoryPair, currentCommand[1]);
		if (cmdReturn == 0) {
			donePurifing();
		}
	}

	private void donePurifing() {
		// TODO inform finished?
		Log.info("Done purifing " + currentCommand[1]);
		if (currentCommand[0].equals("purifyAllBees")) {
			Set<String> availableSpesies = getAvailableBeeSpecies(beeInventoryHandler);
			List<String> sortedSpecies = availableSpesies.stream().sorted().collect(Collectors.toList());
			String currentSpecies = currentCommand[1];
			int indexPlus = sortedSpecies.indexOf(currentSpecies) + 1;
			if (indexPlus >= sortedSpecies.size()) {
				clearCurrentCommand();
				return;
			}
			if (currentCommand.length < 2) {
				currentCommand = Arrays.copyOf(currentCommand, 2);
			}
			currentCommand[1] = sortedSpecies.get(indexPlus);
			return;
		}
		clearCurrentCommand();
	}

	private void clearCurrentCommand() {
		currentCommand = new String[0];
	}

	private Set<String> getAvailableBeeSpecies(InventoryHandler beeInventoryPair) {
		Set<String> availableSpecieses = new HashSet<>();
		List<ItemStackAt> bees = InventoryUtil.getStacksOfType(beeInventoryPair, ItemBeeGE.class);
		for (ItemStackAt stack : bees) {
			IBee bee = BeeUtil.getBeeFromItemStack(stack);
			IAlleleSpecies allele = (IAlleleSpecies) bee.getGenome().getActiveAllele(EnumBeeChromosome.SPECIES);
			availableSpecieses.add(allele.getName());

			allele = (IAlleleSpecies) bee.getGenome().getInactiveAllele(EnumBeeChromosome.SPECIES);
			availableSpecieses.add(allele.getName());
		}
		return availableSpecieses;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		writeStringArrayToNbt(currentCommand, "currentCommand", compound);
		compound.setString("uuID", uuID);
		compound.setBoolean("deleteTrashBees", deleteTrashBees);
		compound.setInteger("tickCnt", tickCnt);
		compound.setInteger("tickModulo", tickModulo);
		return super.writeToNBT(compound);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		currentCommand = readStringArrayFromNBT("currentCommand", compound);
		uuID = compound.getString("uuID");
		deleteTrashBees = compound.getBoolean("deleteTrashBees");
		tickCnt = compound.getInteger("tickCnt");
		tickModulo = compound.getInteger("tickModulo");
		super.readFromNBT(compound);
	}

	private static void writeStringArrayToNbt(String[] stringArray, String tagName, NBTTagCompound compound) {
		NBTTagList tagList = new NBTTagList();
		for (int i = 0; i < stringArray.length; i++) {
			String s = stringArray[i];
			if (s != null) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setString(tagName + i, s);
				tagList.appendTag(tag);
			}
		}
		compound.setTag(tagName, tagList);
	}

	private static String[] readStringArrayFromNBT(String tagName, NBTTagCompound compound) {
		NBTTagList tagList = compound.getTagList(tagName, Constants.NBT.TAG_COMPOUND);
		if (tagList == null) {
			return null;
		}
		String[] stringArray = new String[tagList.tagCount()];
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound tag = tagList.getCompoundTagAt(i);
			String s = tag.getString(tagName + i);
			stringArray[i] = s;
		}
		return stringArray;
	}
}
