package vct.col.ast;

public class CatchClause {
  public final DeclarationStatement decl;
  public final BlockStatement block;
  
  public CatchClause(DeclarationStatement decl, BlockStatement block) {
    this.decl=decl;
    this.block=block;
  }

}
