package vct.col.ast;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Stores values for structs, records, tuples, etc.
 *
 * 
 * @author Stefan Blom
 *
 */
public class StructValue extends ExpressionNode {

  /** The type of the end result. */
  public final Type type;
  
  /** Ordered list of values. */
  public final ASTNode values[];
  
  /** Maps names to positions in the list of values. */
  public final Map<String,Integer> map;
  
  public StructValue(Type type,Map<String,Integer> map,ASTNode ... values){
    this.type=type;
    this.values=Arrays.copyOf(values,values.length);
    if (map==null){
      this.map=null;
    } else {
      this.map=new HashMap<String, Integer>(map);
    }
  }
  
  @Override
  public <R,A> R accept_simple(ASTMapping1<R,A> map,A arg){
    return map.map(this,arg);
  }

  @Override
  public <T> void accept_simple(ASTVisitor<T> visitor){
    try {
      visitor.visit(this);
    } catch (Throwable t){
      if (thrown.get()!=t){
        System.err.printf("Triggered by %s:%n",getOrigin());
        thrown.set(t);
      }
      throw t;
    }
  }
  
  @Override
  public <T> T accept_simple(ASTMapping<T> map){
    try {
      return map.map(this);
    } catch (Throwable t){
      if (thrown.get()!=t){
        System.err.printf("Triggered by %s:%n",getOrigin());
        thrown.set(t);
      }
      throw t;
    }
  }

}
