package vct.col.rewrite;

import hre.ast.MessageOrigin;
import vct.col.ast.*;

public class EncodeAsClass extends AbstractRewriter {

  public EncodeAsClass(ProgramUnit source) {
    super(source);
    cl=new ASTClass("Ref",ASTClass.ClassKind.Plain);
    cl.setOrigin(new MessageOrigin("EncodeAsClass"));
  }

  private ASTClass cl;

  public ProgramUnit rewriteAll() {
    for(ASTDeclaration n:source().get()){
        ASTNode tmp=rewrite(n);
        if (tmp!=null){
          cl.add_dynamic(tmp);
        }
    }
    target().add(cl);
    return target();
  }

}
