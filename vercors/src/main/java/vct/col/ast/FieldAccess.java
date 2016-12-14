package vct.col.ast;

import vct.util.ClassName;

public class FieldAccess extends ASTNode {
  
  @Override
  public <R,A> R accept_simple(ASTMapping1<R,A> map,A arg){
    return map.map(this,arg);
  }

  @Override
  public <T> void accept_simple(ASTVisitor<T> visitor){
    visitor.visit(this);
  }
  
  @Override
  public <T> T accept_simple(ASTMapping<T> map){
    return map.map(this);
  }

  /**
   * The object the be accessed or null for a static variable.
   */
  public final ASTNode object;
  
  /**
   * The fully qualified name of the class in which the variable is to be accessed.
   */
  public final ClassName claz;
  
  /**
   * The name of the field to be accessed.
   */
  public final String name;
  
  /**
   * This field is non-null for a write and null for a read.
   */
  public final ASTNode value;
  
  public FieldAccess(ClassName claz,ASTNode object,String name,ASTNode value){
    this.claz=claz;
    this.object=object;
    this.name=name;
    this.value=value;
  }
  
}
