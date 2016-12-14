package vct.col.rewrite;

import vct.col.ast.ASTSpecial;
import vct.col.ast.Method;
import vct.col.ast.OperatorExpression;
import vct.col.ast.ProgramUnit;
import vct.col.ast.ASTSpecial.Kind;

public class LockEncoder extends AbstractRewriter {

  private static String INV="lock_invariant";
  private static String HELD="lock_held";

  public LockEncoder(ProgramUnit source) {
    super(source);
  }
  
  public void visit(ASTSpecial s){
    switch(s.kind){
    case Lock:
      currentBlock.add(create.special(ASTSpecial.Kind.Inhale,create.invokation(rewrite(s.args[0]),null,INV)));
      currentBlock.add(create.special(Kind.Unfold,create.invokation(rewrite(s.args[0]),null,INV)));
      currentBlock.add(create.special(ASTSpecial.Kind.Inhale,create.invokation(rewrite(s.args[0]),null,HELD)));
      result=null;
      break;
    case Unlock:
      currentBlock.add(create.special(ASTSpecial.Kind.Exhale,create.invokation(rewrite(s.args[0]),null,HELD)));
      currentBlock.add(create.special(Kind.Fold,create.invokation(rewrite(s.args[0]),null,INV)));
      currentBlock.add(create.special(ASTSpecial.Kind.Exhale,create.invokation(rewrite(s.args[0]),null,INV)));
      result=null;
      break;
    case Wait:
      currentBlock.add(create.special(Kind.Fold,create.invokation(rewrite(s.args[0]),null,INV)));
      currentBlock.add(create.special(ASTSpecial.Kind.Exhale,create.invokation(rewrite(s.args[0]),null,INV)));
      currentBlock.add(create.special(ASTSpecial.Kind.Assert,create.invokation(rewrite(s.args[0]),null,HELD)));
      currentBlock.add(create.special(ASTSpecial.Kind.Inhale,create.invokation(rewrite(s.args[0]),null,INV)));
      currentBlock.add(create.special(Kind.Unfold,create.invokation(rewrite(s.args[0]),null,INV)));
      result=null;
      break;
    case Notify:
      currentBlock.add(create.special(ASTSpecial.Kind.Assert,create.invokation(rewrite(s.args[0]),null,HELD)));
      result=null;
      break;
    default:
      super.visit(s);
    }
  }

  @Override
  public void visit(Method m){
    if(m.name.equals(INV)){
      currentTargetClass.add_dynamic(create.predicate(HELD,null));
    }
    super.visit(m);
  }
  
  @Override
  public void visit(OperatorExpression e){
    switch(e.getOperator()){
    case Held:
      result=create.invokation(rewrite(e.getArg(0)),null,HELD);
      break;
    default:
      super.visit(e);
    }
  }
}
