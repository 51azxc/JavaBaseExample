package design.pattern.behavioral.visitor;

/*
访问者模式

examples:
javax.lang.model.element.AnnotationValue/AnnotationValueVisitor
javax.lang.model.element.Element/Element Visitor
java.nio.file.FileVisitor
 */
public class Client {
    public static void main(String[] args) {
        ComputerPart computer = new Computer();
        computer.accept(new ComputerPartDisplayVisitor());
    }
}
