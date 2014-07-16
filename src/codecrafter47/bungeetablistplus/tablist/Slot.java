package codecrafter47.bungeetablistplus.tablist;

public class Slot {
	public String text;
	public int ping;
	public Slot(String text, int ping) {
		super();
		this.text = text;
		//this.ping = ping;//>1000?999:ping;
                if(ping < 0)this.ping = -1;
                else if(ping < 150)this.ping = 0;
                else if(ping < 300)this.ping = 150;
                else if(ping < 600)this.ping = 300;
                else if(ping < 1000)this.ping = 600;
                else this.ping = 1000;
	}
	public Slot(String text) {
		super();
		this.text = text;
		this.ping = 0;
	}
}
