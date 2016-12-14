// -*- tab-width:2 ; indent-tabs-mode:nil -*-
package vct.col.ast;

import hre.ast.FileOrigin;

import java.util.*;

import static hre.lang.System.*;

public class OperatorExpression extends ExpressionNode {

  @Override
  public <R,A> R accept_simple(ASTMapping1<R,A> map,A arg){
    return map.map(this,arg);
  }

  private StandardOperator op;
  private ASTNode args[];

  public OperatorExpression(StandardOperator op,ASTNode ... args){
    this.op=op;
    this.args=Arrays.copyOf(args,args.length);
    if (op.arity()>=0 && args.length != op.arity()) Abort("wrong number of arguments for %s, got %d expected %d",op,args.length,op.arity());
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
  
  public ASTNode getArg(int i){
    if (i>=args.length){
      throw new Error("operator "+op+" does not have an argument "+i);
    }
    return args[i];
  }

  public void mergeOrigins(){
    if (args.length < 2) throw new Error("cannot merge on less than 2 arguments");
    FileOrigin start,end;
    if (args[0].getOrigin() instanceof FileOrigin) {
      start=(FileOrigin)args[0].getOrigin();
    } else {
      throw new Error("merge requires FileOrigin");
    }
    if (args[args.length-1].getOrigin() instanceof FileOrigin) {
      end=(FileOrigin)args[args.length-1].getOrigin();
    } else {
      throw new Error("merge requires FileOrigin");
    }
    setOrigin(start.merge(end));
  }
  
  public static OperatorExpression create(StandardOperator op,ASTNode ... args){
    OperatorExpression res=new OperatorExpression(op,args);
    res.mergeOrigins();
    return res;
  }
  public ASTNode[] getArguments() {
    return Arrays.copyOf(args,args.length);
  }
  public boolean isa(StandardOperator op) {
    return op==this.op;
  }
  
  @Override
  public boolean match(ASTNode ast){
    if (ast instanceof Hole){
      return ast.match(this);
    } else if (ast.isa(this.op)) {
      boolean res=true;
      OperatorExpression e=(OperatorExpression)ast;
      for(int i=0;res && i<op.arity();i++){
        res=args[i].match(e.args[i]);
      }
      return res;
    } else {
      return false;
    }
  }

}

