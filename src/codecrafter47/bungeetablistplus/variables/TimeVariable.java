package codecrafter47.bungeetablistplus.variables;

import codecrafter47.bungeetablistplus.api.Variable;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeVariable implements Variable {

    private final SimpleDateFormat format;

    public TimeVariable(String format) {
        this.format = new SimpleDateFormat(format);
    }

    @Override
    public String getReplacement(String args) {
        if (args == null) {
            return format.format(Calendar.getInstance().getTime());
        } else {
            SimpleDateFormat format2 = new SimpleDateFormat(args);
            return format2.format(Calendar.getInstance().getTime());
        }
    }

}
