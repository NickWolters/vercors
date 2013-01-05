package semper.sil.ast.methods

import implementations.ImplementationFactory
import semper.sil.ast.programs.{NodeFactory, ProgramFactory}
import collection.mutable.ListBuffer
import semper.sil.ast.programs.symbols.{ProgramVariableSequence, ProgramVariable}
import semper.sil.ast.expressions.util.ExpressionSequence
import semper.sil.ast.source.SourceLocation
import semper.sil.ast.expressions.{Expression, ExpressionFactory}
import semper.sil.ast.types.DataType
import collection.mutable

class MethodFactory(
                     val programFactory: ProgramFactory,
                     val name: String
                     )(val sourceLocation: SourceLocation, val comment: List[String])
  extends NodeFactory
  with ExpressionFactory
  with ScopeFactory {
  //  override def scope = method

  def compile(): Method = {
    if (!signatureDefined)
      finalizeSignature()
    for (i <- implementationFactories) i.compile()

    method
  }

  def addParameter(name: String, dataType: DataType, sourceLocation: SourceLocation, comment: List[String] = Nil) = {
    require(!signatureDefined)
    require(programVariables.forall(_.name != name))
    val result = new ProgramVariable(name, dataType)(sourceLocation, comment)
    parametersGenerator += result
    result
  }

  def addResult(name: String, dataType: DataType, sourceLocation: SourceLocation, comment: List[String] = Nil) = {
    require(!signatureDefined)
    require(programVariables.forall(_.name != name))
    val result = new ProgramVariable(name, dataType)(sourceLocation, comment)
    resultsGenerator += result
    //    programVariables += result
    result
  }

  def addPrecondition(e: Expression, sourceLocation: SourceLocation) = {
    require(!signatureDefined)
    preconditions += e
  }

  def addPostcondition(e: Expression, sourceLocation: SourceLocation) = {
    require(!signatureDefined)
    postconditions += e
  }

  def finalizeSignature() {
    require(!signatureDefined)

    pParameters = Some(new ProgramVariableSequence(parametersGenerator)(sourceLocation, Nil))
    pResults = Some(new ProgramVariableSequence(resultsGenerator)(sourceLocation, Nil))
    val preconditions = new ExpressionSequence(this.preconditions) //TODO:more accurate locations
    val postconditions = new ExpressionSequence(this.postconditions) //TODO:more accurate locations
    val signature = new MethodSignature(pParameters.get, pResults.get, preconditions, postconditions)(sourceLocation)
    pMethod = Some(new Method(name, signature, this)(sourceLocation, comment))
    //    signatureDefined = true

  }

  def addImplementation(sourceLocation: SourceLocation, comment: List[String] = Nil): ImplementationFactory = {
    if (!signatureDefined) finalizeSignature()
    val result = new ImplementationFactory(this)(sourceLocation, comment)
    implementationFactories += result
    method.pImplementations += result.implementation
    result
  }


  //////////////////////////////////////////////////////////////////////////////////////
  private var pMethod: Option[Method] = None

  def method: Method = if (pMethod.isDefined) pMethod.get else throw new Exception

  protected[sil] override val parentFactory = Some(programFactory)

  //  val fields: Set[Field] = programFactory.fields.toSet

  var pParameters: Option[ProgramVariableSequence] = None
  var pResults: Option[ProgramVariableSequence] = None

  def parameters: ProgramVariableSequence = {
    require(signatureDefined)
    pParameters.get
  }

  def results: ProgramVariableSequence = {
    require(signatureDefined)
    pResults.get
  }

  private val parametersGenerator = new ListBuffer[ProgramVariable]
  private val resultsGenerator = new ListBuffer[ProgramVariable]
  private val preconditions = new ListBuffer[Expression]
  private val postconditions = new ListBuffer[Expression]

  private def signatureDefined = pMethod != None

  private val implementationFactories = new mutable.HashSet[ImplementationFactory]

  override def programVariables = inputProgramVariables union outputProgramVariables

  override def inputProgramVariables = parametersGenerator.toSet[ProgramVariable]

  override def outputProgramVariables = resultsGenerator.toSet[ProgramVariable]

  override def typeVariables = Set()
}