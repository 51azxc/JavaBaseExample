package algorithm;

import javax.swing.tree.TreeNode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

public class LeetCodeTest {
	public static void main(String[] args) {
		int[] nums = new int[] { -1, -1, 1 };
		System.out.println(subarraySum(nums, 0));
	}

	/*
	 * 1. 两数之和 Two Sum
	 * 给定一个整数数组 nums 和一个目标值 target，请你在该数组中找出和为目标值的那 两个 整数，并返回他们的数组下标。
	 */
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

	/*
	 * 2. 两数相加 Add Two Numbers
	 * 给出两个非空的链表用来表示两个非负的整数。其中它们各自的位数是按照逆序的方式存储的，并且它们的每个节点只能存储一位数字。
	 * 如果，我们将这两个数相加起来，则会返回一个新的链表来表示它们的和。
	 */
	static class ListNode {
		int val;
		ListNode next;
		public ListNode(int val) { this.val = val; }
	}
	public static ListNode addTwoNumbers(ListNode l1, ListNode l2) {
		ListNode head = new ListNode(0), node = head;
		int remain = 0;
		while (l1 != null || l2 != null) {
			int val = remain;
			if (l1 != null) {
				val += l1.val;
				l1 = l1.next;
			}
			if (l2 != null) {
				val += l2.val;
				l2 = l2.next;
			}
			remain = val / 10;
			node.next = new ListNode(val % 10);
			node = node.next;
		}
		if (remain == 1) {
			node.next = new ListNode(remain);
		}
		return head.next;
	}

	/*
	 * 3.无重复字符的最长子串 Longest Substring Without Repeating Characters
	 * 给定一个字符串，找出其中不含有重复字符的最长子串的长度
	 */
	public static int lengthOfLongestSubstring(String s) {
		if (s.length() == 1) return 1;
		char[] chars = s.toCharArray();
		int total = 0, sum = 0;
		StringBuilder sb = new StringBuilder(chars.length);
		for (int i = 0; i < chars.length; i++) {
			String str = String.valueOf(chars[i]);
			if (sb.toString().contains(str)) {
				total = total > sum ? total : sum;
				sb.delete(0,sb.indexOf(str) + 1);
				sum = sb.length();
			}
			sum ++;
			sb.append(chars[i]);
		}
		total = total > sum ? total : sum;
		return total;
	}

	/*
	 * 4. 寻找两个正序数组的中位数 Median of Two Sorted Arrays
	 * 给定两个大小为 m 和 n 的正序（从小到大）数组 nums1 和 nums2, 找出这两个数组的中位数。
	 * 中位数可以保证这两个数组在偶数的情况下左半部分等于右半部分。并且左半部分的最大值 <= 右半部分的最小值。
	 * 因为这两个数组已经有序，所以只需要保证nums1[i] >= num2[j-1] && nums1[i-1] <= nums[j]
	 */
	public static double findMedianSortedArrays(int[] nums1, int[] nums2) {
		int m = nums1.length;
		int n = nums2.length;
		// 需要保证 m <= n
		if (m > n) {
			return findMedianSortedArrays(nums2, nums1);
		}
		int left = 0, right = m;
		while (left <= right) {
			//取nums1的中间数
			int i = (left + right) / 2;
			//中位数可以保证左半部分长度 = 右半部分的长度
			//因此 i + j = m - i + n - j
			int j = (m + n + 1) / 2 - i;
			if (i != m && j != 0 && nums1[i] < nums2[j - 1]) {
				left = i + 1;
			} else if (i != 0 && j != n && nums1[i - 1] > nums2[j]) {
				right = i - 1;
			} else {    //边界处理
				int maxLeft = 0;
				if (i == 0) {
					maxLeft = nums2[j - 1];
				} else if (j == 0) {
					maxLeft = nums1[i - 1];
				} else {
					maxLeft = Math.max(nums1[i - 1], nums2[j - 1]);
				}
				if ((m + n) % 2 == 1) { return maxLeft; }   //奇数直接返回左半数字最大值即可

				int minRight = 0;
				if (i == m) {
					minRight = nums2[j];
				} else if (j == n) {
					minRight = nums1[i];
				} else {
					minRight = Math.min(nums1[i], nums2[j]);
				}
				return (maxLeft + minRight) * 0.5;
			}
		}
		return 0;
	}

	/*
	 * 25. K 个一组翻转链表 Reverse Nodes in k-Group
	 * 给定一个链表，每K个节点一组进行翻转，如果节点总数不是K的整数倍，则最后剩余节点保持顺序不变
	 *
	 * head = 1 -> 2 -> 3 -> 4 -> 5, k = 3
	 * dummy -> 1 -> 2 -> 3 -> 4 -> 5
	 * dummy = pre = end
	 * loop k
	 * dummy = pre -> 1 -> 2 -> 3 (end) -> 4 -> 5
	 * dummy = pre -> 1 (start) -> 2 -> 3 (end) 4(next) -> 5
	 * dummy = pre -> 3 (end) -> 2 -> 1 (start) 4(next) -> 5
	 * dummy -> 3 -> 2 -> 1 (pre | end) -> 4 -> 5
	 */
	public static ListNode reverseKGroup(ListNode head, int k) {
		//使用一个节点来关联住头节点
		ListNode dummy = new ListNode(0);
		dummy.next = head;
		//反转链表的前一个节点
		ListNode pre = dummy;
		//反转链表的后一个节点
		ListNode end = dummy;
		while (end.next != null) {
			//查找需要反转的后一个节点
			for (int i = 0; i < k && end != null; i++) {
				end = end.next;
			}
			//链表比k值短，则不翻转
			if (end == null) break;
			//获取翻转链表的第一个节点
			ListNode start =pre.next;
			//获取翻转链表的后边的第一个节点
			ListNode next = end.next;
			//开始翻转
			end.next = null;
			ListNode node = null;
			ListNode cur = start;
			while (cur != null) {
				ListNode n = cur.next;
				cur.next = node;
				node = cur;
				cur = n;
			}
			//将翻转后的链表第一个节点接住原链表的前置节点
			pre.next = node;
			//继续开始寻找下一个需要翻转的前置节点
			start.next = next;
			pre = start;
			end = pre;
		}
		return dummy.next;
	}

