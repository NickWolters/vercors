package vct.col.rewrite;

import vct.col.ast.ASTFrame;
import vct.col.ast.ASTNode;
import vct.col.ast.OperatorExpression;
import vct.col.ast.StandardOperator;

public class FilterClause extends AbstractRewriter {

  private boolean resource_only;
  
  public FilterClause(ASTFrame<ASTNode> shared,boolean resources){
    super(shared);
    resource_only=resources;
  }
  
  public void visit(OperatorExpression e){
    if (resource_only){
      switch(e.getOperator()){
      case Star:
      case Implies:
      case ITE:
        super.visit(e);
        return;
      case Perm:
      case PointsTo:
        result=create.expression(StandardOperator.Perm,copy_rw.rewrite(e.getArg(0)),copy_rw.rewrite(e.getArg(1)));
        return;
      default:
        result=create.constant(true);
        return;
      }
    } else {
      switch(e.getOperator()){
      case Perm:
        result=create.constant(true);
        return;
      case PointsTo:
        result=create.expression(StandardOperator.EQ,copy_rw.rewrite(e.getArg(0)),copy_rw.rewrite(e.getArg(2)));
        return;
      default:
        super.visit(e);
        return;
      }
    }
  }
  
}
