package design.pattern.behavioral.iterator;

public interface CustomList<T> {
    CustomIterator<T> iterator();
}

class IntegerList implements CustomList<Integer> {
    private Integer[] ins;

    public IntegerList(Integer[] ins) {
        this.ins = ins;
    }

    @Override
    public CustomIterator<Integer> iterator() {
        return new IntegerIterator(ins);
    }
}
