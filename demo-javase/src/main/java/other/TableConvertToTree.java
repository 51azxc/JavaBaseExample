package other;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/*
 * 将表结构转换成树形结构
 */
public class TableConvertToTree {
	public static void main(String[] args) {
		TreeA a1 = new TreeA(202, 2, "營業第二課");
		TreeA a2 = new TreeA(201, 2, "營業第一課");
		TreeA a3 = new TreeA(102, 1, "營業第二課");
		TreeA a4 = new TreeA(101, 1, "營業第二課");
		TreeA a5 = new TreeA(2, 0, "東京支店");
		TreeA a6 = new TreeA(1, 0, "北海道支店");
		List<TreeA> list = Arrays.asList(a1, a2, a3, a4, a5, a6);
		Map<Integer, List<TreeA>> groups = list.stream().collect(Collectors.groupingBy(TreeA::getParentId));
		list.stream().map(a->{
			a.children = groups.get(a.id);
			return a;
		}).filter(a->a.getParentId()==0).forEach(System.out::println);
	}

}

class TreeA {
	public int id;
	public int parentId;
	public String name;
	public List<TreeA> children;

	public TreeA(int id, int parentId, String name) {
		this.id = id;
		this.parentId = parentId;
		this.name = name;
	}

	@Override
	public String toString() {
		StringBuffer sb1 = new StringBuffer();
		sb1.append("{\n");
		sb1.append("  id: "+id+",\n");
		sb1.append("  parentId: "+parentId+",\n");
		sb1.append("  name: "+name);
		if (children != null && !children.isEmpty()) {
			StringBuilder sb2 = new StringBuilder();
			sb2.append(",\n  children: [");
			for (TreeA a : children) {
				sb2.append(a.toString());
				sb2.append(",");
			}
			sb2.append("]");
			sb1.append(sb2);
		} else {
			sb1.append("\n}");
		}
		return sb1.toString();
	}
	public int getParentId() {
		return parentId;
	}
}