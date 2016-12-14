package vct.col.util;

import vct.col.ast.ASTClass;
import vct.col.ast.ASTNode;
import vct.col.ast.AssignmentStatement;
import vct.col.ast.Contract;
import vct.col.ast.DeclarationStatement;
import vct.col.ast.Method;
import vct.col.ast.PrimitiveType.Sort;
import vct.col.ast.RecursiveVisitor;

public class DefinitionScanner extends RecursiveVisitor<Object> {

  private ClassDefinition root;
  private ClassDefinition current;
  private boolean static_context;
  
  public DefinitionScanner(ClassDefinition root){
    super(null,null);
    this.root=root;
  }

  public void visit(ASTClass c) {
    if (c.getParentClass()==null){
      current=root;
    }
    ClassDefinition tmp=current;
    if (c.getName()!=null) {
      current=current.addNested(c.getName());
    }
    Contract contract=c.getContract();
    if (contract!=null){
      for (DeclarationStatement decl : contract.given){
        decl.accept(this);
      }
    }
    int N;
    N=c.getStaticCount();
    for(int i=0;i<N;i++){
      static_context=true;
      ASTNode n=c.getStatic(i);
      n.accept(this);
    }
    N=c.getDynamicCount();
    for(int i=0;i<N;i++){
      static_context=false;
      ASTNode n=c.getDynamic(i);
      n.accept(this);
    }
    if (c.getName()!=null) {
      current=tmp;
    }
  }

  public void visit(DeclarationStatement s){
    if (s.getType().isPrimitive(Sort.Class)){
      current.addNested(s.getName());
    } else {
      current.addField(s.getName(),static_context);
    }
  }

  public void visit(AssignmentStatement s){
    // TODO: scan for nested classes.
  }

  public void visit(Method m){
    current.addMethod(m.getName());
    // TODO: scan body for nested classes.
  }
  
}
