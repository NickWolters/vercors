package vct.col.ast;

/**
 * This class represents magic wand proofs, a.k.a. create blocks.
 * 
 * @author Stefan Blom
 *
 */
public class Lemma extends ASTNode {

  @Override
  public <R,A> R accept_simple(ASTMapping1<R,A> map,A arg){
    return map.map(this,arg);
  }

	public final BlockStatement block;
	
	public Lemma(BlockStatement block) {
		this.block=block;
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
	 

}
