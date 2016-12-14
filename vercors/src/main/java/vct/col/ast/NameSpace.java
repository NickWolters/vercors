package vct.col.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import hre.lang.HREError;
import vct.util.ClassName;

public class NameSpace extends ASTDeclaration implements ASTSequence<NameSpace> {

  public static class Import {
    
    public final boolean static_import;
    
    public final boolean all;
    
    public final String[] name;
    
    public Import(boolean si,boolean all,String ... name){
      static_import=si;
      this.all=all;
      this.name=Arrays.copyOf(name,name.length);
    }
  }
  
  public static final String NONAME = "";

  private static String check_length(String name[]){
    if (name.length==0) throw new HREError("empty name space");
    return name[name.length-1];
  }
  
  public final ArrayList<Import> imports=new ArrayList<Import>();
  
  private ClassName full_name;
  
  public NameSpace(String ... name) {
    super(check_length(name));
    if (name.length==0) throw new HREError("empty name space");
    full_name=new ClassName(name);
  }

  @Override
  public ClassName getDeclName() {
    return full_name;
  }

  @Override
  public <R,A> R accept_simple(ASTMapping1<R,A> map,A arg){
    try {
      return map.map(this,arg);
    } catch (Throwable t){
      if (thrown.get()!=t){
        System.err.printf("Triggered by %s:%n",getOrigin());
        thrown.set(t);
     }
      throw t;
    }
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
  /**
   * A namespace contains declarations.
   */
  private ArrayList<ASTDeclaration> space=new ArrayList<ASTDeclaration>();

  @Override
  public int size(){
    return space.size();
  }
  
  @Override
  public ASTDeclaration get(int i){
    return space.get(i);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public Iterator<ASTNode> iterator() {
    return (Iterator)space.iterator();
  }

  @Override
  public NameSpace add(ASTNode item) {
    if (item instanceof ASTDeclaration){
      space.add((ASTDeclaration)item);
    } else if(item instanceof VariableDeclaration) {
      for(DeclarationStatement d:((VariableDeclaration)item).flatten()){
        space.add(d);
      }
    } else if (item==null) {
    } else {
      hre.lang.System.Warning("cannot insert %s into name space.",item);
      //Abort("cannot insert %s into name space.",item.getClass());
    }
    return this;
  }

  public void add_import(boolean si,boolean all,String ... name) {
    imports.add(new Import(si,all,name));    
  }


}
