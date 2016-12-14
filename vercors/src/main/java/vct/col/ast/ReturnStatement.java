// -*- tab-width:2 ; indent-tabs-mode:nil -*-
package vct.col.ast;

import hre.ast.MessageOrigin;

public class ReturnStatement extends ASTNode implements BeforeAfterAnnotations {

  @Override
  public <R,A> R accept_simple(ASTMapping1<R,A> map,A arg){
    return map.map(this,arg);
  }

  private ASTNode expression;
  
  public ReturnStatement(){
    expression=null;
  }

  public ReturnStatement(ASTNode e){
    expression=e;
  }
  
  public ASTNode getExpression() { return expression; }

  
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
 

  /** Block of proof hints to be executed just before
   *  evaluating the expression represented by this AST node.
   *  But after any argument has been evaluated.
   */
  private BlockStatement before;
  /** Block of proof hints to be executed after the
   *  current node has been evaluated.
   */
  private BlockStatement after;
  
  public ReturnStatement set_before(BlockStatement block){
    before=block;
    if (block!=null) {
      block.setParent(this);
      if (block.getOrigin()==null){
        block.setOrigin(new MessageOrigin("before block"));
      }
    }
    return this;
  }
  public BlockStatement get_before(){
    if (before==null) set_before(new BlockStatement());
    return before;
  }
  
  public ReturnStatement set_after(BlockStatement block){
    after=block;
    if (block!=null) {
      block.setParent(this);
      if (block.getOrigin()==null){
        block.setOrigin(new MessageOrigin("after block"));
      }
    }
    return this;
  }
  public BlockStatement get_after(){
    if (after==null) set_after(new BlockStatement());
    return after;
  }

}

