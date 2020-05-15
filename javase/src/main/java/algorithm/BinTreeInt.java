package algorithm;

import java.util.Random;

public class BinTreeInt {
	
	private BinNode root;

	// 递归创建二叉树
	public void buildTree(BinNode node, int data) {
		// 如果根节点为空，创建根节点
		if (root == null) {
			root = new BinNode(data);
		} else {
			// 插入到左子树
			if (data < node.data) {
				// 左节点为空，直接创建值为data的左节点
				if (node.leftChild == null) {
					node.leftChild = new BinNode(data);
				} else {
					// 左节点不为空，调用buildTree函数插到左子树中
					buildTree(node.leftChild, data);
				}
			} else {
				if (node.rightChild == null) {
					node.rightChild = new BinNode(data);
				} else {
					buildTree(node.rightChild, data);
				}
			}
		}
	}

	// 前序遍历二叉树
	public void preOrder(BinNode node) {
		if (node != null) {
			System.out.print(node.data);
			System.out.print("\t");
			preOrder(node.leftChild);
			preOrder(node.rightChild);
		}
	}

	// 中序遍历二叉树
	public void inOrder(BinNode node) {
		if (node != null) {
			inOrder(node.leftChild);
			System.out.print(node.data);
			System.out.print("\t");
			inOrder(node.rightChild);
		}
	}
	
	//后序遍历二叉树
	public void postOrder(BinNode node) {
		if (node != null) {
			postOrder(node.leftChild);
			postOrder(node.rightChild);
			System.out.print(node.data);
			System.out.print("\t");
		}
	}
	
	public static void main(String[] args) {
		int[] a = new Random().ints(10, 0, 100).toArray();
		System.out.print("原始数组: ");
		for (int i:a) {
			System.out.print(i+"\t");
		}
		System.out.println();
		BinTreeInt binTree = new BinTreeInt();
		for (int i = 0; i < a.length; i++) {
			binTree.buildTree(binTree.root, a[i]);
		}
		System.out.print("前序遍历: ");
		binTree.preOrder(binTree.root);
		System.out.println();
		System.out.print("中序遍历: ");
		binTree.inOrder(binTree.root);
		System.out.println();
		System.out.print("后序遍历: ");
		binTree.postOrder(binTree.root);
	}

	static class BinNode {
		// 左节点
		public BinNode leftChild;
		// 右节点
		public BinNode rightChild;
		// 节点对应的值
		public int data;

		public BinNode(int data) {
			this.leftChild = null;
			this.rightChild = null;
			this.data = data;
		}
	}
}

