package vct.col.ast;

public class LongValue implements Value {
  public final long value;
  public LongValue(long value){
    this.value=value;
  }
  public String toString(){
    return Long.toString(value);
  }
}

