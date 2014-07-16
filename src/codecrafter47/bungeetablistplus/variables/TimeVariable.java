package codecrafter47.bungeetablistplus.variables;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class TimeVariable extends Variable{
	private final SimpleDateFormat format;

	public TimeVariable(String name, String format) {
		super(name);
		this.format = new SimpleDateFormat(format);
	}

	@Override
	public String getReplacement() {
		return format.format(Calendar.getInstance().getTime());
	}

}
