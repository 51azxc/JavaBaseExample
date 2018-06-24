package gof.singleton;

public class Holder {
	private Holder() {}
	
	private static class SingletonHolder {
		private final static Holder holder = new Holder();
	}
	
	public static Holder getInstance() {
		return Holder.SingletonHolder.holder;
	}
}
