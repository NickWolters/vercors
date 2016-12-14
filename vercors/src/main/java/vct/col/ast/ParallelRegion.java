package vct.col.ast;

import java.util.Arrays;

public class ParallelRegion extends ASTNode {
  
  @Override
  public <R,A> R accept_simple(ASTMapping1<R,A> map,A arg){
    return map.map(this,arg);
  }

  @Override
  public <T> void accept_simple(ASTVisitor<T> visitor){
    try {
      visitor.visit(this);
    } catch (Throwable t){
      if (thrown.get()!=t){
        System.err.printf("Triggered by %s:%n",getOrigin());
        thrown.set(t);
    }
      throw t;
    }
  }
  
  @Override
  public <T> T accept_simple(ASTMapping<T> map){
    try {
      return map.map(this);
    } catch (Throwable t){
      if (thrown.get()!=t){
        System.err.printf("Triggered by %s:%n",getOrigin());
        thrown.set(t);
     }
      throw t;
    }
  }

  public final ParallelBlock blocks[];
  public final Contract contract; 
  
  public ParallelRegion(Contract contract,ParallelBlock ... blocks){
    this.blocks=Arrays.copyOf(blocks,blocks.length);
    this.contract=contract;
  }

}
