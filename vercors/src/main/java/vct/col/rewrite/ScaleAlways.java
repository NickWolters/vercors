package vct.col.rewrite;

import vct.col.ast.*;

public class ScaleAlways extends AbstractRewriter {

  public ScaleAlways(ProgramUnit source) {
    super(source);
  }

  
  private boolean scaled=false;
  
  @Override
  public void visit(MethodInvokation e){
    super.visit(e);
    if (e.getDefinition().kind==Method.Kind.Predicate && !scaled){
      result=create.expression(StandardOperator.Scale,create.reserved_name(ASTReserved.FullPerm),result);
    }
  }
  
  @Override
  public void visit(OperatorExpression e){
    boolean scale=e.isa(StandardOperator.Scale);
    if (scale) {
      if (scaled) {
        Fail("nested use of scaling");
      }
      scaled=true;
    }
    super.visit(e);
    if(scale){
      scaled=false;
    }
  }
}
