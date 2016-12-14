package vct.main;

import java.io.File;

import vct.antlr4.parser.Parsers;
import vct.col.ast.ProgramUnit;
import vct.col.rewrite.RewriteSystem;
import vct.util.Configuration;

public class RewriteSystems {

  static ProgramUnit systems;
  static {
    File file=new File(new File(Configuration.getHome().toFile(),"config"),"rules.java");
    systems=Parsers.getParser("java").parse(file);
  }
  
  public static RewriteSystem getRewriteSystem(String name){
    return new RewriteSystem(systems,name);
  }

}
