package vct.antlr4.parser;


import java.util.HashMap;

import vct.col.ast.ASTClass;
import vct.col.ast.ASTNode;
import vct.col.ast.ASTSpecial;
import vct.col.ast.ASTSpecial.Kind;
import vct.col.ast.BeforeAfterAnnotations;
import vct.col.ast.BlockStatement;
import vct.col.ast.ClassType;
import vct.col.ast.LoopStatement;
import vct.col.ast.Method;
import vct.col.ast.MethodInvokation;
import vct.col.ast.NameExpression;
import vct.col.ast.OperatorExpression;
import vct.col.ast.PrimitiveType;
import vct.col.ast.PrimitiveType.Sort;
import vct.col.ast.ProgramUnit;
import vct.col.ast.StandardOperator;
import vct.col.ast.Type;
import vct.col.rewrite.AbstractRewriter;
import vct.col.syntax.JavaDialect;
import vct.col.syntax.JavaSyntax;

/**
 * Rewrite a Java AST, produced by parsing, to conform to the COL AST standard.  
 */
public class JavaPostProcessor extends AbstractRewriter {

  private int wildcard_count=0;
  
  public JavaPostProcessor(ProgramUnit source) {
    super(source);
  }

  @Override
  public void visit(ASTSpecial s){
    switch(s.kind){
    case ActionHeader:
      Fail("cannot create block around action",s.getOrigin());
      break;
    case Expression:
      result=rewrite(s.args[0]);
      break;
    default:
      super.visit(s);
      break;
    }
  }
  
  @Override
  public void visit(ClassType t){
    String name[]=t.getNameFull();
    if (name.length==1){
      switch(name[0]){
      case "String":
        result=create.primitive_type(PrimitiveType.Sort.String);
        return;
      case "seq":
        result=create.primitive_type(PrimitiveType.Sort.Sequence,t.getArgs());
        return;
      case "set":
        result=create.primitive_type(PrimitiveType.Sort.Set,t.getArgs());
        return;
      case "loc":
        result=create.primitive_type(PrimitiveType.Sort.Location,t.getArgs());
        return;
      case "bag":
        result=create.primitive_type(PrimitiveType.Sort.Bag,t.getArgs());
        return;
      default:
        super.visit(t);
        return;
      }
    } else {
      super.visit(t);
    }
  }
  
  @Override
  public void visit(ASTClass c){
    super.visit(c);
    ASTClass decl=(ASTClass)result;
    int N=0;
    for(Method m:decl.dynamicMethods()){
      if (m.kind==Method.Kind.Constructor) N++;
    }
    if (N==0 && c.kind!=ASTClass.ClassKind.Interface) create.addZeroConstructor(decl);
  }

  @Override
  public void visit(Method m){
    if (m.getReturnType().isPrimitive(PrimitiveType.Sort.Resource)){
      result=create.predicate(m.getName(), rewrite(m.getBody()), rewrite(m.getArgs()));
    } else {
      super.visit(m);
    }
  }
  
  @Override
  public void visit(MethodInvokation e){
    if (e.object==null){
      JavaSyntax syntax=JavaSyntax.getJava(JavaDialect.JavaVerCors);
      StandardOperator op=syntax.parseFunction(e.method);
      if (op!=null){
        result=create.expression(op,rewrite(e.getArgs()));
        return;
      }
    }
    result=process_with_then(create.invokation(rewrite(e.object), e.dispatch, e.method, rewrite(e.getArgs())),e);
  }
  
  private <T extends ASTNode & BeforeAfterAnnotations> ASTNode process_with_then(T dst,T src) {
    for(ASTNode n:src.get_before()){
      dst.get_before().add(rewrite(n));
    }
    for(ASTNode n:src.get_after()){
      if (n.isSpecial(ASTSpecial.Kind.Label)) {
    	String label=((NameExpression)((ASTSpecial)n).args[0]).getName();
    	dst.labeled(label);
      } else if (n.isSpecial(ASTSpecial.Kind.With)) {
        for(ASTNode w : ((BlockStatement)((ASTSpecial)n).args[0])){
          dst.get_before().add(rewrite(w));
        }
      } else if (n.isSpecial(ASTSpecial.Kind.Then)){
        for(ASTNode w : ((BlockStatement)((ASTSpecial)n).args[0])){
          dst.get_after().add(rewrite(w));
        }
      } else {
        dst.get_after().add(rewrite(n));
      }
    }
    return dst;
  }

