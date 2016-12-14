package vct.col.ast;

import java.util.ArrayList;
import java.util.Arrays;

import vct.util.ClassName;

public class AxiomaticDataType extends ASTDeclaration {

  @Override
  public <R,A> R accept_simple(ASTMapping1<R,A> map,A arg){
    return map.map(this,arg);
  }

  private ArrayList<Method> cons=new ArrayList<Method>();
  private ArrayList<Method> maps=new ArrayList<Method>();
  private ArrayList<Axiom> axioms=new ArrayList<Axiom>();
  private DeclarationStatement pars[];
  
  public AxiomaticDataType(String name,DeclarationStatement ... pars) {
    super(name);
    this.pars=Arrays.copyOf(pars,pars.length);
  }

  public DeclarationStatement[] getParameters(){
    return pars;
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
  public ClassName getDeclName() {
    return new ClassName(name);
  }

  public Iterable<Method> constructors() {
    return cons;
  }
  
  public Iterable<Method> mappings() {
    return maps;
  }
  
  public Iterable<Axiom> axioms() {
    return axioms;
  }

  public void add_map(Method m){
    m.setFlag(ASTFlags.STATIC,true);
    m.setParent(this);
    maps.add(m);
  }
  
  public void add_cons(Method c){
    c.setFlag(ASTFlags.STATIC,true);
    c.setParent(this);
    cons.add(c);
  }
  
  public void add_axiom(Axiom ax){
    ax.setParent(this);
    axioms.add(ax);
  }
}
