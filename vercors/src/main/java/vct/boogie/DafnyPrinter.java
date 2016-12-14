package vct.boogie;

import hre.ast.TrackingOutput;
import vct.col.ast.ASTClass;
import vct.col.ast.ASTFlags;
import vct.col.ast.ASTNode;
import vct.col.ast.Contract;
import vct.col.ast.DeclarationStatement;
import vct.col.ast.Method;
import vct.col.ast.PrimitiveType;
import vct.col.ast.Type;

public class DafnyPrinter extends AbstractBoogiePrinter {

	public DafnyPrinter(TrackingOutput out) {
	  super(BoogieSyntax.getDafny(),out,false);
  }

  public void visit(ASTClass c){
    String class_name=c.getName();
    if (c.getStaticCount()>0){
    	Fail("Dafny does not support static members.");
    }
    out.lnprintf("class %s {",class_name);
    out.incrIndent();
    for(ASTNode n:c.dynamicMembers()){
    	n.accept(this);
    }
    out.decrIndent();
    out.lnprintf("}");
  }
  
  public void visit(Method m){
    if (m.kind==Method.Kind.Constructor) {
      Debug("skipping constructor");
      return;
    }
    DeclarationStatement args[]=m.getArgs();
    int N=args.length;
    Type result_type=m.getReturnType();
    if (!result_type.equals(PrimitiveType.Sort.Void)) Fail("illegal return type %s",result_type);
    String name=m.getName();
    out.printf("method %s(",name);
    String next="";
    for(int i=0;i<N;i++){
      if (args[i].isValidFlag(ASTFlags.OUT_ARG)&&args[i].getFlag(ASTFlags.OUT_ARG)) continue;
      out.printf("%s%s: ",next,args[i].getName());
      args[i].getType().accept(this);
      next=",";
    }
    out.printf(") returns (");
    next="";
    for(int i=0;i<args.length;i++){
      if (args[i].isValidFlag(ASTFlags.OUT_ARG)&&args[i].getFlag(ASTFlags.OUT_ARG)) {
        out.printf("%s%s: ",next,args[i].getName());
        args[i].getType().accept(this);
        next=",";
      }
    }
    out.lnprintf(")");
    Contract contract=m.getContract();
    if (contract!=null){
      visit(contract);
      post_condition=contract.post_condition;
    }
    ASTNode body=m.getBody();
    body.accept(this);
    out.lnprintf("//end method %s",name);
    post_condition=null;
  }
}
