package vct.col.ast;

public interface ASTMapping1<R,A1> {
  
  public void pre_map(ASTNode n,A1 a);
  
  public R post_map(ASTNode n,R res,A1 a);
  
  public R map(StandardProcedure p,A1 a);
  
  public R map(StructValue v,A1 a);
  
  public R map(ConstantExpression e,A1 a);
  
  public R map(OperatorExpression e,A1 a);
  
  public R map(NameExpression e,A1 a);
  
  public R map(ClassType t,A1 a);
  
  public R map(FunctionType t,A1 a);
  
  public R map(PrimitiveType t,A1 a);
  
  public R map(RecordType t,A1 a);
  
  public R map(MethodInvokation e,A1 a);

  public R map(BlockStatement s,A1 a);
  
  public R map(IfStatement s,A1 a);
  
  public R map(ReturnStatement s,A1 a);
  
  public R map(AssignmentStatement s,A1 a);

  public R map(DeclarationStatement s,A1 a);
  
  public R map(LoopStatement s,A1 a);
  
  public R map(ForEachLoop s,A1 a);
  
  public R map(Method m,A1 a);

  public R map(ASTClass c,A1 a);

  public R map(BindingExpression e,A1 a);

  public R map(Dereference e,A1 a);

  public R map(Lemma lemma,A1 a);

  public R map(ParallelBarrier parallelBarrier,A1 a);

  public R map(ParallelBlock parallelBlock,A1 a);

  public R map(ParallelRegion region,A1 a);

  public R map(Contract contract,A1 a);

  public R map(ASTSpecial special,A1 a);

  public R map(VariableDeclaration variableDeclaration,A1 a);

  public R map(TupleType tupleType,A1 a);

  public R map(AxiomaticDataType adt,A1 a);

  public R map(Axiom axiom,A1 a);

  public R map(Hole hole,A1 a);

  public R map(ActionBlock actionBlock,A1 a);

  public R map(TypeExpression t,A1 a);

  public R map(ParallelAtomic pa, A1 a);
  
  public R map(ParallelInvariant inv, A1 a);
  
  public R map(NameSpace ns, A1 a);

  public R map(TryCatchBlock tcb, A1 a);
  
  public R map(FieldAccess s,A1 a);
  
  public R map(TypeVariable v,A1 a);
  
  public R map(VectorBlock vb,A1 a);
  
  public R map(Constraining c,A1 a);
}
