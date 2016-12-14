package vct.col.rewrite;

import vct.col.ast.ASTSpecial;
import vct.col.ast.OperatorExpression;
import vct.col.ast.ProgramUnit;
import vct.col.ast.StandardOperator;

/**
 * This rewriter changes assignment expressions to assignment statements.
 * 
 * @author Stefan Blom
 */ 
public class AssignmentRewriter extends AbstractRewriter {

  public AssignmentRewriter(ProgramUnit source) {
    super(source);
  }

  @Override
  public void visit(ASTSpecial s){
    switch(s.kind){
    case Expression:
      if (s.args[0].isa(StandardOperator.Assign)){
        OperatorExpression e=(OperatorExpression)s.args[0];
        result=create.assignment(e.getArg(0),e.getArg(1));
        break;
      }
    default:
      super.visit(s);
      break;
    }
  }

}
