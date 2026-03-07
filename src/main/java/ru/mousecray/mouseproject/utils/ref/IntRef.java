package ru.mousecray.mouseproject.utils.ref;

public class IntRef implements Ref<Integer> {
    private int val;
    public IntRef(int val)               { this.val = val; }
    @Override public void $(Integer val) { this.val = val; }
    public void $(int val)               { this.val = val; }
    @Override public Integer $$()        { return val; }
    public int $()                       { return val; }
}
