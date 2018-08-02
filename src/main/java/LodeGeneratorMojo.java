import it.essepuntato.lode.LodeTransformer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @goal lode-gen
 *
 * @configurator include-project-dependencies
 */
public class LodeGeneratorMojo extends AbstractMojo {

	/**
	 * Pointer to the ontology document(s) for which to generate documentation.
	 * Can be a URL, or path to a file.
	 *
	 * @parameter 	property="ontologyURLs"
	 *
	 * @required
	 */
	private List<String> ontologyURLs;

	/**
	 * Pointer to the ontology catalog.
	 *
	 * @parameter 	property="catalog"
	 *
	 */
	private String catalog;

	/**
	 * URL of the transformation XSLT
	 * @parameter 	property="xsltLocation"
	 *
	 * @optional
	 */
	private String xsltLocation;

	/**
	 * URL of the CSS
	 * @parameter 	property="cssLocation"
	 *              default-value="http://eelst.cs.unibo.it/apps/LODE/"
	 *
	 * @optional
	 */
	private String cssLocation = "http://eelst.cs.unibo.it/apps/LODE/";

	/**
	 * Documentation Language
	 * @parameter 	property="language"
	 *              default-value="en"
	 */
	private String language = "en";

	/**
	 * Hint: use OWL API for parsing
	 * @parameter 	property="useOWLAPI"
	 *              default-value="false"
	 */
	private boolean useOWLAPI = false;

	/**
	 * Hint: use Imported Ontologies
	 * @parameter 	property="considerImportedOntologies"
	 *              default-value="false"
	 */
	private boolean considerImportedOntologies = false;

	/**
	 * Hint: use Transitive Closure of the Imported Ontologies
	 * @parameter 	property="considerImportedClosure"
	 *              default-value="false"
	 */
	private boolean considerImportedClosure = false;

	/**
	 * Hint: run an OWL reasoner to generate docs for the inferred ontology
	 * @parameter 	property="useReasoner"
	 *              default-value="false"
	 */
	private boolean useReasoner = false;

	/**
	 * Merge all axioms from imported ontologies (true),
	 * rather than simply the annotation assertions (false)
	 * @parameter 	property="deepImport"
	 *              default-value="true"
	 */
	private boolean deepImport = true;

	/**
	 * Directory to save the LODE docs
	 *
	 * @parameter 	property="targetDirectory"
	 *
	 * @required
	 */
	private String targetDirectory = null;

	/**
	 * File(s) to save the LODE docs, one for each source ontology
	 *
	 * @parameter 	property="targetFileNames"
	 *
	 * @required
	 */
	private List<String> targetFileNames = null;


	public List<String> getOntologyURLs() {
		return Collections.unmodifiableList( ontologyURLs );
	}

	public void setOntologyURLs( List<String> ontologyURLs ) {
		this.ontologyURLs = new ArrayList<>( ontologyURLs );
	}

	public String getCatalog() {
		return catalog;
	}

	public void setCatalog( String catalog ) {
		this.catalog = catalog;
	}

	public String getXsltLocation() {
		return xsltLocation;
	}

	public void setXsltLocation( String xsltLocation ) {
		this.xsltLocation = xsltLocation;
	}

	public String getCssLocation() {
		return cssLocation;
	}

