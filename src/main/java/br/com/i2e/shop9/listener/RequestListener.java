package br.com.i2e.shop9.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import br.com.i2e.common.util.JsonUtils;
import br.com.i2e.shop9.client.Shop9Client;
import br.com.i2e.shop9.model.Shop9IntegrationMessage;

@Component
public class RequestListener {
	 
	private static final Logger logger = LoggerFactory.getLogger( RequestListener.class );
	
    public final String SHOP9_REQUEST_QUEUE = "shop9.request.queue";
    public final String SHOP9_RESPONSE_QUEUE = "shop9.response.queue";
    @Autowired
    private RabbitTemplate rabbitTemplate;
	@Autowired
	private Shop9Client shop9Client;

	public RequestListener() {
		logger.info( ">>>>>>>>>>>>>>>>>>> START LEISTETER: {}",   SHOP9_REQUEST_QUEUE );
	}
	
    @RabbitListener( queues = SHOP9_REQUEST_QUEUE )
	public void onMessage( @Payload String jsonMessage ) {
		
    	var msg = JsonUtils.fromJson( jsonMessage, Shop9IntegrationMessage.class );
		
		switch ( msg.getCategoria() ) {
			case PRODUTOS: 
				requestProdutos( msg );
				break;
			case DETALHE_PRODUTO: 
				requestDetalheProduto( msg );
				break;
			case CLIENTE:
				break;
			case FAMILIA:
				requestFamilia( msg );
			default:
				break;
		}
		
		response( msg );
	}

    private void requestFamilia( Shop9IntegrationMessage msg ) {
    	var p = shop9Client.fetchFamilias();
		writeInfo( msg, p );
	}

	private void response( Shop9IntegrationMessage msg ) {
    	
    	try {
			
    		rabbitTemplate.convertAndSend( SHOP9_RESPONSE_QUEUE, JsonUtils.toJson( msg ) );
		} catch ( JsonProcessingException | AmqpException e ) {
			
			logger.error( "Erro ao processar resposta: {}", msg, e );
		}
    }

    private void requestDetalheProduto( Shop9IntegrationMessage req ) {
    	var p = shop9Client.fetchDetalheProduto( req.getCodigo() );
		writeInfo( req, p );
		
	}

	private void requestProdutos( Shop9IntegrationMessage req ) {
		
		shop9Client.fetchProdutos( req.getDataDe(), req.getDataAte() );
	}

	private void writeInfo( Shop9IntegrationMessage req, Object data ) {
		
		try {
			
			req.setInfo( JsonUtils.toJson( data ) );
			req.setStatus( Shop9IntegrationMessage.STATUS.PRONTO );
		} catch ( JsonProcessingException e) {
			
			logger.error("Erro ao converter para json: ", data, e);
			req.setStatus( Shop9IntegrationMessage.STATUS.ERRO );
			req.setInfo( e.toString() );
		}
	}
	
}
