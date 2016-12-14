package vct.col.ast;

import hre.ast.MessageOrigin;

public abstract class ExpressionNode extends ASTNode implements BeforeAfterAnnotations {

  /** Block of proof hints to be executed just before
   *  evaluating the expression represented by this AST node.
   *  But after any argument has been evaluated.
   */
  private BlockStatement before;
  /** Block of proof hints to be executed after the
   *  current node has been evaluated.
   */
  private BlockStatement after;
  
  public ExpressionNode set_before(BlockStatement block){
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
  
  public ExpressionNode set_after(BlockStatement block){
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
