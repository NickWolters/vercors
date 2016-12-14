package vct.col.ast;

public class DoubleValue implements Value {
  public final double value;
  public DoubleValue(double value){
    this.value=value;
  }
  public String toString(){
    return Double.toString(value);
  }
}

