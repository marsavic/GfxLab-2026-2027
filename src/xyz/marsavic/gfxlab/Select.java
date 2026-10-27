package xyz.marsavic.gfxlab;

public class Select {
	
	private Select() {}
	
	public static <T> T kth(int k) {
		return null;
	}
	
	public static <T> T kth(int k, T o0) {
		if (k == 0) {
			return o0;
		}
		return null;
	}
	
	public static <T> T kth(int k, T o0, T o1) {
		return switch (Math.floorMod(k, 2)) {
			case 0 -> o0;
			case 1 -> o1;
			default -> null;
		};
	}
	
	public static <T> T kth(int k, T o0, T o1, T o2) {
		return switch (Math.floorMod(k, 3)) {
			case 0 -> o0;
			case 1 -> o1;
			case 2 -> o2;
			default -> null;
		};
	}
	
	public static <T> T kth(int k, T o0, T o1, T o2, T o3) {
		return switch (Math.floorMod(k, 4)) {
			case 0 -> o0;
			case 1 -> o1;
			case 2 -> o2;
			case 3 -> o3;
			default -> null;
		};
	}
	
	public static <T> T kth(int k, T o0, T o1, T o2, T o3, T o4) {
		return switch (Math.floorMod(k, 5)) {
			case 0 -> o0;
			case 1 -> o1;
			case 2 -> o2;
			case 3 -> o3;
			case 4 -> o4;
			default -> null;
		};
	}
	
	public static <T> T kth(int k, T o0, T o1, T o2, T o3, T o4, T o5) {
		return switch (Math.floorMod(k, 6)) {
			case 0 -> o0;
			case 1 -> o1;
			case 2 -> o2;
			case 3 -> o3;
			case 4 -> o4;
			case 5 -> o5;
			default -> null;
		};
	}
	
	public static <T> T kth(int k, T o0, T o1, T o2, T o3, T o4, T o5, T o6) {
		return switch (Math.floorMod(k, 7)) {
			case 0 -> o0;
			case 1 -> o1;
			case 2 -> o2;
			case 3 -> o3;
			case 4 -> o4;
			case 5 -> o5;
			case 6 -> o6;
			default -> null;
		};
	}
	
	public static <T> T kth(int k, T o0, T o1, T o2, T o3, T o4, T o5, T o6, T o7) {
		return switch (Math.floorMod(k, 8)) {
			case 0 -> o0;
			case 1 -> o1;
			case 2 -> o2;
			case 3 -> o3;
			case 4 -> o4;
			case 5 -> o5;
			case 6 -> o6;
			case 7 -> o7;
			default -> null;
		};
	}

}
