package vct.col.util;

import vct.col.ast.ASTNode;
import vct.col.ast.DeclarationStatement;
import hre.util.Function;

public class DeclarationFilter implements Function<ASTNode,DeclarationStatement> {

  @Override
  public DeclarationStatement apply(ASTNode e) {
    if(e instanceof DeclarationStatement){
      return (DeclarationStatement)e;
    } else {
      return null;
    }
  }
}
