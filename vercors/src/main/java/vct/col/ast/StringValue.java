// -*- tab-width:2 ; indent-tabs-mode:nil -*-
package vct.col.ast;

/**
 * This class represents s string constant.
 *  
 * @author sccblom
 */
public class StringValue implements Value {
  public final String value;
  public StringValue(String value){
    this.value=value;
  }
  public String toString(){
    return value;
  }
  public String getStripped(){
    String res=value.substring(1,value.length()-1);
    return res;
  }
}
