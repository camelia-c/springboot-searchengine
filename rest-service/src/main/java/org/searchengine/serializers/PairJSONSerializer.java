package org.searchengine.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

import org.javatuples.Pair;


@JsonComponent
public class PairJSONSerializer extends JsonSerializer<Pair> {

    @Override
    public void serialize(Pair pair, JsonGenerator jsonGen, SerializerProvider serProvider) throws IOException{
        jsonGen.writeStartObject();
        jsonGen.writeStringField("f0", "" + pair.getValue0());
        jsonGen.writeStringField("f1", "" +pair.getValue1());
        jsonGen.writeEndObject();
    }

}
