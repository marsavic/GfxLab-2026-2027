package xyz.marsavic.gfxlab.entry_points;

import java.util.Scanner;


/** Reads the parameters from the command line arguments, and from the standard input when the arguments run out. */
class ParameterReader {
	private final String[] args;
	private int i = 0;
	private final Scanner scanner = new Scanner(System.in);
	
	public ParameterReader(String[] args) {
		this.args = args;
	}
	
	public String next(String name) {
		System.out.print(name + ": ");
		String value;
		if (i < args.length) {
			value = args[i];
			System.out.println(value);
		} else {
			value = scanner.nextLine();
		}
		i++;
		return value;
	}
	
}
