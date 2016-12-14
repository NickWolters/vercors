package vct.col.rewrite;

import vct.col.ast.*;
import vct.col.ast.Method.Kind;
import vct.col.ast.PrimitiveType.Sort;
import vct.col.util.FeatureScanner;

/**
 * This class encodes the fork-join primitives of the PVL language.
 * 
 * @author Stefan Blom
 *
 */
public class ForkJoinEncode extends AbstractRewriter {

  public ForkJoinEncode(ProgramUnit source) {
    super(source);
  }
  
  
  @Override
  public void visit(Method m){
    if(m.name.equals("run")){
      Contract c=m.getContract();
      FeatureScanner features=new FeatureScanner();
      c.post_condition.accept(features);
      if(features.usesOperator(StandardOperator.Old)){
        Fail("The post-condition of a run method is not allowed to use the \\old operator.");
      }
      ContractBuilder cb=new ContractBuilder();
      cb.requires(rewrite(c.pre_condition));
      cb.requires(create.invokation(null,null,"idleToken"));
      cb.ensures(create.invokation(null,null,"joinToken"));
      Method fork=create.method_decl(
          create.primitive_type(Sort.Void),
          cb.getContract(),
          "forkOperator",
          new DeclarationStatement[0],
          null
      );
      currentTargetClass.add_dynamic(fork);
      cb=new ContractBuilder();
      cb.requires(create.invokation(null,null,"joinToken"));
      cb.ensures(create.invokation(null,null,"idleToken"));
      cb.ensures(rewrite(c.post_condition));
      Method join=create.method_decl(
          create.primitive_type(Sort.Void),
          cb.getContract(),
          "joinOperator",
          new DeclarationStatement[0],
          null
      );
      currentTargetClass.add_dynamic(join);
      Method token=create.predicate("joinToken",null);
      currentTargetClass.add_dynamic(token);
      token=create.predicate("idleToken",null);
      currentTargetClass.add_dynamic(token);
    }
    if(m.kind==Kind.Constructor){
      ASTClass parent=(ASTClass)m.getParent();
      boolean runnable=false;
      for(Method method:parent.dynamicMethods()){
        if (method.name.equals("run")){
          runnable=true; 
          break;
        }
      }
      if (runnable){
        currentContractBuilder=new ContractBuilder();
        currentContractBuilder.ensures(create.invokation(null,null,"idleToken"));
        super.visit(m);
        Method method=(Method)result;
        BlockStatement block=(BlockStatement)method.getBody();
        block.append(
            create.special(ASTSpecial.Kind.Inhale,
            create.invokation(null,null,"idleToken")));
        return;
      }
    }
    super.visit(m);
  }
  
  @Override
  public void visit(ASTSpecial e){
    switch(e.kind){
    case Fork:
      result=create.invokation(rewrite(e.args[0]),null,"forkOperator");
      break;
    case Join:
      result=create.invokation(rewrite(e.args[0]),null,"joinOperator");
      break;      
    default:
      super.visit(e);
      break;
    }
  }
  
  @Override
  public void visit(OperatorExpression e){
    switch(e.getOperator()){
    case PVLidleToken:
      result=create.invokation(rewrite(e.getArg(0)),null,"idleToken");
      break;
    case PVLjoinToken:
      result=create.invokation(rewrite(e.getArg(0)),null,"joinToken");
      break;
      default:
        super.visit(e);
        break;
    }
  }

}
