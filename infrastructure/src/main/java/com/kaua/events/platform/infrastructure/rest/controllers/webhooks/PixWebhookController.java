package com.kaua.events.platform.infrastructure.rest.controllers.webhooks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhooks/pix")
public class PixWebhookController {

    private static final Logger log = LoggerFactory.getLogger(PixWebhookController.class);

    @PostMapping
    public ResponseEntity<Void> receivePixWebhook(
            @RequestHeader Map<String, String> headers,
            @RequestBody Map<String, Object> payload
    ) {
        log.info("[PIX] Webhook recebido. Headers={} Payload={}", headers, payload);
        payload.forEach((key, value) -> System.out.println("pix payload " + key + " " + value));
//      12:45:26.481-0300 [tomcat-handler-9] [ ] [ ] [anonymous] INFO  c.k.e.p.i.r.c.w.PixWebhookController - [PIX] Webhook recebido. Headers={host=23db661cecfe.ngrok-free.app, user-agent=API Pix Efi (homologacao), content-length=364, content-type=application/json;charset=utf-8, x-forwarded-for=34.193.116.226, x-forwarded-host=23db661cecfe.ngrok-free.app, x-forwarded-proto=https, accept-encoding=gzip} Payload={pix=[{endToEndId=E09089356202508221544APIb936fb54, txid=ca1b8194ccf14c518b5d46adbb28684e, chave=71cdf9ba-c695-4e3c-b010-abb521a3f1be, valor=9.00, horario=2025-08-22T15:44:56.000Z, infoPagador=Teste de pagamento em ambiente sandbox, gnExtras={pagador={nome=CONSULTORIA TÉCNICA EFÍ, cnpj=09089356000118, codigoBanco=09089356}}}]}
//pix payload pix [{endToEndId=E09089356202508221544APIb936fb54, txid=ca1b8194ccf14c518b5d46adbb28684e, chave=71cdf9ba-c695-4e3c-b010-abb521a3f1be, valor=9.00, horario=2025-08-22T15:44:56.000Z, infoPagador=Teste de pagamento em ambiente sandbox, gnExtras={pagador={nome=CONSULTORIA TÉCNICA EFÍ, cnpj=09089356000118, codigoBanco=09089356}}}]
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
