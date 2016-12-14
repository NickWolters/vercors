package vct.col.ast;

public class Dereference extends ASTNode {

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
 

  public final ASTNode object;
  public final String field;
  
  public Dereference(ASTNode object,String field){
    this.object=object;
    this.field=field;
  }

  public final ASTNode getObject(){
    return object;
  }
  
  public boolean match(ASTNode ast){
    if (ast instanceof Hole){
      return ast.match(this);
    } else if (ast instanceof Dereference){
      Dereference d=(Dereference)ast;
      if (field.equals(d.field)){
        return object.match(d.object);
      } else {
        return false;
      }
    } else {
      return false;
    }
  }
  
}

