// -*- tab-width:2 ; indent-tabs-mode:nil -*-
package vct.col.ast;

import vct.util.ClassName;

public class DeclarationStatement extends ASTDeclaration {

  @Override
  public <R,A> R accept_simple(ASTMapping1<R,A> map,A arg){
    return map.map(this,arg);
  }

  // Type should become part of ASTNode!!
  private Type type;
  private ASTNode expr;

  public DeclarationStatement(String name,Type type){
    super(name);
    this.type=type;
    expr=null;
  }

  public DeclarationStatement(String name,Type type,ASTNode expr){
    super(name);
    this.type=type;
    this.expr=expr;
  }
  
  public Type getType() { return type; }

  public String getName() { return name; }
  
  public ASTNode getInit() { return expr; }
  
  @Override
  public ClassName getDeclName() {
    hre.lang.System.Debug("%s.%s",package_name,name);
    return new ClassName(package_name,name);
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

