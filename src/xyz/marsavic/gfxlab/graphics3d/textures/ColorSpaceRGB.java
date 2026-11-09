package xyz.marsavic.gfxlab.graphics3d.textures;

import xyz.marsavic.functions.F1;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.graphics3d.Material;


public class ColorSpaceRGB implements F1<Material, Vector> {
	
	@Override
	public Material at(Vector uv) {
		return Material.matte(Color.rgb(uv.x(), uv.y(), 1 - uv.x() - uv.y()));
	}
	
}
