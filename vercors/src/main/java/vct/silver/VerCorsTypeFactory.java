package vct.silver;

import hre.ast.MessageOrigin;
import vct.col.ast.ClassType;
import vct.col.ast.PrimitiveType.Sort;
import vct.col.ast.Type;
import vct.col.ast.TypeVariable;
import vct.col.util.ASTFactory;
import viper.api.TypeFactory;

public class VerCorsTypeFactory implements
    TypeFactory<Type> {

  public VerCorsTypeFactory(ASTFactory<?> create){
    this.create=create;
  }
  
  private ASTFactory<?> create;

  @Override
  public Type Bag(Type t) {
    return create.primitive_type(Sort.Bag,t);
  }

  @Override
  public Type Bool() {
    Type res=create.primitive_type(Sort.Boolean);
    return res;
  }
  
  @Override
  public Type domain_type(String name,java.util.Map<String,Type> args) {
    create.enter();
    create.setOrigin(new MessageOrigin("Silver Domain Type"));
    ClassType res=create.class_type(name,args);
    create.leave();
    return res;
  }

  @Override
  public Type Int() {
    return create.primitive_type(Sort.Integer);
  }

  @Override
  public Type List(Type t) {
    return create.primitive_type(Sort.Sequence,t);
  }

  @Override
  public Type Perm() {
    Type res=create.primitive_type(Sort.ZFraction);
    return res;
  }
  
  @Override
  public Type Ref() {
    //enter(null);
    Type res=create.class_type("Ref");
    //leave();
    return res;
  }

  @Override
  public Type Set(Type t) {
    //enter(null);
    Type res=create.primitive_type(Sort.Set,t);
    //leave();
    return res;
  }

  @Override
  public Type type_var(String name) {
    TypeVariable res=new TypeVariable(name);
    res.setOrigin(new MessageOrigin("Silver Type Variable"));
    return res;
  }

}
