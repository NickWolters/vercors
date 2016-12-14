package vct.col.ast;

public interface BeforeAfterAnnotations {

  public BeforeAfterAnnotations set_before(BlockStatement block);
  
  public BlockStatement get_before();
  
  public BeforeAfterAnnotations set_after(BlockStatement block);
  
  public BlockStatement get_after();

}
