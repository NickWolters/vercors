package vct.col.rewrite;

import java.util.Map;

import vct.col.ast.ASTNode;
import vct.col.ast.NameExpression;
import vct.col.ast.ProgramUnit;

public class Substitution extends AbstractRewriter {
  Map<NameExpression,ASTNode> map;
  
  public boolean copy=true;
  
  public Substitution(ProgramUnit source,Map<NameExpression,ASTNode> map){
    super(source);
   this.map=map;
  }
  
  public void visit(NameExpression e) {
    ASTNode res=map.get(e);
    if (res==null){
      super.visit(e);
    } else if (copy) {
      result=res.apply(copy_rw);
    } else {
      result=res;
    }
  }
}
