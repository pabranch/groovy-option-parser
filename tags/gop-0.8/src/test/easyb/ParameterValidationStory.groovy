import org.computoring.gop.Parser

scenario "validator is applied to parameter", {
  given "a new parser", { parser = new Parser() }
  and "a required option 'f' having a validator that reverses the value", {
    parser.required( 'f', [validate: { it.reverse() }] )
  }
  when "parsing '-f foo'", { params = parser.parse( '-f foo'.split() )}
  then "parameter f should be 'oof'", {
    params.f.shouldBe( 'oof' )
  }
}

scenario "converted parameter is returned", {
  given "a new parser", { parser = new Parser() }
  and "an optional option 'i' having a validator that converts the value to an int", {
    parser.optional( 'i', [validate: { Integer.parseInt( it ) }] )
  }
  when "parsing '-i 10'", { params = parser.parse( '-i 10'.split() )}
  then "parameter i should by of type Integer", { params.i.shouldBeAn( Integer ) }
  and "parameter i should be 10", { params.i.shouldBe( 10 ) }
}

