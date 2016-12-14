package vct.col.rewrite;

import java.util.HashSet;

import vct.col.ast.BlockStatement;
import vct.col.ast.DeclarationStatement;
import vct.col.ast.ProgramUnit;

public class ReorderAssignments extends AbstractRewriter {
  public ReorderAssignments(ProgramUnit source) {
    super(source);
  }

  public void visit(BlockStatement s){
    int N=s.getLength();
    BlockStatement res=create.block();
    // copy declarations
    HashSet<String> names=new HashSet<String>();
    for(int i=0;i<N;i++){
      if (s.getStatement(i) instanceof DeclarationStatement){
        DeclarationStatement decl=(DeclarationStatement)s.getStatement(i);
        String name=decl.getName();
        if (names.contains(name)){
          Abort("variable %s was declared twice",name);
        }
        names.add(name);
        res.add_statement(decl.apply(this));
      }
    }
    // copy non-declarations
    for(int i=0;i<N;i++){
      if (!(s.getStatement(i) instanceof DeclarationStatement)){
        res.add_statement(s.getStatement(i).apply(this));
      }
    }
    result=res;
  }

}
