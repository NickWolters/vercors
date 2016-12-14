package vct.col.rewrite;

import hre.ast.MessageOrigin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import vct.antlr4.parser.Parsers;
import vct.col.ast.*;
import vct.col.ast.PrimitiveType.Sort;
import vct.col.util.ASTUtils;
import vct.util.Configuration;

/**
 * This rewriter converts a program with classes into
 * a program with records.
 * 
 * 
 * @author Stefan Blom
 *
 */
public class SilverClassReduction extends AbstractRewriter {

  private static final String SEP="__";
      
  private static final String ILLEGAL_CAST="possibly_illegal_cast";
  
  private ASTClass ref_class;
  
  private ClassType ref_type;
  
  private HashSet<Type> ref_items=new HashSet<Type>();
  
  public SilverClassReduction(ProgramUnit source) {
    super(source);
    create.setOrigin(new MessageOrigin("collected class Ref"));
    ref_class=create.ast_class("Ref", ASTClass.ClassKind.Record,null, null, null);
    target().add(ref_class);
    ref_type=create.class_type("Ref");
  }

  @Override
  public void visit(AxiomaticDataType adt){
    super.visit(adt);
    if (adt.name.equals("TYPE")){
      AxiomaticDataType res=(AxiomaticDataType)result;
      res.add_cons(create.function_decl(
          create.class_type("TYPE"),
          null,
          "type_of",
          new DeclarationStatement[]{create.field_decl("val",create.class_type("Ref"))},
          null
      ));
      ref_class.add(create.field_decl(ILLEGAL_CAST,create.class_type("Ref")));
    }
  }
  
  @Override
  public void visit(NameExpression e){
    if (e.isReserved(ASTReserved.OptionNone)){
      Type t=rewrite(e.getType());
      result=create.invokation(t, null, "VCTNone");
    } else {
      super.visit(e);
    }
  }
  
  private boolean options=false;
  
  private boolean floats=false;
  
  private AtomicInteger option_count=new AtomicInteger();
  private HashMap<Type,String> option_get=new HashMap<Type,String>();
  
  @Override
  public void visit(PrimitiveType t){
    switch(t.sort){
    case Cell:
      ref_items.add((Type)rewrite(t.getArg(0)));
      result=ref_type;
      break;
    case Float:
      floats=true;
      result=create.class_type("VCTFloat");
      break;
    case Option:
    {
      options=true;
      ASTNode args[]=rewrite(((PrimitiveType)t).getArgs());
      args[0].addLabel(create.label("T"));
      result=create.class_type("VCTOption",args);
      break;
    }
    default:
      super.visit(t);
      break;
    }    
  }
  
  @Override
  public void visit(ASTClass cl){
    if (cl.getStaticCount()>0){
      Fail("class conversion cannot be used for static entries yet.");
    }
    for(DeclarationStatement decl:cl.dynamicFields()){
      create.enter();
      create.setOrigin(decl.getOrigin());
      DeclarationStatement res=create.field_decl(cl.name+SEP+decl.name,
          rewrite(decl.getType()),rewrite(decl.getInit()));
      create.leave();
      ref_class.add(res);
    }
  }
  
  @Override
  public void visit(ClassType t){
    if (source().find(t.getNameFull())==null){
      // ADT type
      super.visit(t);
    } else {
      result=create.class_type("Ref");
    }
  }
  @Override
  public void visit(Dereference e){
    if (e.object.getType()==null){
      Fail("untyped object %s at %s",e.object,e.object.getOrigin());
      result=create.dereference(rewrite(e.object),"????"+SEP+e.field);
      return;
    }
    Type t=e.object.getType();
    if (t.isPrimitive(Sort.Cell)){
      PrimitiveType tt=(PrimitiveType)t;
      Type type=(Type)rewrite(tt.getArg(0));
      String name=type.toString();
      ref_items.add(type);
      result=create.dereference(rewrite(e.object),name+SEP+e.field);
    } else {
      result=create.dereference(rewrite(e.object),((ClassType)t).getName()+SEP+e.field);
    }
  }
  
