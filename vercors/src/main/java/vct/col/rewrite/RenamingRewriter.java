package vct.col.rewrite;

import vct.col.ast.ASTFrame;
import vct.col.ast.ASTNode;
import vct.col.ast.DeclarationStatement;
import vct.col.ast.NameExpression;
import vct.col.ast.ProgramUnit;

public class RenamingRewriter extends AbstractRewriter {

  public RenamingRewriter(ProgramUnit source, ProgramUnit target) {
    super(source, target);
  }

  public RenamingRewriter(ProgramUnit source) {
    super(source);
  }

  public RenamingRewriter(ASTFrame<ASTNode> shared) {
    super(shared);
  }
  
  public String rename_method(String name){
    return name;
  }
  
  public String rename_variable(String name){
    return name;
  }

  public void visit(NameExpression e){
    if (e.getKind()==NameExpression.Kind.Reserved){
      super.visit(e);
    } else {
      result=create.name(e.getKind(),null,rename_variable(e.getName()));
    }
  }
  
  public void visit(DeclarationStatement decl){
    result=create.field_decl(rename_variable(decl.getName()),rewrite(decl.getType()));
  }
  
}
