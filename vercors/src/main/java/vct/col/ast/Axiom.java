package vct.col.ast;

import vct.util.ClassName;

public class Axiom extends ASTDeclaration {

  @Override
  public <R,A> R accept_simple(ASTMapping1<R,A> map,A arg){
    return map.map(this,arg);
  }

  private ASTNode rule;
  
  public Axiom(String name, ASTNode exp) {
    super(name);
    rule=exp;
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
  public ClassName getDeclName() {
    return new ClassName(name);
  }

  public ASTNode getRule(){
    return rule;
  }
}
