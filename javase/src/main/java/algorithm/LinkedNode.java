package algorithm;

import java.util.Stack;

/*
 * 单链表相关操作
 */
public class LinkedNode {

	public static void main(String[] args) {
		System.out.println("----------origin node----------");
        getNode(setNode());
        
        System.out.println("----------reverse node----------");
        getNode(reverse1(setNode()));
        getNode(reverse2(setNode()));
        getNode(reverse3(setNode()));
        
        System.out.println("----------get middle node----------");
        Node n = setNode();
        Node middle = getMiddleNode(n);
        System.out.println(middle.name);
        
        System.out.println("----------delete node----------");
        deleteNode(middle);
        getNode(n);
        
	}
	
	//链表节点结构
	static class Node {
		public String name;
		public Node next;
	}

	//生成单链表
	public static Node setNode() {
		Node head = null, node = null, next = null;
        for (int i = 97; i < 104; i++) {
            next = new Node();
            next.name = String.valueOf((char)i);
            if (head == null) {
                head = node = next;
            } else {
                node.next = next;
            }
            node = next;
        }
        next.next = null;
        return head;
	}
	
	//读取单链表数据
	public static void getNode(Node n) {
		Node node = n;
        while (node != null) {
            System.out.print(node.name + " ");
            node = node.next;
        }
        System.out.println();
	}
	
	//利用三个节点指针反转单链表
	public static Node reverse1(Node n) {
		Node head = n, node = null, next = null;
        while (head != null) {
            next = head.next;
            head.next = node;
            node = head;
            head = next;
        }
        return node;
	}
	
	//利用栈反转单链表
	public static Node reverse2(Node n) {
        Node node = n, n1 = null, n2 = null;
        Stack<Node> stack = new Stack<Node>();
        while (node != null) {
            n1 = node;
            node = n1.next;
            n1.next = null;
            stack.push(n1);
        }
        node = n2 = stack.lastElement();
        while (!stack.empty()) {
            n1 = stack.pop();
            n2.next = n1;
            n2 = n1;
        }
        return node;
    }
	
	//递归反转单链表
    public static Node reverse3(Node n) {
        if (n == null || n.next == null) {
            return n;
        } 
        Node head = reverse3(n.next);
        n.next.next = n;
        n.next = null;
        return head;
    }

    //获取链表的中间节点
    public static Node getMiddleNode(Node n) {
    	if (n == null) return n;
    	Node n1 = n, n2 = n;
    	//n1每次移动两步，n2每次只移动一步。
    	//这样当n1到达尾节点的时候，n2就刚好在中间
    	while (n1 != null && n1.next != null) {
    		n1 = n1.next.next;
    		n2 = n2.next;
    	}
    	return n2;
    }
    
    //删除链表节点
    public static void deleteNode(Node n) {
    	if (n != null && n.next != null) {
    		Node next = n.next;
    		n.name = next.name;
    		n.next = next.next;
    		next = null;
    	}
    }
    
}


