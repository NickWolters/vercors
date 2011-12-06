package silAST.programs.symbols

import silAST.ASTNode
import silAST.expressions.Expression
import silAST.source.SourceLocation

final class Predicate private[silAST](
                                       sl: SourceLocation,
                                       val name: String
                                       ) extends ASTNode(sl) {
  override def toString = "predicate " + name + " = " + expression.toString

  override def subNodes = expression :: Nil

  private[silAST] var pExpression : Option[Expression] = None

  def expression : Expression = pExpression.get

}