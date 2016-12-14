package vct.col.rewrite;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import vct.col.ast.PrimitiveType.Sort;
import vct.col.ast.*;
import vct.col.util.ASTUtils;
import vct.util.Configuration;

/**
 * This class checks if a specified process algebra is correct.
 * 
 * @author sccblom
 *
 */
public class CheckProcessAlgebra extends AbstractRewriter {

  public CheckProcessAlgebra(ProgramUnit source) {
    super(source);
  }

  private Hashtable<String,String> composite_map;
  private Hashtable<String,Method> process_map;

  @Override
  public void visit(ASTClass cl){
    composite_map=new Hashtable<String,String>();
    process_map=new Hashtable<String,Method>();
    HashSet<NameExpression> hist_set=new HashSet<NameExpression>();
    for(Method m:cl.dynamicMethods()){
      if (!m.getReturnType().isPrimitive(Sort.Process)) continue;
      ASTNode body=m.getBody();
      process_map.put(m.name, m);
      for(ASTNode n:m.getContract().modifies){
        if (n instanceof NameExpression){
          hist_set.add((NameExpression)n);
        } else if (n instanceof Dereference) {
          Dereference d=(Dereference)n;
          hist_set.add(create.field_name(d.field));
        } else {
          Fail("unexpected entry in modifies clause");
        }
      }
      if (body==null) continue;
      if(body.isa(StandardOperator.Or)){
        String composite=":";
        for(ASTNode p:ASTUtils.conjuncts(body, StandardOperator.Or)){
          if (p instanceof MethodInvokation){
            composite+=((MethodInvokation)p).method+":";
          } else {
            Fail("misformed parallel composition");
          }
        }
        // TODO: check if arguments are passed in-order.
        // That is p(a,b)=q(a)||q(b) is allowed
        // p(a,b)=q(b)||q(a) is forbidden.
        composite_map.put(composite,m.name);
        Warning("mapping %s to %s",composite,m.name);
      }
    }
    super.visit(cl);
    /* this block creates _old vars and begin/end methods which are nto needed for checking histories.
    ASTClass res=(ASTClass)result;
    for(NameExpression n:hist_set){
      String name=n.getName();
      VariableInfo info=variables.lookup(name);
      Type t=((DeclarationStatement)info.reference).getType();
      res.add(create.field_decl(n.getName()+"_old",copy_rw.rewrite(t)));
    }
    for(Method m:cl.dynamicMethods()){
      if (!m.getReturnType().isPrimitive(Sort.Process)) continue;
      if (m.getBody()!=null) continue;
      // m defines an action.
      create.enter();
      create.setOrigin(m.getOrigin());
      ContractBuilder begin_cb=new ContractBuilder();
      ArrayList<DeclarationStatement> begin_args=new ArrayList();
      ContractBuilder commit_cb=new ContractBuilder();
      ArrayList<DeclarationStatement> commit_args=new ArrayList();
      res.add(create.method_decl(
          create.primitive_type(Sort.Void),
          begin_cb.getContract(),
          m.name+"_begin",
          begin_args.toArray(new DeclarationStatement[0]),
          null
      ));
      res.add(create.method_decl(
          create.primitive_type(Sort.Void),
          commit_cb.getContract(),
          m.name+"_commit",
          commit_args.toArray(new DeclarationStatement[0]),
          null
      ));
      create.leave();
    }
    result=res;
    */
  }
  
  @Override
  public void visit(MethodInvokation e){
    Method m=e.getDefinition();
    if (m.getReturnType().isPrimitive(Sort.Process)){
      result=create.invokation(null,null, "p_"+e.method,rewrite(e.getArgs()));
    } else {
      super.visit(e);
    }
  }
  
