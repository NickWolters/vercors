package vct.col.ast;

import static hre.lang.System.Abort;
import static hre.lang.System.Debug;

import java.util.Arrays;

public class ClassType extends Type {

  @Override
  public <R,A> R accept_simple(ASTMapping1<R,A> map,A arg){
    return map.map(this,arg);
  }

  public static final ClassType label_type=new ClassType("<<label>>");
  public static final ClassType null_type=new ClassType("<<null>>");
      
  private final String name[];
  
  private ASTDeclaration def;
  
  public void setDefinition(ASTDeclaration def){
    this.def=def;
  }
  
  public ASTDeclaration getDefinition(){
    return def;
  }
  
  public ClassType(String name[],ASTNode ... args){
    super(args);
    if (name.length==0) Abort("illegal name of length 0");
    this.name=Arrays.copyOf(name,name.length);
  }
  
  public String[] getNameFull(){
    return Arrays.copyOf(name,name.length);
  }
  
  public ClassType(String name,ASTNode ... args) {
    super(args);
    this.name=new String[1];
    this.name[0]=name;
  }

  public String getName(){
    return name[name.length-1];
  }
  public String getFullName(){
    String res=name[0];
    for(int i=1;i<name.length;i++){
      res+="."+name[i];
    }
    return res;
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

  public boolean supertypeof(ProgramUnit context, Type t){
    // java.lang.Object is supertype of everything.
    if (name.length==3 && name[0].equals("java") &&
        name[1].equals("lang") && name[2].equals("Object")) return true;
    if (name.length==1 && name[0].equals("java_DOT_lang_DOT_Object")){
      return true;
    }
    if (t instanceof ClassType) {
      ClassType ct=(ClassType)t;
      // no longer needed?? if (name.length==1 && name[0].equals("Object")) return true;
      // Everything is a supertype of <<null>>.
      if (ct.name.length==1 && ct.name[0].equals("<<null>>")) return true;
      if (ct.name.length==1 && ct.name[0].equals("<<label>>")) return true;
      return supertype_search(context,ct);
    }
    return false;
  }
  
  
  private boolean supertype_search(ProgramUnit context, ClassType ct) {
    Debug("checking if %s is asuper type of %s",this,ct);
    if (ct.name.length==name.length){
      int i=0;
      while(i<name.length){
        if (!ct.name[i].equals(name[i])) break;
        i++;
      }
      if (i==name.length) return true;
    }
    Debug("argument is not the same type not the same type");
    if (context==null) {
      Debug("missing context");
      return false;
    }
    ASTClass cl=context.find(ct);
    if (cl==null) {
      Debug("class not found");
      return false;
    }
    for(ClassType ct_parent:cl.super_classes){
      if (supertype_search(context,ct_parent)) return true;
    }
    for(ClassType ct_parent:cl.implemented_classes){
      if (supertype_search(context,ct_parent)) return true;
    }
    return false;
  }

  public String toString(){
    String res=getFullName();
    if (this.args!=null && this.args.length>0){
      res=res+"<"+args[0];
      for(int i=1;i<args.length;i++) res=res+","+args[i];
      res=res+">";
    }
    return res;
  }
  
  public int hashCode(){
    return getFullName().hashCode();
  }
  public boolean equals(Object o){
    if (o instanceof ClassType) {
      return getFullName().equals(((ClassType)o).getFullName());
    } else {
      return false;
    }
  }
  public ASTNode zero(){
    return new NameExpression(ASTReserved.Null);
  }
  
  public boolean isNull(){
    return name.length==1 && name[0].equals("<<null>>"); 
  }
}
