package vct.antlr4.parser;

import vct.col.ast.ASTNode;
import vct.col.ast.BlockStatement;
import vct.col.ast.Contract;
import vct.col.ast.ContractBuilder;
import vct.col.ast.DeclarationStatement;
import vct.col.ast.Method;
import vct.col.ast.ProgramUnit;
import vct.col.ast.Type;
import vct.col.rewrite.AbstractRewriter;


public class RemoveBodies extends AbstractRewriter {

  public RemoveBodies(ProgramUnit source) {
    super(source);
  }
  
  @Override
  public void visit(Method m){
    String name=m.getName();
    if (currentContractBuilder==null) currentContractBuilder=new ContractBuilder();
    DeclarationStatement args[]=rewrite(m.getArgs());
    Contract mc=m.getContract();
    if (mc!=null){
      rewrite(mc,currentContractBuilder);
    }
    Method.Kind kind=m.kind;
    Type rt=rewrite(m.getReturnType());
    Contract c=currentContractBuilder.getContract();
    currentContractBuilder=null;
    ASTNode body;
    switch(kind){
    case Pure:
      if (m.getBody() instanceof BlockStatement){
        // The body of a pure method is hidden. 
        body=null;
      } else {
        // The body of a function is visible.
        body=rewrite(m.getBody());
      }
      break;
    case Predicate:
      // Bodies of predicates are visible.
      body=rewrite(m.getBody());
      break;
    default:
      // Everything else is hidden.
      body=null;
    }
    result=create.method_kind(kind, rt, c, name, args, m.usesVarArgs(), body);
  }

}
