package vct.col.rewrite;

import vct.col.ast.ASTFlags;
import vct.col.ast.DeclarationStatement;
import vct.col.ast.ProgramUnit;
import vct.col.ast.VariableDeclaration;

public class FlattenVariableDeclarations extends AbstractRewriter {

  public FlattenVariableDeclarations(ProgramUnit source) {
    super(source);
  }
  
  @Override
  public void visit(VariableDeclaration decl) {
    for(DeclarationStatement tmp:decl.flatten()){
      if (decl.isValidFlag(ASTFlags.STATIC)){
        tmp.setStatic(decl.isStatic());
      }
      current_sequence().add(tmp);
    }
    result=null;
  }

}
