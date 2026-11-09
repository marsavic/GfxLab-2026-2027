package xyz.marsavic.gfxlab.graphics3d.textures;

import xyz.marsavic.functions.F1;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.graphics3d.Material;


public class Checkerboard implements F1<Material, Vector> {
	
	private final Vector size;
	private final Material materialA, materialB;
	
	
	
	public Checkerboard(Vector size, Material materialA, Material materialB) {
		this.size = size;
		this.materialA = materialA;
		this.materialB = materialB;
	}

	
	@Override
	public Material at(Vector uv) {
		Vector p = uv.div(size).floor();
		return ((p.xInt() ^ p.yInt()) & 1) == 0 ? materialB : materialA;
	}


	public static Checkerboard standardUnit(Color color) {
		return new Checkerboard(
				Vector.UNIT_DIAGONAL,
				Material.matte(color),
				Material.matte(color.mul(0.75))
		);
	}
}
