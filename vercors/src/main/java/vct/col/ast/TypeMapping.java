package vct.col.ast;

public interface TypeMapping<R> {
  
  public void pre_map(Type t);
  
  public R post_map(Type t,R res);
    
  public R map(ClassType t);
  
  public R map(FunctionType t);
  
  public R map(PrimitiveType t);
  
  public R map(RecordType t);
  
  public R map(TupleType t);

  public R map(TypeExpression t);

  public R map(TypeVariable v);

}
