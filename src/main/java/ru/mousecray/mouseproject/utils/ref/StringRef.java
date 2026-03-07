package ru.mousecray.mouseproject.utils.ref;

public class StringRef implements Ref<String> {
    private String val;
    public StringRef(String val)        { this.val = val; }
    @Override public void $(String val) { this.val = val; }
    public void $A(String val)          { this.val += val; }
    @Override public String $$()        { return val; }
    public String $()                   { return val; }
}