  public void visit(NameExpression n){
    if (n.getName().equals("?")){
      wildcard_count++;
      String name="wildcard_"+wildcard_count;
      if (currentContractBuilder==null) Abort("no contract builder set");
      currentContractBuilder.given(create.field_decl(name, create.primitive_type(Sort.Class),create.class_type("Object")));
      result=create.unresolved_name(name);
      return;
    } else {
      super.visit(n);
    }
  }
  
  @Override
  public void visit(OperatorExpression e){
    Type deftype=create.class_type("Object");
    switch(e.getOperator()){
      case StructSelect:
        ASTNode arg=e.getArg(1);
        if (arg instanceof NameExpression){
          result=create.dereference(rewrite(e.getArg(0)), arg.toString());
          return;
        } else {
          Abort("unexpected StructSelect expression");
        }
      case SubType:
        deftype=(Type)rewrite(e.getArg(1));
      case SuperType:
      {
        NameExpression n=(NameExpression)e.getArg(0);
        if (n.getName().equals("?")){
          wildcard_count++;
          String name="wildcard_"+wildcard_count;
          if (currentContractBuilder==null) Abort("no contract builder set");
          currentContractBuilder.given(create.field_decl(name, create.primitive_type(Sort.Class),deftype));
          currentContractBuilder.requires(create.expression(e.getOperator(),create.unresolved_name(name),rewrite(e.getArg(1))));
          result=create.unresolved_name(name);
          return;
        }
        break;
      }
      case LT:
      case LTE:
      {
        ASTNode arg0=rewrite(e.getArg(0));
        ASTNode arg1=rewrite(e.getArg(1));
        OperatorExpression res;
        if (arg0 instanceof OperatorExpression && is_comparison(((OperatorExpression)arg0).getOperator())){
          ASTNode tmp=rewrite(((OperatorExpression)e.getArg(0)).getArg(1));
          arg1=create.expression(e.getOperator(),tmp,arg1);
          res=create.expression(StandardOperator.And,arg0,arg1);
        } else {
          res=create.expression(e.getOperator(),arg0,arg1);
        }
        res.set_before(null);
        res.set_after(null);
        result=process_with_then(res,e);
        return;
      }
    default:
      break;
    }
    super.visit(e);
    OperatorExpression res=(OperatorExpression) result;
    res.set_before(null);
    res.set_after(null);
    result=process_with_then(res,e);
  }
  
  private boolean is_comparison(StandardOperator operator) {
    switch(operator){
    case LT:
    case LTE:
      return true;
    default:
      return false;
    }
  }

  @Override
  public void visit(LoopStatement s){
    super.visit(s);
    LoopStatement res=(LoopStatement)result;
    res.set_before(null);
    res.set_after(null);
    result=process_with_then(res,s);
  }
  
  @Override
  public void visit(BlockStatement b){
    if (b.size()>0 && b.get(0).isSpecial(Kind.ActionHeader)){
      ASTSpecial decl=(ASTSpecial)b.get(0);
      ASTNode history=rewrite(decl.args[0]);
      ASTNode fraction=rewrite(decl.args[1]);
      ASTNode process=rewrite(decl.args[2]);
      ASTNode action=rewrite(decl.args[3]);
      HashMap<String,ASTNode> map=new HashMap<String,ASTNode>();
      for(int i=4;i<decl.args.length;i+=2){
        String field=decl.args[i].toString();
        ASTNode frac=rewrite(decl.args[i+1]);
        map.put(field,frac);
      }
      BlockStatement block=create.block();
      int N=b.size();
      for(int i=1;i<N;i++){
        block.add(rewrite(b.get(i)));
      }
      result=create.action_block(history,fraction,process, action,map,block);
    } else {
      super.visit(b);
    }
  }
}
