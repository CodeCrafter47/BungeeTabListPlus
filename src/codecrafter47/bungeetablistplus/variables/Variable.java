package codecrafter47.bungeetablistplus.variables;

public abstract class Variable {
	private final String name;
	
	public Variable(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public abstract String getReplacement();
}
