package gof.singleton;

//懒汉模式
public class Lazy {
	private static Lazy lazy;
	
	private Lazy() {}
	
	public static synchronized Lazy getInstance() {
		if (lazy == null) {
			lazy = new Lazy();
		}
		return lazy;
	}
}
