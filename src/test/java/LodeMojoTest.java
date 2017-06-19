import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.junit.Assert.*;

public class LodeMojoTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testLodeMojo() {
		LodeGeneratorMojo mojo = new LodeGeneratorMojo();

		try {
			String path = folder.newFolder( "temp" ).getAbsolutePath();
			String fileName = "prov-o.html";

			mojo.setOntologyURLs( Arrays.asList("prov-o.rdf" ) );
			mojo.setTargetDirectory( path );
			mojo.setTargetFileNames( Arrays.asList( fileName ) );

			mojo.execute();

			File f = new File( path + File.separator + fileName );
			assertTrue( f.exists() );

			Document html = Jsoup.parse( f, Charset.defaultCharset().name() );

			String title = html.title();
			assertTrue( title.contains( "PROV-O" ) );

		} catch ( Exception e ) {
			e.printStackTrace();
			fail( e.getMessage() );
		}
	}


	@Test
	public void testLodeMojoWithSubPath() {
		LodeGeneratorMojo mojo = new LodeGeneratorMojo();

		try {
			String path = folder.newFolder( "temp" ).getAbsolutePath();
			String fileName = "prov-o.html";

			mojo.setOntologyURLs( Arrays.asList("prov-o.rdf" ) );
			mojo.setTargetDirectory( path );
			mojo.setTargetFileNames( Arrays.asList( "foo" + File.separator + fileName ) );

			mojo.execute();

			File f = new File( path + File.separator + "foo" + File.separator + fileName );
			assertTrue( f.exists() );

			Document html = Jsoup.parse( f, Charset.defaultCharset().name() );

			String title = html.title();
			assertTrue( title.contains( "PROV-O" ) );

		} catch ( Exception e ) {
			e.printStackTrace();
			fail( e.getMessage() );
		}
	}

	@Test
	public void testLodeMojoWithNestedFolders() {
		LodeGeneratorMojo mojo = new LodeGeneratorMojo();

		try {
			String path = folder.newFolder( "temp" ).getAbsolutePath();

			mojo.setOntologyURLs( Arrays.asList("test" ) );
			mojo.setTargetDirectory( path );

			mojo.execute();

			File f = new File( path + File.separator + "test" + File.separator + "test1.html" );
			assertTrue( f.exists() );

			Document html = Jsoup.parse( f, Charset.defaultCharset().name() );

			String title = html.title();
			assertTrue( title.contains( "Test1" ) );

			File f2 = new File( path + File.separator + "test" + File.separator + "subDir" + File.separator + "test2.html" );
			assertTrue( f2.exists() );

			Document html2 = Jsoup.parse( f2, Charset.defaultCharset().name() );

			String title2 = html2.title();
			assertTrue( title2.contains( "Test2" ) );


		} catch ( Exception e ) {
			e.printStackTrace();
			fail( e.getMessage() );
		}
	}


	@Test
	public void testLodeMojoWithNestedFoldersInJar() {
		LodeGeneratorMojo mojo = new LodeGeneratorMojo();

		try {
			String path = folder.newFolder( "temp" ).getAbsolutePath();

			URL jar = Thread.currentThread().getContextClassLoader().getResource( "mock.jar" );
			String res = "jar:" + jar.toString() + "!" + "/" + "test2" + "/";
			String cat = "jar:" + jar.toString() + "!" + "/" + "test2" + "/" + "catalog-v001.xml";

			mojo.setOntologyURLs( Arrays.asList( res ) );
			mojo.setUseOWLAPI( true );
			mojo.setCatalog( cat );
			mojo.setTargetDirectory( path );

			mojo.execute();

			File f = new File( path + File.separator + "test2" + File.separator + "test3.html" );
			assertTrue( f.exists() );

			Document html = Jsoup.parse( f, Charset.defaultCharset().name() );

			String title = html.title();
			assertTrue( title.contains( "Test1" ) );

			File f2 = new File( path + File.separator + "test2" + File.separator + "subDir2" + File.separator + "test4.html" );
			assertTrue( f2.exists() );

			Document html2 = Jsoup.parse( f2, Charset.defaultCharset().name() );

			String title2 = html2.title();
			assertTrue( title2.contains( "Test2" ) );


		} catch ( Exception e ) {
			e.printStackTrace();
			fail( e.getMessage() );
		}
	}

	@Test
	public void testLodeMojoWithCatalog() {
		LodeGeneratorMojo mojo = new LodeGeneratorMojo();

		try {
			String path = folder.newFolder( "temp" ).getAbsolutePath();

			URL sources = Thread.currentThread().getContextClassLoader().getResource( "test3" );

			assertNotNull( sources );

			mojo.setOntologyURLs( Arrays.asList( sources.toString() ) );
			mojo.setCatalog( "test3/catalog-v001.xml" );
			mojo.setConsiderImportedClosure( true );
			mojo.setTargetDirectory( path );

			mojo.execute();

			File f = new File( path + File.separator + "test3" + File.separator + "root.html" );
			assertTrue( f.exists() );

			Document html = Jsoup.parse( f, Charset.defaultCharset().name() );

			String title = html.title();
			assertTrue( title.contains( "Test Root" ) );

			File f2 = new File( path + File.separator + "test3" + File.separator + "remote.html" );
			assertTrue( f2.exists() );

			Document html2 = Jsoup.parse( f2, Charset.defaultCharset().name() );

			String title2 = html2.title();
			assertTrue( title2.contains( "Test Remote" ) );


		} catch ( Exception e ) {
			e.printStackTrace();
			fail( e.getMessage() );
		}
	}


	@Test
	public void testCustomizeStyleSheet() {
		LodeGeneratorMojo mojo = new LodeGeneratorMojo();

		try {
			String path = folder.newFolder( "temp" ).getAbsolutePath();


			String fileName = "test5.html";

			mojo.setOntologyURLs( Arrays.asList("test5/test5.rdf" ) );
			mojo.setTargetDirectory( path );
			mojo.setXsltLocation( "test5/custom.xsl" );
			mojo.setTargetFileNames( Arrays.asList( fileName ) );

			mojo.execute();

			File f = new File( path + File.separator + fileName );
			assertTrue( f.exists() );

			Document html = Jsoup.parse( f, Charset.defaultCharset().name() );
			//System.out.print( html.toString() );

			assertTrue( html.toString().contains( "Definition of A" ) );
			assertTrue( html.toString().contains( "Example _a" ) );
			assertTrue( html.toString().contains( "This is what A means" ) );

		} catch ( Exception e ) {
			e.printStackTrace();
			fail( e.getMessage() );
		}
	}

}
