/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package viper
package silicon
package theories

import interfaces.PreambleEmitter
import interfaces.decider.Prover
import decider.PreambleFileEmitter
import state.SymbolConvert
import state.terms

trait SetsEmitter extends PreambleEmitter

/* TODO: Shares a lot of implementation with DefaultSequencesEmitter. Refactor! */

class DefaultSetsEmitter(prover: Prover,
                         symbolConverter: SymbolConvert,
                         preambleFileEmitter: PreambleFileEmitter[String, String])
    extends SetsEmitter {

  private var collectedSorts = Set[terms.sorts.Set]()

  def sorts = toSet(collectedSorts)

  /**
   * The symbols are take from a file and it is currently not possible to retrieve a list of
   * symbols that have been declared.
   *
   * @return None
   */
  def symbols = None

  /* Lifetime */

  def reset() {
    collectedSorts = collectedSorts.empty
  }

  def start() {}
  def stop() {}

  /* Functionality */

  def analyze(program: ast.Program) {
    var setTypes = Set[ast.types.Set]()
    var foundQuantifiedPermissions = false

    program visit {
      case q: ast.Quantified if !foundQuantifiedPermissions && !q.isPure =>
        //        println(s"  q = $q")
        /* Axioms generated for quantified permissions depend on sets */
        foundQuantifiedPermissions = true
        program.fields foreach {f => setTypes += ast.types.Set(f.typ)}
        setTypes += ast.types.Set(ast.types.Ref) /* $FVF.domain_f is ref-typed */

      case t: silver.ast.Typed =>
        t.typ :: silver.ast.utility.Types.typeConstituents(t.typ) foreach {
          case s: ast.types.Set =>
            setTypes += s
          case s: ast.types.Multiset =>
            /* Multisets depend on sets */
            setTypes += ast.types.Set(s.elementType)
          case s: ast.types.Seq =>
            /* Sequences depend on multisets, which in turn depend on sets */
            setTypes += ast.types.Set(s.elementType)
          case _ =>
          /* Ignore other types */
        }
    }

    collectedSorts = setTypes map (st => symbolConverter.toSort(st).asInstanceOf[terms.sorts.Set])
  }

  def declareSorts() {
    collectedSorts foreach (s => prover.declare(terms.SortDecl(s)))
  }

  def declareSymbols() {
    collectedSorts foreach {s =>
      val substitutions = Map("$S$" -> prover.termConverter.convert(s.elementsSort))
      prover.logComment(s"/sets_declarations_dafny.smt2 [${s.elementsSort}]")
      preambleFileEmitter.emitParametricAssertions("/sets_declarations_dafny.smt2", substitutions)
    }
  }

  def emitAxioms() {
    collectedSorts foreach {s =>
      val substitutions = Map("$S$" -> prover.termConverter.convert(s.elementsSort))
      prover.logComment(s"/sets_axioms_dafny.smt2 [${s.elementsSort}]")
      preambleFileEmitter.emitParametricAssertions("/sets_axioms_dafny.smt2", substitutions)
    }
  }
}
