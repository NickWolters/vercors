package vct.col.ast;

import vct.util.ClassName;

public abstract class ASTDeclaration extends ASTNode {

  public abstract ClassName getDeclName();

  /** contains the name of the class/module/package. */
  public final String name;

  protected ASTDeclaration(String name){
    this.name=name;
  }
  
  /** contains the root of the source forest. */
  protected ProgramUnit root;
  /** contains the package name */
  protected ClassName package_name;
  
  public boolean packageDefined(){
    return package_name!=null;
  }
  public void attach(ProgramUnit root,ClassName package_name){
    this.root=root;
    this.package_name=package_name;
  }

}
