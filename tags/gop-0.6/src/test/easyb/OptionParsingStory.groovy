import org.computoring.gop.Parser

description "Scenarios to validate the parsing of options"

scenario "invalid option parsing", {
  given "a new parser", { parser = new Parser() }
  and "an optional options 'f' & 'bar'", {
    parser.optional( 'f' )
    parser.optional( 'b', 'bar' )
  }
  when "parsing '-f --bar'", { action = { parser.parse( '-f --bar'.split() )}}
  then "an exception should be thrown because option 'f' is not a flag", {
    ensureThrows( IllegalArgumentException.class ) { action () }
  }
}

scenario "remainder set after parsing options", {
  given "a new parser", { parser = new Parser() }
  and "an optional option 'f'", { parser.optional( 'f' ) }
  when "parsing '-f foo bar baz'", { parser.parse( '-f foo bar baz'.split()) }
  then "remainder should be ['bar','baz']", {
    parser.remainder.shouldBe( ['bar','baz'] )
  }
}

scenario "remainder set after encountering --", {
  given "a new parser", { parser = new Parser() }
  and "an optional option 'f'", { parser.optional( 'f' ) }
  when "parsing '-- bar baz'", { parser.parse( '-- bar baz'.split()) }
  then "remainder should be ['bar','baz']", {
    parser.remainder.shouldBe( ['bar','baz'] )
  }
}

scenario "parsing set parameters for short and long names", {
  given "a new parser", { parser = new Parser() }
  and "an option 'f' with long name 'foo'", { parser.optional( 'f', 'foo' ) }
  when "parsing '-f bar'", { params = parser.parse( '-f bar'.split() )}
  then "both 'f' & 'foo' parameters should be created", {
    ensure( params ) {
      contains( 'f' )
      contains( 'foo' )
    }
  }
  and "parameter f should have a value of 'bar'", { params.f.shouldEqual( 'bar' ) }
  and "parameter foo should equal parameter f", {
    params.f.shouldEqual( params.foo )
  }
}

scenario "default returned when option not specified", {
  given "a new parser", { parser = new Parser() }
  and "an optional option 'f' with a default value 'bar'", {
    parser.optional( 'f', [default: 'bar'] )
  }
  when "parsing ''", { params = parser.parse( [] ) }
  then "default value 'bar' should be returned in parameter 'f'", {
    params.f.shouldBe( 'bar' )
  }
}

scenario "pasing optional set parameter to supplied value", {
  given "a new parser", { parser = new Parser() }
  and "an optional option 'f'", { parser.optional( 'f' ) }
  when "parsing '-f bar'", { params = parser.parse( "-f bar".split() )}
  then "value 'bar' should be returned in parameter 'f'", {
    params.f.shouldBe( 'bar' )
  }
}

scenario "missing required parameter", {
  given "a new parser", { parser = new Parser() }
  and "a required option 'f'", { parser.required( 'f' ) }
  when "parsing ''", { action = { parser.parse( [] ) }}
  then "an exception should be thrown", {
    ensureThrows( Exception.class ) { action() }
  }
}

scenario "missing flag sets parameter to false", {
  given "a new parser", { parser = new Parser() }
  and "a flag option 'f'", { parser.flag( 'f' ) }
  when "parsing ''", { params = parser.parse( [] ) }
  then "parameter 'f' should be false", {
    params.f.shouldBe( false )
  }
}

scenario "parsing supplied flag set parameter to true", {
  given "a new parser", { parser = new Parser() }
  and "a flag option 'f'", { parser.flag( 'f' ) }
  when "parsing '-f'", { params = parser.parse( "-f".split() ) }
  then "parameter 'f' should be true", {
    params.f.shouldBe( true )
  }
}

scenario "validating a flag parameter", {
  given "a new parser", { parser = new Parser() }
  and "a flag option 'f' with a validation closure that returns a string", {
    parser.flag( 'f', [validate: { "abc" }] )
  }
  when "parsing '-f'", { params = parser.parse( "-f".split() ) }
  then "parameters 'f' should be true", {
    params.f.shouldBe( true )
  }
}

