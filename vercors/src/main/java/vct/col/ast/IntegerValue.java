// -*- tab-width:2 ; indent-tabs-mode:nil -*-
package vct.col.ast;

public class IntegerValue implements Value {
  public final int value;
  public IntegerValue(int value){
    this.value=value;
  }
  public String toString(){
    return Integer.toString(value);
  }
  public boolean equals(Object o){
    return o.equals(value);
  }
}

