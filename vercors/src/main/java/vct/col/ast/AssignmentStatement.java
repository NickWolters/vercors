// -*- tab-width:2 ; indent-tabs-mode:nil -*-
package vct.col.ast;

import hre.ast.MessageOrigin;

public class AssignmentStatement extends ASTNode {

  @Override
  public <R,A> R accept_simple(ASTMapping1<R,A> map,A arg){
    return map.map(this,arg);
  }

  private ASTNode expression;
  private ASTNode location;

  public AssignmentStatement(String name,ASTNode expression){
    location=new NameExpression(name);
    location.setOrigin(new MessageOrigin("parser bug: string location in assignment"));
    this.expression=expression;
  }

  public AssignmentStatement(ASTNode location,ASTNode expression){
    this.location=location;
    this.expression=expression;
  }

  public ASTNode getExpression() { return expression; }

  public ASTNode getLocation() { return location; }

  public <T> void accept_simple(ASTVisitor<T> visitor){
    visitor.visit(this);
  }
  public <T> T accept_simple(ASTMapping<T> map){
    return map.map(this);
  }

}

