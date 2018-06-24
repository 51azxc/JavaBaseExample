package other;

/*
 * 范型相关知识
 */
public class GenericsTest {

	public static void main(String[] args) {
		// 设置T的类型为Integer,V的类型为String
		G1<Integer, String> g1 = new G1<Integer, String>();
		g1.setT(111);
		g1.setV("aaa");
		print1(g1);
		
		//泛型数组
		String[] ss = func1("a","b","c","d");
		func2(ss);
		
		//泛型嵌套
		G2<G1<Integer,String>> g2 = new G2<G1<Integer,String>>();
		g2.setK(g1);
		System.out.println(g2.getK().getV());
	}

	// 通过通配符可以获取任意类型的泛型
	public static void print1(G1<?, ?> g) {
		System.out.println(g.getT() + " " + g.getV());
	}
	
	//这里只接受G1<T,V>中T为Number及其子类的类型，V为String或Object类型
	public static void print2(G1<? extends Number,? super String> g){
		System.out.println(g.getT()+" "+g.getV());
	}
	
	//接受不限个数的参数
	@SafeVarargs
	public static <T> T[] func1(T...args){
		return args;
	}

	public static <T> void func2(T ts[]){
		for(T t:ts){
			System.out.println(t);
		}
	}
	
}

/*
 * 普通范型
 */
class G1<T, V> {
	private T t;
	private V v;

	public T getT() {
		return t;
	}
	public void setT(T t) {
		this.t = t;
	}
	public V getV() {
		return v;
	}
	public void setV(V v) {
		this.v = v;
	}
}

/*
 * 泛型嵌套
 */
class G2<K>{
	private K k;
	public K getK() {
		return k;
	}
	public void setK(K k) {
		this.k = k;
	}
}