  @Override
  public void visit(Method m){
    if (m.getReturnType().isPrimitive(Sort.Process)){
      Contract c=m.getContract();
      ContractBuilder cb = new ContractBuilder();
      for (ASTNode v:c.modifies){
        create.enter();
        create.setOrigin(v.getOrigin());
        cb.requires(create.expression(StandardOperator.Perm,rewrite(v),create.reserved_name(ASTReserved.FullPerm)));
        cb.ensures(create.expression(StandardOperator.Perm,rewrite(v),create.reserved_name(ASTReserved.FullPerm)));
        create.leave();
      }
      if (c.pre_condition!=null) cb.requires(rewrite(c.pre_condition));
      if (c.post_condition!=null) cb.ensures(rewrite(c.post_condition));
      DeclarationStatement args[]=rewrite(m.getArgs());
      BlockStatement body=null;
      ASTNode m_body=m.getBody();
      if (m_body!=null){
        create.enter();
        body=create(m_body).block();
        m_body=normalize_body(m_body);
        create_body(body,m_body);
        create.leave();
        int N=m.getArity();
        ASTNode [] arg_names = new ASTNode[N];
        for(int i=0;i<N;i++){
          arg_names[i]=create.local_name(m.getArgument(i));
        }
      }
      result=create.method_decl(create.primitive_type(Sort.Void), cb.getContract(), m.name, args, body);
    } else {
      //super.visit(m);
      result=null;
    }
  }


  private ASTNode normalize_body(ASTNode m_body) {
    System.err.print("normalize input: ");
    Configuration.getDiagSyntax().print(System.err,m_body);
    System.err.println();
    m_body=expand_unguarded(m_body);
    System.err.print("guarded rewrite: ");
    Configuration.getDiagSyntax().print(System.err,m_body);
    System.err.println();
    return m_body;
  }

  @Override
  public void visit(PrimitiveType t){
    if (t.sort==Sort.Process){
      Fail("illegal use of process type");
    } else {
      super.visit(t);
    }
  }

  @SuppressWarnings("incomplete-switch")
  private ASTNode expand_unguarded(ASTNode m_body) {
    if (m_body instanceof MethodInvokation){
      MethodInvokation p=(MethodInvokation)m_body;
      Method def=process_map.get(p.method);
      if (def.getBody()==null){
        return m_body;
      } else {
        Hashtable<NameExpression,ASTNode> map=new Hashtable<NameExpression, ASTNode>();
        int N=def.getArity();
        for(int i=0;i<N;i++){
          map.put(create.unresolved_name(def.getArgument(i)),copy_rw.rewrite(p.getArg(i)));
        }
        Substitution sigma=new Substitution(source(),map);
        ASTNode tmp=sigma.rewrite(def.getBody());
        return expand_unguarded(tmp);
      }
    } else if (m_body.isReserved(ASTReserved.EmptyProcess)) {
      return m_body;
    } else if (m_body instanceof OperatorExpression){
      OperatorExpression p=(OperatorExpression)m_body;
      switch(p.getOperator()){
      case Or:{
        ASTNode p0=p.getArg(0);
        ASTNode g0=expand_unguarded(p0);
        ASTNode p1=p.getArg(1);
        ASTNode g1=expand_unguarded(p1);
        return create.expression(StandardOperator.Plus,
            left_merge(g0,p1),
            left_merge(g1,p0)
        );
      }
      case Plus:{
        ASTNode p0=p.getArg(0);
        ASTNode g0=expand_unguarded(p0);
        ASTNode p1=p.getArg(1);
        ASTNode g1=expand_unguarded(p1);
        return create.expression(StandardOperator.Plus,g0,g1);
      }
      case Mult:{
        ASTNode p0=p.getArg(0);
        ASTNode g0=expand_unguarded(p0);
        ASTNode p1=p.getArg(1);
        return create.expression(StandardOperator.Mult,g0,p1); 
      }
      case ITE:{
        ASTNode b=p.getArg(0);
        ASTNode p0=p.getArg(1);
        ASTNode g0=expand_unguarded(p0);
        ASTNode p1=p.getArg(2);
        ASTNode g1=expand_unguarded(p1);
        return create.expression(StandardOperator.ITE,b,g0,g1);
      }
      }
    }
    Fail("illegal process expression");
    return null;
  }

