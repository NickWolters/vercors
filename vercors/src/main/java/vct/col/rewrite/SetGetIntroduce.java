package vct.col.rewrite;

import vct.col.ast.*;

public class SetGetIntroduce extends AbstractRewriter {

  public SetGetIntroduce(ProgramUnit source) {
    super(source);
  }

  @Override
  public void visit(Dereference e){
    super.visit(e);
    if (!in_ensures && !in_invariant && !in_requires){
      result=create.expression(StandardOperator.Get,result);
    }
  }
  
  @Override
  public void visit(OperatorExpression e){
    switch(e.getOperator()){
    case Assign:{
      ASTNode tmp=rewrite(e.getArg(0));
      if (tmp.isa(StandardOperator.Get)){
        tmp=((OperatorExpression)tmp).getArg(0);
        result=create.expression(StandardOperator.Set,tmp,rewrite(e.getArg(1)));
      } else {
        result=create.expression(StandardOperator.Assign,tmp,rewrite(e.getArg(1)));
      }
      break;
    }
    case Subscript:{
      super.visit(e);
      result=create.expression(StandardOperator.Get,result);
      break;
    }
    default:
      super.visit(e);
      break;
    }
  }
  
  @Override
  public void visit(AssignmentStatement e){
    ASTNode tmp=rewrite(e.getLocation());
    if (tmp.isa(StandardOperator.Get)){
      tmp=((OperatorExpression)tmp).getArg(0);
      result=create.expression(StandardOperator.Set,tmp,rewrite(e.getExpression()));
    } else {
      result=create.expression(StandardOperator.Assign,tmp,rewrite(e.getExpression()));
    }    
  }
}
