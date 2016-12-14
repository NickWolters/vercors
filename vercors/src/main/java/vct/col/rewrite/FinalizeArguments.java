package vct.col.rewrite;

import java.util.HashMap;

import vct.col.ast.ASTFlags;
import vct.col.ast.ASTNode;
import vct.col.ast.BlockStatement;
import vct.col.ast.Contract;
import vct.col.ast.DeclarationStatement;
import vct.col.ast.Method;
import vct.col.ast.NameExpression;
import vct.col.ast.OperatorExpression;
import vct.col.ast.ProgramUnit;
import vct.col.ast.Type;

/**
 * This rewriter converts all method argument to final arguments.
 * @author Stefan Blom
 *
 */
public class FinalizeArguments extends AbstractRewriter {

  public FinalizeArguments(ProgramUnit source) {
    super(source);
  }

  public void visit(Method m) {
    switch(m.kind){
      case Constructor:
      case Plain:
      {
        HashMap<NameExpression,ASTNode> subst=new HashMap<NameExpression, ASTNode>();
        ASTNode body=m.getBody();
        if (body==null){
          result=m.apply(copy_rw);
          return;
        }
        String name=m.getName();
        int N=m.getArity();
        DeclarationStatement old_decls[]=m.getArgs();
        DeclarationStatement args[]=new DeclarationStatement[N];
        BlockStatement block=new BlockStatement();
        block.setOrigin(body);
        //System.out.printf("%s: origin of create is %s%n",m.name,create.getOrigin());
        create.enter();
        for(int i=0;i<N;i++){
          String old_name=m.getArgument(i);
          String new_name="__"+m.getArgument(i);
          args[i]=create(old_decls[i].getOrigin()).field_decl(new_name,rewrite(m.getArgType(i)));
          if (old_decls[i].isValidFlag(ASTFlags.GHOST)){
            args[i].setGhost(old_decls[i].isGhost());
          }
          Type arg_type=rewrite(m.getArgType(i));
          block.add_statement(create.field_decl(old_name,arg_type));
          block.add_statement(create.assignment(create.local_name(old_name),create.local_name(new_name)));
          subst.put(create.local_name(old_name),create.local_name(new_name));
        }
        create.leave();
        //System.out.printf("%s: origin of create is %s%n",m.name,create.getOrigin());
        Method.Kind kind=m.kind;
        Contract mc=m.getContract();
        Contract c=null;
        if (mc!=null){
          Substitution sigma=new Substitution(source(),subst);
          ASTNode inv=mc.invariant.apply(sigma);
          ASTNode pre=mc.pre_condition.apply(sigma);
          ASTNode post=mc.post_condition.apply(sigma);
          c=new Contract(rewrite(mc.given),rewrite(mc.yields),rewrite(mc.modifies),rewrite(mc.accesses),inv,pre,post);
          if (mc.getOrigin()!=null){
            c.setOrigin(mc);
          }
        }
        { // this flattening could also be done by a generic pass.
          BlockStatement orig=(BlockStatement)body;
          for(int i=0;i<orig.getLength();i++){
            block.add_statement(orig.getStatement(i).apply(this));
          }
        }
        result=create.method_kind(kind, rewrite(m.getReturnType()),c, name, args, block);
        break;
      }
      default:
        result=m.apply(copy_rw);
        break;
    }
  }
  
  @Override
  public void visit(OperatorExpression e){
    switch(e.getOperator()){
      case Old:{
        ASTNode arg=e.getArg(0);
        if (arg instanceof NameExpression){
          NameExpression name=(NameExpression)arg;
          if (name.getKind()==NameExpression.Kind.Argument){
            result=create.argument_name("__"+name.getName());
            break;
          }
        }
        super.visit(e);
        break; 
      }
      default:
        super.visit(e);
        break;
    }
  }

}
