package vct.col.ast;

import java.util.ArrayList;

public class ParallelBarrier extends ASTNode {

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
 
  public final String label;
  
  public final Contract contract;
  
  public final ArrayList<String> invs;
  
  public final BlockStatement body;
  
  public ParallelBarrier(String label,Contract contract, ArrayList<String> fences, BlockStatement body){
    this.label=label;
    this.contract=contract;
    this.invs=new ArrayList<String>(fences);
    this.body=body;
  }

}
