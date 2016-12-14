package vct.col.rewrite;

import vct.col.ast.OperatorExpression;
import vct.col.ast.StandardOperator;

public class ApplyOld extends AbstractRewriter {

  private final AbstractRewriter rw_old;
  public ApplyOld(AbstractRewriter rw_old) {
    super(rw_old.source());
    this.rw_old=rw_old;
  }

  @Override
  public void visit(OperatorExpression e){
    if (e.isa(StandardOperator.Old)){
      result=rw_old.rewrite(e.getArg(0));
    } else {
      super.visit(e);
    }
  }
}
