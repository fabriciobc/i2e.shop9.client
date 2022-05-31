package br.com.i2e.shop9.client;



import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.PostConstruct;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import br.com.i2e.shop9.model.AuthenticationToken;
import br.com.i2e.shop9.model.AuxiliaryRegistry;
import br.com.i2e.shop9.model.FotosProduto;
import br.com.i2e.shop9.model.ProdutoS9;
import br.com.i2e.shop9.model.Shop9AuthMessage;
import br.com.i2e.shop9.model.Shop9AuxMessage;
import br.com.i2e.shop9.model.Shop9ClienteMessage;
import br.com.i2e.shop9.model.Shop9FotosProdutoMessage;
import br.com.i2e.shop9.model.Shop9Message;
import br.com.i2e.shop9.model.Shop9ProdutoMessage;
import reactor.core.publisher.Mono;

public class Shop9Client_v1 {
	
	private static int EXPIRE_OFFSET = 10;	
	@Autowired
	private Environment env;
	@Autowired
	private SignatureUtil signatureUtil;
	
	private WebClient webClient;
	
	private Optional<AuthenticationToken> token = Optional.ofNullable( null );
	
	 @PostConstruct
	private void init() {
		 SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
		 HttpClient httpClient = new HttpClient(sslContextFactory) {
			
		     @Override
		     public Request newRequest(URI uri) {
		         Request request = super.newRequest(uri);
		         return enhance(request);
		     }
		     
		     Request enhance(Request request) {
		    	    StringBuilder group = new StringBuilder();
		    	    request.onRequestBegin(theRequest -> {
		    	        // append request url and method to group
		    	    });
		    	    request.onRequestHeaders(theRequest -> {
//		    	        for (HttpField header : theRequest.getHeaders()) {
//		    	            // append request headers to group
//		    	        }
		    	    });
		    	    request.onRequestContent((theRequest, content) -> {
//		    	        System.out.println( "Body: " + content.array() );
		    	    });
		    	    request.onRequestSuccess(theRequest -> {
		    	        
		    	        group.delete(0, group.length());
		    	    });
		    	    group.append("\n");
		    	    request.onResponseBegin(theResponse -> {
		    	        // append response status to group
		    	    });
		    	    request.onResponseHeaders(theResponse -> {
//		    	        for (HttpField header : theResponse.getHeaders()) {
//		    	            System.out.println( header.getValue() );
//		    	        }
		    	    });
		    	    request.onResponseContent((theResponse, content) -> {
		    	        System.out.println( "Response: " + StandardCharsets.UTF_8.decode(content).toString() );
		    	    });
		    	    request.onResponseSuccess(theResponse -> {
		    	        
		    	    });
		    	    return request;
		    	}
		 };
		 webClient = WebClient.builder()
				 // TODO add property to parametrise that 
//				  	.clientConnector(new JettyClientHttpConnector(httpClient))
				  	.baseUrl( env.getProperty( "i2e.shop9.url" ) )
				  .build();
		 
	}
	
	@Bean
	public CommandLineRunner getClientes() throws Exception {
		
		return args -> {
			int pag = 1;
			while ( pag <= 1 ) {
				final String uri = "/clientes/" + pag; 
				var msg = get( uriBuilder -> uriBuilder.path( uri ).build(),
						Optional.empty(), Shop9ClienteMessage.class );
				
				if ( msg.isSucesso() ) {
	
					msg.getDados().stream().forEach( c ->  System.out.println( c ) );
					pag++;
				} else {
					
					System.out.println( msg.getTipo() );
					break;
				}
			}
		};
	}
	
	@Bean
	public CommandLineRunner getProdutos() throws Exception {
		
		return args -> {
			int pag = 1;
			List<ProdutoS9> produtos = new ArrayList<>();
			while ( pag <= 1 ) {
				final String uri = "/produtos/" + pag; 
				var msg = get( uriBuilder -> uriBuilder.path( uri ).build(),
						Optional.empty(), Shop9ProdutoMessage.class );
				
				if ( msg.isSucesso() ) {
	
					msg.getDados().stream().forEach( p ->  { 
						
						System.out.println( p ); 
						produtos.add( p );
					});
					pag++;
				} else {
					
					System.out.println( msg.getTipo() );
					break;
				}
			}
			
			produtos.stream().forEach( p -> getFotos( p ) );
		};
	}
	 
