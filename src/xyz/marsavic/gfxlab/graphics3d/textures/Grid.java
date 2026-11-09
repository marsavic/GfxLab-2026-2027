package xyz.marsavic.gfxlab.graphics3d.textures;


import xyz.marsavic.functions.F1;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.graphics3d.Material;


public class Grid implements F1<Material, Vector> {
	
	private final Vector size, sizeLine;
	private final Material materialCell, materialLine;
	
	// transient
	private final Vector sizeLineHalf;
	
	
	
	public Grid(Vector size, Vector sizeLine, Material materialCell, Material materialLine) {
		this.size = size;
		this.sizeLine = sizeLine;
		this.materialCell = materialCell;
		this.materialLine = materialLine;
		sizeLineHalf = sizeLine.div(2);
	}
	
	
	@Override
	public Material at(Vector uv) {
		Vector p = uv.add(sizeLineHalf).mod(size);
		return p.dominates(sizeLine) ? materialCell : materialLine;
	}
	
	
	public static Grid standardQuarter(Color color) {
		return new Grid(
				Vector.xy(0.25, 0.25),
				Vector.xy(0.01, 0.01),
				Material.matte(color),
				Material.matte(color.mul(0.75))
		);
	}
	
	
	public static Grid standardUnit(Color color) {
		return new Grid(
				Vector.UNIT_DIAGONAL,
				Vector.xy(1.0/64),
				Material.matte(color),
				Material.matte(color.mul(0.75))
		);
	}
}