	public void setCssLocation( String cssLocation ) {
		this.cssLocation = cssLocation;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage( String language ) {
		this.language = language;
	}

	public boolean isUseOWLAPI() {
		return useOWLAPI;
	}

	public void setUseOWLAPI( boolean useOWLAPI ) {
		this.useOWLAPI = useOWLAPI;
	}

	public boolean isConsiderImportedOntologies() {
		return considerImportedOntologies;
	}

	public void setConsiderImportedOntologies( boolean considerImportedOntologies ) {
		this.considerImportedOntologies = considerImportedOntologies;
	}

	public boolean isConsiderImportedClosure() {
		return considerImportedClosure;
	}

	public void setConsiderImportedClosure( boolean considerImportedClosure ) {
		this.considerImportedClosure = considerImportedClosure;
	}

	public boolean isUseReasoner() {
		return useReasoner;
	}

	public void setUseReasoner( boolean useReasoner ) {
		this.useReasoner = useReasoner;
	}

	public boolean isDeepImport() {
		return deepImport;
	}

	public void setDeepImport( boolean deepImport ) {
		this.deepImport = deepImport;
	}

	public String getTargetDirectory() {
		return targetDirectory;
	}

	public void setTargetDirectory( String targetDirectory ) {
		this.targetDirectory = targetDirectory;
	}

	public List<String> getTargetFileNames() {
		return Collections.unmodifiableList( targetFileNames );
	}

	public void setTargetFileNames( List<String> targetFileName ) {
		this.targetFileNames = new ArrayList<>( targetFileName );
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {

			String xslt = null;
			if ( xsltLocation != null ) {
				Optional<URL> xsltUrl = getUrl( xsltLocation );
				xslt = xsltUrl.isPresent() ? xsltUrl.get().toString() : null;
			}
			if ( xslt == null ) {
				xslt = LodeTransformer.resolveDefaultXLS();
			}

			String css = null;
			if ( cssLocation != null ) {
				Optional<URL> cssUrl = getUrl( cssLocation );
				css = cssUrl.isPresent() ? cssUrl.get().toString() : null;
			}
			if ( css == null ) {
				css = LodeTransformer.resolveDefaultCSS();
			}

			LodeTransformer transformer = new LodeTransformer( xslt, css, Optional.of( new ClassPathURIResolver() ) );

			File dir = new File( targetDirectory );
			if ( ! dir.exists() ) {
				dir.mkdirs();
			}

			ontologyURLs.forEach( ( String ontology ) -> {
				Optional<URL> ontologyURL = getUrl( ontology );

				if ( ontologyURL.isPresent() ) {
					try {
						processResource( ontologyURL.get(), ontology, transformer, "" );
					} catch ( Exception e ) {
						e.printStackTrace();
					}
				}
			} );

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processResource( URL ontologyURL, String resourceName, LodeTransformer transformer, String path ) throws URISyntaxException {
		if ( isResourceFolder( ontologyURL ) ) {
			switch ( ontologyURL.getProtocol() ) {
				case "jar":
					processJarDir( ontologyURL, transformer );
					break;
				case "file":
				default:
					processDir( ontologyURL, transformer, path );
			}
		} else {
			process( ontologyURL,
			         getTargetFilePath( resourceName, path ),
			         transformer );
		}
	}

	private String getTargetFilePath( String resource, String path ) {
		StringBuilder filePath = new StringBuilder();
		if ( targetDirectory != null ) {
			filePath.append( targetDirectory );
		}
		if ( ! filePath.toString().endsWith( File.separator ) && ! path.startsWith( File.separator ) ) {
			filePath.append( File.separator );
		}

		filePath.append( path );
		if ( ! filePath.toString().endsWith( File.separator ) ) {
			filePath.append( File.separator );
		}

		if ( targetFileNames != null && ontologyURLs.indexOf( resource ) >= 0 && ! targetFileNames.isEmpty() ) {
			filePath.append( targetFileNames.get( ontologyURLs.indexOf( resource ) ) );
		} else {
			resource = replaceExtension( resource );
			filePath.append( resource.substring( resource.lastIndexOf( File.separator ) + 1 ) );
		}

		return filePath.toString();
	}

	private String replaceExtension( String resource ) {
		return resource.substring( 0, resource.lastIndexOf( "." ) ) + ".html";
	}

	private void processJarDir( URL url, LodeTransformer transformer ) {
		try {
			String inner = url.getFile();
			URL jarUrl = new URL( inner.substring( 0, inner.lastIndexOf( "!" ) ) );
			ZipFile zip = new ZipFile( jarUrl.getFile() );

			String resourceName = inner.substring( inner.lastIndexOf( "!" ) + 2 );

			Enumeration<? extends ZipEntry> x = zip.entries();
			while ( x.hasMoreElements() ) {
				ZipEntry entry = x.nextElement();
				String entryName = entry.getName();
				if ( entryName.startsWith( resourceName ) && ! entryName.equals( resourceName ) ) {

					int idx = entryName.lastIndexOf( '/' );
					String innerPath = entryName.substring( 0, idx + 1 );
					String innerName = entryName.substring( idx + 1 );
					if ( isOntologyFileName( innerName ) ) {
						String filePath = getTargetFilePath( innerName, innerPath );
						process( new URL( "jar:" + jarUrl + "!" + '/' + entryName  ), filePath, transformer );
					}
				}
			}

		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	private void processDir( URL url, LodeTransformer transformer, final String path ) {
		try {
			final File container = new File( url.toURI() );

			StringBuilder recurPath = new StringBuilder( path );
			recurPath.append( File.separator ).append( container.getName() );

			Arrays.stream( container.listFiles() )
			      .filter( (File f) -> isOntologyFile( f ) )
			      .forEach( ( File f ) -> {
				      try {
					      processResource( f.toURI().toURL(), f.getName(), transformer, recurPath.toString() );
				      } catch ( MalformedURLException e ) {
					      e.printStackTrace();
				      } catch ( URISyntaxException e ) {
					      e.printStackTrace();
				      }
			      } );
		} catch ( URISyntaxException e ) {
			e.printStackTrace();
		}
	}

	private boolean isResourceFolder( URL url ) throws URISyntaxException {
		String protocol = url.getProtocol();
		switch ( protocol ) {
			case "file":
				return new File( url.toURI() ).isDirectory();
			case "jar":
				try {
					String inner = url.getFile();
					URL jarUrl = new URL( inner.substring( 0, inner.lastIndexOf( "!" ) ) );
					ZipFile zip = new ZipFile( jarUrl.getFile() );

					String resourceName = inner.substring( inner.lastIndexOf( "!" ) + 2 );
					ZipEntry entry = zip.getEntry( resourceName );

					return entry != null && entry.isDirectory();
				} catch ( Exception e ) {
					e.printStackTrace();
					return false;
				}
			default:
				return false;
		}
	}

	private boolean isOntologyFile( File f ) {
		return f.isDirectory()
				|| isOntologyFileName( f.getName() );
	}


	private boolean isOntologyFileName( String f ) {
		return f.endsWith( ".owl" )
				|| f.endsWith( ".ttl" )
				|| f.endsWith( ".ofn" )
				|| f.endsWith( ".rdf" )
				|| f.endsWith( ".rdfs" );
	}

	private void process( URL ontologyURL, String targetFilePath, LodeTransformer transformer ) {
		try {
			String docs = transformer.transform( ontologyURL,
			                                     getUrl( catalog ),
			                                     language,
			                                     useOWLAPI,
			                                     considerImportedOntologies,
			                                     considerImportedClosure,
			                                     useReasoner,
			                                     deepImport );

			File f = new File( targetFilePath );
			File parentDir = f.getParentFile();
			if ( ! parentDir.exists() ) {
				parentDir.mkdirs();
			}
			new PrintStream( new FileOutputStream( f ) ).print( docs );
		} catch ( Exception e ) {
			e.printStackTrace();
		}

	}

	private Optional<URL> getUrl( String urlString ) {
		URL ontologyURL = null;
		if ( urlString == null ) {
			return Optional.empty();
		}
		try {
			try {
				return Optional.of( new URL( urlString ) );
			} catch ( MalformedURLException me ) {
			}
			ontologyURL = Thread.currentThread().getContextClassLoader().getResource( urlString );

			if ( ontologyURL == null ) {
				File f = new File( urlString );
				if ( f.exists() ) {
					ontologyURL = f.toURI().toURL();
				}
			}

			return Optional.ofNullable( ontologyURL );
		} catch ( Exception e ) {
			e.printStackTrace();
			return Optional.empty();
		}
	}
}
