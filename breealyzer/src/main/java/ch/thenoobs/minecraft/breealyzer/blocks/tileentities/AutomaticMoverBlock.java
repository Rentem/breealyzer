package ch.thenoobs.minecraft.breealyzer.blocks.tileentities;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

public class AutomaticMoverBlock extends CoreTileEntityBlock<AutomaticMoverTE> {

//	public AutomaticMoverBlock(Material material, String name) {
//		super(material, name);
//	}
	
	public AutomaticMoverBlock() {
		super(Material.ROCK, "automaticMover");
	}

	@Override
	public Class<AutomaticMoverTE> getTileEntityClass() {
		return AutomaticMoverTE.class;
	}

	@Override
	public AutomaticMoverTE createTileEntity(World world, IBlockState state) {
		return new AutomaticMoverTE();
	}

}