	/*
	 *  102. 二叉树的层序遍历 Binary Tree Level Order Traversal
	 *  二叉树层序遍历(每层从左到右访问所有节点)
	 *   3			[
     *  / \			 [3],
     * 9  20	->   [9, 20],
     *   /  \		 [15, 7]
     *  15   7      ]
	 */
	public List<List<Integer>> levelOrder(TreeNode root) {
		List<List<Integer>> list = new ArrayList<>();
		list.stream();
		if (root == null) return list;
		Queue<TreeNode> queue = new LinkedList<>();
		queue.offer(root);
		while (!queue.isEmpty()) {
			int size = queue.size();
			List<Integer> subList = new ArrayList<>();
			while (size-- != 0) {
				TreeNode node = queue.poll();
				subList.add(node.val);
				if (node.left != null) { queue.offer(node.left); }
				if (node.right != null) { queue.offer(node.right); }
				//Optional.ofNullable(node.left).ifPresent(queue::offer);
				//Optional.ofNullable(node.right).ifPresent(queue::offer);
			}
			list.add(subList);
		}
		return list;
	}

	/*
	 * 136. 只出现一次的数字 Single Number
	 * 给定一个非空整数数组，除了某个元素只出现一次以外，其余每个元素均出现两次。找出那个只出现了一次的元素。
	 *
     * 交换律：a ^ b ^ c <=> a ^ c ^ b
     * 任何数于0异或为任何数 0 ^ n => n
     * 相同的数异或为0: n ^ n => 0
	 */
	public static int singleNumber(int[] nums) {
		int result = 0;
		for (int i = 0; i < nums.length; i++) {
			result ^= nums[i];
		}
		return result;
	}

	/*
	 * 152. 乘积最大子数组 Maximum Product Subarray
	 * 从nums整数数组中找出乘积最大的连续子数组
	 *
	 * 遍历数组时计算当前最大值iMax
	 * 如果存在负数，最大值会变成最小值，而最小值会变成最大值。因此需要持有一个当前最小值iMin
	 * 当出现负数时就将iMax与iMin交换一下
	 */
	public static int maxProduct(int[] nums) {
		int max = Integer.MIN_VALUE, iMax = 1, iMin = 1;
		for(int i=0; i<nums.length; i++){
			if(nums[i] < 0){
				int tmp = iMax;
				iMax = iMin;
				iMin = tmp;
			}
			iMax = Math.max(iMax*nums[i], nums[i]);
			iMin = Math.min(iMin*nums[i], nums[i]);
			max = Math.max(max, iMax);
		}
		return max;
	}

	/* 560. 和为K的子数组 Subarray Sum Equals K
	 * 给定一个整数数组和一个整数 k，你需要找到该数组中和为 k 的连续的子数组的个数。
	 *
	 * 使用map来存储连续和出现的次数。
	 */
	public static int subarraySum(int[] nums, int k) {
		int total = 0;
		Map<Integer, Integer> map = new HashMap<>(nums.length);
		int sum = 0;
		map.put(sum, 1);
		for (int i = 0; i < nums.length; i++) {
			sum += nums[i];
			if (map.containsKey(sum - k)) {
				total += map.get(sum - k);
			}
			map.put(sum, map.getOrDefault(sum, 0) + 1);
		}
		return total;
	}

	/*
	 * 实现pow(x, n)，计算x的n次幂函数
	 *
	 */
	public static double myPow(double x, int n) {
		double result = 1.0;
		if (x == 1 || x == 0) return result;
		if (n == 0) return 1;
		for (int i = n; i != 0; i /= 2) {
			if (i % 2 != 0) result *= x;
			x *= x;
		}
		return  n > 0 ? result : 1 / result;
	}

	/*
	 * 如果一个数每个位数拆开之后的平方和最后能得到1，则是个快乐数。
	 */
	public static boolean isHappy(int n) {
		if (n <= 0) return false;
		if (n == 1) return true;
		Function<Integer, Integer> func = (x) -> Integer.toString(x).chars()
				.parallel().map(c -> c - '0').map(i -> i*i).reduce(0, (a, b) -> a + b);
		int result = func.apply(n);
		while (result != 1) {
			result = func.apply(result);
			if (result < 10 && result > 1) return false;
		}
		return true;
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

	static class TreeNode {
		int val;
		TreeNode left;
		TreeNode right;

		TreeNode(int x) {
			val = x;
		}
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

