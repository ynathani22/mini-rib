package com.clayfin.training.minirib.stub;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class CoreBankingRoute extends RouteBuilder {

    @Override
    public void configure() {
        from("direct:getAccountsByCif")
                .routeId("core-banking-accounts")
                .log("Reading core banking file for cif=${header.cif}")
                // read <data-dir>/<cif>.json; noop=true -> don't move/delete the file
                .pollEnrich()
                .simple("file:{{app.stub.data-dir}}?fileName=${header.cif}.json&noop=true&idempotent=false")
                .timeout(1000)
                // file not found -> empty list
                .choice()
                .when(body().isNull())
                .setBody(constant("[]"))
                .end()
                .convertBodyTo(String.class)
                // JSON text -> CoreAccount[] (DTO)
                .unmarshal().json(JsonLibrary.Jackson, CoreAccount[].class);
    }
}