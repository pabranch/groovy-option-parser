import org.computoring.gop.Parser
import org.computoring.gop.GOPException

scenario "usage should not list missing required options if parse hasn't been called", {
  given "a Parser", {
    parser = new Parser()
  }
  and "a required option 'f'", {
    parser.required( 'f', [description: 'required option f'] )
  }

  then "usage before pasing should not include 'Missing required parameters' section", {
    (parser.usage =~ 'Missing required parameters' ).count.shouldBe( 0 )
  }
}

scenario "usage should list required options", {
  given "a Parser", {
    parser = new Parser()
  }
  and "a required option 'f'", {
    description = 'required option f'
    parser.required( 'f', [description: description] )
  }

  then "usage statement should include f as a required option", {
    (parser.usage =~ ~/(?ms)^Required.*^  -f.*${description}/ ).count.shouldBe( 1 )
  }
}

scenario "usage should list optional options", {
  given "a Parser", {
    parser = new Parser()
  }
  and "an optional option 'f'", {
    description = 'optional option f'
    parser.optional( 'f', [description: description] )
  }

  then "usage statement should include f as an optional option", {
    (parser.usage =~ ~/(?ms)^Optional.*^  -f.*${description}/ ).count.shouldBe( 1 )
  }
}

scenario "usage should report missing required options", {
  given "a Parser", {
    parser = new Parser()
  }
  and "a required option 'f'", {
    description = 'required option f'
    parser.required( 'f', [description: description] )
  }

  when "parsing ''", {
    ensureThrows( GOPException ) {
      parser.parse( '' )
    }
  }

  then "usage should report a missing required option", {
    (parser.usage =~ ~/(?ms)^Missing required.*^  -f ${description}/ ).count.shouldBe( 1 )
  }
}

scenario "usage should report validation errors", {
  given "a Parser", {
    parser = new Parser()
  }
  and "an optional option 'f' that throws an Exception", {
    parser.optional( 'f', [validate: { throw new Exception( "error message" ) }])
  }

  when "parsing '-f foo'", {
    ensureThrows( GOPException ) {
      parser.parse( '-f foo'.split() )
    }
  }

  then "usage should report a missing required option", {
    (parser.usage =~ ~/(?ms)^Validation errors.*^  -f : .*error message/ ).count.shouldBe( 1 )
  }
}

