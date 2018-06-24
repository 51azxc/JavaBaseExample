package gof.singleton;

//双检索模式
public class DSL {
	//java中使用双重检查锁定机制,
	//由于Java编译器和JIT的优化的原因系统无法保证我们期望的执行次序
	private volatile static DSL dsl;
	
	private DSL() {}
	
	public static DSL getInstance() {
		if (dsl == null) {
			synchronized (DSL.class) {
				if (dsl == null) {
					dsl = new DSL();
				}
			}
		}
		return dsl;
	}
}