	private void getFotos( ProdutoS9 p ) {
		var mfp = get( uriBuilder -> uriBuilder.path( "/fotos/" + p.getCodigo() ).build(),
				Optional.empty(), Shop9FotosProdutoMessage.class );
		
		if ( mfp.isSucesso() ) {
			System.out.println( mfp );
			
			FotosProduto fp = mfp.getDados();
				fp.getFotos().forEach( pos -> {
					byte[] mf = getImage( uriBuilder -> uriBuilder.path( "/fotos/" + fp.getCodigo() + "/" + pos.getPosicao() ).build() );
					
					try( FileOutputStream fos = new FileOutputStream( "/tmp/" + fp.getCodigo() + "_" + pos.getPosicao() + ".jpg" ) ) {
						
						fos.write( mf );
					} catch ( IOException e ) {
	
						e.printStackTrace();
					}
				});
		}
	}
	
	@Bean
	public CommandLineRunner getToken() throws Exception {
		
		return args -> {
			
			var token = authenticationToken();
			System.out.println( token.getToken() + "--" + token.getExpireAt() );
		};
	}
	
	@Bean
	public CommandLineRunner getClasses() throws Exception {
		return args -> {
			Shop9AuxMessage classes = getAuxiliaryRegistry( AuxiliaryRegistry.Type.CLASSES 
					, LocalDate.of( 2000, 01, 01 ), LocalDate.now() 
			);
			System.out.println( classes.getDados() );
		};
	}
	
	@Bean
	public CommandLineRunner getSubClasses() throws Exception {
		return args -> {
			Shop9AuxMessage subClasses = getAuxiliaryRegistry( AuxiliaryRegistry.Type.SUBCLASSES 
					, LocalDate.of( 2000, 01, 01 ), LocalDate.now() 
			);
			System.out.println( subClasses.getDados() );
		};
	}
	
	@Bean
	public CommandLineRunner getGrupos() throws Exception {
		return args -> {
			Shop9AuxMessage grupos = getAuxiliaryRegistry( AuxiliaryRegistry.Type.GRUPOS 
					, LocalDate.of( 2000, 01, 01 ), LocalDate.now() 
			);
			System.out.println( grupos.getDados() );
		};
	}
	
	@Bean
	public CommandLineRunner getFamilias() throws Exception {
		return args -> {
			Shop9AuxMessage familias = getAuxiliaryRegistry( AuxiliaryRegistry.Type.FAMILIAS 
					, LocalDate.of( 2000, 01, 01 ), LocalDate.now() 
			);
			System.out.println( familias.getDados() );
		};
	}
	
	@Bean
	public CommandLineRunner getFabricantes() throws Exception {
		return args -> {
			Shop9AuxMessage fabricantes = getAuxiliaryRegistry( AuxiliaryRegistry.Type.FABRICANTES 
					, LocalDate.of( 2000, 01, 01 ), LocalDate.now() 
			);
			System.out.println( fabricantes.getDados() );
		};
	}
	
	@Bean
	public CommandLineRunner getUnidadesVenda() throws Exception {
		return args -> {
			Shop9AuxMessage unidadesVenda = getAuxiliaryRegistry( AuxiliaryRegistry.Type.UNIDADES_VENDA 
					, LocalDate.of( 2000, 01, 01 ), LocalDate.now() 
			);
			System.out.println( unidadesVenda.getDados() );
		};
	}
	
	@Bean
	public CommandLineRunner getCores() throws Exception {
		return args -> {
			Shop9AuxMessage cores = getAuxiliaryRegistry( AuxiliaryRegistry.Type.CORES );
			System.out.println( cores.getDados() );
		};
	}
	
	@Bean
	public CommandLineRunner getTamanhos() throws Exception {
		return args -> {
			Shop9AuxMessage tamanhos = getAuxiliaryRegistry( AuxiliaryRegistry.Type.TAMANHOS );
			System.out.println( tamanhos.getDados() );
		};
	}

	@Bean
	public CommandLineRunner getMoedas() throws Exception {
		return args -> {
			Shop9AuxMessage moedas = getAuxiliaryRegistry( AuxiliaryRegistry.Type.MOEDAS );
			System.out.println( moedas.getDados() );
		};
	}

