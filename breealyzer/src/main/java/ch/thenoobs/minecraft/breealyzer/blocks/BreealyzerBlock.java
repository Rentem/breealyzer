package ch.thenoobs.minecraft.breealyzer.blocks;

import ch.thenoobs.minecraft.breealyzer.blocks.tileentities.BreealyzerTE;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BreealyzerBlock extends CoreTileEntityBlock<BreealyzerTE> {

	public static PropertyEnum<EnumFacing> FACING = BlockDirectional.FACING;
//	public AutomaticMoverBlock(Material material, String name) {
//		super(material, name);
//	}
	
	public BreealyzerBlock() {
		super(Material.ROCK, "breealyzer");
		
		this.setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}
	
	@Override
	public Class<BreealyzerTE> getTileEntityClass() {
		return BreealyzerTE.class;
	}
	
	@Override
	public BreealyzerTE createTileEntity(World world, IBlockState state) {
	
		return new BreealyzerTE();
	}
	
	@Override
	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(FACING, EnumFacing.values()[(meta%EnumFacing.values().length)]);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).ordinal();
	}
	
	@Override
	public EnumFacing[] getValidRotations(World world, BlockPos pos) {
		//return EnumFacing.HORIZONTALS;
		return EnumFacing.values();
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		return this.getDefaultState().withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer));
	}
	
	/**
	 * A straightforward (vanilla-conforming) implementation for planar machines would be to rotate on only the axis
	 * requested, but in true contract-free form, we just loop through all valid states.
	 */
	@Override
	public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
		IBlockState cur = world.getBlockState(pos);
		
		int next = cur.getValue(FACING).ordinal()+1;
		if (next>=EnumFacing.values().length) next=0;
		
		world.setBlockState(pos, cur.withProperty(FACING, EnumFacing.values()[next]));
		return true;
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, placer, stack);
	}
}
