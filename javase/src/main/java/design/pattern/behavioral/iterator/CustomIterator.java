package design.pattern.behavioral.iterator;

public interface CustomIterator<T> {
    T next();
    boolean hasNext();
}

class IntegerIterator implements CustomIterator<Integer> {
    private Integer[] ins;
    private int index;

    public IntegerIterator(Integer[] ins) {
        this.ins = ins;
        this.index = 0;
    }

    @Override
    public boolean hasNext() {
        return index < ins.length;
    }

    @Override
    public Integer next() {
        return this.hasNext() ? ins[index++] : null;
    }
}
