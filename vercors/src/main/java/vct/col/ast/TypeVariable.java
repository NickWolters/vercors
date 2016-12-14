package vct.col.ast;

public class TypeVariable extends Type {

  @Override
  public <R,A> R accept_simple(ASTMapping1<R,A> map,A arg){
    return map.map(this,arg);
  }

  public final String name;
  
  public TypeVariable(String name) {
    this.name=name;
  }
  
  public boolean isNumeric() {
    return false;
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
 
  @Override
  public <T> T accept_simple(TypeMapping<T> map){
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


  @Override
  public boolean supertypeof(ProgramUnit context, Type t) {
    return false;
  }
  
  public int hashCode(){
    return name.hashCode();
  }
  
  public boolean equals(Object o){
    if (o instanceof TypeVariable){
      return ((TypeVariable)o).name.equals(name);
    }
    return false;
  }

}
