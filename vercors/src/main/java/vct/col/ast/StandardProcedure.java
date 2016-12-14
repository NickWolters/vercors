// -*- tab-width:2 ; indent-tabs-mode:nil -*-
package vct.col.ast;

/**
  Simple class that wraps a StandardOperator as an ASTNode.
  Included in the design to support a future extension into functional languages:
  
  OperatorExpression(op,args) == ApplyExpression(StandardProcedure(op),args)

 */
public class StandardProcedure extends ASTNode {

  @Override
  public <R,A> R accept_simple(ASTMapping1<R,A> map,A arg){
    return map.map(this,arg);
  }

  public final StandardOperator op;

  public StandardProcedure(StandardOperator op){
    this.op=op;
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
 

  public StandardOperator getOperator(){
    return op;
  }
  
}

