package vct.main;

import hre.debug.HeapDump;
import hre.io.PrefixPrintStream;
import hre.util.CompositeReport;
import hre.util.TestReport;
import vct.boogie.BoogieReport;
import vct.col.ast.ASTClass;
import vct.col.ast.ASTClass.ClassKind;
import vct.col.ast.ASTNode;
import vct.col.ast.ASTSpecial.Kind;
import vct.col.ast.BlockStatement;
import vct.col.ast.Contract;
import vct.col.ast.DeclarationStatement;
import vct.col.ast.Method;
import vct.col.ast.OperatorExpression;
import vct.col.ast.PrimitiveType;
import vct.col.ast.ProgramUnit;
import vct.col.ast.RecursiveVisitor;
import vct.col.ast.StandardOperator;
import vct.col.rewrite.AbstractRewriter;
import vct.col.util.ASTFactory;
import vct.col.util.ASTUtils;

/**
 * This class scans a given AST for assertions and checks each assertion as if it were
 * a First Order Logic formula using Boogie.
 * 
 * @author Stefan Blom
 *
 */
public class BoogieFOL {

  /**
   * This class extends the abstract scanner to scan for methods.
   * It will then scan those methods for assertions.
   *  vct --passes="resolv,boogie-fol" BoogieTemp.java 
   *
   * @author Stefan Blom
   *
   */
  private static class MethodFinder extends RecursiveVisitor<Object> {
    
    public MethodFinder(){
      super(null,null);
    }
    
    CompositeReport report=new CompositeReport();
    
    /** 
     * Executed when the abstract scanner finds a method.
     */
    public void visit(Method m){
      PrefixPrintStream out=new PrefixPrintStream(System.out);
      ASTNode body=m.getBody();
      Contract c=m.getContract();
      out.printf("starting%n");
      if (c==null) Fail("method %s has no contract",m.getName());
      out.printf("begin precondition%n");
      HeapDump.tree_dump(out,c.pre_condition,ASTNode.class);
      out.printf("end precondition%n");
      if (body instanceof BlockStatement){
        /* In Java the body of a method always is a block.
         * However, the AST also allows expressions as Method bodies.
         */
        BlockStatement block=(BlockStatement)body;
        int N=block.getLength();
        for(int i=0;i<N;i++){
          out.printf("========%d=======%n",i);
          HeapDump.tree_dump(out,block.getStatement(i),ASTNode.class);
          out.printf("end block%n");
          if (block.getStatement(i) instanceof OperatorExpression){
            OperatorExpression e=(OperatorExpression)block.getStatement(i);
            if (e.isSpecial(Kind.Assert)) continue;
            DeclarationStatement args[]=m.getArgs();
            ASTNode formula=e.getArg(0);
            System.err.printf("checking formula at %s%n",formula.getOrigin());
            vct.util.Configuration.getDiagSyntax().print(System.out,formula);
            for(ASTNode part:ASTUtils.conjuncts(formula,StandardOperator.And)){
              System.err.print("conjuct: ");
              vct.util.Configuration.getDiagSyntax().print(System.out,part);
            }
            BoogieReport res=check_boogie(args,formula);
            System.err.printf("formula at %s: %s%n",e.getOrigin(),res.getVerdict());
            report.addReport(res);
            for(ASTNode part:ASTUtils.conjuncts(formula,StandardOperator.And)){
              System.err.print("conjuct ");
              vct.util.Configuration.getDiagSyntax().print(System.out,part);
              System.err.println();
              res=check_boogie(args,part);
            }
          }
        }
      } else {
        System.err.printf("skipping non-block body of method %s at %s%n",m.getName(),m.getOrigin());
      }
      out.printf("begin precondition2%n");
      HeapDump.tree_dump(out,c.pre_condition,ASTNode.class);
      out.printf("end precondition2%n");
    }
  }

  /** find all assertions in the given program and check them 
   * with Boogie as if they were first order logic formulas.
   * @param program The program to scan for assertions.
   * @return The result of checking assertions in the form of a test report.
   */
  public static TestReport main(ASTClass program) {
    MethodFinder finder=new MethodFinder();
    program.accept(finder);
    return finder.report;
  }


  /**
   * Check a formula using Boogie.
   * @param args The variables used in the formula. These are implicitly universally quantified.
   * @param formula The formula to be checked.
   */
  public static BoogieReport check_boogie(DeclarationStatement args[],ASTNode formula){
    ASTFactory<?> create=new ASTFactory<Object>();
    create.setOrigin(formula.getOrigin());
    
    AbstractRewriter copy_rw=new AbstractRewriter((ProgramUnit)null);
    
    ASTClass program=create.ast_class("dummy",ClassKind.Plain,null,null,null);
    for (ASTNode n:args){
      program.add_static(n.apply(copy_rw));
    }
    ASTNode assertion=create.special(Kind.Assert,formula.apply(copy_rw));
    BlockStatement body=create.block(assertion);
    program.add_static(create.method_decl(
        create.primitive_type(PrimitiveType.Sort.Void),
        null,
        "formula",
        new DeclarationStatement[0],
        body));
    //hre.debug.HeapDump.tree_dump(new hre.io.PrefixPrintStream(System.err),program,ASTNode.class);
    ProgramUnit pgm=new ProgramUnit();
    pgm.add(program);
    return vct.boogie.Main.TestBoogie(pgm);
  }
}
