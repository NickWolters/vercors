package vct.col.ast;

import java.util.Arrays;

public class TypeExpression extends Type {

  @Override
  public <R,A> R accept_simple(ASTMapping1<R,A> map,A arg){
    return map.map(this,arg);
  }

  public final TypeOperator op;
  public final Type types[];
  
  public TypeExpression(TypeOperator op,Type ... t) {
    this.op=op;
    types=Arrays.copyOf(t,t.length);
  }
  
  public boolean isNumeric() {
    switch(op){
    case Local:
    case Global:
    case Long:
      return types[0].isNumeric();
    default:
      return false;
    }
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
 
  @Override
  public <T> T accept_simple(TypeMapping<T> map){
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


  @Override
  public boolean supertypeof(ProgramUnit context, Type t) {
    return false;
  }

}
