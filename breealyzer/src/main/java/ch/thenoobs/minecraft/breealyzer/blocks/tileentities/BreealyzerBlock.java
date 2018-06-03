package ch.thenoobs.minecraft.breealyzer.blocks.tileentities;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

public class BreealyzerBlock extends CoreTileEntityBlock<BreealyzerTE> {

//	public AutomaticMoverBlock(Material material, String name) {
//		super(material, name);
//	}
	
	public BreealyzerBlock() {
		super(Material.ROCK, "breealyzer");
	}

	@Override
	public Class<BreealyzerTE> getTileEntityClass() {
		return BreealyzerTE.class;
	}

	@Override
	public BreealyzerTE createTileEntity(World world, IBlockState state) {
		return new BreealyzerTE();
	}

}