	@Bean
	public CommandLineRunner getPesquisa1() throws Exception {
		return args -> {
			Shop9AuxMessage pesquisa1 = getAuxiliaryRegistry( AuxiliaryRegistry.Type.PESQUISA_1 );
			System.out.println( pesquisa1.getDados() );
		};
	}

	@Bean
	public CommandLineRunner getPesquisa2() throws Exception {
		return args -> {
			Shop9AuxMessage pesquisa2 = getAuxiliaryRegistry( AuxiliaryRegistry.Type.PESQUISA_2 );
			System.out.println( pesquisa2.getDados() );
		};
	}
	
	@Bean
	public CommandLineRunner getPesquisa3() throws Exception {
		return args -> {
			Shop9AuxMessage pesquisa3 = getAuxiliaryRegistry( AuxiliaryRegistry.Type.PESQUISA_3 );
			System.out.println( pesquisa3.getDados() );
		};
	}
	
	private Shop9AuxMessage getAuxiliaryRegistry( AuxiliaryRegistry.Type type ) {

		return get( uriBuilder -> uriBuilder.path( "/aux/" + type.name().toLowerCase() ).build(),
				Optional.empty(),
			    Shop9AuxMessage.class );
	}
	
	private Shop9AuxMessage getAuxiliaryRegistry( AuxiliaryRegistry.Type type, LocalDate dateFrom, LocalDate dateTo ) {

		return get(	uriBuilder -> uriBuilder.path( "/aux/" + type.name().toLowerCase() )
						.queryParam( "datade", "{datade}" )
						.queryParam( "dataate", "{dataate}" )
						.build( dateFrom.format( DateTimeFormatter.ofPattern( "yyyy-MM-dd" ) ),
								dateTo.format( DateTimeFormatter.ofPattern( "yyyy-MM-dd" ) ) ),
				Optional.empty(), Shop9AuxMessage.class );
	}
	
	private <E extends Shop9Message> E get( Function<UriBuilder, URI> uri, Optional<String> payload, Class<E> messageType ) {
		var sig = signatureUtil.assign( "get", payload );
		
		Mono<E> resp = webClient.get().uri( uri )
				.header( "Signature", sig.getToken() )
				.header( "CodFilial", env.getProperty( "i2e.shop9.filial" ) )
				.header( "Authorization", "Token " + authenticationToken().getToken() )
				.header( "Timestamp", sig.getTimestamp() )
				.header( "Accept", "application/json;charset=UTF-8" )
				.retrieve().bodyToMono( messageType );
		
		var sm = resp.block();
		System.out.println( sm.getMensagem() );
		return sm;
	}
	
	private byte[] getImage( Function<UriBuilder, URI> uri ) {
		var sig = signatureUtil.assign( "get", Optional.empty() );
		
		Mono<byte[]> resp = webClient.get().uri( uri )
				.header( "Signature", sig.getToken() )
				.header( "CodFilial", env.getProperty( "i2e.shop9.filial" ) )
				.header( "Authorization", "Token " + authenticationToken().getToken() )
				.header( "Timestamp", sig.getTimestamp() )
				.retrieve().bodyToMono( byte[].class );
		
		var sm = resp.block();
//		System.out.println( sm.getMensagem() );
		return sm;
	}

	private AuthenticationToken authenticationToken() {
		
		if ( token.isEmpty() || token.get().getExpireAt().isBefore( ZonedDateTime.now( ZoneId.of( "UTC" ) ).plusMinutes( EXPIRE_OFFSET ) ) ) {
			
			Mono<Shop9AuthMessage> resp = webClient.get().uri( uriBuilder -> uriBuilder
					    .path("/auth/")
					    .queryParam("serie", "{serie}")
					    .queryParam("codfilial", "{codfilial}")
					    .build( env.getProperty( "i2e.shop9.serie" ), env.getProperty( "i2e.shop9.filial" ) )					    
			   ).retrieve().bodyToMono( Shop9AuthMessage.class );
			
			var sam = resp.block();
			
			this.token = Optional.of( sam.getDados() ); 
		}
		
		return token.get();
	}
}