  private ASTNode left_merge(ASTNode m_body, ASTNode other) {
    if (m_body instanceof MethodInvokation){
      return create.expression(StandardOperator.Mult,m_body,other);
    } else if(m_body.isReserved(ASTReserved.EmptyProcess)){
      return other;
    } else if (m_body instanceof OperatorExpression){
      OperatorExpression p=(OperatorExpression)m_body;
      switch(p.getOperator()){
      case Plus:{
        ASTNode p0=p.getArg(0);
        ASTNode g0=left_merge(p0,other);
        ASTNode p1=p.getArg(1);
        ASTNode g1=left_merge(p1,other);
        return create.expression(StandardOperator.Plus,g0,g1);
      }
      case Mult:{
        ASTNode p0=p.getArg(0);
        ASTNode p1=p.getArg(1);
        if (!(p0 instanceof MethodInvokation)) break;
        if (!(p1 instanceof MethodInvokation)) break;
        MethodInvokation m0=(MethodInvokation)p1;
        if (!(other instanceof MethodInvokation)) break;
        MethodInvokation m1=(MethodInvokation)other;
        ArrayList<ASTNode> args=new ArrayList<ASTNode>();
        String key=":"+m0.method+":"+m1.method+":";
        String merged=composite_map.get(key);
        if (merged==null){
          Abort("missing key %s",key);
        }
        for(ASTNode n:m0.getArgs()) args.add(n);
        for(ASTNode n:m1.getArgs()) args.add(n);
        ASTNode guess=create.invokation(null,null,merged,args.toArray(new ASTNode[0]));
        return create.expression(StandardOperator.Mult,p0,guess); 
      }
      case ITE:{
        ASTNode b=p.getArg(0);
        ASTNode p0=p.getArg(1);
        ASTNode g0=left_merge(p0,other);
        ASTNode p1=p.getArg(2);
        ASTNode g1=left_merge(p1,other);
        return create.expression(StandardOperator.ITE,b,g0,g1);
      }
      default:
        break;
      }
    }
    Fail("illegal process expression in left_merge");
    return null;   
  }

  protected void create_body(BlockStatement body, ASTNode m_body) {
    create.enter();
    create.setOrigin(m_body.getOrigin());
    if (m_body instanceof OperatorExpression) {
      OperatorExpression e=(OperatorExpression)m_body;
      switch(e.getOperator()){
      case ITE:{
        BlockStatement lhs=create.block();
        create_body(lhs,e.getArg(1));
        BlockStatement rhs=create.block();
        create_body(rhs,e.getArg(2));
        body.add(create.ifthenelse(copy_rw.rewrite(e.getArg(0)),lhs,rhs));
        break;
      }
      case Mult:{
        create_body(body,e.getArg(0));
        create_body(body,e.getArg(1));
        break;
      }
      case Plus:{
        BlockStatement lhs=create.block();
        create_body(lhs,e.getArg(0));
        BlockStatement rhs=create.block();
        create_body(rhs,e.getArg(1));
        body.add(create.ifthenelse(create.reserved_name(ASTReserved.Any),lhs,rhs));    
        break;
      }
      case Or:{
        Abort("cannot generate body for parallel composition");
      }
      default:
        Abort("skipping unknown process operator %s",e.getOperator());
      }
    } else if (m_body instanceof MethodInvokation) {
      body.add(copy_rw.rewrite(m_body));
    } else if (m_body.isReserved(ASTReserved.EmptyProcess)){
      body.add(create.comment("// empty process"));
    } else {
      Abort("unknown process %s",m_body.getClass());
    }
    create.leave();
  }

  @Override
  public void visit(OperatorExpression e){
    switch(e.getOperator()){
    case Or:
      if(e.getType().isPrimitive(Sort.Process)){
        result=create.invokation(null,null,"p_merge",rewrite(e.getArguments()));
        return;
      }
      break;
    case Mult:
      if(e.getType().isPrimitive(Sort.Process)){
        result=create.invokation(null,null,"p_seq",rewrite(e.getArguments()));
        return;
      }
      break;
      default:
        break;
    }
    super.visit(e);
  }
}