  @Override
  public void visit(OperatorExpression e){
    switch(e.getOperator()){
    case FoldPlus:{
      if (e.getType().isPrimitive(Sort.Float)){
        result=create.domain_call("VCTFloat", "fsum", rewrite(e.getArguments()));
      } else {
        Fail("cannot do a summation of type %s",e.getType()); 
      }
      break;      
    }
    case Plus:{
      if (e.getType().isPrimitive(Sort.Float)){
        result=create.domain_call("VCTFloat", "fadd", rewrite(e.getArguments()));
      } else {
        super.visit(e); 
      }
      break;
    }
    case OptionSome:{
      options=true;
      Type t=rewrite(e.getType());
      result=create.invokation(t, null,"VCTSome",rewrite(e.getArguments()));
      break;
    }
    case OptionGet:{
      options=true;
      Type t=rewrite(e.getArg(0).getType());
      String method=optionGet(t);
      result=create.invokation(null, null,method,rewrite(e.getArguments()));
      break;
    }
    case New:{
      ClassType t=(ClassType)e.getArg(0);
      ASTClass cl=source().find(t);
      ArrayList<ASTNode>args=new ArrayList<ASTNode>();
      //NameExpression f=create.field_name("A__x");
      //f.setSite(ref_class);
      for(DeclarationStatement field:cl.dynamicFields()){
        args.add(create.dereference(create.class_type("Ref"),cl.name+SEP+field.name));
      }
      result=create.expression(StandardOperator.NewSilver,args.toArray(new ASTNode[0]));
      break;
    }
    case TypeOf:{
      result=create.domain_call("TYPE","type_of",rewrite(e.getArg(0)));
      break;
    }
    case Cast:{
      Type t0=e.getArg(1).getType();
      ASTNode object=rewrite(e.getArg(1));
      Type t=(Type)e.getArg(0);
      if (t.isPrimitive(Sort.Float)){
        if (t0.isPrimitive(Sort.Integer)){
          result=create.domain_call("VCTFloat","ffromint",object);
        } else {
          Fail("cannot convert %s to float yet.",t0);
        }
      } else {
        ASTNode condition=create.invokation(null, null,"instanceof",
            create.domain_call("TYPE","type_of",object),
            //create.invokation(null,null,"type_of",object));
            create.domain_call("TYPE","class_"+t));
            
        ASTNode illegal=create.dereference(object,ILLEGAL_CAST);
        result=create.expression(StandardOperator.ITE,condition,object,illegal);
      }
      break;
    }
    default:
      super.visit(e);
    }
  }
  
  private String optionGet(Type t) {
    String method=option_get.get(t);
    if (method==null){
      method="getVCTOption"+option_count.incrementAndGet();
      option_get.put(t, method);
    }
    return method;
  }

  @Override
  public void visit(Method m){
    String name=m.getName();
    ContractBuilder cb=new ContractBuilder();
    DeclarationStatement args[]=rewrite(m.getArgs());
    Contract c=m.getContract();
    if (c!=null){
      cb.given(rewrite(c.given));
      cb.yields(rewrite(c.yields));
      if (c.modifies!=null) cb.modifies(rewrite(c.modifies)); 
      if (c.accesses!=null) cb.accesses(rewrite(c.accesses)); 
      in_requires=true;
      for(ASTNode clause:ASTUtils.conjuncts(c.invariant,StandardOperator.Star)){
        cb.requires(rewrite(clause));
      }
      for(ASTNode clause:ASTUtils.conjuncts(c.pre_condition,StandardOperator.Star)){
        cb.requires(rewrite(clause));
      }
      in_requires=false;
      in_ensures=true;
      for(ASTNode clause:ASTUtils.conjuncts(c.invariant,StandardOperator.Star)){
        cb.ensures(rewrite(clause));
      }
      for(ASTNode clause:ASTUtils.conjuncts(c.post_condition,StandardOperator.Star)){
        cb.ensures(rewrite(clause));
      }
      in_ensures=false;
      if (c.signals!=null) for(DeclarationStatement decl:c.signals){
        cb.signals((ClassType)rewrite(decl.getType()),decl.getName(),rewrite(decl.getInit()));      
      }
    }
    Method.Kind kind=m.kind;
    Type rt=rewrite(m.getReturnType());
    c=cb.getContract();
    currentContractBuilder=null;
    ASTNode body=rewrite(m.getBody());
    result=create.method_kind(kind, rt, c, name, args, m.usesVarArgs(), body);

  }
  
  @Override
  public ProgramUnit rewriteAll(){
    ProgramUnit res=super.rewriteAll();
    for(Type t:ref_items){
      String s=t.toString();
      ref_class.add_dynamic(create.field_decl(s+SEP+"item",t));
    }
    if (options || floats){
      File file=new File(new File(Configuration.getHome().toFile(),"config"),"prelude.sil");
      ProgramUnit prelude=Parsers.getParser("sil").parse(file);
      for(ASTNode n:prelude){
        if (n instanceof AxiomaticDataType){
          AxiomaticDataType adt=(AxiomaticDataType)n;
          switch(adt.name){
          case "VCTOption":
            if (options) res.add(n);
            break;
          case "VCTFloat":
            if (floats) res.add(n);
            break;
          }
        }
      }
      for(Entry<Type,String> entry:option_get.entrySet()){
        create.enter();
        create.setOrigin(new MessageOrigin("Generated OptionGet code"));
        Type t=rewrite(entry.getKey());
        Type returns=(Type)((ClassType)t).getArg(0);
        String name=entry.getValue();
        ContractBuilder cb=new ContractBuilder();
        cb.requires(neq(create.local_name("x"),create.invokation(t,null,"VCTNone")));
        DeclarationStatement args[]=new DeclarationStatement[]{
          create.field_decl("x",t)
        };
        ASTNode body=create.invokation(t,null,"getVCTOption",create.local_name("x"));
        Contract contract=cb.getContract();
        Method method=create.function_decl(returns, contract, name, args, body);
        method.setStatic(true);
        res.add(method);
        create.leave();
      }
    }
    return res;
  }
}
