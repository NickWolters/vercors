package vct.col.ast;

import java.util.ArrayList;

public class TryCatchBlock extends ASTNode {

  public final BlockStatement main;
  
  public final BlockStatement after;
  
  public final ArrayList<CatchClause> catches;
  
  public TryCatchBlock(BlockStatement main,BlockStatement after){
   this.main=main;
   this.after=after;
   catches=new ArrayList<CatchClause>();
  }
  

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


  public void catch_clause(DeclarationStatement decl, BlockStatement block) {
    catches.add(new CatchClause(decl,block));
  }
 

}
