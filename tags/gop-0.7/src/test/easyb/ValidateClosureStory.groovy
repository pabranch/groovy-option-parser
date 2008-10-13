import org.computoring.gop.Parser
import org.computoring.gop.GOPException

scenario "validate value is not a closure", {
  given "a Parser", {
    parser = new Parser()
  }

  when "an option with a non Closure validate option is set", {
    action = {
      parser.optional( 'f', [validate: 'bar'] )
      println parser.options
    }
  }

  then "an exception should be thrown", {
    ensureThrows( GOPException.class ) {
      action()
    }
  }
}

