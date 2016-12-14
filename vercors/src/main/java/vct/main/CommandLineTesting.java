package vct.main;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import puptol.PuptolConfig;
import hre.config.IntegerSetting;
import hre.config.Option;
import hre.config.OptionParser;
import hre.config.StringListSetting;
import hre.config.StringSetting;
import hre.util.TestReport.Verdict;
import vct.util.Configuration;


public class CommandLineTesting {
  
 
  public static boolean enabled(){
    return append_option.used();
  }
  
  public static void run_testsuites(){
    ToolTest tt=new ToolTest();
    VCTResult res;
    TestcaseVisitor tv=new TestcaseVisitor();
    for(String dir:selftest){
      if (dir.equals("")){
        System.err.println("Testing backends.");
        res=runtest(tt,Verdict.Inconclusive,"z3","-smt2","//examples/backends/test-sat.smt");
        res.mustSay("p true");
        res.mustSay("q true");
        check(res,"z3","sat");

        res=runtest(tt,Verdict.Inconclusive,"z3","-smt2","//examples/backends/test-unsat.smt");  
        res.mustSay("unsat");
        check(res,"z3","unsat");

        res=runtest(tt,Verdict.Inconclusive,"boogie","//examples/backends/test-pass.bpl");  
        res.mustSay("Boogie program verifier finished with 1 verified, 0 errors");
        check(res,"boogie","passing");

        res = runtest(tt,Verdict.Inconclusive,"boogie","//examples/backends/test-fail.bpl");
        res.mustSay("Boogie program verifier finished with 0 verified, 1 error");
        check(res,"boogie","failing");
        
        res=runtest(tt,Verdict.Inconclusive,"chalice","//examples/backends/test-pass.chalice");  
        res.mustSay("Boogie program verifier finished with 3 verified, 0 errors");
        check(res,"chalice","passing");
        
        res=runtest(tt,Verdict.Inconclusive,"chalice","//examples/backends/test-fail.chalice");  
        res.mustSay("Boogie program verifier finished with 2 verified, 1 error");
        check(res,"chalice","failing");
        
        res=runtest(tt,Verdict.Inconclusive,"dafny","/compile:0","//examples/backends/test-pass.dfy");  
        res.mustSay("Dafny program verifier finished with 2 verified, 0 errors");
        check(res,"dafny","passing");
        
        res=runtest(tt,Verdict.Error,"dafny","/compile:0","//examples/backends/test-fail.dfy");  
        res.mustSay("Dafny program verifier finished with 1 verified, 1 error");
        check(res,"dafny","failing");
        
        res=runtest(tt,Verdict.Inconclusive,"carbon","//examples/backends/test-pass.sil");  
        res.mustSay("No errors found.");
        check(res,"carbon","passing");
        
        res=runtest(tt,Verdict.Error,"carbon","//examples/backends/test-fail.sil");  
        res.mustSay("Assignment might fail. Divisor 0 might be zero.");
        check(res,"carbon","failing");

        res=runtest(tt,Verdict.Inconclusive,"silicon","//examples/backends/test-pass.sil");  
        res.mustSay("No errors found.");
        check(res,"silicon","passing");

        res=runtest(tt,Verdict.Error,"silicon","//examples/backends/test-fail.sil");  
        res.mustSay("Assignment might fail. Divisor 0 might be zero.");
        check(res,"silicon","failing");

        continue;
      }
      try {
        EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        Files.walkFileTree(Paths.get(dir),opts,10,tv);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    if (tv.delayed_fail) System.exit(1);
    String testcmd_prefix="<test>";
    PrintStream cmds=null;
    if (command_file.used()){
      testcmd_prefix="java -cp "+Configuration.getHome().toString()+"/vct-tool.jar vct.main.TestRun ";
      try {
        cmds=new PrintStream(new FileOutputStream(command_file.get()));
      } catch (FileNotFoundException e) {
        System.exit(1);
      }
    }
    PuptolConfig puptol_config=null;
    if (puptol_file.used()){
      puptol_config=new PuptolConfig();
    }
    HashMap<String,Integer> times=new HashMap<String, Integer>();
    int successes=0;
    HashMap<String,Testcase> untested=new HashMap<String,Testcase>();
    HashMap<String,String> failures=new HashMap<String, String>();
    
    
    ExecutorService pool = Executors.newFixedThreadPool(workers.get());
    ExecutorCompletionService<TestResult> ecs=new ExecutorCompletionService<TestResult>(pool);
    int submitted=0;
    
    for(Entry<String,Testcase> item:tv.testsuite.entrySet()){
      String name=item.getKey();
      Testcase test=item.getValue();
      if (test.tools.isEmpty()){
        untested.put(name,test);
      }
      if (lang_option.used()){
        boolean possible=true;
        for(Path p:test.files){
          String lang=TestcaseVisitor.extension(p);
          if (!langs.contains(lang)){
            possible=false;
            break;
          }
        }
        if (!possible) continue;
      }
      if (include_option.used()){
        boolean possible=false;
        for(String suite:test.suites){
          if (includes.contains(suite)){
            possible=true;
            break;
          }
        }
        if (!possible) continue;
      }
      if (exclude_option.used()){
        boolean possible=true;
        for(String suite:test.suites){
          if (excludes.contains(suite)){
            possible=false;
            break;
          }
        }
        if (!possible) continue;
      }
      for(String tool:test.tools){
        if (backend_option.used()&&!backends.contains(tool)) {
          // skip tests for back ends that are not selected.
          continue;
        }
        if (puptol_file.used()){
          if (test.files.size()!=1){
            System.err.printf("cannot configure %s/%s in puptol: too many files%n",
                name,tool);
            continue;
          }
          Path file=null;
          for(Path f:test.files) file=f;
          System.err.printf("test %s/%s%n",name,tool);
          Iterator<Path> iter=file.iterator();
          try {
            while(!iter.next().toString().equals("shared")) {}
          } catch (NoSuchElementException e){
            System.err.printf("path element shared not found%n");
            continue;
          }
          String experiment=iter.next().toString();
          String filename=iter.next().toString();
          ArrayList<String> path=new ArrayList<String>();
          while(iter.hasNext()){
            path.add(filename);
            filename=iter.next().toString();
          }
          System.err.printf("  path: %s%n",path);
          System.err.printf("  file: %s%n",filename);
          for(String opt:test.options){
            System.err.printf("  option: %s%n",opt);
          }
          System.err.printf("to be added to %s%n",experiment);
          puptol_config.add(experiment,path,name,tool,filename,test.options);
          continue;
        }
        ArrayList<String> cmd=new ArrayList<String>();
        cmd.add("vct");
        switch(tool){
        case "silicon":
        case "carbon":
          cmd.add("--silver="+tool);
          break;
        default:
          cmd.add("--"+tool);
        }
        for(String opt:test.options) cmd.add(opt);
        for(Path f:test.files) cmd.add(f.toAbsolutePath().toString());
        if (command_file.used()){
          String testcmd=testcmd_prefix+test.verdict;
          for(String s:cmd){
            testcmd+=" "+s;
          }
          System.err.printf("test %s/%s : %s%n",name,tool,testcmd);
          cmds.printf("test %s::%s << EOF%n", name,tool);
          cmds.printf("%s%n",testcmd);
          cmds.printf("EOF%n");
          continue;
        }
        System.err.printf("submitting %s/%s:%n",name,tool);
        for(String s:cmd){
          System.err.printf(" %s",s);
        }
        System.err.printf("%n");
        ecs.submit(new TestResult(cmd,tt,test,name,tool));
        submitted++;
      }
    }
    for(;submitted>0;submitted--){
      try {
        Future<TestResult> c=ecs.take();
        TestResult tr=c.get();
        times.put(tr.name+"/"+tr.tool,tr.res.times.get("entire run"));
        if (tr.res.verdict==null){
          tr.res.verdict=Verdict.Error;
        }
        if (tr.res.verdict.toString().equals(tr.test.verdict)){
          System.err.printf("%4d %s/%s: Pass %n",submitted,tr.name,tr.tool);
          successes++;
        } else {
          System.err.printf("%4d %s/%s: Fail (%s/%s) %n ",submitted,tr.name,tr.tool,tr.res.verdict,tr.test.verdict);
          for(String s:tr.command){
            System.err.printf(" %s",s);
          }
          System.err.println();
          failures.put(tr.name+"/"+tr.tool,String.format(
              "verdict is %s instead of %s",tr.res.verdict,tr.test.verdict));
        }
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(1);
      }
    }
    pool.shutdown();
    if (puptol_file.used()){
      puptol_config.update(puptol_file.get());
    }
    boolean pass=true;
    for (String file:tv.files_by_name.keySet()){
      Set<Path> items=tv.files_by_name.get(file);
      if (items.size()>1){
        System.err.printf("Warning: there are multiple instance of %s:%n ",file);
        for(Path p:items){
          System.err.printf(" %s",p);
        }
        System.err.printf("%n");
      }
    }
    if (!untested.isEmpty()){
      System.err.printf("Warning: the following %d tests have been disabled:%n",untested.size());
      for(Entry<String,Testcase> item:untested.entrySet()){
        String name=item.getKey();
        Testcase test=item.getValue();
        System.err.printf("  %s ",name);
        String sep="(";
        for(Path f:test.files) {
          System.err.printf("%s%s",sep,f.toString());
          sep=" ";
        }
        System.err.printf(")%n");
      }
    }
    System.err.printf("verification times (ms):%n");
    ArrayList<String> list = new ArrayList<String>(times.keySet());
    Collections.sort(list);
    for(String t:list){
      System.err.printf("%35s: %10d%n",t,times.get(t));
    }
    if(failures.isEmpty()){
      System.err.printf("all %d tests passed%n",successes);
    } else {
      pass=false;
      System.err.printf("the following tests failed%n");
      for(Entry<String,String> t:failures.entrySet()){
        System.err.printf("  %s: %s%n",t.getKey(),t.getValue());
      }
      System.err.printf("total %s successes and %d failures%n",successes,failures.size());
    }
    if (tv.unmarked.size()>0){
      pass=false;
      System.err.printf("there were %d unmarked files:%n",tv.unmarked.size());
      for(Path p: tv.unmarked){
        System.err.printf("  %s%n",p);
      }
    }
    if(pass){
      System.exit(0);
    } else {
      System.exit(1);
    }
  }

  private static void check(VCTResult res,String tool,String test) {
    if (res.verdict !=Verdict.Pass) {
      System.err.printf("%s did not pass the %s test%s",tool,test);
      System.exit(1);
    }
  }

  private static VCTResult runtest(ToolTest tt,Verdict expect,String ... args) {
    System.err.printf("executing");
    for(String s:args) System.err.printf(" %s",s);
    System.err.println();
    VCTResult res;
    res=tt.run(args);  
    if (res.verdict==expect){
      res.verdict=Verdict.Pass;
    } else {
      System.err.printf("%s did not execute properly%n",args[0]);
      System.exit(1);
    }
    return res;
  }
  
  private static StringListSetting includes=new StringListSetting();
  private static Option include_option;
  private static StringListSetting excludes=new StringListSetting();
  private static Option exclude_option;
  
  private static StringListSetting langs=new StringListSetting();
  private static Option lang_option;
  private static StringListSetting backends=new StringListSetting();
  private static Option backend_option;
  private static StringListSetting selftest=new StringListSetting();
  private static Option append_option;
  protected static StringSetting savedir=new StringSetting(null);
  
  public static IntegerSetting workers=new IntegerSetting(1);
  
  public static StringSetting command_file=new StringSetting(null);
  private static Option commandlines=
      command_file.getAssign("output file with list of commands instead");
  
  public static StringSetting puptol_file=new StringSetting(null);
  private static Option puptolupdate=
      puptol_file.getAssign("update experiments in puptol file");
  
  
  
  
  public static void add_options(OptionParser clops) {
    append_option=selftest.getAppendOption("execute test suites from the command line. "+
        "Each test suite is a folder which is scanned for valid test inputs");
    clops.add(append_option,"test");
    clops.add(backend_option=backends.getAppendOption("select the back ends to run tests for"),"tool");
    clops.add(lang_option=langs.getAppendOption("select test input languages"),"lang");
    clops.add(savedir.getAssign("save intermediate files to given directory"),"save-intermediate");
    clops.add(include_option=includes.getAppendOption("include test suites"),"include-suite");
    clops.add(exclude_option=excludes.getAppendOption("exclude test suites"),"exclude-suite");
    clops.add(commandlines,"commands");
    clops.add(puptolupdate,"puptol-config");
    clops.add(workers.getAssign("set the number of parallel tests"),"test-workers");
  }

}

class TestResult implements Callable<TestResult> {

  public TestResult(ArrayList<String> cmd,ToolTest tt,Testcase test,String name,String tool){
    command=cmd.toArray(new String[0]);
    this.tt=tt;
    this.test=test;
    this.name=name;
    this.tool=tool;
  }
  
  String name;
  
  String tool;
  
  Testcase test;
  
  VCTResult res;
  
  String command[];
  
  ToolTest tt;
  
  @Override
  public TestResult call() throws Exception {
    res=tt.run(command);
    return this;
  }
  
}
