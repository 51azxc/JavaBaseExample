package other;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/*
 * 集合相关知识
 */
public class CollectionsTest {

	public static void main(String[] args) {
	}
	
	/*
	 * 遍历Map
	 */
	public void mapIterator() {
		Map<String,String> map = new HashMap<String, String>();
		map.put("1", "a");
		map.put("2", "s");
		map.put("3", "d");
		//1. 利用entrySet遍历
		Iterator<Entry<String, String>> it1 = map.entrySet().iterator();
		while(it1.hasNext()){
			Map.Entry<String, String> mn = ((Entry<String, String>) it1.next());
			String key = mn.getKey();
			String value = mn.getValue();
			System.out.println("key: "+key+" value: "+value);
		}
		//2.1  先遍历key值
		for(Iterator<String> it2 = map.keySet().iterator(); it2.hasNext();){
			Object o = it2.next();
			System.out.println("key: "+o);
		}
		//2.2 遍历value值
		for(Iterator<String> it3 = map.values().iterator(); it3.hasNext();){
			Object o = it3.next();
			System.out.println("value: "+o);
		}
		//3.利用keySet遍历
		for(Object o:map.keySet()){
			System.out.println("key: "+o+" value: "+map.get(o));
		}
	}
	
	/*
	 * 列表与数组互转
	 */
	public void listConvertTo() {
		String[] ss = new String[]{"a","d","c","b"};
		//String[] -> list<String>
		ArrayList<String> list = new ArrayList<String>();
		for(int i=0,len=ss.length; i<len; i++){
			list.add(ss[i]);
		}
		
		//ArrayList<String> list2 = Arrays.asList(list);
		//list<String> -> String[]
		String[] lists = new String[list.size()];
		list.toArray(lists);
		//给数组排序
		Arrays.sort(lists);
	}
	
	/*
	 * 使用Comparator接口排序
	 */
	public void sortUseComparator() {
		class Student{
			String name;
			int age;
			public Student(String name, int age) {
				this.name = name;
				this.age = age;
			}
			public String getName() {
				return name;
			}
			public int getAge() {
				return age;
			}
		}
		Comparator<Student> comparator = new Comparator<Student>() {
			public int compare(Student o1, Student o2) {
				if(!o1.getName().equals(o2.getName())){
					//大于0则为o1属性大于o2属性，小于0则相反，等于0则相等
					return o1.getName().compareTo(o2.getName());
				}else if(o1.getAge()!=o2.getAge()){
					//名字不相等则按年龄排序
					return o1.getAge() - o2.getAge();
				}
				return 0;
			}
		};

		Student s1 = new Student("a",13);
		Student s2 = new Student("b",11);
		ArrayList<Student> sl = new ArrayList<Student>();
		sl.add(s1);
		sl.add(s2);
		//排序
		Collections.sort(sl,comparator);
	}
	
	/*
	 * 使用Comparable接口排序
	 */
	public void sortUseComparable() {
		class Student implements Comparable<Student>{
		    String name;
			int age;
			public Student(String name, int age) {
				this.name = name;
				this.age = age;
			}
			
			public String getName() {
				return name;
			}

			public int getAge() {
				return age;
			}

			@Override
		    public int compareTo(Student o) {
				if(!this.getName().equals(o.getName())){
					return this.getName().compareTo(o.getName());
				}else if(this.getAge()!=o.getAge()){
					return this.getAge() - o.getAge();
				}
				return 0;
			}
		}

		Student s1 = new Student("a",13);
		Student s2 = new Student("b",11);
		ArrayList<Student> sl = new ArrayList<Student>();
		sl.add(s1);
		sl.add(s2);
		//通过默认方法进行排序
		Collections.sort(sl);
	}
	
	
	

}
