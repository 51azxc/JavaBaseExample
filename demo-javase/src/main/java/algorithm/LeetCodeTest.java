package algorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

public class LeetCodeTest {
	public static void main(String[] args) {
		System.out.println(titleToNumber("AA"));
	}

	public static int[] twoSum(int[] nums, int target) {
		int[] res = new int[2];
		HashMap<Integer, Integer> map = new HashMap<>();
		for (int i = 0; i < nums.length; i++) {
			int value = target - nums[i];
			if (map.get(value) == null) {
				map.put(nums[i], i);
			} else {
				res[0] = map.get(value);
				res[1] = i;
				break;
			}
		}
		return res;
	}

	public static int removeElement(int[] nums, int val) {
		int j = 0;
		for (int i = 0; i < nums.length; i++) {
			if (nums[i] == val) {
				continue;
			}
			nums[j] = nums[i];
			j++;
		}
		return j;
	}

	public static int removeDuplicates(int[] nums) {
		int j = 0;
		for (int i = 0; i < nums.length; i++) {
			if (nums[i] != nums[j]) {
				nums[++j] = nums[i];
			}
		}
		return j + 1;
	}

	public static int hammingDistance(int x, int y) {
		int i = 0;
		while (x != y) {
			x = x >> 1;
			i++;
		}
		return i;
	}

	public static TreeNode sortedArrayToBST(int[] nums) {
		if (nums.length == 0) {
			return null;
		}
		int mid = nums.length / 2;
		TreeNode tn = new TreeNode(nums[mid]);
		for (int i = mid - 1; i > 0; i--) {
			TreeNode leftNode = new TreeNode(nums[i]);
			tn.left = leftNode;
			tn = leftNode;
		}
		for (int i = mid + 1; i < nums.length; i++) {
			TreeNode rightNode = new TreeNode(nums[i]);
			tn.right = rightNode;
			tn = rightNode;
		}
		return tn;
	}

	public static String[] findRelativeRanks(int[] nums) {
		TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o2 - o1;
			}
		});
		for (int i = 0; i < nums.length; i++) {
			map.put(nums[i], i);
		}
		String[] ss = new String[map.size()];
		int j = 1;
		for (Integer i : map.values()) {
			if (j == 1) {
				ss[i] = "Gold Medal";
			} else if (j == 2) {
				ss[i] = "Silver Medal";
			} else if (j == 3) {
				ss[i] = "Bronze Medal";
			} else {
				ss[i] = String.valueOf(j);
			}
			j++;
		}
		return ss;
	}

	public static String[] findWords(String[] words) {
		if (words.length == 0)
			return new String[] {};
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < words.length; i++) {
			if (words[i].toLowerCase().matches("[qwertyuiop]+") || words[i].toLowerCase().matches("[asdfghjkl]+")
					|| words[i].toLowerCase().matches("[zxcvbnm]+")) {
				list.add(words[i]);
			}
		}
		return list.stream().toArray(String[]::new);
	}

	public static char findTheDifference(String s, String t) {
		ArrayList<Character> list = new ArrayList<Character>();
		for (int i = 0; i < t.length(); i++) {
			list.add(Character.valueOf(t.charAt(i)));
		}
		for (int i = 0; i < s.length(); i++) {
			list.remove(Character.valueOf(s.charAt(i)));
		}
		return list.get(0);
	}

	public static int[] constructRectangle(int area) {
		int[] res = new int[2];
		int small = area;
		for (int i = area; i >= 1; i--) {
			if (area % i == 0) {
				int j = area / i;
				int k = i - j;
				if (k >= 0 && k < small) {
					res[0] = i;
					res[1] = j;
					small = k;
				}
			}
		}
		return res;
	}

	public static boolean canConstruct(String ransomNote, String magazine) {
		boolean b = true;
		HashMap<Character, Integer> map = new HashMap<>();
		for (char c : magazine.toCharArray()) {
			int count = map.getOrDefault(c, 0) + 1;
			map.put(c, count);
		}
		for (char c : ransomNote.toCharArray()) {
			int count = map.getOrDefault(c, 0) - 1;
			if (count < 0) {
				b = false;
				break;
			} else {
				map.put(c, count);
			}
		}
		return b;
	}

	public static int sumOfLeftLeaves(TreeNode root) {
		int sum = 0;
		if (root.left != null) {
			sumOfLeftLeaves(root.left);
			if (root.left == null && root.right == null) {
				sum += root.val;
			}
		}
		sumOfLeftLeaves(root.right);
		return sum;
	}

	public static int[] intersection(int[] nums1, int[] nums2) {
		Set<Integer> n1 = new HashSet<Integer>();
		for (int num1 : nums1) {
			n1.add(num1);
		}
		Set<Integer> n2 = new HashSet<Integer>();
		for (int num2 : nums2) {
			if (n1.contains(num2)) {
				n2.add(num2);
			}
		}
		int[] res = new int[n2.size()];
		int i = 0;
		for (int j : n2) {
			res[i] = j;
			i++;
		}
		return res;
	}

	public static int titleToNumber(String s) {
		int sum = 0;
		for (char c : s.toCharArray()) {
			sum = sum * 26 + (c - 'A') + 1;
		}
		return sum;
	}
}

class TreeNode {
	int val;
	TreeNode left;
	TreeNode right;

	TreeNode(int x) {
		val = x;
	}
}
