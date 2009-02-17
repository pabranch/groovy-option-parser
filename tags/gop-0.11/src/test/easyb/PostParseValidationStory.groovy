import org.computoring.gop.Parser

scenario "post parsing validation", {
  given "a Parser", {
    parser = new Parser()
  }
  and "a required option", {
    parser.required("r")
  }
  and "a post parse validator that exports the parsed parameters", {
    parameters = null
    parser.validate { params -> parameters = params }
  }

  when "parsing '-r'", { parser.parse('-r bar'.split()) }

  then "exported parameters should contain 'r' = 'bar'", {
    ensure(parameters) {
      contains(r:'bar')
    }
  }
}

scenario "post parsing validation failure", {
  given "a Parser", {
    parser = new Parser()
  }
  and "with a post parse validator that throws an exception", {
    parser.validate { throw new Exception("foo exception") }
  }

  then "parsing should throw and exception", {
    ensureThrows( Exception.class ) {
      parser.parse()
    }
  }
  and "usage should include the validation exception", {
    (parser.usage =~ ~/(?ms)foo exception/).count.shouldBe( 1 )
  }
}

