// -*- tab-width:2 ; indent-tabs-mode:nil -*-
package vct.col.ast;

import hre.ast.Origin;

import vct.col.ast.PrimitiveType.Sort;

/**
 * AST node for wrapping constant values.
 * 
 * @author sccblom
 *
 */
public class ConstantExpression extends ASTNode {

  @Override
  public <R,A> R accept_simple(ASTMapping1<R,A> map,A arg){
    return map.map(this,arg);
  }

  public final Value value;

  public ConstantExpression(int i,Origin origin){
    this.value=new IntegerValue(i);
    setType(new PrimitiveType(Sort.Integer));
    setOrigin(origin);
  }
  public ConstantExpression(int i){
    this.value=new IntegerValue(i);
    setType(new PrimitiveType(Sort.Integer));
  }
  
  public ConstantExpression(Value v,Type t,Origin origin){
    this.value=v;
    setType(t);
    setOrigin(origin);
  }

  public ConstantExpression(boolean b,Origin origin){
    this.value=new BooleanValue(b);
    setType(new PrimitiveType(Sort.Boolean));
    setOrigin(origin);
  }
  public ConstantExpression(boolean b){
    this.value=new BooleanValue(b);
    setType(new PrimitiveType(Sort.Boolean));
  }

  public ConstantExpression(String s, Origin origin) {
    this.value=new StringValue(s);
    setType(new PrimitiveType(Sort.String));
    setOrigin(origin);
  }
  public ConstantExpression(String s){
    this.value=new StringValue(s);
    setType(new PrimitiveType(Sort.String));
  }

  public ConstantExpression(long i, Origin origin) {
    this.value=new LongValue(i);
    setType(new PrimitiveType(Sort.Long));
    setOrigin(origin);
  }
  public ConstantExpression(long i) {
    this.value=new LongValue(i);
    setType(new PrimitiveType(Sort.Long));
  }

  public ConstantExpression(double d, Origin origin) {
    this.value=new DoubleValue(d);
    setType(new PrimitiveType(Sort.Double));
    setOrigin(origin);
  }
  public ConstantExpression(double d) {
    this.value=new DoubleValue(d);
    setType(new PrimitiveType(Sort.Double));
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
 

  public Value getValue(){
    return value;
  }
  public String toString(){
    return value.toString();
  }
  
  public boolean equals(Object o){
    return value.equals(o);
  }
  
  public boolean isConstant(Object o){
    return value.equals(o);
  }
  
  public boolean match(ASTNode ast){
    if (ast instanceof Hole){
      return ast.match(this);
    } else { 
      return ast.equals(value);
    }
  }

}

