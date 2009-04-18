import org.computoring.gop.Parser

scenario "validate value is a closure", {
  given "a Parser", {
    parser = new Parser()
  }

  when "an option with a non Closure validate option is set", {
    action = {
      parser.optional( 'f', [validate: 'bar'] )
    }
  }

  then "an exception should be thrown", {
    ensureThrows( Exception.class ) {
      action()
    }
  }
}

scenario "unchecked exceptions during validation should be rethrown as a Exception", {
  given "a Parser", {
    parser = new Parser()
  }
  and "a required option with validation that throws a RuntimeException", {
    parser.required( 'x', [validate: { throw new IllegalArgumentException( "test message" )}])
  }

  when "parsing '-x foo'", {
    action = { parser.parse( "-x foo".split() )}
  }

  then "a Exception should be thrown", {
    ensureThrows( Exception.class ) {
      action()
    }
  }
  and "params.x.error should be IllegalArgumentException", {
    parser.options.x.error.shouldBeAn( IllegalArgumentException )
  }
}

