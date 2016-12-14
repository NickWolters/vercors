// -*- tab-width:2 ; indent-tabs-mode:nil -*-
package vct.col.ast;

import java.util.*;

public class FunctionType extends Type {

  @Override
  public <R,A> R accept_simple(ASTMapping1<R,A> map,A arg){
    return map.map(this,arg);
  }

  private Type args[];
  private Type result;
  public FunctionType(Type args[],Type result){
    this.args=Arrays.copyOf(args,args.length);
    this.result=result;
  }
  public FunctionType(ArrayList<Type> args,Type result){
    this.args=args.toArray(new Type[0]);
    this.result=result;
  }

  public int getArity(){ return args.length; }
  
  public Type getResult(){ return result; }
  
  public Type getArgument(int i){ return args[i]; }
  
  @Override
  public ASTNode zero(){
    return null;
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
    // TODO Auto-generated method stub
    return false;
  }

  public boolean equals(Object o){
    if (o instanceof FunctionType){
      FunctionType t=(FunctionType)o;
      if (t.args.length!=args.length) return false;
      for(int i=0;i<args.length;i++){
        if(!args[i].equals(t.args[i])) return false;
      }
      return result.equals(t.result);
    } else {
      return false;
    }
  }
}
