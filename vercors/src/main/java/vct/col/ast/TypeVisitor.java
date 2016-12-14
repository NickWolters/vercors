package vct.col.ast;

public interface TypeVisitor {

  public void visit(PrimitiveType t);
  
  public void visit(ClassType t);

  public void visit(RecordType t);
  
  public void visit(FunctionType t);

}
