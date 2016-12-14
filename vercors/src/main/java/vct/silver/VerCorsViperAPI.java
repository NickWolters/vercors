package vct.silver;

import hre.ast.MessageOrigin;
import hre.ast.Origin;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import vct.col.ast.ASTNode;
import vct.col.ast.Axiom;
import vct.col.ast.Method;
import vct.col.ast.ProgramUnit;
import vct.col.ast.Type;
import vct.col.util.ASTFactory;
import vct.error.VerificationError;
import viper.api.VerificationControl;
import viper.api.ViperAPI;
import viper.api.ViperError;

public class VerCorsViperAPI extends ViperAPI<
    Origin, VerificationError, Type, ASTNode, ASTNode,
    Method, Axiom, ProgramUnit> {

  public Hashtable<String,Set<Origin>> refuted=new Hashtable<String,Set<Origin>>();
  
  private VerCorsViperAPI(HREOrigins origin, VerCorsTypeFactory type,
      VerCorsExpressionFactory expr, VerCorsStatementFactory stat,
      VerCorsProgramFactory prog) {
    super(origin, type, expr, stat, prog);
  }

  public static VerCorsViperAPI get() {
    HREOrigins origin=new HREOrigins();
    ASTFactory<Object> create=new ASTFactory<Object>();
    create.setOrigin(new MessageOrigin("VerCorsViperAPI"));
    VerCorsTypeFactory type=new VerCorsTypeFactory(create);
    VerCorsExpressionFactory expr=new VerCorsExpressionFactory(create);
    VerCorsStatementFactory stat=new VerCorsStatementFactory(create);
    VerCorsProgramFactory prog=new VerCorsProgramFactory(create);
    VerCorsViperAPI res=new VerCorsViperAPI(origin, type, expr, stat, prog);
    prog.refuted=res.refuted;
    return res;
  }

  @Override
  public List<? extends ViperError<Origin>> verify(Path tool_home,
      Properties settings, ProgramUnit program, Set<Origin> reachable,
      VerificationControl<Origin> control) {
    throw new Error("Using VerCors backends for Viper is not implemented.");
  }

  @Override
  public void write_program(PrintWriter pw, ProgramUnit program) {
    pw.printf("%s",program);
  }

}
