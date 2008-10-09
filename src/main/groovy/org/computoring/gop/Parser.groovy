package org.computoring.gop

/**
 * Groovy Option Parser
 * (not affiliated with the Grand Old Party)
 * 
 * GOP is inspired by the excellent ruby option parser clip (http://github.com/alexvollmer/clip).  Clip favors
 * easily creating options and easily using the parsed values.
 *
 * An example:
 *  def parser = new org.computoring.gop.Parser(description: "An example parser.")
 *  parser.required('f', 'foo-bar', [description: 'The foo-bar option'])
 *  parser.optional('b', [longName: 'bar-baz', default: 'xyz', description: 'The optional bar-baz option with a default of "xyz"'])
 *  parser.flag('c')
 *  parser.flag('d', 'debug', [default: true])
 *  parser.required('i', 'count', [description: 'A required, validated option', validate: {
 *    Integer.parseInt(it)
 *  }])
 *                                                                                                                                  
 *  def params = parser.parse("-f foo_value --debug --count 123 -- some other stuff".split())
 *  assert params.'foo-bar' == 'foo_value'
 *  assert params.b == 'xyz'
 *  assert params.c == false
 *  assert params.debug == true
 *  assert params.count instanceof Integer
 *  assert params.i == 123
 *  assert parser.remainder.join(' ') == 'some other stuff'
 *                                                                                                                                   
 *  System.out << parser.usage() // will spit out a usage statement like this:
 *
 *  An example parser.
 *
 *  Required
 *  --------
 *  -f, --foo-bar           The foo-bar option
 *  -i, --count             A required, validated option
 *
 *  Optional
 *  --------
 *  -b, --bar-baz  [xyz]    The optional bar-baz option with a default of "xyz"
 *
 *  Flags
 *  -----
 *  -c             [false]  
 *  -d, --debug    [true]   
 *
 */
public class Parser {

  def description
  def options = [:]
  def parameters = [:]
  def remainder = []

  def required( shortName, longName, Map opts = [:]) { required( shortName, [longName: longName] + opts) }
  def required( shortName, Map opts = [:]) {
    if( opts.default ) {
      throw new IllegalArgumentException( "Default values don't make sense for required options" )
    }
    addOption( shortName, 'required', opts )
  }

  def optional( shortName, longName, Map opts = [:] ) { optional( shortName, [longName: longName] + opts ) }
  def optional( shortName, Map opts = [:] ) {
    addOption( shortName, 'optional', opts )
  }

  def flag( shortName, longName, Map opts = [:] ) { flag( shortName, [longName: longName] + opts ) }
  def flag( shortName, Map opts = [:] ) {
    opts.default = ( opts.default ) ? true : false
    addOption( shortName, 'flag', opts )
  }

  def parse( args ) {
    // add defaults
    parameters = options.inject( [:] ) { map, entry ->
      if( entry.value.default != null ) map[entry.key] = entry.value.default
      map
    }

    def option = null
    args.each { arg ->
      // options can't look like -foo
      if( arg =~ ~/^-[^-].+/ ) {
        throw new IllegalArgumentException( "Illegal option [$arg], short options must be a single character" )
      }

      if( arg =~ ~/^(-[^-]|--.+)$/ ) {
        if( option ) {
          throw new IllegalArgumentException( "Illegal value [$arg] supplied for option ${option.shortName}" )
        }

        def name = arg.replaceFirst( /--?/, '' )
        if( !options.containsKey( name )) {
          throw new Exception( "unknown option $arg" )
        }

        option = options[name]
        if( option.type == 'flag' ) {
          addParameter(option, true)
          option = null
        }
      }
      else if( option ) {
        addParameter(option, arg)
        option = null
      }
      else {
        if( !( arg == '--' )) remainder << arg
      }
    }

    def missing = ( requiredOptions.keySet() - parameters.keySet() )
    if( missing ) throw new Exception( "Missing required parameters: ${missing.collect { "-$it" }}" )

    return parameters
  }

  def usage( errorMsg = null ) {
    def buffer = new StringWriter()
    def writer = new PrintWriter( buffer )

    if( errorMsg ) {
      writer.println( "Error: $errorMsg" )
      writer.println()
    }

    if( description ) {
      writer.println( description )
      writer.println()
    }

    def longestName = 5 + options.inject( 0 ) { max, entry -> 
      entry.value.longName ? Math.max( max, entry.value.longName.size() ) : max
    }
    def longestDefault = 5 + options.inject( 0 ) { max, entry -> 
      def x = entry.value.default
      (x && x.metaClass.respondsTo(x, "size")) ? Math.max( max, x.size() ) : max
    }

    def pattern = "%s%-${longestName}s %-${longestDefault}s %s"
    ['Required': requiredOptions, 'Optional': optionalOptions, 'Flags': flagOptions].each { header, map ->
      if( map ) {
        writer.println( header )
        writer.println( "-"*header.size() )
        map.each { name, opts ->
          def shortName = "-$opts.shortName"
          def longName = opts.longName ? ", --$opts.longName" : ""
          def defaultValue = (opts.default || opts.type == 'flag') ? "[${opts.default.toString()}]" : ""
          def description = opts.description ?: ""
          writer.printf( pattern, shortName, longName, defaultValue, description)
          writer.println()
        }
      }

      writer.println()
    }

    return buffer.toString()
  }

  private def addParameter(option, value) {
    value = (option.validate) ? option.validate( value ) : value
    parameters[option.shortName] = value
    if( option.longName ) parameters[option.longName] = value
  }

  private def getRequiredOptions() {
    findOptions( 'required' )
  }

  private def getOptionalOptions() {
    findOptions( 'optional' )
  }

  private def getFlagOptions() {
    findOptions( 'flag' )
  }

  private def findOptions( type ) {
    options.inject( [:] ) { map, entry ->
      if( entry.value.type == type ) map[entry.value.shortName] = entry.value
      map
    }
  }

  private def addOption( shortName, type, opts ) {
    opts = opts ?: [:]

    if( !shortName ) {
      throw new IllegalArgumentException( "Option name cannot not be null" )
    }

    if( options[shortName] ) {
      throw new IllegalArgumentException( "Dup option specified: $shortName" )
    }

    if( shortName.size() != 1 ) {
      throw new IllegalArgumentException( "Invalid option name: $shortName.  Option names must be a single character.  To set a long name for this option add [longName: 'long-name']" )
    }

    if( opts.validate && !(opts.validate instanceof Closure) ) {
      throw new IllegalArgumentException( "Invalid validate option, must be a Closure" )
    }

    opts.type = type ?: 'optional'
    opts.shortName = shortName
    options[shortName] = opts
    if( opts.longName ) options[opts.longName] = opts
  }

  private setOptions( arg ) {}
}

