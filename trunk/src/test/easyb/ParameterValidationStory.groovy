import org.computoring.gop.Parser
import org.computoring.gop.GOPException

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

scenario "validating integers are positive", {
  given "a new parser", { parser = new Parser() }
  and "a required option 'i' having a validator that rejects negative numbers", {
    parser.required( 'i', [
      validate: {
        def i = new Integer(it)
        if( i < 0 ) throw new IllegalArgumentException("supplied paramter $it is negative")
        i
      }
    ])
  }
  when "parsing '-i -1'", { action = { parser.parse("-i -1".split()) }}
  then "An GOPException should be thrown", {
    ensureThrows( GOPException ) { action() }
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

scenario "default values should be validated", {
  given "a new parser", { parser = new Parser() }
  and "an option with a default value and a validator", {
    parser.optional( 'i', [default: "123", validate: { Integer.parseInt( it ) }] )
  }

  when "parsing ''", { params = parser.parse( ''.split() )}

  then "parameter i should by of type Integer", { params.i.shouldBeAn( Integer ) }
}
