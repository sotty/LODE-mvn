import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.net.MalformedURLException;
import java.net.URL;

public class ClassPathURIResolver implements URIResolver {
	@Override
	public Source resolve( String href, String base ) throws TransformerException {
		try {
			URL url = new URL( href );
			return new StreamSource( url.toString() );
		} catch ( MalformedURLException e ) {
			return new StreamSource( Thread.currentThread().getContextClassLoader().getResourceAsStream( href ) );
		}
	}
}
