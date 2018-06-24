package algorithm;

import java.util.Stack;

public class ReverseLinkedNode {

	public static void main(String[] args) {
        getNode(setNode());
        getNode(reverse1(setNode()));
        getNode(reverse2(setNode()));
        getNode(reverse3(setNode()));
	}

	
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
	
	public static void getNode(Node n) {
		Node node = n;
        while (node != null) {
            System.out.print(node.name + " ");
            node = node.next;
        }
        System.out.println();
	}
	
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
    public static Node reverse3(Node n) {
        if (n == null || n.next == null) {
            return n;
        } 
        Node head = reverse3(n.next);
        n.next.next = n;
        n.next = null;
        return head;
    }
}

class Node {
	public String name;
	public Node next;
}
